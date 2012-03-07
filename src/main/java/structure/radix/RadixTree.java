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
    
    /** Called with the values:
     * <ul>
     * <li>key (full length)</li>
     * <li>node with the longest prefix of key. 
     * <li>N: length in prefix of the key match. negative for diverging key</li>
     * <li>parent of the node </li>
     * </ul> 
     */
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
            if ( N < node.prefix.length )
                // Includes negative N
                // Key ran out.
                return action.exec(key, node, N, nodePrev) ;
            // N == prefix
            if ( node.isLeaf() )
                // Whether it matches or not, this is the node where the action is.
                return action.exec(key, node, N, nodePrev) ;
            
            // Reached end; may end here, may not.
            // Longer or same length key.
            int j = node.locate(key, node.lenFinish) ;
            if ( j < 0 ) //|| j == node.nodes.length )
                // No match across subnodes - this node is the point of longest match.
                return action.exec(key, node, node.prefix.length, nodePrev) ;
            // There is a next node down to try.
            nodePrev = node ;
            RadixNode node1 = node.get(j) ;
            // Nothign to go to
            if ( node1 == null )
                return action.exec(key, node, node.prefix.length, nodePrev) ;
            node = node1 ;
        }
        // Does not happen
    }

    public boolean contains(byte[] key)
    {
        return find(key) != null ;
    }
    
    // Find exact match (unlike search)
    RadixNode find(byte[] key)
    {
        if ( root == null )
            return null ;
        return applicator(root, key, findAction) ;
    }        

    
    private static Action findAction = new Action() {

        @Override
        public RadixNode exec(byte[] key, RadixNode node, int N, RadixNode nodePrev)
        {
            if ( N == node.prefix.length && node.lenFinish == key.length )
            {
                if ( node.isValue() )
                    // Exact match
                    return node ;
            }
            return null ;
        }
    } ;
    
    private static Action searchAction = new Action() {

        @Override
        public RadixNode exec(byte[] key, RadixNode node, int N, RadixNode nodePrev)
        {
            if ( N != node.prefix.length )
                return null ;
            if ( node.isValue())
                return node ;
            return null ;
        }
    } ;
     
    private RadixNode search(byte[] key)
    {
        if ( root == null )
            return null ;
        return applicator(root, key, searchAction) ;
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
                log.debug("insert: here => "+node) ;
                log.debug("insert: prev => "+ ((nodePrev==null)?"null":nodePrev.toString()) ) ;
                log.debug("insert: N = "+N) ;
            }
            /* Cases:
             * Leaf.
             * L1/  Key exists                 (N == prefix.length && node.lenFinish == key.length )
             * L2/  Key does not exist         (error)
             * L3/  Inserted key shorter       (N >=0 && N < prefix.length)
             * L4/  Inserted key diverges      (N < 0)
             * L5/  Inserted key longer        (N == prefix.length)
             * Branch:
             *   Key same length               (N == prefix.length && node.lenFinish == key.length )
             * B1/    Exists, and is already a value branch.
             * B2/       Does not exists, is not a value branch.
             * B3/  Key shorter than prefix.   (N >=0 && N < prefix.length)
             * B4/  Key diverges               (N < 0)
             * B5/  Key longer than prefix.    (N == prefix.length)
             *
             * There is common processing.
             */

            // Key already present - we ended at a leaf.
            if ( N == node.prefix.length && node.lenFinish == key.length ) 
            {
                // Cases L1, B1 and B2
                if (  node.isLeaf() )
                {          
                    // L1 and B1
                    if (logging && log.isDebugEnabled() )
                        log.debug("insert: Already present") ;
                    return null ;
                }
                // B2
                node.setAsValue(true) ;
                return node ;
            }

            // Key longer than an existing key
            if ( N == node.prefix.length )
            {
                byte[] prefixNew = Bytes.copyOf(key, node.lenFinish, key.length - node.lenFinish) ;
                if (logging && log.isDebugEnabled() )
                {
                    log.debug("Key longer than matching node") ;
                    log.debug("  Prefix new : "+Bytes.asHex(prefixNew)) ;
                }

                // Case L5 and B5
                // L5: Leaf to branch.
                if ( node.isLeaf() )
                {
                    node = node.convertToEmptyBranch() ;
                    node.setAsValue(true) ;
                }
                RadixNode n = RadixNode.allocBlank(node) ;
                n = n.convertToLeaf() ;
                n.prefix = prefixNew ;
                n.lenStart = node.lenFinish ;
                n.lenFinish = key.length ;
                
                int idx = node.locate(prefixNew) ;
                if ( node.get(idx) != null )
                    error("Key longer than node but subnode location already set") ;
                node.set(idx, n) ;
                return node ;
            }

            // Cases remaining. L3, B3, L4, B4  
            
            // Cases L3, B3 : Key is shorter than prefix.
            // Split the prefix upto the end of the key.
            // Make this node a value node (or leaf).
            
            
            // Cases L4, B4.
            // Key diverges at this node at point N.
            // Split the prefix upto the point of diverence.
            // Don't make this node a value node.
            
            byte[] prefixHere ;
            // Original data.
            byte[] prefixSub1 ;
            // New data.
            byte[] prefixSub2 ;
            
            boolean makeAValueNode = false ;
            
            if ( N < 0 )
            {
                // Key ran out.
                N = -(N+1) ;
                prefixHere = Bytes.copyOf(node.prefix, 0, N) ;
                // Remainder of original data.
                prefixSub1  = Bytes.copyOf(node.prefix, N) ;
                // New data.  Keys end here so no more data.
                prefixSub2  = null ;
                makeAValueNode = true ;
            }
            else
            {
                // N >= 0
                // Key diverges at N
                if ( key.length <= node.lenStart )
                    error("Incorrect key length: "+key.length+" ("+node.lenStart+","+node.lenFinish+")") ;
                // Case N = 0 only occurs at root (else a match to previous node).
                // Key shorter than this node and ends here.
                prefixHere = Bytes.copyOf(node.prefix, 0, N) ;
                // Remainder of original data.
                prefixSub1  = Bytes.copyOf(node.prefix, N) ;
                // New data from key.
                prefixSub2  = Bytes.copyOf(key, N+node.lenStart) ;
                makeAValueNode = false ;
            }
            
                
            // Key shorter than this node and ends here. 
                
            if (logging && log.isDebugEnabled() )
            {
                log.debug("Key splits this node") ;
                log.debug("  Prefix here : "+Bytes.asHex(prefixHere)) ;
                log.debug("  Prefix sub1 : "+Bytes.asHex(prefixSub1)) ;
                log.debug("  Prefix sub2 : "+((prefixSub2==null)?"null":Bytes.asHex(prefixSub2))) ;
            }
                
            // The tail of the original data and all the sub nodes.
            // Could do this in-place but have to alter the parent to point to a new node.
            // XXX
            RadixNode node1 = RadixNode.allocBlank(node) ;
            node1.prefix = prefixSub1 ; 
            node1.lenStart = node.lenStart+N ;
            node1.lenFinish = node.lenFinish ;
            if ( ! node.isLeaf() )
            {
                node1 = node1.convertToEmptyBranch() ;
                node1.takeSubNodes(node) ;
            }
            node1.setAsValue(node.isValue()) ;

            // The new leaf for the new data if key longer.
            RadixNode node2 = null ;
            if ( prefixSub2 != null )
            {
                node2 = RadixNode.allocBlank(node) ;
                node2.prefix = prefixSub2 ; 
                node2.lenStart = node.lenStart+N ;
                node2.lenFinish = key.length ;
                node2.setAsValue(true) ;
            }
            else
                node.setAsValue(true) ;
                
            // Now make node a two way (or one way if it is now the value)
            node = node.convertToEmptyBranch() ;
            node.prefix = prefixHere ;
            //node.lenStart
            node.lenFinish = node.lenStart+N ;
            node.setAsValue(makeAValueNode) ;
                
            int idx1 = node.locate(prefixSub1) ;
            node.set(idx1, node1) ;
            if ( node2 != null )
            {
                int idx2 = node.locate(prefixSub2) ;
                node.set(idx2, node2) ;
            }
            return node ;
        }
    } ;
    
    public boolean insert(byte[] key)
    {
        if (logging && log.isDebugEnabled() )
            log.debug("** Insert : "+Bytes.asHex(key)) ;
        
        if ( root == null )
        {
            root = RadixNode.allocBlank(null) ;
            root.prefix = key ;
            root.lenStart = 0 ;
            root.lenFinish = key.length ;
            root.setAsValue(true) ;
            return true ;
        }
        
        return applicator(root, key, insertAction) != null ;
    }
    
    private static Action deleteAction = new Action()
    {
        @Override
        public RadixNode exec(byte[] key, RadixNode node, int N, RadixNode prevNode)
        {
            if (logging && log.isDebugEnabled() )
            {
                log.debug("delete: "+RLib.str(key)) ;
                log.debug("delete: here => "+node) ;
                log.debug("delete: prev => "+ ((prevNode==null)?"null":prevNode.toString()) ) ;
                log.debug("delete: N = "+N) ;
            }
            
            /* Cases
             * 1/ Node = leaf, does not exist
             * 2/ Node = branch, not value
             * 
             * Then we have to work to sort out the tree.
             * A/ If leaf delete, remove from parent.
             * B/ If branch/value delete switch off flag.
             * Then  
             * C/ Simplify parent or branch
             *    If not a value and has one subnode, collapse
             */
            
            // Cases 1 and 2 : must be full length
            
            // Key not already present - not a full match (short, diverges) 
            if ( N != node.prefix.length || node.lenFinish != key.length )
            {
                if (logging && log.isDebugEnabled() )
                    log.debug("delete: Not present") ;
                return null ;
            }

            if ( node.isLeaf() ) 
            {
                // Leaf - delete node.
                if ( prevNode != null )
                {
                    // not leaf root.
                    int idx = prevNode.locate(node.prefix) ;
                    RadixNode x = prevNode.get(idx) ;
                    prevNode.set(idx, null) ;
                    RadixNode.dealloc(node) ;
                    node = null ;
                    // Drop though to fixup.
                }
                else
                {
                    // Root.
                    node.setAsValue(false) ;
                    return node ;
                }
            }
            else
            {
                // Branch.  Delete is a value branch.
                if ( node.isValue() && node.isValue() )
                    node.setAsValue(false) ;
                    // Drop though to fixup.
                else
                    // Didn't match after all.
                    return null ;
            }

            // Now we need to sort out the tree after a change.
            // We need to work on the parent if it was a leaf, or node, if it was a branch.
            
            RadixNode fixupNode = (node==null ? prevNode : node ) ;
            
            RadixNode fixupNode2 = fixup(fixupNode) ;
            if ( fixupNode2 != null )
                fixupNode = fixupNode2 ;
            return fixupNode ;

        }
    } ;
    
    /** Delete - return true if the tree changed (i.e the key was present and so was removed) */
    public boolean delete(byte[] key)
    {
        if ( root == null )
            return false ;
        RadixNode n = applicator(root, key, deleteAction) ;
        
        // Fixup root.
        // If the root changed and now has no-subnodes and no value, free it.
        if ( n != null && n.isRoot() && (n.countSubNodes() == 0 && ! n.isValue() ) )
        {
            RadixNode.dealloc(n) ;
            root = null ;
        }
        return n != null ;
    }
    
    /** After delete, need to remove redundant nodes. */
    protected static RadixNode fixup(RadixNode node)
    {
        // Must be a branch.
        if ( node.isLeaf() )
            error("Attempt to fixup a leaf") ;

        // count = 0 => can we become a leaf?
        // count = 1 => c an we merge with subnode? 
        
        int c = node.countSubNodes() ;
        if ( c == 0 )
        {
            if ( node.isValue() )
                node = node.convertToLeaf() ;
            // else; should not happen  - a non-value branch which had one leaf subnode 
            else
                error("Branch has no subnodes but didn't have a value") ;
            return node ;
        }

        if ( c != 1 )
            return null ;
        
        if ( node.isValue() )
            return null ;
        // Find exactly one subnode.
        RadixNode sub = node.oneSubNode() ;
        if ( sub == null )
            error("Branch has one subnodes but can't find it") ;
        // Single subnode to node.
        // Merge it in and delete it.
        // It may be a leaf.
        if ( logging && log.isDebugEnabled() )
        {
            log.debug("Collapse node") ;
            log.debug("  node: "+node) ;
            log.debug("  sub : "+sub) ;
        }
        
        // We're a value if the subnode was a value.
        node.setAsValue(sub.isValue()) ;

        // Combined prefix.
        int len1 = node.prefix.length + sub.prefix.length ;  
        int len =  sub.lenFinish - node.lenStart ;
        if ( len1 != len )
            error("Inconsistency in length calculations") ;
        // Merged prefix.
        byte [] p = new byte[len] ;
        System.arraycopy(node.prefix, 0, p, 0, node.prefix.length) ;
        System.arraycopy(sub.prefix,  0, p, node.prefix.length, sub.prefix.length) ;  
        if ( logging && log.isDebugEnabled() )
            log.debug("New prefix: "+RLib.str(p)) ;
        
        // Clear, pull up subnodes.
        node = node.convertToEmptyBranch() ;
        node.takeSubNodes(sub) ;
        
        // Prefix
        node.prefix = p ;
        // node.lenStart ;
        node.lenFinish = sub.lenFinish ;

        if ( logging && log.isDebugEnabled() )
            log.debug("  --> : "+node) ;

        if ( sub.isLeaf() )
          node = node.convertToLeaf() ;    
      
        RadixNode.dealloc(sub) ;
        return node; 
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

    public void clear()
    {
        if ( root == null )
            return ;
        clear(root) ;
    }
    
    private void clear(RadixNode node)
    {
        if ( !node.isLeaf() )
        {
            int idx = 0 ;
            while( ( idx = node.nextIndex(idx)) >= 0 )
            {
                RadixNode n = node.get(idx) ;
                clear(node) ;
            }
        }        
        RadixNode.dealloc(node) ;
        return ;
    }

    public ByteBuffer min() { return min(null) ; }
    
    public ByteBuffer min(byte[] b)
    {
        if ( root == null )
            return null ;
        ByteBuffer bb = (b == null ) ? ByteBuffer.allocate(50) : ByteBuffer.wrap(b) ;
        bb = RadixIterator.min(root, bb) ;
        bb.flip() ;
        return bb ;
    }
    
    public ByteBuffer max() { return max(null) ; }

    public ByteBuffer max(byte[] b)
    {
        if ( root == null )
            return null ;
        ByteBuffer bb = (b == null ) ? ByteBuffer.allocate(50) : ByteBuffer.wrap(b) ;
        bb = RadixIterator.max(root, bb) ;
        bb.flip() ;
        return bb ;
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
                if (node.isValue()) 
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
        return root.zeroSubNodes() ;
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
        // TODO -- Empty root : shoudl not occur but copy with it.
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
    
    // TODO Leaves and values.        
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
                if ( node.isValue() )
                {
                    System.out.print("[") ;
                    System.out.print(Iter.iter(x.iterator()).map(hex).asString(", ")) ;
                    System.out.print("]\n") ;
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
