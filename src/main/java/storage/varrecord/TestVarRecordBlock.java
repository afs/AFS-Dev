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

package storage.varrecord;

import java.nio.ByteBuffer;

import org.apache.jena.tdb.base.record.Record;

import org.junit.Test;
import org.apache.jena.atlas.junit.BaseTest ;

public class TestVarRecordBlock extends BaseTest
{
    // Some data items.
    static byte[] d1 = make(1,2,3,4) ;
    static byte[] d2 = make(0xFF, 0xFE) ;

    static byte[] d3 = make(5,4,5,4,3,2,3,2,1,1) ;
    static byte[] d4 = make(0xFF, 0xFE, 0xFD, 0xFC, 0xFB) ;

    
    //Record r1 = new Record() ;
    
    @Test public void vrb_new_1()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(100) ;
        assertEquals(100, vrb.getByteBuffer().capacity()) ;
        assertEquals(100, vrb.getByteBuffer().limit()) ;
        assertEquals(0, vrb.size()) ;
        assertEquals(4, vrb.bytesInUse()) ;
    }
    
    @Test public void vrb_2()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(20) ;
        vrb.put(d1,d2) ;
        assertEquals(1, vrb.size()) ;
        int len = d1.length+d2.length+1+1 +2+2+4 ; // varInt of length 1,twice, the index slots, twice and the count slot 
        assertEquals(len, vrb.bytesInUse()) ;
    }
    
    @Test public void vrb_3()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(50) ;
        vrb.put(d1,d2) ;
        vrb.put(d1,d2) ;
        assertEquals(2, vrb.size()) ;
    }
    
    @Test public void vrb_3a()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(50) ;
        vrb.put(d1,d2) ;
        vrb.put(d3,d4) ;
        int len = d1.length + d2.length + d3.length + d4.length + 4/*VarInts*/ + 4*2 + 4 ;
        assertEquals(2, vrb.size()) ;
        assertEquals(len, vrb.bytesInUse()) ;
    }
    

    @Test public void vrb_4()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(20) ;
        vrb.put(0, d1,d2) ;
        Record r = vrb.get(0) ;
        assertArrayEquals(d1, r.getKey()) ;
        assertArrayEquals(d2, r.getValue()) ;
    }

    @Test public void vrb_4a()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(100) ;
        vrb.put(d1,d2) ;
        vrb.put(d3,d4) ;
        Record r = vrb.get(0) ;
        assertArrayEquals(d1, r.getKey()) ;
        assertArrayEquals(d2, r.getValue()) ;
        
        r = vrb.get(1) ;
        assertArrayEquals(d3, r.getKey()) ;
        assertArrayEquals(d4, r.getValue()) ;
    }
    
    @Test public void vrb_5()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(20) ;
        vrb.put(0, d1, d2) ;
        byte[] v = vrb.find(d1) ;
        assertArrayEquals(d2, v) ;
        assertNull(vrb.find(d2)) ;
        assertNull(vrb.find(d3)) ;
    }

    @Test public void vrb_6()
    {
        // Insert at specific place.
        VarRecordBuffer vrb = new VarRecordBuffer(50) ;
        vrb.put(0, d1,d2) ;
        vrb.put(0, d1,d2) ;
        assertEquals(2, vrb.size()) ;
    }
    
    @Test public void vrb_7()
    {
        // Too small.
        VarRecordBuffer vrb = new VarRecordBuffer(20) ;
        vrb.put(d1,d2) ;
        try { vrb.put(d1,d2) ; } catch (Exception ex) {} 
        assertEquals(1, vrb.size()) ;
        Record r = vrb.get(0) ;
        assertArrayEquals(d1, r.getKey()) ;
        assertArrayEquals(d2, r.getValue()) ;
    }
    
    
    // Delete
    @Test public void vrb_10()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(20) ;
        vrb.put(0, d1,d2) ;
        vrb.delete(0) ;
        assertEquals(0, vrb.size()) ;
    }
    
    @Test public void vrb_11()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(20) ;
        vrb.put(0, d1,d2) ;
        vrb.delete() ;
        assertEquals(0, vrb.size()) ;
    }

    @Test public void vrb_12()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(100) ;
        vrb.put(0, d1,d2) ;
        vrb.put(1, d3,d4) ;
        vrb.delete() ;
        assertEquals(1, vrb.size()) ;
        Record r = vrb.get(0) ;
        assertArrayEquals(d1, r.getKey()) ;
        assertArrayEquals(d2, r.getValue()) ;
        
    }
    
    @Test public void vrb_13()
    {
        VarRecordBuffer vrb = new VarRecordBuffer(100) ;
        vrb.put(0, d1,d2) ;
        vrb.put(1, d3,d4) ;
        vrb.delete(0) ;
        assertEquals(1, vrb.size()) ;
        Record r = vrb.get(0) ;
        assertArrayEquals(d3, r.getKey()) ;
        assertArrayEquals(d4, r.getValue()) ;
        
    }

    @Test public void vrb_reattach_1()
    {
        int capacity = 100 ;
        VarRecordBuffer vrb = new VarRecordBuffer(capacity) ;
        vrb.put(0, d1,d2) ;
        
        ByteBuffer bb = ByteBuffer.allocate(capacity) ;
        bb.put(vrb.getByteBuffer()) ;
        VarRecordBuffer vrb2 = VarRecordBuffer.wrap(bb) ;
        vrb2.put(1, d3,d4) ;
        
        assertEquals(2, vrb2.size()) ;
        assertEquals(1, vrb.size()) ;
        
        Record r = vrb2.get(0) ;
        assertArrayEquals(d1, r.getKey()) ;
        assertArrayEquals(d2, r.getValue()) ;
        r = vrb2.get(1) ;
        assertArrayEquals(d3, r.getKey()) ;
        assertArrayEquals(d4, r.getValue()) ;
    }

    
    // A neater way to write byte[] x = {(byte)0xFF...} because no casts needed for > 128.
    private static byte[] make(int ... values)
    {
        byte[] array = new byte[values.length] ;
        for ( int i = 0 ; i < values.length ; i++ )
        {
            int v = values[i] ;
            if ( v > 255 )
                throw new IllegalArgumentException("Bad value for byte: "+v) ;
            byte b = (byte)(v&0xFF) ;
            array[i] = b ;
        }
        return array ;
    }
    
    
}
