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

import java.io.IOException ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.IO ;

/** UTF8 library*/

public class U8 {

    /** Length of UTF-8 byte sequence */ 
    public static int codepointLength(int x) {
        // 10 => extension byte
        // 110..... => 2 bytes
        if ( (x & 0x80) == 0 )
            return 1 ;
        if ( (x & 0xE0) == 0xC0 )
            return 2 ;
        if ( (x & 0xF0) == 0xE0 )
            return 3 ;
        if ( (x & 0xF8) == 0xF0 )
            return 4 ;
        return -1 ;
    }
    
    public static boolean isCodepointStart(int x) {
        return (x & 0x80) == 0 || (x & 0xC0) == 0xC0 ; 
        //return codepointLength(x) > 0 ;
    }
    
    /** Next codepoint */
    public static final int codepoint(IntSequence input)
    {
        int x = input.current() ;
        if ( x == -1 ) return -1 ;
        return advance(input, x) ;
    }
    
    /** Next codepoint, given the first byte of any UTF-8 byte sequence is already known.
     * Not necessarily a valid char (this function can be used a straight UTF8 decoder
     */
    
    private static final int advance(IntSequence input, int x)
    {
        //count++ ;
        // Fastpath
        if ( x == -1 || (x > 0  && x <= 127 ) )
        {
            //count++ ;
            return x ;
        }

        // 10 => extension byte
        // 110..... => 2 bytes
        if ( (x & 0xE0) == 0xC0 )
        {
            int ch = readMultiBytes(input, x & 0x1F, 2) ;
            // count += 2 ;
            return ch ;
            
        }
        //  1110.... => 3 bytes : 16 bits : not outside 16bit chars 
        if ( (x & 0xF0) == 0xE0 ) 
        {
            int ch = readMultiBytes(input, x & 0x0F, 3) ;
            // count += 3 ;
            //if ( ! Character.isDefined(ch) ) throw new AtlasException(String.format("Undefined codepoint: 0x%04X", ch)) ;
            return ch ;
        }

        // Looking like 4 byte charcater.
        int ch = -2 ;
        // 11110zzz => 4 bytes.
        if ( (x & 0xF8) == 0xF0 )
        {
             ch = readMultiBytes(input, x & 0x08, 4) ;
             // Opsp - need two returns. Character.toChars(ch, chars, 0) ;
             // count += 4 ;
        }
             
        else 
            IO.exception(new IOException("Illegal UTF-8: "+x)) ;

        // This test will go off.  We're processing a 3 or 4 byte sequence but Java only supports 16 bit chars. 
        if ( ch > Character.MAX_VALUE )
            throw new AtlasException("Out of range character (must use a surrogate pair)") ;
        if ( ! Character.isDefined(ch) ) throw new AtlasException(String.format("Undefined codepoint: 0x%04X", ch)) ;
        return ch ;
    }
    
    private static int readMultiBytes(IntSequence input, int start, int len) //throws IOException
    {
        //System.out.print(" -("+len+")") ; p(start) ;
        
        int x = start ;
        for ( int i = 0 ; i < len-1 ; i++ )
        {
            int x2 = input.forward() ;
            if ( x2 == -1 )
                throw new AtlasException("Premature end to UTF-8 sequence at end of input") ;
            
            if ( (x2 & 0xC0) != 0x80 )
                //throw new AtlasException("Illegal UTF-8 processing character "+count+": "+x2) ;
                throw new AtlasException(String.format("Illegal UTF-8 processing character: 0x%04X",x2)) ;
            // 6 bits of x2
            x = (x << 6) | (x2 & 0x3F); 
        }
        return x ;
    }

}
