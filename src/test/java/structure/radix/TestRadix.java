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

import java.nio.ByteBuffer ;
import java.util.Iterator ;
import java.util.List ;

import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
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
    
    // TODO contains tests
    // TODO Min tests, max tests, iterator tests.
    // TODO isEmpty
    
    @Test public void radix_01()
    { 
        RadixTree t = tree() ;
        test(t) ;
        count(t, 0) ;
        assertTrue(t.isEmpty()) ;
    }

    @Test public void radix_02()
    { 
        byte[] k = {1,2,3,4} ;
        test(k) ;
    }
   
    @Test public void radix_03()
    {
        byte[] k1 = {1,2,3,4} ;
        byte[] k2 = {0,1,2,3} ;
        test(k1, k2) ;
    }

    @Test public void radix_04()
    {
        // Reverse order
        byte[] k1 = {0,1,2,3} ;
        byte[] k2 = {1,2,3,4} ; 
        test(k1, k2) ;
    }
    
    @Test
    public void radix_05()
    {
        byte[] k1 = { 1 , 2 , 3 } ;
        byte[] k2 = { 1 , 2 } ;
        byte[] k3 = { 1 } ;
        testPermute(k1,k2,k3) ;
    }

    @Test
    public void radix_06()
    {
        byte[] k1 = { 1 , 2 , 3 } ;
        byte[] k2 = { 1 , 2 , 5 } ;
        byte[] k3 = { 1 , 2 , 6 } ;
        testPermute(k1,k2,k3) ;
    }

    @Test
    public void radix_07()
    {
        byte[] k1 = { 1 , 3 , 4 } ;
        byte[] k2 = { 1 , 3 , 5 } ;
        byte[] k3 = { 1 , 2 , 6 } ;
        testPermute(k1,k2,k3) ;
    }
    
    @Test
    public void radix_08()
    {
        byte[] k1 = { 3 , 4 } ;
        byte[] k2 = { 3 , 5 } ;
        byte[] k3 = { 2 , 6 } ;
        testPermute(k1, k2, k3) ;
    }
    
    @Test
    public void radix_09()
    {
        byte[] k1 = { } ;
        byte[] k2 = { 1 } ;
        byte[] k3 = { 2, 3 } ;
        testPermute(k1,k2,k3) ;
    }

    @Test
    public void radix_10()
    {
        byte[] k1 = { 2 , 4 , 6 , 8  } ;
        byte[] k2 = { 2 , 4 , 6 , 10  } ;
        byte[] k3 = { 2 , 4 } ;
        testPermute(k1, k2, k3) ;
    }

    @Test public void radix_11()
    { 
        test(key1, key2, key3, key4, key5, key6) ;
    }
    
    
    @Test
    public void radix_12()
    {
        byte[] k1 = { 2 , 4 , 6 , 8  } ;
        byte[] k2 = { 2 , 4 , 6 , 10  } ;
        RadixTree t = tree(k1) ;
        assertTrue(t.contains(k1)) ;
        assertFalse(t.contains(k2)) ;
    }
    
    @Test
    public void radix_13()
    {
        byte[] k1 = { 2 , 4 , 6 , 8  } ;
        byte[] k2 = { 2 , 4 , 6 , 10  } ;
        byte[] k3 = { 2 , 4 , 6 , 9  } ;
        RadixTree t = tree(k1, k2) ;
        assertTrue(t.contains(k1)) ;
        assertTrue(t.contains(k2)) ;
        assertFalse(t.contains(k3)) ;
    }
    
    @Test
    public void radix_iter_1()
    {
        RadixTree t = tree() ;
        Iterator<ByteBuffer> iter = t.iterator() ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test
    public void radix_iter_2()
    {
        RadixTree t = tree(key1, key2, key3) ;
        Iterator<ByteBuffer> iter = t.iterator() ;
        List<ByteBuffer> x = Iter.toList(iter) ;
        assertArrayEquals(key3, x.get(0).array()) ;
        assertArrayEquals(key1, x.get(1).array()) ;
        assertArrayEquals(key2, x.get(2).array()) ;
    }

    static RadixTree tree(byte[] ... keys)
    {
        return tree(new RadixTree(), keys) ;
    }

    final static boolean print = false ; 
    static RadixTree tree(RadixTree t, byte[] ... keys)
    {
        for ( byte[]k : keys )
        {
            if (print) System.out.println("Build: "+RLib.str(k)) ;
            t.insert(k) ;
            if (print) t.print() ;
            t.check() ;
            if (print) System.out.println() ;
        }
        return t ;
    }
    
    /** Add the keys, delete the keys. */
    static void test(byte[]... keys)
    {
        test(new RadixTree(), keys) ;
    }
    
    static void test(RadixTree t, byte[]... keys)
    {
        for ( byte[]k : keys )
        {
            t.insert(k) ;
            t.check() ;
            assertTrue(t.contains(k)) ; 
        }
        count(t, keys.length) ;
        check(t, keys) ;
        if ( keys.length > 0 )
            assertFalse(t.isEmpty()) ;
        for ( byte[]k : keys )
        {
            t.delete(k) ;
            t.check() ;
            assertFalse(t.contains(k)) ; 
        }
        assertTrue(t.isEmpty()) ;
    }

    static void check(RadixTree t, byte[] ... keys)
    {
        for ( byte[]k : keys )
            assertTrue("Not found: Key: "+RLib.str(k), t.contains(k)) ;
    }
    
    private static void insert(RadixTree t, byte[] key)
    {
        t.insert(key) ;
        t.check();
        RadixNode n = t.find(key) ;
        assertNotNull(n) ;
        assertEquals(key.length, n.lenFinish) ;
    }
    
    private static void delete(RadixTree trie, byte[] key)
    {
        boolean b2 = ( trie.find(key) != null ) ;
        boolean b = trie.delete(key) ;
        System.out.flush() ;
        if ( b != b2 )
            System.err.println("Inconsistent (delete)") ;
        trie.check() ;
    }

    static void count(RadixTree t, int size)
    {
        t.check();
        assertEquals(size, t.size()) ;
    }

    static void testPermute(byte[] k1, byte[] k2, byte[] k3)
    {
        test(k1, k2, k3) ;
        test(k1, k3, k2) ;
        test(k2, k1, k3) ;
        test(k2, k3, k1) ;
        test(k3, k1, k2) ;
        test(k3, k2, k1) ;
    }
}
