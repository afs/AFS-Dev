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

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.InternalErrorException ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.solver.BindingNodeId ;
import com.hp.hpl.jena.tdb.solver.SolverLib ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class OpExecutorMerge extends OpExecutor
{
    static OpExecutorFactory factory = new OpExecutorFactory() {
        @Override
        public OpExecutor create(ExecutionContext execCxt)
        {
            return new OpExecutorMerge(execCxt) ;
        }} ;
    
    public OpExecutorMerge(ExecutionContext execCxt)
    {
        super(execCxt) ;
    }

    private static final boolean PRINT = false ; 
    
    @Override
    protected QueryIterator execute(OpBGP opBGP, QueryIterator input)
    {
        BasicPattern bgp = opBGP.getPattern() ;
        List<Triple> triples = bgp.getList() ;
        
        if (triples.size() == 0 )
            return input ; 
            
        DatasetGraphTDB dsg = (DatasetGraphTDB)(execCxt.getDataset()) ;
        NodeTable nodeTable = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
        
        if (triples.size() == 1 )
        {
            // Convoluted?
            Triple triple = triples.get(0) ;
            final Tuple<Slot> tuple = MergeLib.convert(triple, nodeTable) ;
            Iterator<BindingNodeId> iter1 = access(tuple, dsg.getTripleTable().getNodeTupleTable()) ;
            Iterator<Binding> iter2 = SolverLib.convertToNodes(iter1, nodeTable) ;
            return new QueryIterPlainWrapper(iter2, execCxt) ;
        }
        
        TupleIndex[] indexes = dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;

        Triple triple1 = triples.get(0) ;
        Triple triple2 = triples.get(1) ;

        Tuple<Slot> tuple1 = MergeLib.convert(triple1, nodeTable) ;
        Tuple<Slot> tuple2 = MergeLib.convert(triple2, nodeTable) ;
        
        Iterator<BindingNodeId> iter1 = mergeJoin(tuple1, tuple2, indexes) ;
        
        // More triples.
        
        Iterator<Binding> iter2 = SolverLib.convertToNodes(iter1, nodeTable) ;
        return new QueryIterPlainWrapper(iter2, execCxt) ;
    }
    
    /** Access a NodeTupleTable, get back a stream of BindingNodeId */
    static Iterator<BindingNodeId> access(final Tuple<Slot> tuple, NodeTupleTable ntt)
    {
        if ( ntt.getTupleTable().getTupleLen() != tuple.size() )
            throw new InternalErrorException("Not aligned: "+tuple+" expected="+ntt.getTupleTable().getTupleLen()) ;
        
        Tuple<NodeId> tupleAccess = nodeIds(tuple) ;
        Iterator<Tuple<NodeId>> iter1 = ntt.find(tupleAccess) ;
        Iterator<BindingNodeId> iter2 = Iter.map(iter1, new Transform<Tuple<NodeId>, BindingNodeId>(){
            @Override
            public BindingNodeId convert(Tuple<NodeId> item)
            {
                return AccessOps.bind(item, tuple) ; 
            }}) ;
        return iter2 ; 
    }
    
    static Iterator<BindingNodeId> mergeJoin(Tuple<Slot> triple1, Tuple<Slot> triple2, TupleIndex[] indexes)
    {
        MergeActionIdxIdx action = MergeLib.calcMergeAction(triple1, triple2, indexes) ;
        Iterator<BindingNodeId> iter1 = merge(action, triple1, triple2) ;
        return iter1 ; 
    }

    private static Iterator<BindingNodeId> merge(MergeActionIdxIdx action, Tuple<Slot> triple1, Tuple<Slot> triple2)
    {
        int len1 = action.getIndexAccess1().getPrefixLen() ;
        int len2 = action.getIndexAccess2().getPrefixLen() ;
        
        TupleIndex tupleIndex1 = action.getIndexAccess1().getIndex() ;
        TupleIndex tupleIndex2 = action.getIndexAccess2().getIndex() ;
        
        Tuple<NodeId> tuple1 = nodeIds(triple1) ;
        Tuple<NodeId> tuple2 = nodeIds(triple2) ;
        
        Iterator<Tuple<NodeId>> iter1 = tupleIndex1.find(tuple1) ;
        if ( PRINT )
        {
            System.out.println("-- Left:") ;
            System.out.println(triple1) ;
            Iter.print(iter1) ;
            iter1 = tupleIndex1.find(tuple1) ;
        }
        
        Iterator<Tuple<NodeId>> iter2 = tupleIndex2.find(tuple2) ;
        if ( PRINT )
        {
            System.out.println("-- Right:") ;
            System.out.println(triple2) ;
            Iter.print(iter2) ;
            iter2 = tupleIndex2.find(tuple2) ;
            System.out.println("----") ;
        }
        
        Tuple<NodeId> row1 = null ;
        Tuple<NodeId> row2 = null ;
        
        List<BindingNodeId> results = new ArrayList<>() ;
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
            
            if ( v1 > v2 )
            {
                row2 = null ;
                continue ;
            }
            if ( v1 < v2 )
            {
                row1 = null ;
                continue ;
            }
            
            // if ( v1 == v2 )
            long v = v1 ;
            row1 = advance(v, tupleIndex1, len1, iter1, tmp1, row1) ;
            row2 = advance(v, tupleIndex2, len2, iter2, tmp2, row2) ;
            join(results, action.getVar(), triple1, tmp1, triple2, tmp2) ;
        }
        return results.iterator() ;
    }

    static Tuple<NodeId> nodeIds(Tuple<Slot> slots)
    {
        int N = slots.size() ;
        NodeId n[] = new NodeId[N] ;
        for ( int i = 0 ;  i < N ; i++ )
        {
            n[i] = slots.get(i).id ;
        }
        return Tuple.create(n) ;
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

    private static void join(List<BindingNodeId> results, Var joinVar, 
                             Tuple<Slot> vars1 , List<Tuple<NodeId>> tmp1, 
                             Tuple<Slot> vars2 , List<Tuple<NodeId>> tmp2)
    {
        if ( PRINT )
            System.out.println("join left="+tmp1.size()+" right="+tmp2.size()) ;
        for ( Tuple<NodeId> row1 : tmp1 )
            for ( Tuple<NodeId> row2 : tmp2 )
            {
                if ( PRINT )
                    System.out.println("Join: "+row1+" "+row2) ;
                BindingNodeId b = new BindingNodeId((Binding)null) ;
                b = bind(b, joinVar, row1, vars1) ;
                if ( b == null )
                    continue ;
                b = bind(b, joinVar, row2, vars2) ;
                if ( b == null )
                    continue ;
                if ( PRINT )
                    System.out.println("Bind => "+b) ;
                results.add(b) ;
            }
        tmp1.clear() ;
        tmp2.clear() ;
    }

    static BindingNodeId bind(BindingNodeId b, Var joinVar, Tuple<NodeId> row, Tuple<Slot> vars)
    {
        // Tuples from indexes are in natural order.
        if ( PRINT ) 
            System.out.println("Bind: "+vars+" "+row) ;
    
        for ( int i = 0 ; i < vars.size() ; i++ )
        {
            Slot slot = vars.get(i) ; 
            if ( ! slot.isVar() )
                continue ;
            NodeId id = row.get(i) ;
            NodeId id2 = b.get(slot.var) ;
            if ( id2 != null )
            {
                // already bound ... test compatibility.
                if ( id2.getId() == id.getId() )
                    continue ;
                return null ;
            }
            b.put(slot.var, id) ;
        }
        return b ;
    }
    
    // See NodeTupleTableConcrete - common code?
    // See NodeTupleTableConcrete.findAsNodeIds
    
    static protected final NodeId idForNode(NodeTable nodeTable, Node node)
    {
        if (node == null || node == Node.ANY) return NodeId.NodeIdAny ;
        //if (node.isVariable()) throw new TDBException("Can't pass variables to NodeTupleTable.find*") ;
        if (node.isVariable()) return NodeId.NodeIdAny ;
        return nodeTable.getNodeIdForNode(node) ;
    }

    public static Tuple<Var> vars(Triple triple)
    {
        Var[] v = new Var[3] ;
        v[0] = var(triple.getSubject()) ;
        v[1] = var(triple.getPredicate()) ;
        v[2] = var(triple.getObject()) ;
        return Tuple.create(v) ;
    }

    private static Var var(Node node)
    {
        return Var.isVar(node) ? Var.alloc(node) : null ;
    }
    
}
