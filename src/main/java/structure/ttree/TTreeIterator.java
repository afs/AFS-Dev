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

package structure.ttree;

import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.IteratorArray ;
import org.openjena.atlas.lib.Alg ;

public class TTreeIterator<T extends Comparable<? super T>> implements Iterator<T>
{
    public static <T extends Comparable<? super T>> Iterator<T> iterator(TTree<T> ttree, T min, T max)
    {
        if ( ttree.root == null )
            return Iter.nullIterator() ;
        
        return new TTreeIterator<T>(ttree, min, max) ;
    }

    boolean finished = false ;
    Iterator<T> nodeIter ;
    TTreeNode<T> node ;
    T slot ;
    T max ;

    TTreeIterator(TTree<T> ttree, T min, T max)
    {
        this.max = max ;
        this.finished = false ;
        this.slot = null ;
        
        if ( min != null )
        {
            node = TTree.findBoundingNode(ttree.root, min) ;
            int idx = node.find(min) ;
            // Does not short-cut min being larger than all elements in the tree. 
            if ( idx < 0 )
            {
                idx = Alg.decodeIndex(idx) ;
                // idx may actually be out of the array - that means this node is "finished" with
                if ( idx > node.nodeSize )
                    nodeIter = null ;
                else
                    nodeIter = IteratorArray.create(node.elements, idx, node.nodeSize) ; 
            }
            else
                nodeIter = IteratorArray.create(node.elements, idx, node.nodeSize) ;
        }
        else
        {
            //min == null
            node = ttree.root ;
            while(node.left != null)
                node = node.left ;
            nodeIter = IteratorArray.create(node.elements, 0, node.nodeSize) ;
        }
        
    }

    @Override
    public boolean hasNext()
    {
        if ( finished )
            return false ;
        
        if ( slot != null )
            return true ;
        
        if ( nodeIter != null && nodeIter.hasNext() )
        {
            T item = nodeIter.next() ;
            return testAndSetSlot(item) ;
        }

        // End of current node elements.
        // Slot == null
        // Have yielded the value for this tree node (and hence all relevant left subtree)

        // Move the left-est node of the right subtree.
        // If no right subtree
        //   Go up to parent.
        //   Check we were the left subtree of parent, not right.
        // If no parent (this is the root), and we were left of parent,
        //   go down to min of left.
        
        TTreeNode<T> nextNode = node.right ;
        
        // There is a right
        if ( nextNode != null )
            nextNode = TTreeNode.getLeftDeep(nextNode) ;
        else
            //if ( nextNode == null )
        {
            // No right subtree from here.
            // Walk up tree until we were not the right node of our parent.
            TTreeNode<T> n2 = node ;
            TTreeNode<T> n3 = n2.parent ;
            while( n3 != null )
            {
                if ( n3.right != n2 ) // Same as n3.left == n2
                {
                    n2 = n3 ;
                    break ;
                }
           
                n2 = n3 ;
                n3 = n2.parent ;
            }
            
            if ( n3 == null )
            {
                finished = true ;
                return false ;
            }

            // Now at the first node upwards when we're the left
            // (i.e. less than the value)
            
            nextNode = n2 ;
        }

        // On exit nextNode is the node of interest.
        // Yield it's elements 
        
        node = nextNode ;
        nodeIter = IteratorArray.create(node.elements, 0, node.nodeSize) ;
        // And try again.
        // Rafactor.
        if ( ! nodeIter.hasNext() )
            return false ;
        
        T item = nodeIter.next() ;
        return testAndSetSlot(item) ;
    }

    private boolean testAndSetSlot(T item)
    {
        if ( max != null )
        {
            int x = max.compareTo(item) ;
            if ( x <= 0 )
            {
                // End
                finished = true ;
                slot = null ;
                return false ;
            }
        }
        slot = item ;
        return true ;
    }

    @Override
    public T next()
    {
        if ( ! hasNext())
            throw new NoSuchElementException("TTreeIterator") ;
        T rc = slot ;
        slot = null ;
        return rc ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("TTreeIterator") ; }

//
//
//    // Iterator of zero
//    static <R extends Comparable<? super R>> Iter<R>  nothing()
//    {
//        return Iter.nullIter() ;
//    }
//
//    // Iterator of one
//    static <R extends Comparable<? super R>> Iter<R>  singleton(R singleton)
//    {
//        return Iter.singletonIter(singleton) ;
//    }
//
//    // Iterator of a pair of iterators concatenated
//    static <R extends Comparable<? super R>> Iter<R>  concat(Iter<R> iter1, Iter<R> iter2)
//    {
//        return Iter.concat(iter1, iter2) ;
//    }
//
//    // Calculate the iterator.  For testing. 
//    static <R extends Comparable<? super R>> List<R> calcIter(TTreeNode<R> node, R min, R max)
//    {
//        List<R> x = new ArrayList<R>() ;
//        if ( node == null )
//            return x ;
//        node.records(x) ; // Sorted.
//        if ( min != null )
//            while ( x.size() > 0 && x.get(0).compareTo(min) < 0 )
//                x.remove(0) ;
//        
//        if ( max != null )
//            while ( x.size() > 0 && x.get(x.size()-1).compareTo(max) >= 0 )
//                x.remove(x.size()-1) ; 
//        return x ;
//    }
//
}