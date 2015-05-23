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

import java.util.Objects ;

import org.apache.jena.atlas.lib.Lib ;

public class Tuple4<T> extends ZTuple<T>
{
    final T item1 ;
    final T item2 ;
    final T item3 ;
    final T item4 ;

    protected Tuple4(T item1, T item2, T item3, T item4) 
    {
        this.item1 = item1 ; 
        this.item2 = item2 ; 
        this.item3 = item3 ;
        this.item4 = item4 ;
    }
    
    @Override
    protected final boolean equalElements(ZTuple<?> x)
    {
        Tuple4<?> x1 = (Tuple4<?>)x ;
        return  Objects.equals(x1.item1, item1) && 
                Objects.equals(x1.item2, item2) &&
                Objects.equals(x1.item3, item3) &&
                Objects.equals(x1.item4, item4) ;
    }
    
    @Override
    public final T get(int idx)
    {
        switch (idx)
        {
            case 0: return item1 ;
            case 1: return item2 ;
            case 2: return item3 ;
            case 3: return item4 ;
            default: throw new TupleException("Out of bounds: "+idx) ;
        }
    }

    @Override
    public final int hashCode()
    {
        int x = Lib.hashCodeObject(item1, ZTuple.NulItemHashCode) ; 
        x = x<<1 ^ Lib.hashCodeObject(item2, ZTuple.NulItemHashCode) ; 
        x = x<<1 ^ Lib.hashCodeObject(item3, ZTuple.NulItemHashCode) ; 
        x = x<<1 ^ Lib.hashCodeObject(item4, ZTuple.NulItemHashCode) ; 
        return x ;
    }

    @Override
    public final int size()
    {
        return 4 ;
    }

}
