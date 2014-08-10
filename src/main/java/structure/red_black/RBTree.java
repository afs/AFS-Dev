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

package structure.red_black;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;
import structure.OrderedSet ;

public class RBTree<T extends Comparable<T>> implements OrderedSet<T> {
    RBTreeNode<T> root ;
    
    public RBTree() { root = null ; }
    
    @Override
    public boolean isEmpty()
    { 
        if ( root == null ) return true ;
        return false ; /*root.isEmpty() ;*/
    }

    @Override
    public boolean contains(T item)
    { return search(item) != null ; } 


    @Override
    public T search(T item) { return null ; }
    
    public T insert(T newRecord) { return null ; }
    
    public T delete(T newRecord) { return null ; }

    @Override
    public void output(IndentedWriter out) {}

    @Override
    public void clear() {}

    @Override
    public boolean add(T item) {
        return false ;
    }

    @Override
    public boolean remove(T item) {
        return false ;
    }

    @Override
    public T max() {
        return null ;
    }

    @Override
    public T min() {
        return null ;
    }

    @Override
    public long size() {
        return 0 ;
    }

    @Override
    public long count() {
        return 0 ;
    }

    @Override
    public void checkTree() {}

    @Override
    public List<T> elements() {
        return null ;
    }

    @Override
    public Iterator<T> iterator() {
        return null ;
    }

    @Override
    public Iterator<T> iterator(T startInc, T endExc) {
        return null ;
    }
}

