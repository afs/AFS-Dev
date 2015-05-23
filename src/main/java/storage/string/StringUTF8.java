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

package storage.string;

import java.nio.ByteBuffer ;
import java.nio.charset.CharacterCodingException ;
import java.nio.charset.CharsetDecoder ;
import java.util.Arrays ;

import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.atlas.lib.StrUtils ;

public abstract class StringUTF8
{
    // See also:
    // http://www.unicode.org/reports/tr6/ -- a compression scheme for Unicode. 
    // ftp://ftp.unicode.org/Public/PROGRAMS/SCSU/
    
    
    /* A compact string.
     * Stored as:
     * [int - allocation space]
     * int - length
     * bytes - UTF8 
     * except:
     *   ints are held as Varints
     *   bytes (FF, 00) is "http://"
     *   bytes (FF, 01) is "http://www."
     *   
     *   FF is an illegal UTF8 byte, as a first byte of a UTF-8 codepoint sequence.
     *   F5-FF would be 4 byte sequences above 10FFFF
     *   80-BF not in a first byte.  
     *   
     *   Eventually, will be a slice of a very large ByteBuffer and we will do our own malloc.
     *   
     * 00000000-01111111   00-7F   0-127       Single-byte encoding (compatible with US-ASCII)
     * 10000000-10111111   80-BF   128-191     Second, third, or fourth byte of a multi-byte sequence
     * 11000000-11000001   C0-C1   192-193     Overlong encoding: start of 2-byte sequence, but would encode a code point ≤ 127
     * 11000010-11011111   C2-DF   194-223     Start of 2-byte sequence
     * 11100000-11101111   E0-EF   224-239     Start of 3-byte sequence
     * 11110000-11110100   F0-F4   240-244     Start of 4-byte sequence
     * 11110101-11110111   F5-F7   245-247     Restricted by RFC 3629: start of 4-byte sequence for codepoint above 10FFFF
     * 11111000-11111011   F8-FB   248-251     Restricted by RFC 3629: start of 5-byte sequence
     * 11111100-11111101   FC-FD   252-253     Restricted by RFC 3629: start of 6-byte sequence
     * 11111110-11111111   FE-FF   254-255     Invalid: not defined by original UTF-8 specification
     *     
     */
    
    /* Space usage in a Java string.
       + A char[] for the characters (which is 3 slots = space used.
       + int offset start, in a larger buffer.
       + int count length
       + int hash; // Default to 0
       
       In addition, a substring can be a slice of another string, sharing char[]
       so string.substring is not a copy. 
     */
    
    /* To create a more compact string, we have some choices.
     * 1/ manage a slice via a ByteBuffer.
     * 2/ 
     * and whether substring capability is worth having some space.
     */
    
    // V1 - object allocation.
    
    public static StringUTF8 alloc(String string)
    {
        return new StringUTF8_String(string) ;
    }
    
    public static StringUTF8 alloc(ByteBuffer string)
    {
        return new StringUTF8_ByteBuffer(string) ;
    }

    public abstract String asString() ;  
    @Override public String toString() { return asString(); }
    
    // Different implementions.
    
    private static class StringUTF8_String extends StringUTF8
    {
//      private int offset ;
//      private int length ;
        
        @Override
        public int hashCode()
        {
            final int prime = 31 ;
            int result = 1 ;
            result = prime * result + Arrays.hashCode(bytes) ;
            return result ;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true ;
            if (obj == null) return false ;
            if (getClass() != obj.getClass()) return false ;
            StringUTF8_String other = (StringUTF8_String)obj ;
            if (!Arrays.equals(bytes, other.bytes)) return false ;
            return true ;
        }

        private byte[] bytes ;
        private StringUTF8_String(String string)
        {
            // Not subtle.
            // A copy ...
            bytes = StrUtils.asUTF8bytes(string) ;
        }
        
        @Override
        public String asString() {return StrUtils.fromUTF8bytes(bytes) ; }
        
    }
    
    /* But ByteBuffers have a large overhead ... */
    private static class StringUTF8_ByteBuffer extends StringUTF8
    {
        private ByteBuffer buffer ;
        // position to limit.
        private StringUTF8_ByteBuffer(ByteBuffer buffer)
        {
            this.buffer = buffer ; 
        }
        
        @Override
        public String asString()
        {
            if ( buffer.hasArray() )
                return StrUtils.fromUTF8bytes(buffer.array()) ;
            CharsetDecoder dec = Chars.allocDecoder() ;
            try
            {
                int old_position = buffer.position();
                String x = dec.decode(buffer).toString() ;
                buffer.position(old_position) ;
                Chars.deallocDecoder(dec) ;
                return x ;
            } catch (CharacterCodingException e)
            {
                e.printStackTrace();
                return null ;
            }
        }
        
        @Override
        public int hashCode()
        {
            final int prime = 31 ;
            int result = 1 ;
            result = prime * result + ((buffer == null) ? 0 : buffer.hashCode()) ;
            return result ;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true ;
            if (obj == null) return false ;
            if (getClass() != obj.getClass()) return false ;
            StringUTF8_ByteBuffer other = (StringUTF8_ByteBuffer)obj ;
            if (buffer == null)
            {
                if (other.buffer != null) return false ;
            } else
                if (!buffer.equals(other.buffer)) return false ;
            return true ;
        }
    }
}
