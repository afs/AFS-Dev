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

package projects.mvccds;

import java.util.ArrayList ;
import java.util.List ;

import storage.StorageException ;

public class TNodeAllocator <R extends Comparable<R>>
{
    List<TNode<R>> nodes = new ArrayList<TNode<R>>(1000) ;
    int nextIdx = 0 ;
    
    private TNode<R> blank()
    { 
        TNode<R> x = new TNode<>(nodes.size()) ; 
        nodes.add(x) ;
        
        TNode<R> tn = nodes.get(nextIdx) ;
        nextIdx ++ ;
        tn.left = -1 ;
        tn.right = -1 ;
        tn.generation = -1 ;
        tn.record = null ;
        return tn ;  
    }
    
    TNode<R> fetch(long id)
    {
        if ( id < 0 )
            throw new StorageException("id = "+id) ;
        
        return nodes.get((int)id) ;
    }
    
    TNode<R> alloc(R record, long left, long right, int generation)
    {
        //return new TNode<>(record, left, right, generation) ;
        TNode<R> tn = blank() ;
        tn.left = left ;
        tn.right = right ;
        tn.generation = generation ;
        tn.record = record ;
        return tn ;
    }
    
    TNode<R> alloc(R record, int generation)
    {
        return alloc(record, -1, -1, generation) ; 
    }
    
    TNode<R> cloneRoot(TNode<R> node, int generation)
    {
        return alloc(node.record, node.left, node.right, generation) ;
    }
    
    TNode<R> clone(TNode<R> node, int generation)
    {
        return alloc(node.record, node.left, node.right, generation) ;
    }

}

