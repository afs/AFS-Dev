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

package projects.blkmgr;

import java.nio.ByteBuffer ;
import java.util.ArrayDeque ;
import java.util.Deque ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.concurrent.atomic.AtomicLong ;

import org.openjena.atlas.lib.Chars ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockException ;
import com.hp.hpl.jena.tdb.base.file.BlockAccess ;

public class BlockAllocatorAccess implements BlockAllocator
{
    final static int SUPERROOT = 0 ;
    
    private Map<String, Long> rootId = new HashMap<>() ;
    private AtomicLong sequence = new AtomicLong(0) ;
    private Deque<Block> freeBlocks = new ArrayDeque<Block>();
    private final BlockAccess blockAccess ;
    private Block control ;
    
    /** Size, in bytes, of a Java long */
    public static final int SizeOfLong              = Long.SIZE/Byte.SIZE ;
    
    /** Size, in bytes, of a Java int */
    public static final int SizeOfInt               = Integer.SIZE/Byte.SIZE ;
    
    public BlockAllocatorAccess(BlockAccess blockAccess)
    {
        this.blockAccess = blockAccess ;
        if ( ! blockAccess.valid(SUPERROOT) )
        {
            // Initialize 
        }
        // Get our root ... which is always block zero.
        control = blockAccess.read(SUPERROOT) ;
        parseControl(control) ; 
    }
    
    /*
     * Layout of the control block
     *   Free chain : 8 bytes :
     *   Allocation start : 8 bytes.  
     *   (string, long) pairs
     *      string encoded as (int, bytes UTF-8) so
     *      (int, bytes, long)
     *   (-1, ????) for end.
     */
    private void parseControl(Block control)
    {
        ByteBuffer bb = control.getByteBuffer() ;
        bb.position(0) ;
        int idx = 0 ;
        
        long freeChain = bb.getLong() ;
        idx += SizeOfLong ;
        
        long sequenceNum = bb.getLong() ;
        idx += SizeOfLong ;
        
        for ( ;; )
        {
            int len = bb.getInt() ;
            if ( len < 0 )
                break ;
            idx += SizeOfInt ;
            
            byte[] b = new byte[len] ;
            bb.get(b) ;
            String key = new String(b, Chars.charsetUTF8) ;
            idx += len ;
            long blockId = bb.getLong() ;
            idx += SizeOfLong ;
            // check.
            if ( bb.position() != idx )
                throw new BlockException("Alignment error parsing control block") ;
            rootId.put(key, blockId) ;
        }

        // Free chain.
        
        // Set sequence.BlockAllocatorMem
        sequence.set(sequenceNum) ;
    }

    private void writeControl(Block control)
    {
        
    }

    
    @Override
    public void initialize()
    {}

    @Override
    public void reinitialize()
    {}

    @Override
    public void registerRoot(String key, long id)
    { 
        if ( rootId.containsKey(key) )
            throw new BlockException("Root id "+id+" already registered") ;
        rootId.put(key, id) ;  
    }

    @Override
    public void unregisterRoot(String key, long id)
    {
        if ( ! rootId.containsKey(key) )
            throw new BlockException("No such root id registered: "+id) ;
        rootId.remove(key) ;
    }

    @Override
    public long findRootId(String key)
    {
        Long x = rootId.get(key) ;
        if ( x == null )
            return -1 ;
        return x.longValue() ; 
    }
    
    @Override
    public Block allocate(int blockSize)
    {
        Block blk = freeBlocks.pollFirst() ;
        if ( blk == null )
        {
            long id = sequence.incrementAndGet() ;
            
        }
        
        return blk ;
    }

    @Override
    public void deallocate(Block block)
    {
        freeBlocks.add(block) ;  
    }
}

