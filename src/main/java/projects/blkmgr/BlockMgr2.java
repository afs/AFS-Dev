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

package projects.blkmgr;

import java.io.File ;
import java.io.IOException ;
import java.nio.ByteBuffer ;
import java.nio.channels.FileChannel ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.NotImplemented ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.file.ChannelManager ;

public class BlockMgr2 implements BlockMgr
{
    public static void main(String ...args)
    {
        BlockMgr blkMgr = BlockMgr2.connect("BLK") ;
        
        Block blk = blkMgr.allocate(BlockSize) ;
        blkMgr.release(blk) ;
        Block blk2 = blkMgr.allocate(BlockSize) ;
        
        System.out.println(blk.getId()) ;
        System.out.println(blk2.getId()) ;
    }
    
    
    private static Logger log = LoggerFactory.getLogger(BlockMgr2.class) ;
    
    private static int BlockSize = 8 * 1024 ;
    
    FileChannel file ;
    int freeChain = -1 ;
    
    static BlockMgr connect(String filename)
    {
        return new BlockMgr2(filename) ;
    }
    

    
    private static void create(File f)
    {
        
    }

    private static void format(FileChannel file)
    {
        try {
            file.truncate(BlockSize) ;
            ByteBuffer bb = ByteBuffer.allocate(BlockSize) ;
            bb.putLong(0, 0xABCD) ;
            bb.putInt (8, -1) ;  // CHECK 
            bb.putInt (12, -1) ; // CHECK
        } catch (IOException ex) { IO.exception(ex) ; }
    }
    
    private BlockMgr2(String filename)
    {
        init(filename) ;
    }
    
    private void init(String filename)
    {
        /*
         * 8K blocks, int block id.
         * => 2G*8K bytes addressing => 16T (per file).
         */
        try {

            File f = new File(filename) ;
            if ( ! f.exists() )
                create(f) ;
            FileChannel file = ChannelManager.acquire(filename, "rw") ;
            if ( file.size() == 0 )
            {
                format(file) ;
                freeChain = -1 ;
                return ;
            }

            // Read meta block.
            ByteBuffer bb = ByteBuffer.allocate(BlockSize) ;
            file.read(bb, 0) ;
            // Bytes 0-7    : version (long)
            // Bytes 8-11   : next meta block(int)
            // Bytes 12-15  : free chain pointer (int) 

            long version = bb.getLong(0) ;
            int metaChain = bb.getInt(8) ;
            int freeChain = bb.getInt(12) ;
            
            // Roots.
            // A root of zero (i.e this block) is illegal.
            List<Integer> roots = new ArrayList<Integer>() ;
            
            // 8 byte slots, first
            bb.position(16) ;
            for ( int i = 16 ; i < BlockSize ; i += 4 )
            {
                int rootId = bb.getInt() ;
                if ( rootId == 0 || rootId == -1 )
                {
                    break ;
                }
                roots.add(rootId) ;
            }
        } catch (IOException ex) { IO.exception(ex) ; return ; }
        
        return ;
    }
    
    private void reset()
    {
        format(file) ;
        freeChain = -1 ;
        // Clear the roots copy.
        
    }
    
    @Override
    public Block allocate(int blockSize)
    {
        if ( freeChain != -1 )
        {
            int id = freeChain ;
            int idNext = getNext(id) ;  /* From the block */
            freeChain = idNext ;
            // Transactions
        }
        
        
        return null ;
    }

    private int getNext(int id)
    {
        if ( true) throw new NotImplemented() ; 
        return 0 ;
    }



    @Override
    public Block getRead(long id)
    {
        return null ;
    }

    @Override
    public Block getReadIterator(long id)
    {
        return null ;
    }

    @Override
    public Block getWrite(long id)
    {
        return null ;
    }

    @Override
    public void release(Block block)
    {}

    @Override
    public boolean isEmpty()
    {
        return false ;
    }

    @Override
    public Block promote(Block block)
    {
        return null ;
    }

    @Override
    public void write(Block block)
    {}

    @Override
    public void overwrite(Block blk)
    {}

    @Override
    public void free(Block block)
    {}

    @Override
    public boolean valid(int id)
    {
        return false ;
    }

    @Override
    public void close()
    {}

    @Override
    public boolean isClosed()
    {
        return false ;
    }

    @Override
    public void sync()
    {}

    @Override
    public void syncForce()
    {}

    @Override
    public void beginUpdate()
    {}

    @Override
    public void endUpdate()
    {}

    @Override
    public void beginRead()
    {}

    @Override
    public void endRead()
    {}

    @Override
    public void beginIterator(Iterator<? > iterator)
    {}

    @Override
    public void endIterator(Iterator<? > iterator)
    {}

    @Override
    public String getLabel()
    {
        return null ;
    }

}

