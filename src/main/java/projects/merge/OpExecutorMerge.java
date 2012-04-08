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

import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.solver.BindingNodeId ;
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

    // (real) default graph only for now.
    public Iterator<BindingNodeId> execute(OpBGP opBGP, DatasetGraphTDB dsg)
    {
        BasicPattern bgp = opBGP.getPattern() ;
        List<Triple> triples = bgp.getList() ;
        
        if (triples.size() == 0 )
        {}
        if (triples.size() == 1 )
        {}
        //if ( constant )
        
        
        
        NodeTable nodeTable = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
        Triple triple1 = triples.get(0) ;
        Triple triple2 = triples.get(1) ;
        TupleIndex[] indexes = dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;
        MergeActionIdxIdx action = MergeLib.calcMergeAction(triple1, triple2, indexes) ;
        if ( action == null )
        {}
       
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
            if ( action2 == null )
            {}
        }
        return null ;
    }
    
    private static Iterator<BindingNodeId> exec(MergeActionIdxIdx action, Triple triple1, Triple triple2)
    {
        // Corresponds to triple1.
        IndexAccess access1 = action.getIndexAccess1() ;
        TupleIndex index1 = access1.getIndex() ;
        int len1 = access1.getPrefixLen() ;
        // Calc 
        
        
        // from len1 prefix, to same+1
        
        IndexAccess access2 = action.getIndexAccess2() ;
        TupleIndex index2 = access2.getIndex() ;
        int len2 = access1.getPrefixLen() ;
        
        
        Var var1 = access1.getVar() ;
        Var var2 = access2.getVar() ;
        // var1 == var2
        Var var = var1 ;
        

        
        
        
        return null ;
    }
    
    private static Iterator<BindingNodeId> exec(MergeActionVarIdx action, Iterator<BindingNodeId> iterator, Triple triple)
    {
        // Corresponds to triple1.
        IndexAccess access = action.getIndexAccess() ;
        TupleIndex index = access.getIndex() ;
        int len = access.getPrefixLen() ;
        Var var = access.getVar() ;
      
        return null ;
    }
    
    
    private static Iterator<Tuple<NodeId>> exec(TupleIndex index, int len, Triple triple, NodeTable nodeTable)
    {
        
        
        
        
        return null ;
    }
    
    static Tuple<NodeId> convert(NodeTable nodeTable, Triple triple)
    {
        return convert(nodeTable, triple.getSubject(), triple.getPredicate(), triple.getObject()) ; 
    }
    
    // XXX TupleLib.
    static Tuple<NodeId> convert(NodeTable nodeTable, Node...nodes)
    {
        NodeId n[] = new NodeId[nodes.length] ;
        for (int i = 0; i < nodes.length; i++)
        {
            NodeId id = idForNode(nodeTable, nodes[i]) ;
            if (NodeId.isDoesNotExist(id)) return null ;
            n[i] = id ;
        }
        return Tuple.create(n) ;
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
