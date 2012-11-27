/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lib;

import static lib.CSVParser.TokenType.NL ;
import static lib.CSVParser.TokenType.QSTRING ;
import static lib.CSVParser.TokenType.STRING ;

import java.io.ByteArrayInputStream ;
import java.io.InputStream ;
import java.io.UnsupportedEncodingException ;
import java.util.ArrayList ;
import java.util.List ;

import lib.CSVParser.TokenIterator ;
import org.junit.Test ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Sink ;

public class TestCSVParser extends BaseTest
{
    String[] row1 = {} ;
    String[] row2 = { "" } ;
    String[] row3 = { "a", "b" } ;
    String[] row4 = { "123", "\"aa\"", "'bb'", "\"''\"Z", "A'\"\"'" } ;
    
    CSVParser.Token t1 = new CSVParser.Token(-1, -1, CSVParser.TokenType.COMMA, ",") ;
    
    
    @Test public void csv_parse_term_01() {  csvTerm("123", STRING, "123") ; }
    @Test public void csv_parse_term_02()  { csvTerm("aa", STRING, "aa") ; }
    @Test public void csv_parse_term_03()  { csvTerm("\" \"", QSTRING, " ") ; }
    @Test public void csv_parse_term_04()  { csvTerm("' '", QSTRING, " ") ; }
    
    @Test public void csv_parse_term_05()  { csvTerm("\"a\"\"b\"", QSTRING, "a\"b") ; }
    @Test public void csv_parse_term_06()  { csvTerm("'a\"b'", QSTRING, "a\"b") ; }
    
    @Test public void csv_parse_term_07()  { csvTerm("\n", NL, "\n") ; }
    @Test public void csv_parse_term_08()  { csvTerm("\r", NL, "\n") ; }
    @Test public void csv_parse_term_09()  { csvTerm("\r\n", NL, "\n") ; }
    
    private static void csvTerm(String input, CSVParser.TokenType type, String output)
    {
        try
        {
            CSVParser.Token expected = new CSVParser.Token(-1, -1, type, output) ;

            InputStream in = new ByteArrayInputStream(input.getBytes("UTF-8")) ;
            TokenIterator iter = new TokenIterator(in) ;
            assertTrue(iter.hasNext()) ;
            CSVParser.Token t = iter.next() ;
            assertTrue(expected.same(t)) ;
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e) ;
        }
    }
    
    @Test public void csv_parse_01() { csv("\n", new String[][] {{""}}) ; }
    @Test public void csv_parse_02() { csv("a\n", new String[][] {{"a"}}) ; }
    @Test public void csv_parse_03() { csv("a,b\n", new String[][] {{"a", "b"}}) ; }
    @Test public void csv_parse_04() { csv(",b\n", new String[][] {{"", "b"}}) ; }
    @Test public void csv_parse_05() { csv("a,\n", new String[][] {{"a", ""}}) ; }
    @Test public void csv_parse_06() { csv(",\n", new String[][] {{"", ""}}) ; }
    @Test public void csv_parse_07() { csv(",,\n", new String[][] {{"", "", ""}}) ; }
    
    @Test public void csv_parse_10() { csv("\n\n", new String[][] { {""}, {""} }) ; }
    @Test public void csv_parse_11() { csv("'aa'\naa\n", new String[][] { {"aa"}, {"aa"} }) ; }
    @Test public void csv_parse_12() { csv("\naa", new String[][] { {""}, {"aa"} }) ; }
    @Test public void csv_parse_13() { csv("a,b\nc,d", new String[][] { {"a", "b"}, {"c", "d"} }) ; }
    @Test public void csv_parse_14() { csv("a,b\rc,d", new String[][] { {"a", "b"}, {"c", "d"} }) ; }
    
    
    private void csv(String input, String[][] strings)
    {
        List<List<String>> x = new ArrayList<>() ;
        for ( String[] a : strings )
        {
            List<String> y = new ArrayList<>() ;
            for ( String b : a )
                y.add(b) ;
            x.add(y) ;
        }
        csv(input, x) ;
    }
    
    private static void csv(String input, List<List<String>> answers)
    {
        final List<List<String>> x = new ArrayList<>() ;
        Sink<List<String>> catcher = new Sink<List<String>>(){
            @Override public void send(List<String> item) { x.add(item) ; }
            @Override public void close() {}
            @Override public void flush() {}
        } ;
        try {
            InputStream in = new ByteArrayInputStream(input.getBytes("UTF-8")) ;
            TokenIterator iter = new TokenIterator(in) ;
            CSVParser parser = new CSVParser(iter) ;
            parser.parse(catcher) ;
            assertEquals(answers, x) ;
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e) ;
        }
        
        
    }
}

