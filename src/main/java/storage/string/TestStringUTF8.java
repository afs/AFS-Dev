/**
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

package storage.string;

import java.nio.charset.StandardCharsets ;
import java.text.CharacterIterator ;
import java.text.StringCharacterIterator ;
import java.util.Arrays ;

import static org.junit.Assert.* ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

@RunWith(Parameterized.class)
public class TestStringUTF8 {
    @Parameters(name = "{index}: \"{0}\"")
    public static Iterable<Object[]> data() {
        Object[][] x = {{""}, {"a"}, {"abc"} , 
                        {"½"} ,
                        {"a½c"} ,
                        {"αβγ"} ,
                        {"ᚠ"},
        } ;
        
        
        return Arrays.asList(x) ;
    }

    private String string;
    
    public TestStringUTF8(String string) {
        this.string = string ;
    }
    
    @Test public void createStringUTF8() {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8) ;
        StringUTF8 s = StringUTF8.alloc(bytes) ;
        String str = s.asString() ;
        assertEquals(string, str);
        assertEquals(bytes.length, s.byteLength()) ;
    }

    // Repeat - in case first call is somekind of initialization. 
    
    @Test public void beginEndStringUTF8_1() {
        StringUTF8 s = StringUTF8.alloc(string) ;
        CharacterIterator iter = s.iterator() ;
        CharacterIterator iterRef = new StringCharacterIterator(string) ;
        assertEquals(iterRef.getBeginIndex(), iter.getBeginIndex()) ;
        assertEquals(iterRef.getEndIndex(), iter.getEndIndex()) ;
        // And repeat 
        assertEquals(iterRef.getBeginIndex(), iter.getBeginIndex()) ;
        assertEquals(iterRef.getEndIndex(), iter.getEndIndex()) ;
    }
    
    @Test public void firstStringUTF8_2() {
        StringUTF8 s = StringUTF8.alloc(string) ;
        CharacterIterator iter = s.iterator() ;
        CharacterIterator iterRef = new StringCharacterIterator(string) ;
        
        assertEquals(iterRef.first(), iter.first()) ;
        assertEquals(iterRef.current(), iter.current()) ; 
        // And repeat 
        assertEquals(iterRef.first(), iter.first()) ;
        assertEquals(iterRef.current(), iter.current()) ; 
    }
    
    @Test public void lastStringUTF8_2() {
        StringUTF8 s = StringUTF8.alloc(string) ;
        CharacterIterator iter = s.iterator() ;
        CharacterIterator iterRef = new StringCharacterIterator(string) ;
        
        assertEquals(iterRef.last(), iter.last()) ;
        assertEquals(iterRef.current(), iter.current()) ; 
        // And repeat
        assertEquals(iterRef.last(), iter.last()) ;  
        assertEquals(iterRef.current(), iter.current()) ; 
    }
    

    @Test public void forwardStringUTF8() {
        StringUTF8 s = StringUTF8.alloc(string) ;
        CharacterIterator iter = s.iterator() ;
        CharacterIterator iterRef = new StringCharacterIterator(string) ;
        
        while(iterRef.current() != CharacterIterator.DONE ) {
            assertEquals(iterRef.current(), iter.current()) ; 
            assertEquals(iterRef.next(), iter.next()) ;   
        }
        assertEquals(CharacterIterator.DONE, iterRef.current()) ;
    }
    
    @Test public void backwardStringUTF8() {
        StringUTF8 s = StringUTF8.alloc(string) ;
        CharacterIterator iter = s.iterator() ;
        CharacterIterator iterRef = new StringCharacterIterator(string) ;
        assertEquals(iterRef.last(), iter.last()) ;
        int x = iterRef.current() ;
        while(x != CharacterIterator.DONE ) {
            assertEquals(x, iter.current()) ; 
            x = iterRef.previous() ;
            assertEquals(x, iter.previous()) ;
        }
    }


}
