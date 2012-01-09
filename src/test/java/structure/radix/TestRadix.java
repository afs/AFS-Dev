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

package structure.radix;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

public class TestRadix extends BaseTest
{
    // Test order : This sequence of keys triggers every case of insert.
    
    static byte[] key1 = { 2 , 4 , 6 , 8  } ;
    
    static byte[] key2 = { 2 , 4 , 6 , 10  } ;
    // Insert - shorter key
    static byte[] key3 = { 2 , 4 } ;
    
    // Insert - existing leaf
    static byte[] key4 = { 2 , 4 , 6,  8 , 10 } ;

    // Insert - partial prefix match.
    static byte[] key5 = { 2 , 4 , 3 , 1 } ;
    
    // Insert - new root
    static byte[] key6 = { 0 , 1 , 2 , 3 , 4  } ;
    
    @Test public void radix_01()
    { 
        RadixTree t = new RadixTree() ;
        test(0, t) ;
    }

    @Test public void radix_02()
    { 
        RadixTree t = new RadixTree() ;
        t.insert(new byte[]{1,2,3,4}) ;
        test(1, t) ;
    }

    @Test public void radix_03()
    { 
        RadixTree t = new RadixTree() ;
        t.insert(new byte[]{1,2,3,4}) ;
        t.insert(new byte[]{0,1,2,3}) ;
        test(2, t) ;
    }

    @Test public void radix_04()
    { 
        RadixTree t = new RadixTree() ;
        t.insert(new byte[]{0,1,2,3}) ;
        t.insert(new byte[]{1,2,3,4}) ;
        test(2, t) ;
    }
    
    @Test public void radix_10()
    { 
        test(new byte[][] { key1, key2, key3, key4, key5, key6 }) ;
    }
        

    private void test(byte[][] keys)
    {
        RadixTree t = new RadixTree() ;
        for ( byte[]k : keys )
        {
            t.insert(k) ;
            t.check() ;
            assertTrue(t.contains(k)) ; 
        }
        assertFalse(t.isEmpty()) ;
        for ( byte[]k : keys )
        {
            t.delete(k) ;
            t.check() ;
            assertFalse(t.contains(k)) ; 
        }
        assertTrue(t.isEmpty()) ;
    }

    private static void insert(RadixTree t, byte[] key)
    {
        t.insert(key) ;
        t.check();
        RadixNode n = t.find(key) ;
        assertNotNull(n) ;
        assertEquals(key.length, n.lenFinish) ;
    }
    
    private static void test(int size, RadixTree t)
    {
        t.check();
        assertEquals(size, t.size()) ;
    }
}