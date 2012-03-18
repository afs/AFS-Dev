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

package structure.radix;

import static structure.radix.RadixTree.* ;
import static structure.radix.Str.* ;

import java.nio.ByteBuffer ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.openjena.atlas.AtlasException ;

class RadixIterator implements Iterator<RadixEntry>
{
    // Or parent.
    // Deque<RadixNode> stack = new ArrayDeque<RadixNode>() ;
    // Still need the place-in-parent.
    RadixNode node ;
    ByteBuffer slot = null ;
    ByteBuffer prefix = null ;

    byte[] finish = null ;

    RadixIterator(RadixTree tree, byte[] start, byte[] finish)
    {
        node = tree.getRoot() ;
        this.finish = finish ;
        if ( start == null )
        {
            prefix = ByteBuffer.allocate(50) ;    //Reallocating?
            node = downToMinNode(node, prefix) ;
            slot = prefix ;
            if ( logging && log.isDebugEnabled() )
            {
                log.debug("Iterator start min node") ;
                log.debug("Iterator start: "+node) ;
            }
            return ;
        }

        // BB : basically broken.

        // We need to find the first node equal to or just greater than the start.
        // If we find that start is (just) bugger than some point, we can do that
        // by setting slot to null so it's fixed on first .hasNext.


        // Like RadixTree.applicator, except copies into slot on the way down.
        // RadixTree.applicator -> a struct
        // Add arg: a per step action.

        int N = -1 ;

        prefix = ByteBuffer.allocate(start.length) ;
        slot = prefix ;

        // Find node of interest.

        for(;;)
        {
            // Does the prefix (partially) match?
            N = node.countMatchPrefix(start) ;

            // Copy prefix.
            int numMatch = N ;
            if ( numMatch < 0 )
                numMatch = -(numMatch+1) ;
            // else matched up to end of prefix.

            // Copy all bytes that match.
            prefix = appendBytes(node.prefix, 0, numMatch, slot) ;
            if ( logging && log.isDebugEnabled() )
            {
                log.debug("    Loop: node   = "+node) ;
                log.debug("    Loop: prefix = "+strToPosn(prefix)) ;
            }

            if ( N < 0 )
                break ;
            if ( N < node.prefix.length )
                break ;
            
            // N == node.prefix.length, not a leaf.
            int j = node.locate(start, node.lenFinish) ;
            if ( j < 0 ) //|| j == node.nodes.size() )
                // No match across subnodes - this node is the point of longest match.
                break ;
            // There is a next node down to try.
            RadixNode node2 = node.get(j) ;
            
            if ( node2 == null )
            {
                // no matching next level down but key longer.  
                // Start at min tree of next index, if any.
                int idx = node.nextIndex(j) ;
                node2 = ( idx < 0 ) ? null : node.get(idx) ;
                node = node2 ;
                slot = null ;
                return ;
            }
             
            // Start key continues.
            node = node2 ;
        }                

        // Important part now position-limit.
        
        if ( logging && log.isDebugEnabled() )
        {
            log.debug("  node   = "+node) ;
            log.debug("  prefix = "+strToPosn(prefix)) ;
            log.debug("  slot   = "+strToPosn(slot)) ;
            log.debug("  N = "+N) ;
        }

        // Exit at Node of interest.
        if ( N < 0 )
        {
            int numMatch = -(N+1) ;

            byte a = node.prefix[numMatch] ;
            byte b = start[node.lenStart+numMatch] ;
            int x = Byte.compare(a, b) ;
            if ( x == 0 )
                throw new AtlasException("bytes compare same - expected different") ;
            if ( x < 0 )
            {
                // Diverge - less.
                // Start min here
                node = downToMinNode(node, prefix) ;
                slot = prefix ;
                if ( logging && log.isDebugEnabled() )
                {
                    log.debug("  Diverge/less: "+strToPosn(prefix)) ;
                    log.debug("  Iterator start: "+node) ;
                }
            }
            else
            {
                if ( logging && log.isDebugEnabled() )
                {
                    log.debug("  Diverge/more: "+strToPosn(prefix)) ;
                    log.debug("  Iterator (non)start: "+node) ;
                }
                // Diverge - key more than this node and all it's sub nodes.
                // Start here but do not yield a slot.
                slot = null ;
            }
            // Done.
            return ;
        }

        // N < node.prefix.length
        // N == node.prefix.length
        // Ends here; key may continue.  Start is min of this tree.
        
        if ( start.length > node.lenFinish )
        {
            // Don't yield this node. Start from next one.
            slot = null ;
            return ;
        }
        

        node = downToMinNode(node, prefix) ;
        slot = prefix ;                    

        if ( logging && log.isDebugEnabled() )
        {
            log.debug("  Min subtree: "+node) ;
            log.debug("  Slot: "+strToPosn(slot)) ;
        }
    }

    static ByteBuffer min(RadixNode node, ByteBuffer slot)
    {
        while(!node.hasEntry())
        {
            // Copy as we go.
            slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
            int idx = node.nextIndex(0) ;
            if ( idx < 0 )
                break ;
            node = node.get(idx) ;
        }
        // Copy leaf details.
        slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
        return slot ;
    }

    // TODO assumes bytebuffer large enough.
    // TODO Common code in radix
    // TODO Check downToMinNode() with RadixTree.min() -- common code?

    private static RadixNode downToMinNode(RadixNode node, ByteBuffer slot)
    {
        while(!node.hasEntry())
        {
            // Copy as we go.
            slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
            int idx = node.nextIndex(0) ;
            if ( idx < 0 )
                break ;
            node = node.get(idx) ;
        }
        // Copy leaf details.
        slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
        return node ;
    }

    static ByteBuffer max(RadixNode node, ByteBuffer slot)
    {
        while(!node.isLeaf())
        {
            // Copy as we go.
            slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
            int idx = node.lastIndex() ;
            if ( idx < 0 )
                break ;
            node = node.get(idx) ;
        }
        // Copy leaf details.
        slot = appendBytes(node.prefix, 0, node.prefix.length, slot) ;
        return slot ;
    }

    /** Copy bytes from the array ([], start, length) to the end of a ByteBuffer */
    static ByteBuffer appendBytes(byte[] array, int start, int length, ByteBuffer bb) 
    {
        if ( bb.position()+length > bb.capacity() )
        {
            ByteBuffer bb2 = ByteBuffer.allocate(bb.capacity()*2 ) ;
            System.arraycopy(bb.array(), 0, bb2.array(), 0, bb.position()) ;
            return bb2 ;
        }
        //            System.arraycopy(bb.array(), bb.position(), array, 0, length) ;
        //            bb.position((bb.position()+length)) ;
        try {
            bb.put(array, start, length) ;
        } catch (java.nio.BufferOverflowException ex)
        {
            System.err.println() ;
            System.err.println(bb) ;
            System.err.printf("([%d], %d, %d)", array.length, start, length) ;
            throw ex ;
        }
        return bb ;
    }

    @Override
    public boolean hasNext()
    {
        if ( slot != null )
            return true ;
        if ( node == null )
            // Ended
            return false ;

        RadixNode node2 ;
        if ( node.isLeaf() )
        {
            // Go up one or more.
            node2 = gotoUpAndAcross(node) ;
        }
        else
        {
            int idx = node.nextIndex(0) ;
            node2 = ( idx < 0 ) ? null : node.get(idx) ;
        }
        if ( node2 == null )
        {
            node = null ;
            return false ;
        }
        prefix.position(node2.lenStart) ;

        // Now go down the next one
        node2 = downToMinNode(node2, prefix) ;
        slot = prefix ;
        node = node2 ;
        return true ;
    }

    private static RadixNode gotoUpAndAcross(RadixNode node2)
    {
        //System.out.println("gotoUpAndAcross: "+node2) ;

        RadixNode parent = node2.getParent() ;
        //System.out.println("gotoUpAndAcross:     "+parent) ;
        if ( parent == null )
            return null ;
        // Find self.
        int idx = parent.locate(node2.prefix) ;

//            // Find self.
//            int N = parent.nodes.size() ;
//            int idx = 0 ; 
//            for ( ; idx < N ; idx++ )
//            {
//                if ( parent.nodes.get(idx) == node2)
//                    break ;
//            }

//            if ( idx >= N )
//            {
//                System.out.println("NOT FOUND") ;
//                System.out.println("   "+parent) ;
//                System.out.println("   "+node2) ;
//            }
        idx = parent.nextIndex(idx+1) ;
        if ( idx >= 0 )
            return parent.get(idx) ;
        // tail recursion - remove.
        return gotoUpAndAcross(parent) ;
    }

    @Override
    public RadixEntry next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        if ( ! node.hasEntry() )
            throw new AtlasException("yielding a non value") ;
        byte[] x = RLib.bb2array(prefix, 0, slot.position()) ;
        slot = null ;
        return new RadixEntry(x, node.getValue()) ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException() ; }

}
