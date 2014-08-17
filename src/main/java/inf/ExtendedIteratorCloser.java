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

package inf;

import java.util.Iterator ;
import java.util.NoSuchElementException ;

import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

/** Provided an ExtendedIterator over a plain iterator and also close a (related) ExtendedIterator */ 
public class ExtendedIteratorCloser<T> extends WrappedIterator<T> {
    private ExtendedIterator<T> underlying ;

    public ExtendedIteratorCloser(Iterator<T> iter, ExtendedIterator<T> underlying) {
        super(iter, true) ;
        this.underlying = underlying ;
    }

    @Override
    public void close() {
        underlying.close() ;
    }
    
    @Override
    public boolean hasNext() {
        boolean b = super.hasNext() ;
        if ( !b )
            underlying.close();
        return b ;
    }

    @Override
    public T next() {
        try { return super.next() ; }
        catch (NoSuchElementException ex) { underlying.close() ; throw ex ; }
    }
}
