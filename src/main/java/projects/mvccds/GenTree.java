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

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.iterator.Iter ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Simple binary tree */
public class GenTree<T extends Comparable<T>>
{
    // Mutable
    // checks : root generation = tree generation.
    
    private final TNodeAllocator<T> allocator ;
    
    public  static boolean DEBUG = false ;
    private static Logger log = LoggerFactory.getLogger(GenTree.class) ;

    private TNode<T> root = null ; 
    private boolean readOnly = false ;
    private int generation ;
    private boolean inUpdate = false ;
    private TNode<T> oldRoot ;
    
    public static <T extends Comparable<T>> GenTree<T> create(TNodeAllocator<T> allocator)
    { return new GenTree<T>(null, 0, allocator) ; }
    
    // And mark read only?
    public static <T extends Comparable<T>> GenTree<T> duplicate(GenTree<T> tree)
    { 
        GenTree<T> tree2 = new GenTree<>(tree.root, tree.generation, tree.allocator) ;
        tree2.readOnly = true ;
        return tree2 ;
    }
    
    public GenTree<T> beginUpdate()
    {
        if ( readOnly )
            throw new RuntimeException("Readonly") ;

        oldRoot = root ;
        generation += 1 ;
        inUpdate = true ;
        TNode<T> newRoot = root ;
        if ( newRoot != null )
            root = allocator.cloneRoot(root, generation) ;
        return this ;
    }

    public GenTree<T> commitUpdate()
    {
        inUpdate = false ;
        oldRoot = null ;
        return this ;
    }

    public GenTree<T> abortUpdate()
    {
        // Reset allocator return null ;
        inUpdate = false ;
        root = oldRoot ;
        oldRoot = null ;
        return this ;
    }
    
    private GenTree(TNode<T> root, int generation, TNodeAllocator<T> allocator)
    {
        this.allocator = allocator ;
        this.generation = generation ;
        this.root = root ;
        this.oldRoot = null ;
    }

    public void add(T record)
    {
        
        if ( ! inUpdate )
            throw new RuntimeException("Attempt to add outside of update boundaries") ;
        
        if ( root == null )
        {
            root = allocator.alloc(record, generation) ;
            return ;
        }
        
        insert(root, null, record, false, generation) ;
    }
    
    private T insert(TNode<T> node, TNode<T> parent, T newRecord, boolean duplicates, int generation)
    {
        for(;;)
        {
            log("insert: %s", node) ;
            // Assumes an update will happen.
            TNode<T> n = fixup(node, parent, generation) ;

            int x = n.record.compareTo(newRecord) ;

            if ( x > 0 )
            {
                if ( n.isNullLeft() )
                {
                    n.left = allocator.alloc(newRecord, generation).id ;
                    return null ;
                }
                parent = n ;
                node = fetch(n.left) ;
                continue ;
                // Tail recursion,
                // return insert(n.left, n, newRecord, duplicates, generation) ;
            }

            if ( x == 0 && ! duplicates )
                return n.record ;

            // x > 0 
            if ( n.isNullRight() )
            {
                n.right = allocator.alloc(newRecord, generation).id ;
                return null ;
            }
            parent = n ;
            node = fetch(n.right) ;
            continue ;
            // return insert(n.right, n, newRecord, duplicates, generation) ;
        }

    }
    
    private void updateParent(TNode<T> oldNode, TNode<T> p, TNode<T> newNode)
    {
        if ( oldNode == null || p == null ) 
            return ;
        if ( p.left == oldNode.id )
            p.left = newNode.id ;
        else if ( p.right == oldNode.id ) 
            p.right = newNode.id ;
    }
    
    private TNode<T> fixup(TNode<T> node, TNode<T> parent, int generation)
    {
        if ( node.generation == generation )
            // Already current generation
            return node ;
        TNode<T> n = allocator.clone(node, generation) ;
        updateParent(node, parent, n) ;
        return n ;
    }

    public void records()
    {
        if ( root == null )
        {
            System.out.println("<empty>") ;
            return ;
        }
        List<T> list = new ArrayList<>() ;
        records(list, root) ;
        String x = Iter.asString(list.iterator()) ;
        System.out.println(x) ;
    }
    
    private void records(List<T> x, TNode<T> node)
    {
        if ( ! node.isNullLeft() )
            records(x, fetch(node.left)) ;
        x.add(node.record) ;
        if ( ! node.isNullRight() )
            records(x, fetch(node.right)) ;
    }

    public void dumpFlat()
    {
        output(IndentedWriter.stdout, root) ;
        IndentedWriter.stdout.println() ;
        IndentedWriter.stdout.flush() ;
    }
    
    public void dump()
    {
        if ( root == null )
        {
            System.out.println("<empty>") ;
            return ;
        }
        outputNested(IndentedWriter.stdout, root) ;
        IndentedWriter.stdout.flush() ;
    }
    
    public void log(String fmt, Object ... args)
    {
        if ( DEBUG && log.isDebugEnabled() )
            log.debug(String.format(fmt, args)) ;
    }
    
    private TNode<T> fetch(long id) { return allocator.fetch(id) ; }
    
    public void outputNested(IndentedWriter out, TNode<T> node)
    {
        if ( node.isNullLeft() && node.isNullRight() )
        {
            node.output(out) ;
            out.println() ;
            return ;
        }
        
        // At least one of left and right.
        
        //out.print('(') ;
        node.output(out) ;
        out.incIndent(4) ;
        out.println() ;
        if ( ! node.isNullLeft() )
            outputNested(out, fetch(node.left)) ;
        else
            out.println("undef") ;

        if ( ! node.isNullRight() )
            outputNested(out, fetch(node.right)) ;
        else
            out.println("undef") ;
        //out.print(')') ;
        out.decIndent(4) ;
    }
    
    public void output(IndentedWriter out, TNode<T> node)
    {
        if ( node.isNullLeft() && node.isNullRight() )
        {
            out.print(node.record) ;
            return ;
        }
        
        out.print('(') ;
        out.print(node.record) ;
        if ( ! node.isNullLeft() )
        {
            out.print(' ') ;
            output(out, fetch(node.left)) ;
        }
        if ( ! node.isNullRight() )
        {
            out.print(' ') ;
            output(out, fetch(node.right)) ;
        }
        out.print(')') ;
    }


}

