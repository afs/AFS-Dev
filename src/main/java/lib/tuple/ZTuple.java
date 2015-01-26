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

package lib.tuple;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.lib.ColumnMap;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.NotImplemented;

/** Tuples - use less space by specific implementations for 
 *  various sizes.  Saves the array overhead of 3 slots.  
 *  And 3 slots for two items might amount to a lot of space.
 */
public abstract class ZTuple<T>
{
    static final int NulItemHashCode = 3 ;

    public abstract T get(int idx) ;
    // Immutable
    // public abstract T set(int idx, T item) ;

    public List<T> asList()
    {
        return Arrays.asList(tuple()) ;
    }
    
    public T[] tuple() 
    {
        int N = size() ;
        @SuppressWarnings("unchecked")
        T[] array = (T[])new Object[N] ;
        for (int i = 0; i < N; i++)
            array[i] = get(i) ;
        return array ;
    }

    /** Return a tuple with the column mapping applied */
    public ZTuple<T> map(ColumnMap colMap)
    {
        //return colMap.map(this) ;
        throw new NotImplemented("Tuple.map") ; 
    }
    
    /** Return a tuple with the column mapping reversed */
    public ZTuple<T> unmap(ColumnMap colMap)
    {
        //return colMap.unmap(this) ;
        throw new NotImplemented("Tuple.unmap") ; 
    }
    
    public abstract int size() ;
    
    // Cache?
    @Override
    public int hashCode()
    { 
        int x = 99 ;
        for ( int i = 0; i < size() ; i++)
            x ^= get(i).hashCode() ;
        return x ;  
    }
    
    @Override
    public boolean equals(Object other) 
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof ZTuple<?> ) )
            return false ;
        ZTuple<?> x = (ZTuple<?>)other ;
        if ( x.size() != this.size() )
            return false ;
        return equalElements(x) ;
    }
        
    protected boolean equalElements(ZTuple<?> tuple)
    {
        for ( int i = 0 ; i < tuple.size() ; i++ )
        {
            Object obj1 = get(i) ;
            Object obj2 = tuple.get(i) ;
            if ( ! Lib.equal(obj1, obj2) )
                return false ;
        }
        return true ; 
    }
    
}
