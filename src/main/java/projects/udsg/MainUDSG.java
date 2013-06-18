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

package projects.udsg;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.GraphView ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class MainUDSG
{
    /* In OpExecutor?
     * or in datasetgraph.find?
     * Quadify or not quadify?
     * ==> BGP in OpExecutor, check active graph. 
     * 
     */
    
    public static void main(String[] args) {
        //DatasetGraph dsg = TDBFactory.createDatasetGraph() ; // DatasetGraphFactory.createMem() ;
        Dataset ds = DatasetFactory.createMem() ;
        DatasetGraph dsg = ds.asDatasetGraph() ;
        Quad q = SSE.parseQuad("(<g> <s> <p> <o>)") ;
        dsg.add(q) ;
        
        if (true) {
            Graph g = GraphView.createNamedGraph(dsg, Quad.unionGraph) ;
            Model m = ModelFactory.createModelForGraph(g) ;
            ds.setDefaultModel(m) ;
        }        
        //String qs = "SELECT * { GRAPH <"+Quad.unionGraph+"> { ?s ?p ?o } }" ;
        String qs = "SELECT * { ?s ?p ?o }" ;
        Query query = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSet rs = qExec.execSelect() ;
        ResultSetFormatter.out(rs) ;
    }
}
