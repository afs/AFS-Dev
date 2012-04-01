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
import java.util.List ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.solver.BindingNodeId ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

public class OpExecutorMerge
{
    // (real) default graph only for now.
    public Iterator<BindingNodeId> execute(OpBGP opBGP, DatasetGraphTDB dsg)
    {
        BasicPattern bgp = opBGP.getPattern() ;
        List<Triple> triples = bgp.getList() ;
        
        if (triples.size() == 0 )
        {}
        if (triples.size() == 1 )
        {}
        Triple triple1 = triples.get(0) ;
        Triple triple2 = triples.get(1) ;
        
        TupleIndex[] indexes = dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;
        
        MergeActionIdxIdx action = MergeLib.calcMergeAction(triple1, triple2, indexes) ;
       
        // perform action in NodeId space, get iterator of Bindings.
        
        Iterator<BindingNodeId> chain = null ; 
        // which comes out in sorted var order.
        
        Var v = action.getVar() ;
        for ( int i = 2 ; i < triples.size() ; )
        {
            // Next triples.
            Triple triple = triples.get(i) ;
            // Is it joined by a common variable?
            MergeActionVarIdx action2 = MergeLib.calcMergeAction(v, triple, indexes) ;
        }
        return null ;
    }
}
