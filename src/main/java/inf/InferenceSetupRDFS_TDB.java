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

package inf ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;

/*8 RDFS setup in NodeId space */
public class InferenceSetupRDFS_TDB extends BaseInfSetupRDFS<NodeId>{
    private final DatasetGraphTDB dsgtdb ;
    private final NodeTable nodetable ;

    public InferenceSetupRDFS_TDB(Graph vocab, DatasetGraphTDB dsgtdb) {
        this(vocab, dsgtdb, false) ;
    }

    public InferenceSetupRDFS_TDB(Graph vocab, DatasetGraphTDB dsgtdb, boolean incDerivedDataRDFS) {
        super(vocab, incDerivedDataRDFS) ;
        this.dsgtdb = dsgtdb ;
        this.nodetable = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable() ;
    }
    public InferenceSetupRDFS_TDB(Model vocab, DatasetGraphTDB dsgtdb) {
        this(vocab, dsgtdb, false) ;
    }
    
    public InferenceSetupRDFS_TDB(Model vocab, DatasetGraphTDB dsgtdb, boolean incDerivedDataRDFS) {
       super(vocab, incDerivedDataRDFS) ;
       this.dsgtdb = dsgtdb ;
       this.nodetable = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable() ;
    }

    @Override
    protected NodeId fromNode(Node node) {
        NodeId n = nodetable.getAllocateNodeId(node) ;
        return n ;
    }
}