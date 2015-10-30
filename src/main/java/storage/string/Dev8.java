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

import java.text.CharacterIterator ;
import java.text.StringCharacterIterator ;

public class Dev8 {

    public static void main(String[] args) {
        
        String string = "½" ;
//        {
//            StringUTF8 s = StringUTF8.alloc(string) ;
//            CharacterIterator iter = s.iterator() ;
//            //dwim(string) ;
//            dwim(iter) ;
//            System.exit(0) ;
//        }
        
        StringUTF8 s = StringUTF8.alloc(string) ;
        CharacterIterator iter = s.iterator() ;
        CharacterIterator iterRef = new StringCharacterIterator(string) ;
        
        
//        char ch = iter.previous() ;
        int i = iter.getEndIndex() ;
        
        System.out.printf("BeginIndex   %s -- %s\n", iterRef.getBeginIndex(), iter.getBeginIndex()) ;
        System.out.flush() ;
        System.out.printf("EndIndex     %s -- %s\n", iterRef.getEndIndex(), iter.getEndIndex()) ;
        System.out.flush() ;

        System.out.printf("First        %s -- %s\n", iterRef.first(), iter.first()) ;
        System.out.flush() ;
        System.out.printf("Last         %s -- %s\n", iterRef.last(),  iter.last()) ;
        System.out.flush() ;
        
        System.out.printf("Previous     %s -- %s\n", iterRef.previous(), iter.previous()) ;
        System.out.flush() ;
    }
    
    public static void dwim(String s) {
        CharacterIterator iter = new StringCharacterIterator(s) ;
        dwim(iter) ;
        
    }

    private static void dwim(CharacterIterator iter) {
        char ch0 = iter.current() ;
        System.out.printf("%d -> %c\n", iter.getIndex(), iter.current()) ;
        for(;;) {
            char ch = iter.next() ; 
            System.out.printf("%c\n", ch) ;
            if ( ch == CharacterIterator.DONE )
                break ;
        }
    }

}
