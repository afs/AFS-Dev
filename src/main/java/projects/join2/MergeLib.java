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

package projects.join2;

import org.apache.jena.atlas.lib.Tuple ;
import projects.join2.access.IndexAccess ;
import projects.join2.access.Slot ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class MergeLib
{

    public static MergeActionIdxIdx calcMergeAction(Tuple<Slot> triple1, Tuple<Slot> triple2, TupleIndex[] indexes)
    {
        IndexAccess[] access1 = access(triple1, indexes) ;
        IndexAccess[] access2 = access(triple2, indexes) ;
            
//        System.out.println(Arrays.asList(access1)) ;
//        System.out.println(Arrays.asList(access2)) ;
        // Special case? access1.length=1, access2.length=1

        MergeActionIdxIdx action = null ;
        for ( IndexAccess a1 : access1 )
        {
            if ( a1 == null ) continue ;
            for ( IndexAccess a2 : access2 )
            {
                if ( a2 == null ) continue ;
                //System.out.println("Consider: "+a1+" // "+a2) ;
                if ( a1.getVar().equals(a2.getVar()))
                {
                    MergeActionIdxIdx action2 = new MergeActionIdxIdx(a1,a2) ;
                    // Longest prefixes.
                    if ( action == null )
                        action = action2 ;
                    else
                    {
                        //System.out.println("Choose: "+action+" // "+action2) ;
                        // Choose one with most prefixing.
                        int len1 = action.getPrefixCount() ;
                        int len2 = action2.getPrefixCount() ;
                        if ( len2 == len1 )
                        {
                            // Example: same var uses more than once in a triple.
                            if ( action2.getIndexAccess1().getIndex() == action2.getIndexAccess2().getIndex() )
                                // Prefer same index.
                                // Better - special action.
                                action = action2 ;
                        }
                        else if ( len2 > len1 )
                            action = action2 ;
                        // else do nothing.
                    }
                }
            }
        }
        return action ;
    }
    
    public static MergeActionVarIdx calcMergeAction(Var var, Tuple<Slot> triple, TupleIndex[] indexes)
    {
        IndexAccess[] access = access(triple, indexes) ;
        MergeActionVarIdx iacc = null ;
        int len = -1 ;
        
        for ( IndexAccess a : access )
        {
            if ( len < a.getPrefixLen() )
            {
                // Find first longest IndexAccess
                if ( var.equals(a.getVar()) )
                {
                    iacc = new MergeActionVarIdx(var, a) ;
                    len = a.getPrefixLen() ;
                }
            }
        }
        return iacc ;
    }

    private static IndexAccess[] access(Tuple<Slot> triple, TupleIndex[] indexes)
    {
        IndexAccess[] accesses = new IndexAccess[indexes.length] ;
        int i = 0 ;
        for ( TupleIndex idx : indexes )
        {
            IndexAccess a = access(triple, idx) ;
            accesses[i++] = a ;
        }
        
        return accesses ;
    }

    private static IndexAccess access(Tuple<Slot> triple, TupleIndex idx)
    {
        Tuple<Slot> t = idx.getColumnMap().map(triple) ;
        for ( int i = 0 ; i < triple.size() ; i++ )
        {
            Slot n = t.get(i) ;
            if ( n.isVar() )
                return new IndexAccess(idx, i, n.var) ;
        }
        return null ;
    }
//
//    private static Tuple<Node> tripleAsTuple(Triple triple)
//    {
//        return Tuple.create(triple.getSubject(),
//                            triple.getPredicate(),
//                            triple.getObject()) ;
//    }

    static Tuple<Slot> convert(Triple triple, NodeTable nodeTable)
    {
        return convert(triple, nodeTable, false) ;
    }
    
    static Tuple<Slot> convert(Triple triple, NodeTable nodeTable, boolean allocate)
    {
        Slot[] slots = new Slot[3] ;
        slots[0] = convert(triple.getSubject(), nodeTable, allocate) ;
        if ( slots[0].id == NodeId.NodeDoesNotExist )
            return null ;
        slots[1] = convert(triple.getPredicate(), nodeTable, allocate) ;
        if ( slots[1].id == NodeId.NodeDoesNotExist )
            return null ;
        slots[2] = convert(triple.getObject(), nodeTable, allocate) ;
        if ( slots[2].id ==  NodeId.NodeDoesNotExist )
            return null ;
        return Tuple.create(slots) ;
    }

    static Slot convert(Node node, NodeTable nodeTable, boolean allocate)
    {
        if ( Var.isVar(node) )
            return new Slot(Var.alloc(node)) ;
        if ( allocate )
            return new Slot(nodeTable.getAllocateNodeId(node)) ;
        else
            return new Slot(nodeTable.getNodeIdForNode(node)) ;
    }
    
    static Node convert(Slot slot, NodeTable nodeTable)
    {
        if ( slot.isVar() )
            return slot.var ;
        return nodeTable.getNodeForNodeId(slot.id) ;
    }

    static Triple convertToTriple(Tuple<Slot> tuple, NodeTable nodeTable)
    {
        if ( tuple.size() != 3 )
            throw new TDBException("Tuple is not of length 3 : "+tuple) ;
        
        return Triple.create(convert(tuple.get(0), nodeTable), 
                             convert(tuple.get(1), nodeTable),
                             convert(tuple.get(2), nodeTable)
                             ) ;
    }

    static Quad convertToQuad(Tuple<Slot> tuple, NodeTable nodeTable)
    {
        if ( tuple.size() != 4 )
            throw new TDBException("Tuple is not of length 4 : "+tuple) ;
        
        return Quad.create(convert(tuple.get(0), nodeTable), 
                           convert(tuple.get(1), nodeTable),
                           convert(tuple.get(2), nodeTable),
                           convert(tuple.get(3), nodeTable)
                           ) ;
    }

}

