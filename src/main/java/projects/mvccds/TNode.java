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

package projects.mvccds;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.openjena.atlas.io.IndentedLineBuffer ;
import org.openjena.atlas.io.IndentedWriter ;
import structure.tree.TreeException ;


/** Simple binary tree nodes, including operations that apply to all trees */  

public class TNode<R extends Comparable<R>> //implements Printable
{
    static AtomicInteger counter = new AtomicInteger(0) ; 
    final int generationNumber ;
    final int id = counter.incrementAndGet() ;                  // Debug.
    final TNode<R> parent ;
    TNode<R> left ;
    TNode<R> right ;
    R record ;
    
    static <R extends Comparable<R>> TNode<R> alloc(R record, TNode<R> parent, TNode<R> left, TNode<R> right, int generation)
    {
        return new TNode<>(record, parent, left, right, generation) ; 
    }
    
    static <R extends Comparable<R>> TNode<R> alloc(R record, TNode<R> parent, int generation)
    {
        return new TNode<>(record, parent, generation) ; 
    }
    
    private TNode(R record, TNode<R> parent, int generation)
    {
        this(record, parent, null, null, generation) ;
    }
    
    private TNode(R record, TNode<R> parent, TNode<R> left, TNode<R> right, int generation)
    {
        this.generationNumber = generation ;
        this.parent = parent ;
        this.record = record ;
        this.left = left ;
        this.right = right ;
    }
    
    
    // Naming: rotateRight means move the left child up to the root and the root to the right
    // The left is the pivot 
    // == shift right
    // == clockwise
    // This is the wikipedia naming but that does not extend to the double rotations. 
    // Different books have different namings, based on the location of the pivot (which would be a left rotate)
    // But when we talk about double rotations, the pivotLeft terminolgy works better.
    // pivotLeft (= case left left) , pivotLeftRight, 
    
    
    // Not in this class
    public Iterator<R> records(R min, R max)
    { 
        return null ;
    }
    
    //public Iterator<R> records()
    
    public List<R> records()
    {
        List<R> x = new ArrayList<R>() ;
        records(x) ;
        return x ; // .iterator() ;
    }
    
    public void records(List<R> x)
    {
        if ( left != null )
            left.records(x) ;
        x.add(record) ;
        if ( right != null )
            right.records(x) ;

    }

    @Override
    public String toString()
    { 
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        output(buff) ;
        return buff.toString() ;
    }
    
    final private static void checkNotNull(Object object)
    {
        if ( object == null )
            throw new TreeException("Null") ;
    }

    public void output(IndentedWriter out)
    {
        out.printf("(id=%-2d, G:%02d) [P:%s L:%s R:%s] -- %s", id, generationNumber, 
                   idStr(parent), idStr(left), idStr(right), record ) ;
    }
    
    private static <R extends Comparable<R>> String idStr(TNode<R> node)
    {
        if ( node == null ) return "null" ;
        return String.format("%d", node.id) ;
    }
}
