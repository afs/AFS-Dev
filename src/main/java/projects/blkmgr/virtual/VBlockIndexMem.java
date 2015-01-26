/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package projects.blkmgr.virtual;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.List ;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.* ;

import storage.StorageException ;

public class VBlockIndexMem implements VirtualBlockIndex
{
    // See Indirection.
    private static final int N = 100 ;
    private ByteBuffer buffer = ByteBuffer.allocate(N*(SizeOfLong+SizeOfInt)) ;
    
    // CRUDE / TEMPORARY
    private List<DiskBlock> blocks = new ArrayList<>() ;
    
    @Override
    public long alloc(long length)
    {
        long location = 0 ; //****
        DiskBlock blk = new DiskBlock(location, length) ;
        // Scan (!) for nulls.
        for ( int i = 0 ; i < blocks.size() ; i++ )
        {
            if ( blocks.get(i) == null ) 
            {
                blocks.set(i, blk) ;
                return i ;
            }
        }
        blocks.add(blk) ;
        return blocks.size()-1 ;
    }

    @Override
    public DiskBlock get(long vIndex)
    {
        check(vIndex) ;
        return blocks.get((int)vIndex) ;
    }

    @Override
    public void set(long vIndex, long location, long length)
    {
        DiskBlock blk = new DiskBlock(location, length) ;
        set(vIndex, blk) ;
    }

    @Override
    public void set(long vIndex, DiskBlock pBlock)
    {
        check(vIndex) ;
        blocks.set((int)vIndex, pBlock) ;
    }

    @Override
    public void remove(long vIndex)
    {
        check(vIndex) ;
        blocks.set((int)vIndex, null) ;
    }

    private final void check(long vIndex)
    {
        if ( vIndex < 0 || vIndex >= blocks.size() )
            throw new StorageException("Out of range: "+vIndex+" [0,"+(blocks.size()-1)+"]") ;
    }
}

