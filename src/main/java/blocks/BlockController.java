/*
 *  Copyright 2013, 2014 Andy Seaborne
 *
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
 */

package blocks;

import java.util.* ;

import org.apache.jena.atlas.lib.StrUtils ;

public class BlockController {
    // Free chain.
    // Named areas.
    //   Not contiguous.
    
    private Map<String, BlockArea> areas = new HashMap<>() ;
    
    private List<Block> blocks = new ArrayList<>() ; 
    private LinkedList<Block> freeChain = new LinkedList<>() ;
    
    // On-disk format.
    // This is Block 0.
    
    // 0 - info / extension block? meta?
    // 8-15 - long ; free chain -> or just a "named" area
    // 16- : the named sections
    //       (int/length of name, bytes of name, long/root ptr).
    // or (8 bytes name, 8 bytes root ptr) = 16 => 64 in a block.
    
    //1k block , => 
    
    // Naming: database:filename:index.
    public BlockController(String filename, int blockSize) {
    }
    
    public BlockArea getNamedArea(String name) {
        return areas.get(name) ;
    }

    public BlockArea createNamedArea(String name) {
        if ( areas.containsKey(name)) {
            
        }
        
        byte[] nameBytes = StrUtils.asUTF8bytes(name) ; 
        return null ;
    }
    
    public Block getBlock(long id) {
        if ( id < 0 )
            throw new BlockException("Negative block number: "+id) ;
        if ( id >= blocks.size())
            throw new BlockException("Block number out of range [0,"+(blocks.size()-1)+"] : "+id) ;
        Block blk = blocks.get((int)id) ; 
        if ( isFree(blk, id) )
            throw new BlockException("Block not currently in use: "+id) ;
        
        
        return blk ;
    }

    private boolean isFree(Block blk, long id) {
        return freeChain.contains(blk) ;
    }

    public Block allocBlock()  {
        if ( ! freeChain.isEmpty() ) {
            Block blk = freeChain.removeFirst() ;
            return blk ;
        }
        int x = blocks.size() ;
        Block blk = new Block(x, null) ;
        blocks.add(blk) ;
        return blk ;
    }

    public void freeBlock(Block blk)  {
        if ( isFree(blk, blk.getId()) )
            throw new BlockException("Block already free: "+blk.getId()) ;
        freeChain.addFirst(blk) ;
        // Destroy "Block"
    }

    public void sync() {
        // Make sure 
    }
    
    /** Write this block using a temporary file. */
    public void writeVeryCarefully(Block block) {
        
    }
    
    static class BlockArea {
        BlockArea() {}
        // Root block
        // Name
    }
    
    
}

