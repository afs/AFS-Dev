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

package projects.tools;

import java.util.HashMap;
import java.util.HashSet;

public class Memory
{
    // See also:
    // http://www.javaworld.com/javaworld/javatips/jw-javatip130.html (32 bit only)
    
    
    /* 32 bit
Object                    => 8
FakeAvlNode1              => 24
FakeAvlNode2              => 32
FakeTTreeNodeNull         => 32 -- 2 ints, 4 slots (inc array), null array, parent
FakeTTreeNode0            => 48 -- 2 ints, 4 slots (inc array), 0 array, parent
FakeTTreeNode0np          => 48 -- 2 ints, 4 slots (inc array), 0 array, no parent
FakeTTreeNode1            => 48
FakeTTreeNode1np          => 48
FakeTTreeNode1npSlice     => 48
FakeTTreeNode2            => 56
FakeTTreeNode2np          => 56
FakeTTreeNode2npSlice     => 56
Struct1                   => 16
Struct1a                  => 16
Struct2                   => 16
Struct3                   => 24
Struct4                   => 24
Struct5                   => 32
     */

    /* 64 bit
Object                    => 16
FakeAvlNode1              => 48
FakeAvlNode2              => 56
FakeTTreeNodeNull         => 56
FakeTTreeNode0            => 80
FakeTTreeNode0np          => 72
FakeTTreeNode0npSlice     => 80
FakeTTreeNode1            => 88
FakeTTreeNode1np          => 80
FakeTTreeNode1npSlice     => 88
FakeTTreeNode2            => 96
FakeTTreeNode2np          => 88
FakeTTreeNode2npSlice     => 96
Struct1                   => 24
Struct1a                  => 32
Struct2                   => 32
Struct3                   => 40
Struct4                   => 48
Struct5                   => 56
     */
    
    static class FakeTTreeNodeNull { 
        int height = 99 ;
        FakeTTreeNodeNull parent = null ;
        FakeTTreeNodeNull left  = null ;
        FakeTTreeNodeNull right  = null ;
        int arrayActiveLen = 0 ;
        Object[] record  = null ;
    } 
    
    static class FakeTTreeNode0 { 
        int height = 99 ;
        FakeTTreeNode0 parent = null ;
        FakeTTreeNode0 left  = null ;
        FakeTTreeNode0 right  = null ;
        int arrayActiveLen = 0 ;
        Object[] record  = new Object[0] ;
    }   
    
    static class FakeTTreeNode0np { 
        int height = 99 ;
        //*** FakeTTreeNode1 parent = null ;
        FakeTTreeNode0np left  = null ;
        FakeTTreeNode0np right  = null ;
        int arrayActiveLen = 0 ;
        Object[] record  = new Object[0] ;
    }   
    
    static class FakeTTreeNode0npSlice { 
        int height = 99 ;
        //*** FakeTTreeNode1 parent = null ;
        FakeTTreeNode0npSlice left  = null ;
        FakeTTreeNode0npSlice right  = null ;
        int arrayActiveLen = 0 ;
        int arrayStart = 0 ;
        Object[] record  = new Object[0] ;
    }   
    
    static class FakeTTreeNode0Slice { 
        int height = 99 ;
        FakeTTreeNode0Slice parent = null ;
        FakeTTreeNode0Slice left  = null ;
        FakeTTreeNode0Slice right  = null ;
        int arrayActiveLen = 0 ;
        int arrayStart = 0 ;
        Object[] record  = new Object[0] ;
    }   

    static class FakeTTreeNode1 { 
        int height = 99 ;
        FakeTTreeNode1 parent = null ;
        FakeTTreeNode1 left  = null ;
        FakeTTreeNode1 right  = null ;
        int arrayActiveLen = 0 ;
        Object[] record  = new Object[1] ;
    }   

    static class FakeTTreeNode1np { 
        int height = 99 ;
        //*** FakeTTreeNode1 parent = null ;
        FakeTTreeNode1np left  = null ;
        FakeTTreeNode1np right  = null ;
        int arrayActiveLen = 0 ;
        Object[] record  = new Object[1] ;
    }   
    
    static class FakeTTreeNode1npSlice { 
        int height = 99 ;
        //*** FakeTTreeNode1 parent = null ;
        FakeTTreeNode1npSlice left  = null ;
        FakeTTreeNode1npSlice right  = null ;
        int arrayActiveLen = 0 ;
        int arrayStart = 0 ;
        Object[] record  = new Object[1] ;
    }   
    
    static class FakeTTreeNode1Slice { 
        int height = 99 ;
        FakeTTreeNode1Slice parent = null ;
        FakeTTreeNode1Slice left  = null ;
        FakeTTreeNode1Slice right  = null ;
        int arrayActiveLen = 0 ;
        int arrayStart = 0 ;
        Object[] record  = new Object[1] ;
    }   


    static class FakeTTreeNode2 { 
        int height = 99 ;
        FakeTTreeNode2 parent = null ;
        FakeTTreeNode2 left  = null ;
        FakeTTreeNode2 right  = null ;
        int arrayActiveLen = 0 ;
        Object[] record  = new Object[2] ;
    }   
    
    static class FakeTTreeNode2np { 
        int height = 99 ;
        //*** FakeTTreeNode2 parent = null ;
        FakeTTreeNode2np left  = null ;
        FakeTTreeNode2np right  = null ;
        int arrayActiveLen = 0 ;
        Object[] record  = new Object[2] ;
    }   
    
    static class FakeTTreeNode2npSlice { 
        int height = 99 ;
        //*** FakeTTreeNode2 parent = null ;
        FakeTTreeNode2npSlice left  = null ;
        FakeTTreeNode2npSlice right  = null ;
        int arrayActiveLen = 0 ;
        int arrayStart = 0 ;
        Object[] record  = new Object[2] ;
    }   
    
    static class FakeTTreeNode2Slice { 
        int height = 99 ;
        FakeTTreeNode2Slice parent = null ;
        FakeTTreeNode2Slice left  = null ;
        FakeTTreeNode2Slice right  = null ;
        int arrayActiveLen = 0 ;
        int arrayStart = 0 ;
        Object[] record  = new Object[2] ;
    }   

    
    static class FakeAvlNode1 {
        // No parent
        int height = 99 ;
        FakeAvlNode1 left  = null ;
        FakeAvlNode1 right  = null ;
        Object record  = null ;
    }



    static class FakeAvlNode2 {
        int height = 99 ;
        FakeAvlNode2 parent = null ;
        FakeAvlNode2 left  = null ;
        FakeAvlNode2 right  = null ;
        Object record  = null ;
    }


    static class Struct1
    {
        Object slot1 = null ;
    }

    static class Struct1a
    {
        int x = 0 ;
        Object slot1 = null ;
    }

    static class Struct2
    {
        Object slot1 = null ;
        Object slot2  = null ;
    }

    static class Struct3
    {
        Object slot1 = null ;
        Object slot2  = null ;
        Object slot3  = null ;
    }

    static class Struct4
    {
        Object slot1 = null ;
        Object slot2  = null ;
        Object slot3  = null ;
        Object slot4  = null ;
    }

    static class Struct5
    {
        Object slot1 = null ;
        Object slot2  = null ;
        Object slot3  = null ;
        Object slot4  = null ;
        Object slot5  = null ;
    }

    /* A Java HashMap is:--
    transient Entry[] table;
    transient int size;
    int threshold;
    final float loadFactor;
    transient volatile int modCount;
    or 4*4 byte values and an array (3+N) 
   */
    /* A HashSet is:
    HashMap<E,Object> map;
    and a fixed Object value of "PRESENT" ;
    so +16 bytes (8 object, one slot, round to 8 bytes) 
    */
    
    // Trove HashMaps are larger although shoudl grow less as they don't have a Map.Entry overhead.
    
    // HashMap - default size.
    static class StructMap16
    {
        Object slot1 = new HashMap<Object, Object>() ;
    }
    
    static int Size = 100*1000 ;
    public static void main(String...argv)
    {
        // Warm up.
        for ( int i = 0 ; i < 2 ; i++ )
            run(false) ;
        run(true) ;
        System.out.println() ;
        run(true) ;
    }
    
    
    
    private static void run(boolean print)
    {
        Class<?> classes[] = 
        {   
            HashMap.class, 
            HashSet.class , 
            StructMap16.class ,
//            gnu.trove.THashMap.class ,
//            gnu.trove.THashSet.class ,
            
//            HashMap.class, HashSet.class , 
//            StructMap16.class ,
            
//            Object.class, 
//            FakeAvlNode1.class, FakeAvlNode2.class,
//            FakeTTreeNodeNull.class,
//            
//            FakeTTreeNode0.class,
//            FakeTTreeNode0Slice.class,
//            FakeTTreeNode0np.class,
//            FakeTTreeNode0npSlice.class,
//            
//            FakeTTreeNode1.class,
//            FakeTTreeNode1Slice.class,
//            FakeTTreeNode1np.class,
//            FakeTTreeNode1npSlice.class,
//
//            FakeTTreeNode2.class,
//            FakeTTreeNode2Slice.class,
//            FakeTTreeNode2np.class,
//            FakeTTreeNode2npSlice.class,
//
//            Struct1.class, Struct1a.class, Struct2.class,Struct3.class,Struct4.class,Struct5.class
        } ;

        int sizes[] = new int[classes.length] ;
        String names[] = new String[classes.length] ;
        Factory[] factories = new Factory[classes.length] ;
        
        String here = classShortName(Memory.class)+"$" ;
        
        for ( int i = 0 ; i < classes.length ; i++ )
        {
            Class<?> cls = classes[i] ;
            String name = cls.getName() ;
            names[i] = classShortName(cls) ;
            factories[i] = factory(name) ;
            
            int idx = names[i].indexOf(here) ;
            if ( idx == 0 )
                names[i] = names[i].substring(here.length()) ;
        }
        
        boolean missing = false  ;
        for ( int i = 0 ; i < classes.length ; i++ )
        {
            Class<?> cls = classes[i] ;
            String name = cls.getName() ;
//            if ( print )
//                System.out.println(name) ;
            int x = -1 ;
            while( x <= 0 )
                x = space(factories[i]) ;
            if ( print )
                System.out.printf("%-25s => %d\n",names[i], x) ;
            if ( x > 0 && x%4 == 0 )
                sizes[i] = x ; 
//            else
//                if ( sizes[i] == 0 )
//                    missing = true ;
        }
    }


    interface Factory { Object make() ; }

    private static Factory objFactory = new Factory() {
        @Override
        public Object make()
        {
            return new Object() ;
        } } ;
    
     private static Factory mapFactory(final int n)
    {
        return new Factory() {
            @Override
            public Object make()
            {
                return new HashMap<Object, Object>(n) ;
            }
        } ;
    }        
        
    private static Factory arrayFactory(final int n)
    {
        return new Factory() {
            @Override
            public Object make()
            {
                return new Object[n] ;
            } } ;
    }
        
    private static Factory factory(String className)
    {
        try
        {
            final Class<?> cls = Class.forName(className) ;
            return new Factory() {
                @Override
                public Object make()
                {
                    try
                    {
                        return cls.newInstance() ;
                    } catch (InstantiationException ex) { throw new RuntimeException("Factory", ex) ; } 
                    catch (IllegalAccessException ex)   { throw new RuntimeException("Factory", ex) ; }
                } } ;
        } catch (ClassNotFoundException ex)
        {
            ex.printStackTrace();
        } 
        return null ;
    }
    
    static int space(Factory factory)
    {
        Object[] array = new Object[Size] ;
        //System.out.printf("Start...\n") ;
        gcMem() ;
        long bytes1 = usedMemory() ;

        for ( int i = 0 ; i < Size ; i++ )
        {
            array[i] = factory.make() ;
            //array[i].hashCode() ;
            //synchronized (array[i]) { array[i].hashCode() ; }
        }

        gcMem() ;
        long bytes2 = usedMemory() ;
        //System.out.printf("%,d => %,d bytes (%,d => %,d)\n", Size, bytes2-bytes1, bytes1, bytes2) ;

        // Silly thing to stop the compiler guessing we don't use the space again. 
        for ( int i = 0 ; i < Size ; i++ )
            if ( array[i].hashCode() < 0 )
                System.out.println(array[i]) ;

        // Add 100 for some GC rounding
        return (int)(bytes2-bytes1+100)/Size ;

        //System.exit(0) ;
    }
    
    static private void gcMem()
    {
        
        Runtime.getRuntime().gc() ;
    }

    static Runtime runtime = Runtime.getRuntime() ;
    private static long usedMemory ()
    {
        return runtime.totalMemory () - runtime.freeMemory ();
    }

    
    static public String classShortName(Class<?> cls)
    {
        String tmp = cls.getName() ;
        int i = tmp.lastIndexOf('.') ;
        tmp = tmp.substring(i+1) ;
        return tmp ;
    }

}
