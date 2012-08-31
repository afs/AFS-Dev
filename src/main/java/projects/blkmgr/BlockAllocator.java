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

import com.hp.hpl.jena.tdb.base.block.Block ;

/** Manage and handout blocks */
public interface BlockAllocator
{
    /** initialize */
    public void initialize() ;
    
    /** destroy the storage and reset to all empty. */
    public void reinitialize() ;

    /** Allocate and remember a root block */ 
    public void registerRoot(String key, long id) ;

    /** Deallocate root registration - does not release the block */ 
    public void unregisterRoot(String key, long id) ;

    /** Locate a root block id. Retunr -1 for no such key registered */ 
    public long findRootId(String key) ;
    
    // Interface from the BlockMgrs 
    
    /** Allocate a block */ 
    public Block allocate(int blockSize) ;
    
    /** Deallocate a block */
    public void deallocate(Block block) ;
}

