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

package projects.merge;

import java.util.Iterator ;

import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.solver.BindingNodeId ;

/** Access operators - algebra operations on the TDB storage objects liek indexes and node tables.
 */
public class AccessOps
{
    // Need to indicate the sortedness coming out.
    public static Iterator<BindingNodeId> mergeJoin(Tuple<Slot> triple1, Tuple<Slot> triple2, TupleIndex[] indexes)
    {
        return null ;
    }
    
    // Should these be "actions"?
    // Should actions include mapping to variables?
    
    // Need to indicate the join key.
    public static Iterator<BindingNodeId> hashJoin(Tuple<Slot> triple1, Tuple<Slot> triple2, TupleIndex[] indexes)
    {
        return null ;
    }

    // Need to indicate the join key.
    public static Iterator<BindingNodeId> hashJoin(Iterator<BindingNodeId> incoming, Tuple<Slot> triple2, TupleIndex[] indexes)
    {
        return null ;
    }

    // Need to indicate the join key.
    public static Iterator<BindingNodeId> indexJoin(Iterator<BindingNodeId> incoming, Tuple<Slot> triple2, TupleIndex[] indexes)
    {
        return null ;
    }
}

