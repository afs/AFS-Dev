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

package storage.varrecord ;

import java.nio.ByteBuffer;
import java.util.Arrays;

// Thisis not the master version

/** Variable length integers for tight packing into byte arrays and ByteBuffers.
 *  Packing is 7bits per byte and is the same format as vints in Lucene. 
 */
final class VarInteger$
{
    public static void main(String ... argv) { varint() ; } 
    private static void varint()
    {
        long[] testValues = {0, 1, 127, 128, 129, (1L<<63)-1} ; // { 0 , 5 , 127, 128 , 0x1000} ;
        for ( long x : testValues )
        {
            VarInteger$ vint = new VarInteger$(x) ;
            System.out.printf("0x%04X => %s\n", x , vint) ;
            long z = vint.value() ;
            System.out.printf("0x%04X => %s ==> 0x%04X\n", x , vint, z) ;
        }
    }
    
    /* See:
        http://lucene.apache.org/java/docs/fileformats.html#VInt
        FYI: src/java/org/apache/lucene/store/IndexInput.java, IndexOutput.java
          but this code is for ByteBuffers.
    */
    
    /* Encoding
     * Low to high, 7 bits (low end) per byte.
     * High bit is 1 for "more" and 0 for last byte.
     */
    
    // Either 1: ByteSink etc.
    // or 2: ByteBuffer + wrappers (what about OutputStreams?)
    
    static final long maxValue = (1L<<63) - 1 ;
 
    // The core encoder and decoder could take a ByteSink/ByteSource - that
    // would be a well-structured way to do it but of extra objects for each
    // number might become non-trivial.
    // Currently, works specifically on ByteBuffers.
    
    public static byte[] encode(long value)
    {
        // Max length is ceiling(63/7) = (63+6)/7 = 9  
        // Unlikely.
        if ( value > maxValue )
            throw new IllegalArgumentException("Too large: "+value) ;
        int len = calcLength(value) ;
        byte[] bytes = new byte[len] ;
        encode(ByteBuffer.wrap(bytes), 0, value) ;
        return bytes ;
    }
    
    // Relative vs absolute put/get.
    
    public static int encode(ByteBuffer bytes, long value) { return encode(bytes, bytes.position(), value) ; }
    
    public static int encode(ByteBuffer bytes, int startIdx, long value)
    {
        long N = value ;
        int idx = startIdx ;
        byte b = 0 ;
        while (true)
        {
            b = (byte)(N&0x7FL) ;
            N = ( N >>> 7 ) ;
            if ( N == 0 )
                break ;
            bytes.put(idx, (byte) (b | 0x80)) ;
            idx ++ ;
        }
        bytes.put(idx, b) ;
        return (idx+1)-startIdx ;
    }

    public static long decode(byte[] bytes, int idx)
    { return decode(ByteBuffer.wrap(bytes), idx) ; }
    
    public static long decode(ByteBuffer bytes)
    { return decode(bytes, 0) ; }
    
    public static long decode(ByteBuffer bytes, int idx)
    {
        // Low to high
        long value = 0 ;
        int shift = 0 ;
        
        while (true)
        {
            byte b = bytes.get(idx) ;
            value |= (b & 0x7FL) << shift ;
            if ( (b & 0x80) == 0 )
                  return value ;
            shift +=  7 ;
            idx++ ;
        }
    }
    
    /** Make a VarInteger from the bytes found start from idx */ 
    public static VarInteger$ make(ByteBuffer bb, int idx)
    {
        int start = idx ;
        while (true)
        {
            byte b = bb.get(idx) ;
            if ( (b & 0x80) == 0 )
                  break ;
            idx++ ;
        }
        // points idx at he last bytes
        int len = idx-start+1 ;
        byte[] bytes = new byte[len] ;
        for ( int i = 0 ; i < len ; i++ )
            bytes[i] = bb.get(start+i) ;
        return make(bytes) ;
    }
    
    /** Make a VarInteger from the bytes found start from idx */ 
    public static VarInteger$ make(byte[] bytes)
    {
        return new VarInteger$(bytes) ;
    }
    
    private static String toString(byte[] bytes) { return toString(bytes, 0) ; }
    private static String toString(byte[] bytes, int idx)
    {
        StringBuilder buff = new StringBuilder() ;
        buff.append("[") ;
        
        String sep = null ;
        while(true)
        {
            byte b = bytes[idx] ; 
            if ( sep != null )
                buff.append(sep) ;
            else
                sep = ", " ;
            buff.append(String.format("%02X", b)) ;
            if ( b >= 0 )
                break ;
            idx++ ;
        }
        buff.append("]") ;
        return buff.toString() ;
    }
    
    public static boolean equals(long value, byte[] bytes) { return equals(value, ByteBuffer.wrap(bytes)) ; }
    
    public static boolean equals(long value, ByteBuffer bytes) { return equals(value, bytes, 0) ; }
    
    public static boolean equals(long value, ByteBuffer bytes, int idx)
    {
        long x = decode(bytes, idx) ;
        return x == value ;
    }
    
    // ---- Factory
    
    // -- Some constants
    public static VarInteger$ varint_0 = new VarInteger$(0) ;
    public static VarInteger$ varint_1 = new VarInteger$(1) ;
    public static VarInteger$ varint_2 = new VarInteger$(2) ;

    /** Return a VarInteger that encodes the value */
    public static VarInteger$ valueOf(long value)
    { 
        if ( value == 0 ) return varint_0 ;
        if ( value == 1 ) return varint_1 ;
        if ( value == 2 ) return varint_2 ;
        return new VarInteger$(value) ;
    }
    
    public static int lengthOf(long value)
    {
        return calcLengthTable(value) ;
    }
    
    // ---- The object

    byte[] bytes ;
    long value = -1 ;
    
    private VarInteger$(long value)
    {
        Integer.valueOf(0) ;
        if ( value < 0 )
            throw new IllegalArgumentException("Positive integers only") ;
        bytes = encode(value) ;
    }
    
    private VarInteger$(byte[] bytes)
    {
        if ( bytes.length == 0 )
            throw new IllegalArgumentException("Zero length byte[]") ;
        this.bytes = bytes ;
    }
    
    public int length()     { return bytes.length ; }
    public byte[] bytes()   { return bytes ; }
    
    public long value()
    { 
        if ( value == -1 )
            value = decode(bytes, 0) ;
        return value ;
    }
    
    @Override
    public int hashCode()
    { return Arrays.hashCode(bytes) ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof VarInteger$ ) ) return false ;
        VarInteger$ vint = (VarInteger$)other ;
        return Arrays.equals(bytes, vint.bytes) ;
    }
    
    @Override
    public String toString() { return toString(bytes) ; }
    
    // Next is the impossible (it's the sign bit) 1L<<63
    static final long VarIntLengths[] = { 1L<<7 , 1L<<14 , 1L<<21 , 1L<<28 , 1L<<35, 1L<<42, 1L<<49, 1L<<56 } ;

    // By semi-lookup.
    static int calcLengthTable(long value)
    {
        int len = -1 ;
        for ( int i = 0 ; i < VarIntLengths.length ; i++ )
        {
            if ( value < VarIntLengths[i] )
                return i+1 ; 
        }
        //throw new IllegalArgumentException("Value too long: "+value) ;
        return VarIntLengths.length+1 ;
    }
    
    // By calculation.
    static int calcLength(long value)
    {
        int len = 1 ;   // The byte with high bit zero.
        long N = value ;
        byte b = 0 ;
        while (true)
        {
            N >>>= 7 ;
            if ( N == 0 )
                break ;
            len ++ ;
        }
        return len ;
    }

}
