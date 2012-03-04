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
import static structure.radix.RLib.str ;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.junit.Assert ;
import org.junit.runner.JUnitCore ;
import org.junit.runner.Result ;
import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.TextListener2 ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.RandomLib ;
import org.openjena.atlas.logging.Log ;

public class RadixRun
{
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
    
    public static void main(String ...argv)
    { 
        Log.enable("structure.radix") ;
        // TODO
        // check tests.
        // turn on insert then delete for all tests.
        
//        byte[] k1 = { 2 , 4 , 6 , 8  } ;
//        byte[] k2 = { 2 , 4 , 6 , 10  } ;
//        // @Test 3: Insert existing
//        tree(k1, k2, k1, k2) ;
//        System.exit(0) ;
        
//        RadixTree t = new RadixTree() ;
//        t.print() ;
//        t.insert(key1) ;
//        t.print() ;
//        System.out.println("Key: "+RLib.str(key1)+" = "+t.contains(key1)) ;
//        System.out.println("Key: "+RLib.str(key2)+" = "+t.contains(key2)) ;
//        t.contains(key2) ;
//        System.exit(0) ;
        //test(new byte[][] { key1, key2, key3, key4, key5, key6 }) ;

        test(key1, key2, key3, key4, key5, key6) ;
        
        // Test A diverging key, shorter than existing
        // Test B diverging key, longer  than existing
        // Test C: add shortening keys 321 32 1
        // Test D: add lengthening keys 1 12 123 (test exists already?)
        
    }
    
    static boolean print = true ;
    
    static private RadixTree tree(byte[] ... keys)
    {
        RadixTree t = new RadixTree() ;
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
    
    static private void test(byte[]... keys)
    {
        RadixTree t = new RadixTree() ;
        for ( byte[]k : keys )
        {
            if (print) System.out.println("Ins: "+RLib.str(k)) ;
            t.insert(k) ;
            if (print) t.print() ;
            t.check() ;
            if (print) t.print() ;
            Assert.assertTrue("Key not found after insert: "+RLib.str(k), t.contains(k)) ; 
            if (print) System.out.println() ;
        }
        Assert.assertFalse(t.isEmpty()) ;
        for ( byte[]k : keys )
        {
            if (print) System.out.println("Del: "+RLib.str(k)) ;
            t.delete(k) ;
            t.check() ;
            if (print) t.print() ;
            Assert.assertFalse("Key found after delete: "+RLib.str(k), t.contains(k)) ; 
            if (print) System.out.println() ;
        }
        Assert.assertTrue(t.isEmpty()) ;
    }
    
    public static void main1(String ...argv) 
    {
        Log.setLog4j() ;
        Log.enable(RadixTree.class) ;
        //RadixTree.logging = false ;
        
        if ( false )
        {
            JUnitCore runner = new org.junit.runner.JUnitCore() ;
            runner.addListener(new TextListener2(System.out)) ;
            //TestRadix.beforeClass() ;
            Result result = runner.run(TestRadix.class) ;
            //TestRadix.afterClass() ;
            System.exit(0) ;
        }

        if ( true )
        {
            RadixTree.logging = false ;
            
            int nRuns = 1000000 ;
            int maxLen = 7 ;
            int nKeys = 20 ;
            int dotsToCycle = nRuns > 10000 ? 100 : 10 ;
            int dotsPerLine = 100 ;
            
            final int ticksPerLine = dotsToCycle*dotsPerLine ;
            
            System.out.printf("Runs: %d, maxLen=%d, nKeys=%d\n", nRuns, maxLen, nKeys ) ;
            for ( int i = 0 ; i < nRuns ; i++ )
            {
                RadixTree trie = new RadixTree() ;
                List<byte[]> x = gen(nKeys, maxLen, (byte)20) ;
                List<byte[]> x2 = randomize(x) ;
                
                if ( i%ticksPerLine == 0 )
                    System.out.printf("%6d: ",i) ;
                if ( i%dotsToCycle == (dotsToCycle-1) )
                    System.out.print(".") ;
                if ( i%ticksPerLine == (ticksPerLine-1) )
                    System.out.println() ;
                
                try { 
                    execInsert(trie, x, false) ;
                    execDelete(trie, x2, false) ;
                } catch (AtlasException ex)
                {
                    print(x) ;
                    print(x2) ;
                    return ;
                }
            }
            if ( nRuns%ticksPerLine != 0 )
                System.out.println() ;
            System.out.println("Done") ;
            System.exit(0) ;
        }
        
        RadixTree.logging = true ;
        RadixTree trie = new RadixTree() ;
        
        byte[][] data1$ = { {0x05,0x00,0x06}, {0x05,0x02}, {}, {0x09,0x01,0x01,0x01}, {0x08,0x07} } ;
        byte[][] data2$ = { {0x09,0x01,0x01,0x01}, {0x05,0x00,0x06}, {0x05,0x02}, {}, {0x08,0x07} } ;
            
        List<byte[]> x1 = Arrays.asList(data1$) ;
        List<byte[]> x2 = Arrays.asList(data1$) ;
        print(x1) ;
        print(x2) ;
        execInsert(trie, x1, true) ;
        execDelete(trie, x2, true) ;
        System.out.println("Done") ;
        System.exit(0) ;
    }
    
    
    
    private static List<byte[]> randomize(List<byte[]> x)
    {
        x = new ArrayList<byte[]>(x) ;
        List<byte[]> x2 = new ArrayList<byte[]>() ;
        for ( int i = 0 ; x.size() > 0 ; i++ )
        {
            int idx = RandomLib.qrandom.nextInt(x.size()) ;
            x2.add(x.remove(idx)) ;
        }
        return x2 ;
    }

    static boolean contains(byte[] b, List<byte[]> entries)
    {
        for ( byte[] e : entries )
        {
            if ( Arrays.equals(e, b) )
                return true ;
        }
        return false ;
    }
    

//    public static void exec(RadixTree trie, int[] ... e)
//    {
//        List<byte[]> entries = new ArrayList<byte[]>() ;
//        for ( int j = 0 ; j < e.length ; j++ )
//        {
//            byte[] b = new byte[e[j].length] ;
//            for ( int i = 0 ; i < e[j].length ; i++ )
//                b[i] = (byte)(e[j][i]) ;
//            entries.add(b) ;
//        }
//        exec(trie, entries, false) ;
//    }
        
        
    private static void execInsert(RadixTree trie, List<byte[]> entries, boolean debugMode)
    {
        try {
            for ( byte[] arr : entries )
            {
//                if ( debugMode )
//                    System.out.println("Insert: "+str(arr)) ;
                insert(trie, arr) ;
                if ( debugMode )
                {
                    trie.print() ;
                    System.out.println() ;
                }
            }
        } catch (AtlasException ex)
        {
            System.out.flush() ;
            ex.printStackTrace(System.err) ;
            trie.print() ;
            throw ex ;
        }
        
        check(trie, entries) ;
    }
   
    private static void execDelete(RadixTree trie, List<byte[]> entries, boolean debugMode)
    {
        try {
            for ( byte[] arr : entries )
            {
//                if ( debugMode )
//                    System.out.println("Delete: "+str(arr)) ;
                delete(trie, arr) ;
                if ( debugMode )
                {
                    trie.print() ;
                    System.out.println() ;
                }
            }
        } catch (AtlasException ex)
        {
            System.out.flush() ;
            ex.printStackTrace(System.err) ;
            trie.print() ;
            throw ex ;
        }
        if ( trie.iterator().hasNext() )
        {
            System.out.flush() ;
            System.err.println("Tree still has elements") ;
            trie.print() ;
        }
        
    }

    //    static void search(RadixTree trie, byte[] key)
//    {
//        System.out.println("Search--'"+Bytes.asHex(key)+"'") ;
//        RadixNode node = trie.search(key) ;
//        System.out.println("Search>> "+node) ;
//        System.out.println() ;
//    }

    private static void print(List<byte[]> entries)
    {
        boolean first = true ;
        StringBuilder sb = new StringBuilder() ;
        sb.append("byte[][] data = { ") ;
        for ( byte[] e : entries )
        {
            if ( ! first ) 
                sb.append(", ") ;
            first = false ;
            
            sb.append("{") ;
            sb.append(str(e,"," )) ;
            sb.append("}") ;
        }
        sb.append(" } ;") ;
        System.out.println(sb.toString()) ;
    }
    
    private static String strall(List<byte[]> entries)
    {
        boolean first = true ;
        StringBuilder sb = new StringBuilder() ;
        for ( byte[] e : entries )
        {
            if ( ! first ) 
                sb.append(" ") ;
            first = false ;
            sb.append("[") ;
            sb.append(str(e)) ;
            sb.append("]") ;
        }
        return sb.toString() ;
    }

    static void check(RadixTree trie, List<byte[]> keys)
    {
        for ( byte[] k : keys )
        {
            if ( trie.find(k) == null )
                System.err.println("Did not find: "+str(k)) ;
        }
        
        long N1 = trie.size() ;
        long N2 = Iter.count(trie.iterator()) ; 
        if ( N1 != N2 )
            System.err.printf("size[%d] != count[%d]",N1, N2) ;
        if ( N1 != keys.size() )
            System.err.printf("size[%d] != length[%d]",N1, keys.size()) ;
        
        // check ordered.
        ByteBuffer prev = null ;
        for ( Iterator<ByteBuffer> elements = trie.iterator() ; elements.hasNext() ; )
        {
            ByteBuffer here = elements.next() ;
            if ( prev != null )
            {
                if ( Bytes.compare(prev.array(), here.array()) >= 0 )
                    System.err.println("Not increasing: "+str(prev.array())+" // "+str(here.array())) ;
            }
            prev = here ;
        }

        
    }
    
    static void find(RadixTree trie, byte[] key)
    {
//        System.out.println("Find --'"+Bytes.asHex(key)+"'") ;
        RadixNode node = trie.find(key) ;
//        System.out.println("Find >> "+node) ;
//        System.out.println() ;
    }

    static void insert(RadixTree trie, byte[] key)
    {
//        System.out.println("Insert--'"+str(key)+"'") ;
        boolean b2 = ( trie.find(key) == null ) ;
        boolean b = trie.insert(key) ;
//        System.out.print(" >> "+b+" ["+b2+"]") ;
//        System.out.print(": ") ;
//        trie.print() ;
//        trie.printLeaves() ;
        //trie.print(); 
        System.out.flush() ;
        if ( b != b2 )
            System.err.println("Inconsistent (insert)") ;
        trie.check() ;
    }
    
    static void delete(RadixTree trie, byte[] key)
    {
//        System.out.println("Delete--'"+str(key)+"'") ;
        boolean b2 = ( trie.find(key) != null ) ;
        boolean b = trie.delete(key) ;
//        System.out.print(" >> "+b+" ["+b2+"]") ;
//        System.out.print(": ") ;
//        trie.printLeaves() ;
//        trie.print(); 
        System.out.flush() ;
        if ( b != b2 )
            System.err.println("Inconsistent (delete)") ;
        trie.check() ;
    }

    // Generate nKeys entries upto to nLen long
    static List<byte[]> gen(int nKeys, int maxLen, byte maxVal)
    {
        List<byte[]> entries = new ArrayList<byte[]>() ;
        
        for ( int i = 0 ; i < nKeys ; i++)
        {
            
            while(true)
            {
                byte[] b = gen1(maxLen, maxVal) ;
                if ( ! contains(b, entries) )
                {
                    entries.add(b) ;
                    break ;
                }
            }
        }
        
        return entries ;
    }
    
    static byte[] gen1(int nLen, byte maxVal )
    {
        int len = RandomLib.qrandom.nextInt(nLen) ;
        byte[] b = new byte[len] ;
        for ( int i = 0 ; i < len ; i++ )
            b[i] = (byte)(RandomLib.qrandom.nextInt(maxVal)&0xFF) ;
        return b ;
    }
}
