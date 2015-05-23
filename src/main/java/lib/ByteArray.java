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

package lib ;

import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.AtlasException ;


/** An expanding byte array.
 *  Like a byte[] but it can grow. 
 */
public final class ByteArray //implements java.lang.Iterable<Byte>
{
    private int length ;
    private byte[] bytes ;
    
    /** Allocate size bytes of active space */ 
    public ByteArray(int size)
    { 
        this(size, size) ;
    }
    
    /** Allocate empty array (may have a few bytes for expansion) */ 
    public ByteArray()
    { this(0, 8) ; }
    
    /** Allocate an array with a hint to sufficient space for growth */
    public ByteArray(int size, int initialAllocation)
    {
        bytes = new byte[initialAllocation] ;
        length = size ;
    }
    
    /** Return a byte */
    public byte get(int idx)
    {
        check(idx) ;
        return bytes[idx] ;
    }

    /** Set a byte */
    public void set(int idx, byte b)
    {
        if ( idx == length)
        {
            add(b) ;
            return ;
        }
        
        check(idx) ;
    
        bytes[idx] = b ;
    }

    /** add a byte to the end of the array */
    public void add(byte b)
    {
        if ( length == bytes.length )
            realloc(8) ;
        bytes[length] = b ;
        length++ ;
    }
    
    /** add bytes to the end of the array */
    public void add(byte[] b)
    { add(b, 0, b.length) ; }

    /** add bytes to the end of the array */
    public void add(byte[] b, int start, int len)
    {
        if ( length+len >= bytes.length )
            realloc(len) ;
        System.arraycopy(bytes, length, b, start, len) ;
    }

    
    /** add copy ByteArray bytes to the end of the array */
    public void add(ByteArray b)
    {
        add(b.bytes, 0, b.length) ;
    }
    
    private void realloc(int minGrowth)
    {
        int currLen = bytes.length ;
        int newLen = currLen+currLen/2+1 ;
        if ( newLen-currLen < minGrowth )
            newLen = currLen + minGrowth ; 
        
        byte[] bytes2 = new byte[newLen] ;
        System.arraycopy(bytes, 0, bytes2, 0, currLen) ;
        // length still the same.
    }

    /** Current length */
    public int length() { return length ; }
    
    /** Current max size without reallocation */
    public int currentAlloc() { return bytes.length ; }

    public void ensure(int size)
    {
        if ( size < bytes.length )
            realloc(bytes.length-size) ;
    }
    
    private final void check(int idx)
    {
        if ( idx < 0 || idx > length)
            throw new AtlasException(String.format("Out of bounds %d [0, %d]", idx, length)) ;
    }

    // Don't encourage iterators - boxing overhead.
    //@Override
    public Iterator<Byte> iterator()
    {
        return new IteratorByteArray(bytes, 0, length) ;
    }

    static class IteratorByteArray implements Iterator<Byte>
    {
        private int idx ;
        private byte[] bytes ;
        private int finishIdx ;
        
        public IteratorByteArray(byte[] bytes, int start, int finish)
        {
            if ( start < 0 )
                throw new IllegalArgumentException("Start: "+start) ;

            if ( start > finish )
                throw new IllegalArgumentException("Start >= finish: "+start+" >= "+finish) ;

            // Instead: truncate to array length          
            //        if ( finish > array.length )
            //            throw new IllegalArgumentException("Finish outside array") ;
            //        
            // Instead: immediate end iterator                
            //        if ( start >= array.length )
            //            throw new IllegalArgumentException("Start outside array") ;

            this.bytes = bytes ;
            idx = start ;
            finishIdx = finish ;
            if ( idx < 0 )
                idx = 0 ;
            if ( finishIdx > bytes.length ) 
                finishIdx = bytes.length ;
        }

    //@Override
    @Override
    public boolean hasNext()
    {
//        if ( idx < 0 )
//            return false ;
        if ( idx >= finishIdx )
            return false ;
        return true ;
    }

    public byte current()
    {
        if ( idx >= finishIdx )
            throw new NoSuchElementException() ;
        return bytes[idx] ;
    }
    
    //@Override
    @Override
    public Byte next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ; 
        return bytes[idx++] ;
    }

    //@Override
    @Override
    public void remove()
    { throw new UnsupportedOperationException("IterBytes") ; }
        
    }

    
}
