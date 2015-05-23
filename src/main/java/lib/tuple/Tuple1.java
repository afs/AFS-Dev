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

public class Tuple1<T> extends ZTuple<T>
{
    final T item ;

    protected Tuple1(T item) 
    {
        this.item = item ; 
    }
    
    @Override
    protected final boolean equalElements(ZTuple<?> x)
    {
        Tuple1<?> z = (Tuple1<?>)x ;
        return Objects.equals(z.item, item) ; 
    }
    
    @Override
    public final T get(int idx)
    {
        if ( idx != 0 )
            throw new TupleException("Out of bounds: "+idx) ;
        return item ;
    }

    @Override
    public final int hashCode()
    {
        return Lib.hashCodeObject(item, ZTuple.NulItemHashCode) ;
    }

    @Override
    public final int size()
    {
        return 1 ;
    }

}
