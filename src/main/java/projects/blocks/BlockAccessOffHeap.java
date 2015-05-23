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

package projects.blocks;

import java.nio.ByteBuffer ;
import java.util.Deque ;
import java.util.LinkedList ;

import org.apache.jena.ext.com.google.common.cache.* ;
import org.apache.jena.tdb.base.block.Block ;
import org.apache.jena.tdb.base.file.BlockAccessDirect ;
import org.apache.jena.tdb.base.file.FileException ;

/** Direct ByteBuffers - normal Java I/O.
 *  Associated cache.
 */
public class BlockAccessOffHeap extends BlockAccessDirect {

    public BlockAccessOffHeap(String filename, int blockSize) {
        super(filename, blockSize) ;
    }

    // Single global pool.
    static int POOL_SIZE = 10 ;
    //static int CHUNK_SIZE = 10 ;
    
    private void initPool() {
        // Chunks
        //int CHUNKS = POOL_SIZE/CHUNK_SIZE ;
        //int chunkBytes = CHUNK_SIZE*blockSize ;
        
        int allocSize = blockSize ;
        
        // Very, very large.
        
        Deque<ByteBuffer> pool = new LinkedList<>() ;
        for(int i = 0 ; i < POOL_SIZE ; i++ ) {
            ByteBuffer bb = ByteBuffer.allocateDirect(blockSize) ; 
            pool.addLast(bb) ;
        }
    }
    
    // Alloc-release model?
    
    /*
     * BlockMgr.beginRead()/endRead()
     * BlockMgr.getRead(long)->release
     * BlockMgr.getWrite(long)->write->release
     * ==> 
     */
    
    RemovalListener<Integer, Block> listener = new RemovalListener<Integer, Block>(){
        @Override
        public void onRemoval(RemovalNotification<Integer, Block> notification) {
            RemovalCause c = notification.getCause() ;
            switch(c) {
                case COLLECTED :
                    break ;
                case EXPIRED :
                    break ;
                case EXPLICIT :
                    break ;
                case REPLACED :
                    break ;
                case SIZE :
                    break ;
                default :
                    break ;
                
            }
            notification.getKey() ;
            notification.getValue() ;
        }
    } ;
    
    Cache<Integer, Block> cache = CacheBuilder
        .newBuilder()
        .maximumSize(POOL_SIZE)
        
        .removalListener(null)
        .build() ;
    
    // Cache
    class BlockPool {
        
        
    }

    @Override
    public Block allocate(int blkSize) {
        if ( blkSize > 0 && blkSize != this.blockSize )
            throw new FileException("Fixed blocksize only: request= "+blkSize+"fixed size="+this.blockSize) ;
        int x = allocateId() ;
        
        // ByteBuffer bb = ByteBuffer.allocate(blkSize) ;
        // From pool.
        ByteBuffer bb = ByteBuffer.allocateDirect(blkSize) ;
        Block block = new Block(x, bb) ;
        return block;
    }

}

