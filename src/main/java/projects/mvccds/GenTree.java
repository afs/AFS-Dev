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

import org.openjena.atlas.io.IndentedWriter ;

/** Simple binary tree */
public class GenTree<T extends Comparable<T>>
{
    public TNode<T> root = null ; 
    
    static int genCounter = 0 ;
    int generation = genCounter++ ;
    boolean inUpdate = true ;
    
    public GenTree<T> beginUpdate()
    {
        TNode<T> newRoot = root ;
        if ( newRoot != null )
            // Yuk - knows constructor does genCounter++ ; 
            newRoot = TNode.clone(newRoot, null, generation+1) ;
        return new GenTree<>(newRoot) ;
    }

    public void commitUpdate()
    {
        inUpdate = false ;
    }

    public GenTree<T> abortUpdate()
    {
        // Reset allocator return null ;
        inUpdate = false ;
        return new GenTree<>(root) ;
    }
    
    
    GenTree() { this.root = null ; }
    private GenTree(TNode<T> root) { this.root = root ; }

    public void add(T record)
    {
        if ( root == null )
        {
            root = TNode.alloc(record, null, generation) ;
            return ;
        }
        insert(root, record, false, generation) ;
    }
    
//    public T insert(TNode<T> node, T newRecord, int generation)
//    { return insert(node, newRecord, true, gener) ; }
    
    // Unbalanced insert - return the record if the record already present, else null if new.
    private T insert(TNode<T> node, T newRecord, boolean duplicates, int generation)
    {
        System.out.println("insert: "+node) ;
        int x = node.record.compareTo(newRecord) ;
        
        if ( x > 0 )
        {
            if ( node.left == null )
            {
                TNode<T> n = ripple(node, generation) ;
                n.left = TNode.alloc(newRecord, node, generation) ;
                updateParent(n) ;
                return null ;
            }
            return insert(node.left, newRecord, duplicates, generation) ;
        }
        
        if ( x == 0 && ! duplicates )
            return node.record ;

        // x > 0 
        if ( node.right == null )
        {
            TNode<T> n = ripple(node, generation) ;
            n.right = TNode.alloc(newRecord, node, generation) ;
            updateParent(n) ;
            return null ;
        }
        
        return insert(node.right, newRecord, duplicates, generation) ;
    }

    private void updateParent(TNode<T> node)
    {
        if ( node == null || node.parent == null ) 
            return ;
        TNode<T> p = node.parent ;
        if ( p.left == node ) p.left = node ;
        if ( p.right == node ) p.right = node ;
    }
    
    private TNode<T> ripple(TNode<T> node, int generation)
    {
        if ( node.generationNumber == generation )
            // Already current generation
            return node ;

        // Do parent first.
        TNode<T> p = node.parent ;
        if ( p != null )
            p = ripple(p, generation) ;
        
        TNode<T> n = TNode.clone(node, p, generation) ;
        updateParent(n) ;
        if ( n != node )
            updateParent(n) ;
        
        // Ripple up the tree.
        // A simple "return node" is mutable tree (change in place)
        // Add args for new left, new right, etc than this is the call to alloc -> make slots final.
        return n ;
    }

    public void dump()
    {
        if ( root == null )
        {
            System.out.println("<empty>") ;
            return ;
        }
        output(IndentedWriter.stdout, root) ;
        IndentedWriter.stdout.println() ;
        IndentedWriter.stdout.flush() ;
    }
    
    public void dumpFull()
    {
        if ( root == null )
        {
            System.out.println("<empty>") ;
            return ;
        }
        outputNested(IndentedWriter.stdout, root) ;
        IndentedWriter.stdout.flush() ;
    }
    
    public void outputNested(IndentedWriter out, TNode<T> node)
    {
        
        if ( node.left == null && node.right == null )
        {
            node.output(out) ;
            return ;
        }
        
        // At least one of left and right.
        
        //out.print('(') ;
        node.output(out) ;
        out.incIndent() ;
        out.println() ;
        if ( node.left != null )
            outputNested(out, node.left) ;
        else
            out.print("undef") ;
        out.println();

        if ( node.right != null )
            outputNested(out, node.right) ;
        else
            out.print("undef") ;
        //out.print(')') ;
        out.println();
        out.decIndent() ;
    }
    
    public void output(IndentedWriter out, TNode<T> node)
    {
        if ( node.left == null && node.right == null )
        {
            out.print(node.record) ;
            return ;
        }
        
        out.print('(') ;
        out.print(node.record) ;
        if ( node.left != null )
        {
            out.print(' ') ;
            output(out, node.left) ;
        }
        if ( node.right != null )
        {
            out.print(' ') ;
            output(out, node.right) ;
        }
        out.print(')') ;
    }


}

