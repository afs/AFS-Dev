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

import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.logging.Log ;
import projects.tools.IndexLib ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.solver.BindingNodeId ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

public class Main
{
    public static void main(String ... argv)
    {
        // TODO
        //   Merge - read first index, jump to start of second.
        //      ?x :p ?v . ?x :q ?w . 
        //      Can start :q scan P-- from P(firstS)
        //   Tests
        //   repackage - physical operators.
        //   "Explain" functionality.
        
        // Setup
        Log.setLog4j() ;
        
        Location loc = new Location("DB") ;
        DatasetGraphTDB dsg = SetupTDB.buildDataset(loc) ;
        
        
        TupleIndex[] indexes = dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;
        TupleIndex indexPSO = IndexLib.connect(loc, Names.primaryIndexTriples, "PSO") ;
        // Stamp on OSP
        indexes[2] = indexPSO ; 
        // Wire in.
        QC.setFactory(ARQ.getContext(), OpExecutorMerge.factory) ;
        
        Query query = QueryFactory.read("Q.rq") ;
        Op op = Algebra.compile(query) ;
        //op = Algebra.toQuadForm(op) ;
        //op = Algebra.optimize(op) ;
        System.out.println(op) ;
        
        QueryExecUtils.execute(op, dsg) ;

        if ( false )
        {
            QueryIterator qIter = Algebra.exec(op, dsg) ;
            for ( ; qIter.hasNext() ; )
            {
                Binding b = qIter.next() ;
                System.out.println(b) ;
            }
        }
        
        System.out.println("DONE") ;
        System.exit(0) ;
        
        hashJoin() ; System.exit(0) ;
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
