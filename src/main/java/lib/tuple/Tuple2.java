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

import org.openjena.atlas.lib.Lib;

public class Tuple2<T> extends ZTuple<T>
{
    final T item1 ;
    final T item2 ;

    protected Tuple2(T item1, T item2) 
    {
        this.item1 = item1 ; 
        this.item2 = item2 ; 
    }
    
    @Override
    protected final boolean equalElements(ZTuple<?> x)
    {
        Tuple2<?> x1 = (Tuple2<?>)x ;
        return Lib.equal(x1.item1, item1) && Lib.equal(x1.item2, item2); 
    }
    
    @Override
    public final T get(int idx)
    {
        switch (idx)
        {
            case 0: return item1 ;
            case 1: return item2 ;
            default: throw new TupleException("Out of bounds: "+idx) ;
        }
    }

    @Override
    public final int hashCode()
    {
        return
            Lib.hashCodeObject(item1, ZTuple.NulItemHashCode)<<1
            ^ Lib.hashCodeObject(item2, ZTuple.NulItemHashCode) ; 
    }

    @Override
    public final int size()
    {
        return 2 ;
    }

}
