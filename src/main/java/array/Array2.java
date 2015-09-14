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

package array;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

/** A dope-vector, multilevel array.
 *  useful because it does not do internal copies as it grows
 *  (at the cost of two level of access) 
 *  
 *  This implementation does not support remove() or removeAll()
 */
public class Array2<T> implements /*List<T>,*/ Iterable<T> {
    // If List, then int index.
    
//    private static int N1 = 1024 ;
//    private static int N2 = 1024 ;
    
    private static int N1 = 4 ;
    private static int N2 = 4 ;

    private int level1(long x) {
        return (int)(x % N1) ; // x >> 10
    }

    private int level2(long x) {
        return (int)(x & (N1-1)) ;
    }
    
    private long size = 0 ;
    // Overflow to the next in the chain.
    private Array2<T> next = null ;
    
    private long startIdx ;
    private long finishIdx ;
    
    // Better : each sublevel has a length so remove is fastish.
    //Object[][] level1 = new Object[N1][] ;
    @SuppressWarnings("unchecked")
    List<Object>[] level1a = new List[N1] ;
    

    public int size() {
        return 0 ;
    }

    public boolean isEmpty() {
        return false ;
    }

    public boolean contains(T o) {
        return false ;
    }

    @Override
    public Iterator<T> iterator() {
        return null ;
    }

    public boolean add(T e) {
        int x1 = level1(size) ;
        int x2 = level2(size) ;
//        Object[] v = level1[x1] ;
//        if ( v == null ) {
//            v = new Object[N2] ;
//            level1[x1] = v ;
//        }
        List<Object> v = level1a[x1] ;
        if ( v == null ) {
            v = new ArrayList<Object>(N2) ;
            level1a[x1] = v ;
        }
        v.add(e) ;
        size++ ;
        return true ;
    }

    public T get(long index) {
        // Does not cope with removes.?
        rangeCheck(index) ;
        int x1 = level1(index) ;
        int x2 = level2(index) ;
//        Object[] v = level1[x1] ;
//        if ( v == null ) {
//            v = new Object[N2] ;
//            level1[x1] = v ;
//        }
        List<Object> v = level1a[x1] ;
        if ( v == null )
            return null ;
        return access(v, x2) ;
    }
        
    @SuppressWarnings("unchecked")
    private static <T> T access(List<Object> v, int idx) { return (T)v.get(idx) ; }
    
    /**
     * Checks if the given index is in range.  If not, throws an appropriate
     * runtime exception.
     */
    private void rangeCheck(long index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    
    private String outOfBoundsMsg(long index) {
        return "Index: "+index+", Size: "+size;
    }

    public T set(int index, T element) {
        return null ;
    }

    public void clear() {}

    public int indexOf(T o) {
        return 0 ;
    }

    public int lastIndexOf(T o) {
        return 0 ;
    }

    public Array2<T> subList(int fromIndex, int toIndex) {
        return null ;
    }

    public boolean addAll(Collection<? extends T> c) {
        for( T elt : c ) {
            add(elt) ;
        }
        return true ;
    }

    public boolean addAll(int index, Collection<? extends T> c) { throw new UnsupportedOperationException("Array2.addAll(index)") ; }

    public boolean remove(T o) { throw new UnsupportedOperationException("Array2.remove(element)") ; }

    public T remove(int index) { throw new UnsupportedOperationException("Array2.remove(index)") ; }

    public boolean removeAll(Collection<T> c)  { throw new UnsupportedOperationException("Array2.removeAll") ; }

    public boolean retainAll(Collection<T> c) { throw new UnsupportedOperationException("Array2.retainAll") ; }

    public void add(int index, T element) { throw new UnsupportedOperationException("Array2.add(index)") ; }
}
