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

package projects.merge;

import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.solver.BindingNodeId ;
import com.hp.hpl.jena.tdb.solver.SolverLib ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

public class Main
{
    public static void main(String ... argv)
    {
        // TODO
        //   tests
        //   "Explain" functionality.
        
        // Setup
        Log.setLog4j() ;
        // Fake the dataset.
        
        DatasetGraphTDB dsg = StoreConnection.make(Location.mem()).getBaseDataset() ;

        // -- Fix up
        Location loc = Location.mem() ;
        TupleIndex POS = SetupTDB.makeTupleIndex(loc, "SPO", "POS", "POS", 3*NodeId.SIZE) ;
        TupleIndex PSO = SetupTDB.makeTupleIndex(loc, "SPO", "PSO", "PSO", 3*NodeId.SIZE) ;
        TupleIndex[] indexes = dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;

        //indexes[0] = SetupTDB.makeTupleIndex(loc, "SPO", "POS", "POS", 3*NodeId.SIZE) ;
        indexes[1] = POS ;
        indexes[2] = PSO ;
        NodeTable nodeTable = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;

        
        //System.out.println("== Data") ;
        // -- Data 
        String $ = StrUtils.strjoinNL(
            "(graph",                                      
            "  (<s>   <p>  '1')",
            "  (<s1>  <p>  '3')",
            "  (<s1>  <p>  '4')",
            "  (<s>   <q>  '5')",
            "  (<s1>  <q>  '6')" ,
            "  (<s>   <p>  '6')" ,
            ")" 
            ) ;
        Graph g = SSE.parseGraph($) ;
        dsg.getDefaultGraph().getBulkUpdateHandler().add(g) ;
        // This is the triple access
        
        if ( false )
        {
            Triple triple1 = SSE.parseTriple("(?s <p> ?o)") ;
            Triple triple2 = SSE.parseTriple("(?s <q> ?v)") ;
            Iterator<BindingNodeId> iter1 = OpExecutorMerge.mergeJoin(triple1, triple2, nodeTable, indexes) ;
            Iterator<Binding> iter2 = SolverLib.convertToNodes(iter1, nodeTable) ;
            QueryIterator qIter = new QueryIterPlainWrapper(iter2) ;
        }

        // -- Execute
        ExecutionContext execCxt = new ExecutionContext(TDB.getContext(),
                                                        dsg.getDefaultGraph(),
                                                        dsg,
                                                        OpExecutorMerge.factory
                                                        ) ;
        //Op op = SSE.parseOp("(bgp (?s <p> ?o) (?s <q> ?v))") ;
        Op op = SSE.parseOp("(bgp (?s1 <p> ?o) (<s1> <q> ?o))") ;
        
        QueryIterator qIter = QC.execute(op, QueryIterRoot.create(execCxt), execCxt) ;
        
        // -- Results.
        List<String> varNames = Var.varNames(OpVars.patternVars(op)) ; 
        ResultSet rs = ResultSetFactory.create(qIter, varNames) ;
        ResultSetFormatter.out(rs) ;

        System.out.println("DONE") ;
    }
}
