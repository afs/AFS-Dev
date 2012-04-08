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

import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;

public class MergeLib
{

    public static MergeActionIdxIdx calcMergeAction(Triple triple1, Triple triple2, TupleIndex[] indexes)
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
                        int len1 = action.getIndexAccess1().getPrefixLen()+action.getIndexAccess2().getPrefixLen() ;
                        int len2 = action2.getIndexAccess1().getPrefixLen()+action2.getIndexAccess2().getPrefixLen() ;
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
    
    public static MergeActionVarIdx calcMergeAction(Var var, Triple triple, TupleIndex[] indexes)
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

    private static IndexAccess[] access(Triple _triple, TupleIndex[] indexes)
    {
        IndexAccess[] accesses = new IndexAccess[indexes.length] ;
        Tuple<Node> triple = tripleAsTuple(_triple) ;
        int i = 0 ;
        for ( TupleIndex idx : indexes )
        {
            IndexAccess a = access(triple, idx) ;
            accesses[i++] = a ;
        }
        
        return accesses ;
    }

    private static IndexAccess access(Tuple<Node> triple, TupleIndex idx)
    {
        Tuple<Node> t = idx.getColumnMap().map(triple) ;
        for ( int i = 0 ; i < triple.size() ; i++ )
        {
            Node n = t.get(i) ;
            if ( Var.isVar(n) )
                return new IndexAccess(idx, i, Var.alloc(n)) ;
        }
        return null ;
    }

    private static Tuple<Node> tripleAsTuple(Triple triple)
    {
        return Tuple.create(triple.getSubject(),
                            triple.getPredicate(),
                            triple.getObject()) ;
    }

}

