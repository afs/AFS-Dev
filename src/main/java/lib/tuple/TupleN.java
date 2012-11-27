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

package lib.tuple;

import java.util.Arrays ;

import org.apache.jena.atlas.lib.Lib ;

public final class TupleN<T> extends ZTuple<T>
{
    T[] tuple ;

    protected TupleN(@SuppressWarnings("unchecked") T...tuple) 
    {
        this.tuple = Arrays.copyOfRange(tuple, 0, tuple.length) ;
    }
    
    @Override
    public T[] tuple() { return tuple ; }
    
    @Override
    protected boolean equalElements(ZTuple<?> x)
    {
        TupleN<?> x1 = (TupleN<?>)x ;
        for ( int i = 0 ; i < tuple.length ; i++ )
        {
            Object obj1 = x1.tuple[i] ;
            Object obj2 = tuple[i] ;
            if ( ! Lib.equal(obj1, obj2) )
                return false ;
        }
        return true ;
    }
    
    @Override
    public T get(int idx)
    {
        if ( idx < 0 || idx >= tuple.length )
            throw new TupleException("Out of bounds: "+idx) ;
        return tuple[idx] ;
    }

    @Override
    public int hashCode()
    {
        int x = 0 ;
        for ( T t: tuple)
            x = x<<1 ^ Lib.hashCodeObject(t, ZTuple.NulItemHashCode) ; 
        return x ;
    }

    @Override
    public int size()
    {
        return tuple.length ;
    }

}
