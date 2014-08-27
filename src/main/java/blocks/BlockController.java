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

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.lib.StrUtils ;

public class BlockController {
    // Free chain.
    // Named areas.
    //   Not contiguous.
    
    private Map<String, BlockArea> areas = new HashMap<>() ;
    
    // On-disk format.
    // This is Block 0.
    
    // 0 - info / extension block? meta?
    // 8-15 - long ; free chain -> or just a "named" area
    // 16- : the named sections
    //       (int/length of name, bytes of name, long/root ptr).
    // or (8 bytes name, 8 bytes root ptr) = 16 => 64 in a block.
    
    //1k block , => 
    
    
    public BlockController(String filename) {
        
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

    public Block allocBlock()  {
        
        //if free chain
        
        return null ;
    }
    
    static class BlockArea {
        BlockArea() {}
        // Root block
        // Name
    }
    
    
}

