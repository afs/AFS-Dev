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

package structure.radix;

import java.nio.ByteBuffer ;
import java.util.ArrayDeque ;
import java.util.ArrayList ;
import java.util.Deque ;
import java.util.Iterator ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Chars ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/* http://en.wikipedia.org/wiki/Radix_tree */
public final class RadixTree
{
    // TODO
    // More checking, all if'ed out
    // Iteration
    // Value?
    // Architecture : action visitor pattern - find&do (overhead?)
    
    static public boolean logging = true ;
    static public /*final*/ boolean checking = true ;
    static final byte[] bytes0 = new byte[]{} ;
    
    static Logger log = LoggerFactory.getLogger(RadixTree.class) ;
    private RadixNode root = null ;
    
    public RadixNode getRoot() { return root ; }
    
    private interface Action { RadixNode exec(byte[] key, RadixNode node, int N, RadixNode parentNode) ; }

    /** Find the starting point - call the action */
    private static RadixNode applicator(RadixNode root, byte[] key, Action action)
    {
        
        
        // A convoluted way to "return" with three results by calling on to a handler.
        // Assumes root is not null.
        RadixNode nodePrev = null ; // Last node.
        RadixNode node = root ;     // The current node.
        
        for(;;)
        {
            // Does the prefix (partially) match?
            int N = node.countMatchPrefix(key) ;
            if ( N < node.prefix.length )   // Includes negative N
                return action.exec(key, node, N, nodePrev) ;
            // else matched up to end of prefix. 
            // Longer or same length key.
            int j = node.locate(key, node.lenFinish) ;
            if ( j < 0 || j == node.nodes.size() )
                // No match across subnodes - this node is the point of longest match.
                return action.exec(key, node, node.prefix.length, nodePrev) ;
            // There is a next node down to try.
            nodePrev = node ;
            node = node.nodes.get(j) ;
        }
        // Does not happen
    }

    private RadixNode _find(byte[] key)
    {
        RadixNode node = search(key) ;
        int N = node.countMatchPrefix(key) ;

        if ( N == node.prefix.length && node.lenFinish == key.length && node.isLeaf() )
            // Exact match
            return node ;
        return null ;
    }        
        
    public boolean contains(byte[] key)
    {
        RadixNode node = search(key) ;
        if ( node == null )
            return false ;
        if ( node.lenFinish == key.length && node.isLeaf() )
            return true ;
        return false ;
    }
    
    public RadixNode find(byte[] key)
    {
        if ( root == null )
            return null ;
        return applicator(root, key, findAction) ;
    }        

    
    private static Action findAction = new Action() {

        @Override
        public RadixNode exec(byte[] key, RadixNode node, int N, RadixNode nodePrev)
        {
            if ( N == node.prefix.length && node.lenFinish == key.length && node.isLeaf() )
                // Exact match
                return node ;
            return null ;
        }
    } ;
    
    private static Action identityAction = new Action() {

        @Override
        public RadixNode exec(byte[] key, RadixNode node, int N, RadixNode nodePrev)
        {
            return node ;
        }
    } ;
     
    private RadixNode search(byte[] key)
    {
        if ( root == null )
            return null ;
        return applicator(root, key, identityAction) ;
    }

    // Return top changed node.
    private static Action insertAction = new Action()
    {
        @Override
        public RadixNode exec(byte[] key, RadixNode node, int N, RadixNode nodePrev)
        {
            if (logging && log.isDebugEnabled() )
            {
                log.debug("insert: "+RLib.str(key)) ;
                log.debug("insert: search => "+node) ;
                log.debug("insert N = "+N) ;
            }
            /* Cases:
             * Not a leaf node (tested above):
             *   0/ key same as prefix and it's a leaf => already there
             *   1/ key longer than prefix : N == node.prefix.length
             *   2/ key same as prefix : N == node.prefix.length but not already in tree.
             *   3/ key shorter than prefix :  0 <= N < node.prefix.length
             *   4/ key diverges from prefix :  N < 0
             *      
             */

            // Key already present - we ended at a leaf.
            if ( N == node.prefix.length && node.lenFinish == key.length && node.isLeaf() )
            {
                // Or has a leaf of ""?
                if (logging && log.isDebugEnabled() )
                    log.debug("insert: Already present") ;
                return null ;
            }

            /* Actions
             * 1 => split, then as 3 ?
             * 2 => split, then as 3 ?
             * 3 => new subnode.
             * 4 => new subnode ""
             */

            if ( N == node.prefix.length )
            {
                // We will modify the node array
                if ( node.isLeaf() )
                {
                    node.nodes = new ArrayList<RadixNode>() ;
                    RadixNode n = RadixNode.alloc(node) ;
                    n.prefix = bytes0 ;
                    n.lenStart = node.lenFinish ;
                    n.lenFinish = node.lenFinish ;
                    n.nodes = null ;
                    node.nodes.add(0, n) ;
                }

                // Key ends here but it's not a leaf already
                byte[] prefixNew = Bytes.copyOf(key, node.lenFinish, key.length - node.lenFinish) ;

                if (logging && log.isDebugEnabled() )
                    log.debug("Prefix new : "+Bytes.asHex(prefixNew)) ;

                RadixNode node1 = RadixNode.alloc(node) ;
                node1.prefix = prefixNew ;
                node1.lenStart = node.lenFinish ;
                node1.lenFinish = key.length ;
                node1.nodes = null ; // It's a leaf.

                int i = node.locate(prefixNew, 0) ;

                if ( i > 0 )
                    error("Duplicate start byte") ;
                i = -(i+1) ;
                node.nodes.add(i, node1) ;
                return node ;
            }

            // Key diverges or is shorter than prefix.
            byte[] prefix1 ;
            if ( N < 0 )
            {
                // Short key.
                prefix1 = bytes0 ;
                N = -(N+1) ;
            }
            else
                prefix1 = Bytes.copyOf(key, N+node.lenStart) ;

            // The two parts of the prefix.
            byte[] prefix0 = Bytes.copyOf(node.prefix, 0, N) ;
            byte[] prefix2 = Bytes.copyOf(node.prefix, N) ; 

            if (logging && log.isDebugEnabled() )
            {
                log.debug("Prefix0 : "+Bytes.asHex(prefix0)) ;
                log.debug("Prefix1 : "+Bytes.asHex(prefix1)) ;
                log.debug("Prefix2 : "+Bytes.asHex(prefix2)) ;
            }
            // This is the new leaf due to key added.
            RadixNode node1 = RadixNode.alloc(node) ;
            node1.prefix = prefix1 ;
            node1.lenStart = node.lenStart+N ;
            node1.lenFinish = key.length ;
            node1.nodes = null ;

            // This the tail of the original data
            RadixNode node2 = RadixNode.alloc(node) ;
            node2.prefix = prefix2 ;
            node2.lenStart = node.lenStart+N ;
            node2.lenFinish = node.lenFinish ;
            node2.nodes = null ;

            // Alter the node in-place, rather than create a new node and insert node1, node2.
            // This keeps the root in-place.
            if ( node.nodes != null )
            {
                node2.nodes = new ArrayList<RadixNode>(node.nodes) ;
                for ( RadixNode n : node.nodes )
                {
                    n.parent = node2 ;
                    n.parentId = node2.id ;
                }
            }
            
            // Swap if necessary
            if ( Bytes.compare(prefix1, prefix2) > 0 )
            { RadixNode t = node2 ; node2 = node1 ; node1 = t ; } 

            node.prefix = prefix0 ;
            node.lenFinish = prefix0.length+node.lenStart ;
            if ( node.nodes != null )
                node.nodes.clear() ;
            else
                node.nodes = new ArrayList<RadixNode>() ;
            node.nodes.add(node1) ;
            node.nodes.add(node2) ;
            return node ;
        }
    } ;
    
    public boolean insert(byte[] key)
    {
        if (logging && log.isDebugEnabled() )
            log.debug("** Insert : "+Bytes.asHex(key)) ;
        
        if ( root == null )
        {
            root = RadixNode.alloc(null) ;
            root.prefix = key ;
            root.lenStart = 0 ;
            root.lenFinish = key.length ;
            root.nodes = null ;
            return true ;
        }
        
        return applicator(root, key, insertAction) != null ;
    }
    
    private static Action deleteAction = new Action()
    {
        @Override
        public RadixNode exec(byte[] key, RadixNode leaf, int N, RadixNode node)
        {
            // Key not already present - not a full match (short, diverges) or not-a-leaf. 
            if ( N != leaf.prefix.length || leaf.lenFinish != key.length || ! leaf.isLeaf() )
            {
                if (logging && log.isDebugEnabled() )
                    log.debug("delete: Not present") ;
                return null ;
            }
         
            if (logging && log.isDebugEnabled() )
            {
                log.debug("delete: "+Bytes.asHex(key)) ;
                log.debug("  "+leaf) ;
                log.debug("  "+node) ;
            }
            
            if ( node == null )
            {
                // Root - deleting the last key.
                return leaf ;
            }
            
            // Then work on the parent.
            int i = node.locate(key, leaf.lenStart) ;
            if ( i < 0 )
                error("Can't find child in parent") ;

            {
                RadixNode x = node.nodes.remove(i) ;
                if ( checking && x != leaf )
                    error("Removing the wrong node") ;
            }

            if ( node.nodes.size() == 1 )
            {
                // This other node subnode.
                // Collapse n into node - that is, pull up all n into node. 
                
                RadixNode n = node.nodes.remove(0) ;
                if ( logging && log.isDebugEnabled() )
                {
                    log.debug("Collapse node") ;
                    log.debug("  node: "+node) ;
                    log.debug("  n   : "+n) ;
                }   

                int len = node.prefix.length + n.prefix.length ;
                node.nodes = n.nodes ;
                // fix up children of n
                if ( node.nodes != null )
                {
                    for ( RadixNode x : node.nodes )
                    {
                        x.parent = node ;
                        x.parentId = node.id ;
                    }
                }
                node.lenFinish = n.lenFinish ;
                
                byte [] a = new byte[len] ;
                System.arraycopy(node.prefix, 0, a, 0, node.prefix.length) ;
                System.arraycopy(n.prefix,    0, a, node.prefix.length, n.prefix.length) ;
                if ( logging && log.isDebugEnabled() )
                    log.debug("New prefix: "+Bytes.asHex(a)) ;
                
                node.prefix = a ;
                if ( logging && log.isDebugEnabled() )
                    log.debug("  --> : "+node) ;
            }
            return node ; 
        }
    } ;
        /** Delete - return true if the tree changed (i.e the key was present and so was removed) */
    public boolean delete(byte[] key)
    {
        if ( root == null )
            return false ;
        boolean b = root.isLeaf() ;
        RadixNode n = applicator(root, key, deleteAction) ;
        if ( b && n != null )
            root = null ;
        return n != null ;
    }
    
    public void print()
    {
        if ( root == null )
        {
            System.out.println("<empty>") ;
            return ;
        }
        root.output(IndentedWriter.stdout) ;
        IndentedWriter.stdout.flush();
    }

    public long size()
    {
        if ( root == null )
            return 0 ;
        
        RadixNodeVisitor<Object> v = new RadixNodeVisitorBase()
        {
            int count = 0 ;
            @Override
            public void before(RadixNode node)
            {
                if (node.isLeaf()) 
                    count++ ;
            }
            
            @Override
            public Object result()
            { return count ; }
        } ;
        root.visit(v) ;
        return (Integer)v.result() ;
    }
    
    public boolean isEmpty()
    {
        if ( root == null )
            return true ;
        if ( root.isLeaf() )
            return false ;
        // Denormalized tree
        return root.nodes.size() == 0 ;
    }
    
    public Iterator<ByteBuffer> iterator() { return iterator(null, null) ; }
    
    public Iterator<ByteBuffer> iterator(byte[] start, byte[] finish)
    { 
        if ( logging && log.isDebugEnabled() )
        {
            log.debug("iterator: start:  "+((start==null)?"null":Bytes.asHex(start))) ;
            log.debug("iterator: finish: "+((finish==null)?"null":Bytes.asHex(finish))) ;
        }
        
        if ( root == null )
        {
            if ( logging && log.isDebugEnabled() )
                log.debug("iterator: empty tree") ;
            return Iter.nullIterator() ;
        }
        return new RadixIterator(root, start, finish) ;
    }
    
    static Transform<Byte, String> hex = new Transform<Byte, String>(){

        @Override
        public String convert(Byte item)
        {
            int hi = (item.byteValue() >> 4) & 0xF ;
            int lo = item.byteValue() & 0xF ;
            
            return "0x"+Chars.hexDigitsUC[hi]+Chars.hexDigitsUC[lo] ;
        }} ;
    
    public void printLeaves()
    {
        if ( root == null )
        {
            System.out.println("Tree: empty") ;
            return ;
        }
        
        final Deque<Byte> x = new ArrayDeque<Byte>() ;
        RadixNodeVisitor<Object> v = new RadixNodeVisitorBase()
        {
            @Override
            public void before(RadixNode node)
            {
                for ( byte b : node.prefix )
                    x.addLast(b) ;
                if ( node.isLeaf() )
                {
                    System.out.print("[") ;
                    System.out.print(Iter.iter(x.iterator()).map(hex).asString(", ")) ;
                    System.out.print("] ") ;
                }
            }

            @Override
            public void after(RadixNode node)
            {
                for ( int i = node.lenStart ; i < node.lenFinish ; i++ )
                    x.removeLast() ;
            }

        } ;
        
        
        root.visit(v) ;
        System.out.println() ;
        System.out.flush() ;
    }
    
    static void error(String string)
    {
        throw new AtlasException(string) ;
    }

    public void check()
    { 
        if ( root != null )
            root.check() ; 
    }
}
