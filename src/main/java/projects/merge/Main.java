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

import static projects.merge.Main.ColNames.O ;
import static projects.merge.Main.ColNames.P ;
import static projects.merge.Main.ColNames.S ;
import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.Tuple ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

public class Main
{
    // Preferred.
    enum ColNames { S("S"), P("P"), O("O"), G("G") ;
        private String col ;
        ColNames(String col) { this.col = col ; }
        @Override public String toString() { return col ;}  
    }
    
    public static void main(String[] args)
    {
        // Setup
        Log.setLog4j() ;
        ColumnMap colMap = new ColumnMap("SPO", "POS") ;
        Location loc = Location.mem() ;

        TupleIndex POS = SetupTDB.makeTupleIndex(loc, "SPO", "POS", "POS", 3*NodeId.SIZE) ;
        TupleIndex PSO = SetupTDB.makeTupleIndex(loc, "SPO", "PSO", "PSO", 3*NodeId.SIZE) ;
        
        TupleIndex [] indexes = { POS, PSO } ;
        // Add "columns" to indexes.
        Tuple<ColNames> primary = Tuple.create(S,P,O) ;
        System.out.println(POS.getColumnMap().map(primary)) ; 
        
        
        // Setup
        test("(?s <p> ?o)", "(?s <q> 123)", indexes, PSO, POS) ;
        test("(?s <p> ?o)", "(?s <q> ?v)",  indexes, PSO, PSO) ;
        test("(?s <p> ?z)", "(?z <q> ?v)",  indexes, POS, PSO) ;
        
        test("(?s <p> ?z)", "(?z <q> 123)", indexes, POS, POS) ;
        test("(?x <p> ?x)", "(?x <q> ?v)",  indexes, PSO, PSO) ;
        
        test("(?a <p> ?b)", "(?c <q> ?d)",  indexes, PSO, PSO) ;

    }

    private static void test(String tripleStr1, String tripleStr2, TupleIndex[] indexes, TupleIndex index1, TupleIndex index2)
    {
        Triple triple1 = SSE.parseTriple(tripleStr1) ;
        Triple triple2 = SSE.parseTriple(tripleStr2) ;
        
        System.out.print("Join: ") ;
        SSE.write(triple1) ;
        System.out.print("  ") ;
        SSE.write(triple2) ;
        System.out.println() ;
        
        //System.out.println("{"+triple1+"}    {"+triple2+"}") ;
        MergeAction action = choose(triple1, triple2, indexes) ;
        
        if ( action == null )
        {
            System.out.println("** No match") ;
            return ;
        }
        TupleIndex i1 = action.getIndexAccess1().getIndex() ;
        TupleIndex i2 = action.getIndexAccess2().getIndex() ;
        
        if ( !index1.equals(i1) || !index2.equals(i2) )
            System.out.println("** Expected: "+index1+"-"+index2+" : Got "+i1+"-"+i2) ;
        else
            System.out.println("** "+action) ;
        System.out.println() ;
    }

    private static MergeAction choose(Triple triple1, Triple triple2, TupleIndex[] indexes)
    {
        IndexAccess[] access1 = access(triple1, indexes) ;
        IndexAccess[] access2 = access(triple2, indexes) ;
        
//        System.out.println(Arrays.asList(access1)) ;
//        System.out.println(Arrays.asList(access2)) ;
        
        MergeAction action = null ;
        for ( IndexAccess a1 : access1 )
        {
            if ( a1 == null ) continue ;
            for ( IndexAccess a2 : access2 )
            {
                if ( a2 == null ) continue ;
                if ( a1.getVar().equals(a2.getVar()))
                {
                    MergeAction action2 = new MergeAction(a1,a2) ;
                    // Longest prefixes.
                    if ( action == null )
                        action = action2 ;
                    else
                    {
                        System.out.println("Choose: "+action+" // "+action2) ;
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
