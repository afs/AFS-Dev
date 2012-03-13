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

import java.nio.ByteBuffer ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.openjena.atlas.lib.ByteBufferLib ;

class RadixIterator implements Iterator<ByteBuffer>
    {
        // Or parent.
        // Deque<RadixNode> stack = new ArrayDeque<RadixNode>() ;
        // Still need the place-in-parent.
        RadixNode node ;
        ByteBuffer slot = null ;
        ByteBuffer prefix = null ;
        
        byte[] finish = null ;
        
        RadixIterator(RadixNode root, byte[] start, byte[] finish)
        {
            this.finish = finish ;
            node = root ;
            int N = -1 ;
           
            if ( start != null )
            {
                // Find ....
                
                prefix = ByteBuffer.allocate(start.length) ;
                slot = prefix ;
                for(;;)
                {
                    // Does the prefix (partially) match?
                    N = node.countMatchPrefix(start) ;
                    int numMatch = N ;
                    if ( numMatch < 0 )
                        numMatch = -(numMatch+1) ;
                    // else matched up to end of prefix.
                    
                    // Copy all bytes that match.
                    prefix = appendBytes(node.prefix, 0, numMatch, slot) ;
                    
                    
                    if ( N < node.prefix.length )   // Includes negative N
                        break ;
                    // Longer or same length key.
                    int j = node.locate(start, node.lenFinish) ;
                    if ( j < 0 ) //|| j == node.nodes.size() )
                        // No match across subnodes - this node is the point of longest match.
                        break ;
                    // There is a next node down to try.
                    node = node.get(j) ;
                }
            }
            else
                prefix = ByteBuffer.allocate(10) ;    //Reallocating?
            // Now move until leaf.
            node = downToMinNode(node, prefix) ;
            slot = prefix ;
            // But we need to track the key for copying reasons.
            if ( false && RadixTree.logging && RadixTree.log.isDebugEnabled() )
            {
                RadixTree.log.debug("Iterator start: "+node) ;
                RadixTree.log.debug("Iterator start: "+slot) ;
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
        public ByteBuffer next()
        {
            if ( ! hasNext() )
                throw new NoSuchElementException() ;
            ByteBuffer x = ByteBuffer.allocate(slot.position()) ;
            ByteBufferLib.bbcopy(slot,  0, x, 0, slot.position(), 1) ;
            slot = null ;
            return x ;
        }

        @Override
        public void remove()
        { throw new UnsupportedOperationException() ; }
        
    }
