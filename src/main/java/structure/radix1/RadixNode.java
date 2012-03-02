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

package structure.radix1;

import java.util.HashSet;
import java.util.List ;
import java.util.Set;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.logging.Log;



public final class RadixNode //extends PrintableBase implements Printable 
{
    // Debugging? Needed for traversal?
    // Not needed for deployment version (we only go down the tree).
    // Iteration?
    /*package*/ int parentId ;
    /*package*/ RadixNode parent ;

    /*
     * http://en.wikipedia.org/wiki/Radix_tree
     */

    static int counter = 0 ; 
    int id = (counter++); // Debugging
    
    // Prefix to this node from node above.
    
    byte[] prefix ;     // Null means "".
    
    // Position of the end of the prefix in the overall key at this point.
    int lenFinish ; // Debugging? Use tracking to know these values. 
    int lenStart ;
    
    // The nodes below this one, corresponding to each possible next byte
    List<RadixNode> nodes ;         // When real, use an array[]; null for leaf.
    
    static RadixNode alloc(RadixNode parent) { return new RadixNode(parent) ; }
    static void dealloc(RadixNode node) { }    
    
    private RadixNode(RadixNode parent)
    { 
        this.parent = parent ;
        this.parentId = (parent==null)? -1 : parent.id ;
    }

    // Space cost:
    //     parent
    //     prefix array (so 3 slot overhead)
    //     node array   (can use array so 3 slot overhead)
    
    // More (??) compact would be a giant, segemented array for 
    // each of the node arrays then
    
    // Optimization (to do): nodes entry of null mean zero length prefix/leaf.
    
    // --------
    // Return
    //   Index of first non-matching byte.
    //   - (1+length) if key runs out
    //   prefix length means complete match
    
    public final int countMatchPrefix(byte[] key)
    {
        for ( int i = 0 ; i < prefix.length ; i++ )
        {
            // Index into key.
            int j = i+lenStart ;
            if ( j == key.length )
                // Key ran out.
                return -(i+1) ;
            if ( prefix[i] != key[j] )
                return i ;
        }
        return prefix.length ;
    }

    @Override
    public String toString()
    {
        String prefixStr = Bytes.asHex(prefix) ;
        if ( isLeaf() )
            return String.format("Leaf[%d/%d]: Length=(%d,%d) :: prefix = %s", id, parentId, lenStart, lenFinish, prefixStr) ; 
        
        StringBuilder b = new StringBuilder() ;
        for ( RadixNode n : nodes )
        {
            b.append(" ") ;
            b.append(n.id+"") ;
        }
        return String.format("Node[%d/%d]: Length=(%d,%d) :: prefix = %s -> Sub:%s", id, parentId, lenStart, lenFinish, prefixStr, b.toString() ) ;
    }
    
    /*public*/ void output(final IndentedWriter out)
    {
        RadixNodeVisitor<Object> v = new RadixNodeVisitorBase()
        {
            @Override
            public void before(RadixNode node)
            {
                String str = node.toString() ;
                out.print(str) ;
                out.println();
                out.incIndent() ;
            }

            @Override
            public void after(RadixNode node)
            {
                out.decIndent() ;
            }
            
        } ;
        this.visit(v) ;
    }

    // Re-consider for persistence - this looks into the subnode.
    // Should pull up first byte (nibble?) for dispatch purposes.
    
    /** Return the index of the node with this byte as the start of prefix
     *  or -(i+1) for insertion point if not found.
     */
    /*package*/ int locate(byte[] bytes, int start)
    {
        // XXX Should we use locate(byte b) only?
//        if ( RadixTree.logging  && RadixTree.log.isDebugEnabled() )
//        {
//            RadixTree.log.debug("locate: <"+Bytes.asHex(bytes, start, bytes.length)) ;
//        }
            
        if ( nodes == null )
            return -1 ;
        // Nothing to test -- so is there a subnode of prefix ""?
        if ( bytes.length == start )
        {
            if ( nodes.get(0).prefix.length == 0 )
                return 0 ;
            else
                return -(0+1) ;
        }
        
        byte b = bytes[start] ;
        return locate(b) ;
    }
        
    /*package*/int locate(byte b)
    {
        for ( int i = 0 ; i < nodes.size() ; i++ )
        {
//            if ( RadixTree.logging  && RadixTree.log.isDebugEnabled() )
//                RadixTree.log.debug("locate: >"+Bytes.asHex(nodes.get(i).prefix)) ;
            // Look first byte only.
            // Prefixes must differ at first byte
            if ( nodes.get(i).prefix.length == 0 )
                // No bytes is lowest, so no match. 
                continue ;
                //return -(i+1) ;   // Would have thought this was right.
            
            int x = Bytes.compareByte(b, nodes.get(i).prefix[0]) ;
            //int x = compare(bytes, start, nodes.get(i).prefix) ;
            if ( x > 0 ) 
                continue ;
            if ( x < 0 )
                // Overshoot.
                return -(i+1) ;
            //if ( keyByte == b ) 
            return i ;
        }
        return -(nodes.size()+1) ;
    }
    
    private int compare(byte[] bytes, int start, byte[] nextPrefix)
    {
        int n = Math.min(bytes.length-start, nextPrefix.length) ;
        
        for ( int i = 0 ; i < n ; i++ )
        {
            byte b1 = bytes[i+start] ;
            byte b2 = nextPrefix[i] ;
            if ( b1 == b2 )
                continue ;
            // Treat as unsigned values in the bytes. 
            return (b1&0xFF) - (b2&0xFF) ;  
        }

        return (bytes.length-start) - nextPrefix.length ;
    }

    public void check()
    { 
        _check(0, new HashSet<Integer>()) ; 
    }
    
    private void _check(int length, Set<Integer> seen)
    {
        if ( RadixTree.logging && RadixTree.log.isDebugEnabled() )
        {
            RadixTree.log.debug("Check: "+this.id) ;
            System.out.flush() ;
        }
        // It's a tree and so we seen nodes only once. 
        if ( seen.contains(this) )
        {
            error(this, "Node %d already seen",id) ;
            return ;
        }
        seen.add(this.id) ;
        
        if (parentId != -1 && !seen.contains(parentId) )
            error(this, "Parent not seen") ;

        if ( prefix == null )
            error(this, "Null prefix") ;
     
        if ( lenStart != length )
            error(this, "Start length error %d/%d", lenStart, length) ;

        if ( lenStart > lenFinish )
            error(this, "Finish length error") ;
        
        if ( lenFinish - lenStart != prefix.length )
            error(this, "Prefix lenth error %d,%d", lenFinish - lenStart, prefix.length) ;

        // Find self in parent.
        if ( parent != null )
        {
            if ( parent.id != parentId )
                error(this, "parent.id != parentId (%d != %d)", parent.id, parentId ) ;
            
            int idx = 0 ;
            int N = parent.nodes.size() ;
            for ( ; idx < N ; idx++ )
            {
                if ( parent.nodes.get(idx) == this)
                    break ;
            }

            if (idx >= N )
                error(this, "Not a child of the parent %s : %s", Iter.map(parent.nodes, idOfNode), parent) ;
        }

        if ( nodes == null )
            return ;

        // Legal?
        // Yes - during push-dwn we can end pusing down one node.
        // Should really avoid this.
        
//        if ( nodes.size() < 2 )
//            error(this, "Internal node has length of "+nodes.size()) ;
        // Check subnodes are sorted and start with a different byte
        Set<Integer> bytes = new HashSet<Integer>() ;
        int last = -2 ;
        for ( RadixNode n : nodes )
        {
            int b = -1 ;
            if ( n.prefix.length > 0 )
                b = (n.prefix[0]&0xFF) ;
            if ( last >= b )
                error(this, "Prefix start not strictly increasing") ;
            if ( n.parentId != id )
                error(this, "Child %d points to %d, not parent %d", n.id, n.parentId, id) ;
        }
        
        int nextStartLen = length+prefix.length ;
        for ( RadixNode n : nodes )
            n._check(nextStartLen, seen) ;
    }
    
    static Transform<RadixNode, Integer> idOfNode = new Transform<RadixNode, Integer>(){

        @Override
        public Integer convert(RadixNode item)
        {
            return item.id ;
        }} ; 

    public boolean isLeaf()
    {
        return nodes == null ;
    }

    public <T> void visit(RadixNodeVisitor<T> visitor)
    {
        _visit(visitor, new HashSet<RadixNode>()) ;
    }

    private <T> void _visit(RadixNodeVisitor<T> visitor, Set<RadixNode> seen)
    {
        if ( seen.contains(this) )
        {
            Log.warn(this, "Bad tree: "+id) ;
            return ;
        }
        seen.add(this) ;
        visitor.before(this) ;
        if ( nodes != null )
        {
            for ( RadixNode n : nodes )
                n._visit(visitor, seen) ;
        }
        visitor.after(this) ;
    }
    
    private static void error(RadixNode node, String message, Object... args)
    {
        System.out.flush() ;
        message = String.format(message, args) ;
        System.err.println("Error: "+node) ;
        System.err.println(message) ;
        RadixTree.error(message) ;
    }
}
