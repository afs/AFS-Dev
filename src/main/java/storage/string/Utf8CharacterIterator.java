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

/** Iterator over UTF8 bytes, so that the string is not materialized. */
public class Utf8CharacterIterator implements CharacterIterator {

    // Could use InputStream+InStreamUTF8.
    
    // Starts before first character. ?
    
    private IntSequence bytes;
    // This requires calculation.
    private int indexEnd            = -1 ; 
    private char currentChar        = DONE ; 

    public Utf8CharacterIterator(IntSequence bytes) {
        this.bytes = bytes ;
        reset() ;
    }
    
    
    private void reset() {
        bytes.position(0) ;
        setCharacter();
    }

    private int getCodepoint() {
        return currentChar ;
    }
    
    @Override
    public char first() {
        bytes.position(0);
        setCharacter() ;
        return current() ; 
    }

    @Override
    public char last() {
        bytes.position(bytes.length()) ;
        if ( bytes.length() == 0 ) {
            currentChar = DONE ;
        } else
            moveBackwards();
        return currentChar ;
    }

    @Override
    public char current() {
        return currentChar ;
    }

    @Override
    public char next() {
        moveForward() ;
        return currentChar ;
    }

    /** Move on a character */
    private void moveForward() {
        if ( bytes.position() >= bytes.length() ) {
            currentChar = DONE ;
            return ;
        }
        
        int x = bytes.current() ;
        int len = U8.codepointLength(x) ;
        for ( int i = 0 ; i < len ; i++ )
            bytes.forward() ;
        if ( bytes.position() == bytes.length() ) {
            currentChar = DONE ;
            return ;
        }
            
        setCharacter() ; 
    }
    
    /** Move back to the start of a codepoint.*/
    private void moveBackwards() {
        // XXX Boundaries.
        while( bytes.position() >= 0 ) {
            int x = bytes.backward() ;
            if ( x == -1 ) {
                break ;
            }
            if ( U8.isCodepointStart(x) ) // First byte test. Firts bit zero of first 2 are 11.
                break ;
        }        
        setCharacter() ;
    }
    
    // Index on the first byte of a UTF8 sequence.
    private void setCharacter() {
        if ( bytes.position() >= bytes.length() ) {
            currentChar = DONE ;
            return ;
        }
        int posn = bytes.position() ;
        try {
            int x = U8.codepoint(bytes) ;
            if ( x == -1 ) {
                currentChar = DONE ;
                return ;
            }
            currentChar = (char)x ;
        } finally { bytes.position(posn) ; }
    }
    
    @Override
    public char previous() {
        if ( bytes.position() == 0 )
            return DONE ;
        moveBackwards();
        return current() ;
    }

    @Override
    public char setIndex(int position) {
        reset() ;
        // count forward.
        for(int i = 0 ; i < position ; i++ )
            moveForward() ;
        return currentChar ;
    }

    @Override
    public int getBeginIndex() {
        return 0;
    }

    @Override
    public int getEndIndex() {
        // Calculate
        if ( indexEnd == -1 ) {
            indexEnd = 0 ;
            if ( bytes.length() == 0 ) {
                indexEnd = 0 ;
                return 0 ;
            }
            int posn = bytes.position() ;
            //bytes.position(bytes.length()) ;
            // Forward version.
            bytes.position(0) ;
            int x = 0 ;
            int count = 0 ; 
            int z = -2 ;
            while( (z = bytes.current()) != -1 ) {
                if ( U8.isCodepointStart(z) ) {
                    x = bytes.position() ;
                    count ++ ;
                }
                bytes.forward() ;
            }
            bytes.position(posn);
            indexEnd = count ;
        }
        return indexEnd ;
    }

    @Override
    public int getIndex() {
        return bytes.position() ;
    }
    
    @Override
    public Object clone() {
        throw new UnsupportedOperationException() ;
    }

}
