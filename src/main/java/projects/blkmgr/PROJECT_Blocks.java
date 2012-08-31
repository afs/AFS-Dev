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

public class PROJECT_Blocks
{
    /*
     * Adds:
     *   Free chain management on-disk
     *   Multiple block managers per file
     *      Moveable Root blocks.
     *   
     * Other
     *   Virtualization / indirection. (use case?)
     *   Variable size blocks.
     *   Compression (to one of a few fixed sizes?)
     */
    
    /* Decision 1: 
     * Global number of blocks or pre index+indirection
     *    => global
     *    
     * BlockAllocator
     *   Manages the blocks for a single file.
     *   Multiple roots.
     *   Persistent free chain.
     * Add to BlockMgr:
     *   Get root.
     */
}

