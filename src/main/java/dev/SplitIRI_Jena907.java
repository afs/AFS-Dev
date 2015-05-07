/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package dev;

import java.util.Objects ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.riot.system.RiotChars ;
import org.junit.Assert ;
import org.junit.Test ;

//@RunWith(Parameterized.class)
//public class SplitIRI_Jena907
//{
//    @Parameters(name="{0} -> {1} : {2}")
//    public static Collection<Object[]> data() {
//        return Arrays.asList(new Object[][]{
//              { "http://example/foo", "http://example/", "foo" }
//            , { "http://example/foo#bar", "http://example/foo#", "bar"}
//            , { "http://example/foo#", "http://example/foo#", "" }
//            }) ;
//    }
    
public class SplitIRI_Jena907
{
    static { LogCtl.setCmdLogging(); }
//    public static void main(String ...argv) {
//    }
    
    // Basics
    @Test public void localname_01() { testPrefixLocalname("http://example/foo",            "http://example/",      "foo"       ) ; }
    @Test public void localname_02() { testPrefixLocalname("http://example/foo#bar",        "http://example/foo#",  "bar"       ) ; }
    @Test public void localname_03() { testPrefixLocalname("http://example/foo#",           "http://example/foo#",  ""          ) ; }
    @Test public void localname_04() { testPrefixLocalname("http://example/",               "http://example/",      ""          ) ; }
    @Test public void localname_05() { testPrefixLocalname("http://example/1abc",           "http://example/",      "1abc"      ) ; }
    @Test public void localname_06() { testPrefixLocalname("http://example/1.2.3.4",        "http://example/",      "1.2.3.4"   ) ; }
    @Test public void localname_07() { testPrefixLocalname("http://example/xyz#1.2.3.4",    "http://example/xyz#",  "1.2.3.4"   ) ; }
    @Test public void localname_08() { testPrefixLocalname("http://example/xyz#_abc",       "http://example/xyz#",  "_abc"       ) ; }
    @Test public void localname_09() { testPrefixLocalname("http://example/xyz/_1.2.3.4",   "http://example/xyz/", "_1.2.3.4"   ) ; }
    
    // URNs split differently.
    @Test public void localname_20() { testPrefixLocalname("urn:foo:bar",                   "urn:foo",              "bar"       ) ; }

    // Splitting rules - no escapes? 

    @Test public void localname_30() { testPrefixLocalname("http://example/id=89",        "http://example/",      "id=89"   ) ; }
    @Test public void localnameEsc_30() { testPrefixLocalnameEsc("http://example/id=89",  "id\\=89"   ) ; }
    
    @Test public void localname_40() { testPrefixLocalname("http://example/foo#bar:baz",    "http://example/foo#",  "bar:baz"   ) ; }
    @Test public void localname_41() { testPrefixLocalname("http://example/a:b:c",          "http://example/",      "a:b:c"     ) ; }
    @Test public void localname_42() { testPrefixLocalname("http://example/.2.3.4",         "http://example/.",     "2.3.4"     ) ; }
    
    @Test public void localname_51() { testPrefixLocalnameNot("http://example/foo#bar:baz", "http://example/foo#bar", "baz"     ) ; }

    
    // Test for PrefixLocalnameEsc
    
    
    
    @Test public void split() { testSplit("http://example/foo", "http://example/".length()) ; }
    
    private void testSplit(String string, int expected) {
        int i = splitpoint(string) ;
        Assert.assertEquals(expected, i) ;
    }

    private void testPrefixLocalname(String string, String expectedPrefix, String expectedLocalname) {
//        Node n = NodeFactory.createURI(string) ;
//        String ns = n.getNameSpace() ;
//        String ln = n.getLocalName() ;
        String ns = namespace(string) ;
        String ln = localname(string) ;

        if ( expectedPrefix != null )
            Assert.assertEquals(expectedPrefix, ns);
        if ( expectedLocalname != null )
            Assert.assertEquals(expectedLocalname, ln);
        if (  expectedPrefix != null && expectedLocalname != null ) {
            String x = ns+ln ;
            Assert.assertEquals(string, x) ;
        }
    }

    private void testPrefixLocalnameEsc(String string, String expectedLocalname) {
//      Node n = NodeFactory.createURI(string) ;
//      String ns = n.getNameSpace() ;
//      String ln = n.getLocalName() ;
      String ln = localnameEsc(string) ;
      Assert.assertEquals(expectedLocalname, ln);
  }

    private void testPrefixLocalnameNot(String string, String expectedPrefix, String expectedLocalname) {
//      Node n = NodeFactory.createURI(string) ;
//      String ns = n.getNameSpace() ;
//      String ln = n.getLocalName() ;
        String ns = namespace(string) ;
        String ln = localname(string) ;

        boolean b1 = Objects.equals(expectedPrefix, ns) ;
        boolean b2 = Objects.equals(expectedLocalname, ln) ;

        // Test not both true.
        Assert.assertFalse("Wrong: ("+ns+","+ln+")", b1&&b2);
        // But it still combines.
        String x = ns+ln ;
        Assert.assertEquals(string, x) ;
    }

    private String namespace(String string) {
        int i = splitpoint(string) ;
        if ( i < 0 )
            return string ;
        return string.substring(0, i) ;
    }
    
    
    /** Calculate a localname - do not escape PN_LOCAL_ESC */
    private String localname(String string) {
        int i = splitpoint(string) ;
        if ( i < 0 )
            return string ;
        return string.substring(i) ;
    }
    
    /** Calculate a localname - escape PN_LOCAL_ESC */
    private String localnameEsc(String string) {
        String x = localname(string) ;
        return escape_PN_LOCAL_ESC(x) ;
    }

    private String escape_PN_LOCAL_ESC(String x) {
        // Asusme that escapes are rare so scan once to make sure there
        // is work to do then scan again doing the work.
        //'\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
        
        int N = x.length() ;
        boolean escchar = false ;
        for ( int i = 0 ; i < N ; i++ ) {
            char ch = x.charAt(i) ;
            if ( isPN_LOCAL_ESC(ch) ) {
                escchar = true ;
                break ;
            }
        }
        if ( ! escchar )
            return x ;
        StringBuilder sb = new StringBuilder(N+10) ;
        for ( int i = 0 ; i < N ; i++ ) {
            char ch = x.charAt(i) ;
            if ( isPN_LOCAL_ESC(ch) )
                sb.append('\\') ;
            sb.append(ch) ;
        }
        return sb.toString() ; 
    }
    
    public static boolean /*RiotChars.*/isPN_LOCAL_ESC(char ch) {
        switch (ch) {
            case '\\': case '_':  case '~': case '.': case '-': case '!': case '$':
            case '&':  case '\'': case '(': case ')': case '*': case '+': case ',':
            case ';':  case '=':  case '/': case '?': case '#': case '@': case '%':
                return true ;
            default:
                return false ;
        }
    }
    
    
    /*
[136s]  PrefixedName    ::=     PNAME_LN | PNAME_NS
Productions for terminals


[163s]  PN_CHARS_BASE   ::=     [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
[164s]  PN_CHARS_U  ::=     PN_CHARS_BASE | '_'
[166s]  PN_CHARS    ::=     PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
[167s]  PN_PREFIX   ::=     PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?

[168s]  PN_LOCAL    ::=     (PN_CHARS_U | ':' | [0-9] | PLX) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX))?
[169s]  PLX     ::=     PERCENT | PN_LOCAL_ESC
[170s]  PERCENT     ::=     '%' HEX HEX
[171s]  HEX     ::=     [0-9] | [A-F] | [a-f]
[172s]  PN_LOCAL_ESC    ::=     '\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
*/
    
    // Special cases: : 
    // a:b:c is legal.
    // PLX
    
    // Try # and /, then work harder.
    // Some light URI parsing?
    
    static int splitpoint(String uri) {
        // Fast track.
        int idx1 = uri.lastIndexOf('#') ;
//        if ( idx1 >= 0 ) {
//            // If legal URI.
//            return idx1+1 ;
//        }
//        
        // Not so simple - \/ in local names 
        int idx2 = uri.lastIndexOf('/') ;
//        if ( idx2 >= 0 ) {
//            if ( idx1 < 0 || idx2 > idx1 ) 
//                // If legal URI.
//                return idx2+1 ;
//        }
        
        // Test the discovered local part.
        int limit = Math.max(idx1, idx2) ;
        limit = Math.max(0, limit) ;
        
        // Work harder.
        for ( int i = uri.length()-1 ; i >= limit ; i-- ) {
            char ch = uri.charAt(i) ;
            // Temp
            // XXX
            // Does not consider '.' and '-' which can't be leading.
            //
            // TODO Better
            if ( RiotChars.isPNChars_U_N(ch) || isPN_LOCAL_ESC(ch) || ch == ':' || ch == '-' || ch == '.' ) 
                continue ;
            return i+1 ; 
        }
        // Should not happen?
        return limit+1 ;
    }
    
    
    
    
/*
[141s]  BLANK_NODE_LABEL    ::=     '_:' (PN_CHARS_U | [0-9]) ((PN_CHARS | '.')* PN_CHARS)?
[144s]  LANGTAG     ::=     '@' [a-zA-Z]+ ('-' [a-zA-Z0-9]+)*
[19]    INTEGER     ::=     [+-]? [0-9]+
[20]    DECIMAL     ::=     [+-]? [0-9]* '.' [0-9]+
[21]    DOUBLE  ::=     [+-]? ([0-9]+ '.' [0-9]* EXPONENT | '.' [0-9]+ EXPONENT | [0-9]+ EXPONENT)
[154s]  EXPONENT    ::=     [eE] [+-]? [0-9]+
[22]    STRING_LITERAL_QUOTE    ::=     '"' ([^#x22#x5C#xA#xD] | ECHAR | UCHAR)* '"'
[23]    STRING_LITERAL_SINGLE_QUOTE     ::=     "'" ([^#x27#x5C#xA#xD] | ECHAR | UCHAR)* "'"
[24]    STRING_LITERAL_LONG_SINGLE_QUOTE    ::=     "'''" (("'" | "''")? ([^'\] | ECHAR | UCHAR))* "'''"
[25]    STRING_LITERAL_LONG_QUOTE   ::=     '"""' (('"' | '""')? ([^"\] | ECHAR | UCHAR))* '"""'
[26]    UCHAR   ::=     '\ u' HEX HEX HEX HEX | '\U' HEX HEX HEX HEX HEX HEX HEX HEX
[159s]  ECHAR   ::=     '\' [tbnrf"'\]
[161s]  WS  ::=     #x20 | #x9 | #xD | #xA
[162s]  ANON    ::=     '[' WS* ']'
 
 */
    
    
}

