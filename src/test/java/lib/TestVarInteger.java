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

package lib ;

import java.nio.ByteBuffer ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.ByteBufferLib ;

public class TestVarInteger extends BaseTest
{
    @Test public void varint_01()
    {
        VarInteger vint = VarInteger.valueOf(0) ;
        assertEquals(1, vint.length()) ;
        assertEquals((byte)0, vint.bytes()[0]) ;
        assertEquals(0L, vint.value()) ;
    }
    
    @Test public void varint_02()
    {
        VarInteger vint = VarInteger.valueOf(1) ;
        assertEquals(1, vint.length()) ;
        assertEquals((byte)1, vint.bytes()[0]) ;
        assertEquals(1L, vint.value()) ;
    }
    
    @Test public void varint_03()
    {
        VarInteger vint = VarInteger.valueOf(127) ;
        assertEquals(1, vint.length()) ;
        assertEquals((byte)0x7F, vint.bytes()[0]) ;
        assertEquals(127L, vint.value()) ;
    }

    @Test public void varint_04()
    {
        VarInteger vint = VarInteger.valueOf(128) ;
        assertEquals(2, vint.length()) ;
        assertEquals((byte)0x80, vint.bytes()[0]) ;
        assertEquals((byte)0x01, vint.bytes()[1]) ;
        assertEquals(128L, vint.value()) ;
    }

    @Test public void varint_05()
    {
        VarInteger vint = VarInteger.valueOf(129) ;
        assertEquals(2, vint.length()) ;
        assertEquals((byte)0x81, vint.bytes()[0]) ;
        assertEquals((byte)0x01, vint.bytes()[1]) ;
        assertEquals(129L, vint.value()) ;
    }

    @Test public void varint_10()
    {
        VarInteger vint = VarInteger.valueOf(1L<<45) ;
        //assertEquals(2, vint.length()) ;
        assertEquals(1L<<45, vint.value()) ;
    }
    
    // General hammering.
    @Test public void varint_N()
    {
        for ( long x = 0 ; x < (1L<<17) ; x++ )
        {
            VarInteger vint = VarInteger.valueOf(x) ;
            assertEquals(x, vint.value()) ;
        }
    }
    
    @Test public void varint_eq_1()
    {
        VarInteger x = VarInteger.valueOf(0) ;
        VarInteger x0 = VarInteger.valueOf(0) ;
        VarInteger x1 = VarInteger.valueOf(1) ;
        assertEquals(x.hashCode(), x0.hashCode()) ;
        assertNotEquals(x.hashCode(), x1.hashCode()) ;
        assertEquals(x, x0) ;
        assertNotEquals(x, x1) ;
    }
    
    @Test public void varint_eq_2()
    {
        VarInteger x = VarInteger.valueOf(1) ;
        VarInteger x0 = VarInteger.valueOf(0) ;
        VarInteger x1 = VarInteger.valueOf(1) ;
        assertEquals(x.hashCode(), x1.hashCode()) ;
        assertNotEquals(x.hashCode(), x0.hashCode()) ;
        assertEquals(x, x1) ;
        assertNotEquals(x, x0) ;
    }

    private static void eq(long value)
    {
        VarInteger x0 = VarInteger.valueOf(value) ;
        VarInteger x1 = VarInteger.valueOf(value) ;
        assertEquals(x0.hashCode(), x1.hashCode()) ;
        assertEquals(x0, x1) ;
    }
    
    @Test public void varint_eq_3()     { eq(127) ; }
    @Test public void varint_eq_4()     { eq(128) ; }
    @Test public void varint_eq_5()     { eq(129) ; }
    
    @Test public void varint_bb_1()
    {
        ByteBuffer bb = ByteBuffer.allocate(8) ;
        ByteBufferLib.fill(bb, (byte)0) ;
        VarInteger.encode(bb, 1, 2L<<14) ;
        assertEquals(0, bb.get(0)) ;
    }

    @Test public void varint_extract_1()
    {
        VarInteger x0 = VarInteger.valueOf(113) ;
        VarInteger x1 = VarInteger.make(x0.bytes) ;
        assertEquals(x0, x1) ;
    }

    @Test public void varint_extract_2()
    {
        VarInteger x0 = VarInteger.valueOf(113) ;
        ByteBuffer bb = ByteBuffer.wrap(x0.bytes()) ;
        VarInteger x1 = VarInteger.make(bb,0) ;
        assertEquals(x0, x1) ;
    }
    
    @Test public void varint_extract_3()
    {
        VarInteger x0 = VarInteger.valueOf(11377) ;
        ByteBuffer bb = ByteBuffer.wrap(x0.bytes()) ;
        VarInteger x1 = VarInteger.make(bb,0) ;
        assertEquals(x0, x1) ;
    }
    
    @Test public void varint_length_1()
    {
        assertEquals(1, VarInteger.lengthOf(0)) ;
        assertEquals(1, VarInteger.lengthOf(1)) ;
        assertEquals(1, VarInteger.lengthOf(127)) ;
        assertEquals(2, VarInteger.lengthOf(128)) ;
        assertEquals(2, VarInteger.lengthOf(1L<<14-1)) ;
        assertEquals(3, VarInteger.lengthOf(1L<<14)) ;
        assertEquals(8, VarInteger.lengthOf(1L<<56-1)) ;
    }
}
