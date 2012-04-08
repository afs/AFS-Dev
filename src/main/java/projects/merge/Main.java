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

import static projects.merge.ColNames.O ;
import static projects.merge.ColNames.P ;
import static projects.merge.ColNames.S ;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.Tuple ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.lib.TupleLib ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

public class Main
{
    public static void main(String ... argv)
    {
        // Setup
        Log.setLog4j() ;
        //ColumnMap colMap = new ColumnMap("SPO", "POS") ;
        
        if ( false )
        {
            ColumnMap colMap = new ColumnMap("POS", Arrays.asList(S,P,O), Arrays.asList(P,O,S)) ;
            Tuple<ColNames> primary = Tuple.create(S,P,O) ;
            System.out.println(colMap.map(primary)) ;
            System.exit(0) ;
        }
        
        // Fake the dataset.
        
        //BasicPattern bgp = SSE.parseBGP("((?s :p ?o) ( ?s :q ?z))") ;
        Op op = SSE.parseOp("(bgp (?s :p ?o) ( ?s :q ?z))") ;
        
        DatasetGraphTDB dsg = StoreConnection.make(Location.mem()).getBaseDataset() ;

        // Fix up
        Location loc = Location.mem() ;
        TupleIndex POS = SetupTDB.makeTupleIndex(loc, "SPO", "POS", "POS", 3*NodeId.SIZE) ;
        TupleIndex PSO = SetupTDB.makeTupleIndex(loc, "SPO", "PSO", "PSO", 3*NodeId.SIZE) ;
        TupleIndex[] indexes = dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;

        //indexes[0] = SetupTDB.makeTupleIndex(loc, "SPO", "POS", "POS", 3*NodeId.SIZE) ;
        indexes[1] = POS ;
        indexes[2] = PSO ;
        NodeTable nodeTable = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;

        System.out.println("== Data") ;
        List<String> $ = Arrays.asList(
            "(<s>   <p>  '1')",
            "(<s1>  <p>  '3')",
            //"(<s1>  <p>  '4')",
            "(<s>   <q>  '5')",
            "(<s1>  <q>  '6')" ,
            "(<s>   <p>  '2')"
            ) ;
        for ( String s : $ )
            dsg.getDefaultGraph().add(SSE.parseTriple(s)) ;
        //System.out.println(dsg) ;
        
        //Iter.print(nodeTable.all()) ;
        //System.out.println(dsg) ;
        //System.exit(0) ;

        if ( false )
        {
            System.out.println("== Access 1") ;
            // This is the triple access
            Triple triple = SSE.parseTriple("(?s <p> ?o)") ;
            
            // Convert triple to NodeIds or NodeId.ANY.
            Tuple<NodeId> tuple = OpExecutorMerge.convert(nodeTable, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
            //System.out.println(tuple) ;
            
            // action.
            MergeActionVarIdx action = MergeLib.calcMergeAction(Var.alloc("s"), triple, indexes) ;
            System.out.println(action) ;
            
            TupleIndex tupleIndex = action.getIndexAccess().getIndex() ;
            
            // access the index for a ann with this prefix.
            // (It will do a partial scan if a trailing constant is set).
            Iterator<Tuple<NodeId>> iter = tupleIndex.find(tuple) ;
            List<Tuple<NodeId>> x = Iter.toList(iter) ;
            System.out.println( Iter.asString(x.iterator(), "\n" ) ) ;
            
            Iterator<Tuple<Node>> iter2 = TupleLib.convertToNodes(nodeTable, x.iterator()) ;
            
            System.out.println( Iter.asString(iter2, "\n" ) ) ;
        }
        
        System.out.println("== Access 2") ;
        // This is the triple access
        Triple triple1 = SSE.parseTriple("(?s <p> ?o)") ;
        Triple triple2 = SSE.parseTriple("(?s <q> ?v)") ;
        Tuple<NodeId> tuple1 = OpExecutorMerge.convert(nodeTable, triple1) ;
        Tuple<NodeId> tuple2 = OpExecutorMerge.convert(nodeTable, triple2) ;
        Tuple<Var> vars1 =  OpExecutorMerge.vars(triple1) ;
        Tuple<Var> vars2 =  OpExecutorMerge.vars(triple2) ;
        
        MergeActionIdxIdx action = MergeLib.calcMergeAction(triple1, triple2, indexes) ;
        Iterator<Binding> iter = merge(action, tuple1, vars1, tuple2, vars2) ;
        
        
       
        
        
//        ExecutionContext eCxt = new ExecutionContext(TDB.getContext(), dsg.getDefaultGraph(), dsg, OpExecutorMerge.factory) ;
//
//        OpExecutor opExec = OpExecutorMerge.factory.create(eCxt) ;
//        opExec.executeOp(op, QueryIterRoot.create(eCxt)) ;
        
        System.out.println("DONE") ;
 
    }

    private static Iterator<Binding> merge(MergeActionIdxIdx action, 
                                           Tuple<NodeId> tuple1, Tuple<Var> vars1,
                                           Tuple<NodeId> tuple2, Tuple<Var> vars2)
    {
        int len1 = action.getIndexAccess1().getPrefixLen() ;
        int len2 = action.getIndexAccess2().getPrefixLen() ;
            
        TupleIndex tupleIndex1 = action.getIndexAccess1().getIndex() ;
        TupleIndex tupleIndex2 = action.getIndexAccess2().getIndex() ;
        
        Iterator<Tuple<NodeId>> iter1 = tupleIndex1.find(tuple1) ;
        System.out.println("-- Left:") ;
        Iter.print(iter1) ;
        iter1 = tupleIndex1.find(tuple1) ;
        
        Iterator<Tuple<NodeId>> iter2 = tupleIndex2.find(tuple2) ;
        System.out.println("-- Right:") ;
        Iter.print(iter2) ;
        iter2 = tupleIndex2.find(tuple2) ;
        System.out.println("----") ;
        Tuple<NodeId> row1 = null ;
        Tuple<NodeId> row2 = null ;
        
        List<Binding> results = new ArrayList<>() ;
        List<Tuple<NodeId>> tmp1 = new ArrayList<>() ;
        List<Tuple<NodeId>> tmp2 = new ArrayList<>() ;
        
        for(;;)
        {
            if ( row1 == null )
            {
                if ( ! iter1.hasNext() )
                    break ;
                row1 = iter1.next() ;
            }
            if ( row2 == null )
            {
                if ( ! iter2.hasNext() )
                    break ;
                row2 = iter2.next() ;
            }
            
            NodeId join1 = tupleIndex1.getColumnMap().fetchSlot(len1, row1) ;
            NodeId join2 = tupleIndex2.getColumnMap().fetchSlot(len2, row2) ;
            
            long v1 = join1.getId() ;
            long v2 = join2.getId() ;
            
            if ( v1 == v2 )
            {
                long v = v1 ;
                row1 = advance(v, tupleIndex1, len1, iter1, tmp1, row1) ;
                row2 = advance(v, tupleIndex2, len2, iter2, tmp2, row2) ;
                join(results, vars1, tmp1, vars2, tmp2) ;
            }
            else if ( v1 > v2 )
            {
                row2 = null ;
            }
            else
            {
                // v1 < v2
                row1 = null ;
            }
        }
        return results.iterator() ;
    }

    private static Tuple<NodeId> advance(long v, TupleIndex tupleIndex , int len , Iterator<Tuple<NodeId>> iter, List<Tuple<NodeId>> acc, Tuple<NodeId> row)
    {
        for (;;)
        {
            // Overshoot :-(
            long v1 = tupleIndex.getColumnMap().fetchSlot(len, row).getId() ;
            if ( v != v1 )
                break ;
            acc.add(row) ;
            if ( ! iter.hasNext() )
                return null ;
            row = iter.next() ;
        }
        return row ;
    }

    private static void join(List<Binding> results, Tuple<Var> vars1 , List<Tuple<NodeId>> tmp1, Tuple<Var> vars2 , List<Tuple<NodeId>> tmp2)
    {
        System.out.println("join left="+tmp1.size()+" right="+tmp2.size()) ;
        for ( Tuple<NodeId> row1 : tmp1 )
            for ( Tuple<NodeId> row2 : tmp2 )
            {
                System.out.println("Join: "+row1+" "+row2) ;
            }
        tmp1.clear() ;
        tmp2.clear() ;
    }



}
