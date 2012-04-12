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

package projects.join2;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.lib.Tuple ;
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
        //   Tests
        //   repackage - physical operators.
        //   "Explain" functionality.
        
        // Setup
        Log.setLog4j() ;
        
        hashJoin() ; System.exit(0) ;

        
        if ( false )
        {
            List<BindingNodeId> x1 = Support.parseTableNodeId("(table",
                                                              "  (row (?a 1) (?b 1))", 
                                                              "  (row (?a 1) (?b 2))", 
                                                              ")"
                ) ;
            List<BindingNodeId> x2 = Support.parseTableNodeId("(table",
                                                              "  (row (?a 1) (?c 4))", 
                                                              "  (row (?a 4) (?c 1))", 
                                                              ")"
                ) ;

            Var key = Var.alloc("a") ;
            List<BindingNodeId> r = Iter.toList(AccessOps.mergeJoin(x1.iterator(), x2.iterator(), key)) ;

            System.out.println() ;
            for ( BindingNodeId b : r )
                System.out.println(b) ;
            System.out.println() ;
            System.out.println("DONE") ;
            System.exit(0) ;
        }
        
        // Fake the dataset.
        
        DatasetGraphTDB dsg = StoreConnection.make(Location.mem()).getBaseDataset() ;
        // -- Fix up
        Location loc = Location.mem() ;
        TupleIndex PSO = SetupTDB.makeTupleIndex(loc, "SPO", "PSO", "PSO", 3*NodeId.SIZE) ;
        TupleIndex[] indexes = dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;
        
        TupleIndex SPO = indexes[0] ;
        TupleIndex POS = indexes[1] ;
        TupleIndex OSP = indexes[2] ;
        
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
            "  (<s>   <p>  <s>)" ,
            ")" 
            ) ;
        Graph g = SSE.parseGraph($) ;
        dsg.getDefaultGraph().getBulkUpdateHandler().add(g) ;

        if ( false )
        {
            // Single index access
            TupleIndex[] indexes1 = { PSO } ; 
            Triple triple = SSE.parseTriple("(?x <p> ?x)") ;   //  SPO/?p => no action found. OSP/?x -> wrong.
            
            Tuple<Slot> triple1 = MergeLib.convert(triple, nodeTable) ;
            MergeActionVarIdx action = MergeLib.calcMergeAction(Var.alloc("x"), triple1, indexes1) ;
            
            System.out.println(action) ;
            TupleIndex index = action.getIndexAccess().getIndex() ;
            
            Tuple<NodeId> tuple = OpExecutorMerge.nodeIds(triple1) ;

            Iterator<Tuple<NodeId>> iter = index.find(tuple) ;
            List<BindingNodeId> r = new ArrayList<>() ;
            
            for ( ; iter.hasNext() ; )
            {
                Tuple<NodeId> row = iter.next() ;
                // Library ise.
                BindingNodeId b = new BindingNodeId((Binding)null) ;
                b = OpExecutorMerge.bind(b, null, row, triple1) ;
                if ( b != null )
                    r.add(b) ;
            }
            Iterator<Binding> iter2 = SolverLib.convertToNodes(r.iterator(), nodeTable) ;
            QueryIterator qIter = new QueryIterPlainWrapper(iter2) ;
            List<String> varNames = Arrays.asList("x") ;
            ResultSet rs = ResultSetFactory.create(qIter, varNames) ;
            ResultSetFormatter.out(rs) ;
            System.out.println("DONE") ;
            System.exit(0) ;
        }

        // -- Execute
        ExecutionContext execCxt = new ExecutionContext(TDB.getContext(),
                                                        dsg.getDefaultGraph(),
                                                        dsg,
                                                        OpExecutorMerge.factory
                                                        ) ;
        Op op = SSE.parseOp("(bgp (?s <p> ?o) (?s <q> ?v))") ;
        //Op op = SSE.parseOp("(bgp (?s1 <p> ?o) (<s1> <q> ?o))") ;
        //Op op = SSE.parseOp("(bgp (<s> <p> ?o))") ;
        
        QueryIterator qIter = QC.execute(op, QueryIterRoot.create(execCxt), execCxt) ;
        
        // -- Results.
        List<String> varNames = Var.varNames(OpVars.patternVars(op)) ; 
        ResultSet rs = ResultSetFactory.create(qIter, varNames) ;
        ResultSetFormatter.out(rs) ;

        System.out.println("DONE") ;
    }
    
    static boolean PRINT = true ;

    static public void hashJoin()
    {
        Var key = Var.alloc("a") ;
        
        //hashJoin(iter1, iter2, key) ;
        
        List<BindingNodeId> x1 = Support.parseTableNodeId("(table",
                                                          "  (row (?a 1) (?b 1))", 
                                                          "  (row (?a 1) (?b 2))", 
                                                          ")"
            ) ;
        List<BindingNodeId> x2 = Support.parseTableNodeId("(table",
                                                          "  (row (?a 1) (?c 4))", 
                                                          "  (row (?a 4) (?c 1))", 
                                                          ")"
            ) ;
        
        Iterator<BindingNodeId> iter1 = x1.iterator() ;
        Iterator<BindingNodeId> iter2 = x2.iterator() ;
        
        
        
        if ( PRINT )
        {
            List<BindingNodeId> x = Iter.toList(iter1) ;
            System.out.println("-- Left:") ;
            Iter.print(x.iterator()) ;
            iter1 = x.iterator() ;
        }
        
        if ( PRINT )
        {
            List<BindingNodeId> x = Iter.toList(iter2) ;
            System.out.println("-- Right:") ;
            Iter.print(x.iterator()) ;
            iter2 = x.iterator() ;
        }
        
        if ( PRINT )
            System.out.println("--") ;
        
        {
            Iterator<BindingNodeId> r = AccessOps.hashJoin(iter1, iter2, key) ;
            for ( ; r.hasNext() ; )
            {
                BindingNodeId b = r.next() ;
                System.out.println(b) ;    
            }
        }
        System.out.println() ;
        Iterator<BindingNodeId> r2 = AccessOps.hashJoin(x2.iterator(), x1.iterator(), key) ;
        for ( ; r2.hasNext() ; )
        {
            BindingNodeId b = r2.next() ;
            System.out.println(b) ;    
        }

    }
}
