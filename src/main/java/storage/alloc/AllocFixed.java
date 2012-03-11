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

package storage.alloc;

import java.nio.ByteBuffer; 
import storage.StorageException ;

/** Fixed size block allocator. */ 

public class AllocFixed implements Alloc
{
    ByteBuffer space ;
    final int blkSize ;
    int freeChain ;
    /*
     * A free block uses the first 4 bytes as the index of the next block in the chain.
     * A mark of -1 means all the space above this point is free.  
     */
    
    // Recycle MemBlocks
    
    public AllocFixed(int spaceSize, int blkSize) 
    {
        if ( spaceSize < 0 || spaceSize%(blkSize) != 0 )
            throw new StorageException("Bad spaceSize "+spaceSize+ "(blkSize="+blkSize+")") ;
        if ( blkSize < 4 )
            throw new StorageException("blkSize too small: "+blkSize) ;
        space = ByteBuffer.allocateDirect(spaceSize) ;
        freeChain = -1 ;
        this.blkSize = blkSize ;
        space.limit(0) ;
        // Create a real freechain?
    }

    // Alternative:
    // Keep a local Map<ByteBuffer, Long> 
    // and return ByetBuffers.
    
    @Override
    public MemBlock alloc(int size)
    {
        if ( freeChain == -1 )
        {
            if (space.limit() == space.capacity() )
                throw new StorageException("Out of allocation memory") ;
            // Release a block. 
            int x = space.limit() ;
            space.limit(x+blkSize) ;
            space.position(x) ;
            ByteBuffer bb = space.slice() ;
            MemBlock mBlk = new MemBlock(freeChain, blkSize, bb) ;
            return mBlk ;
        }

        int next = space.getInt(freeChain) ;
        // Remember where we are in the overall buffer.
        space.position(freeChain) ;
        space.limit(freeChain+blkSize) ;
        ByteBuffer bb = space.slice() ;
        MemBlock mBlk = new MemBlock(freeChain, blkSize, bb) ;
        freeChain = next ;
        return mBlk ; // Do not use first 4 bytes.
    }

    @Override
    public void free(MemBlock mBlk)
    {
        // To front of free chain.
        int freeChain0 = freeChain ;
        freeChain = mBlk.location ;
        space.putInt(freeChain, freeChain0) ;
    }
}

