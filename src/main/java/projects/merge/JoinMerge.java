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

import java.util.Set ;

import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.lib.SetUtils ;
import org.openjena.atlas.lib.Tuple ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.VarUtils ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

public class JoinMerge
{
    public static void main(String ... argv)
    {
        Log.setLog4j() ;
        ColumnMap colMap = new ColumnMap("SPO", "POS") ;
        Location loc = Location.mem() ;

        TupleIndex POS = SetupTDB.makeTupleIndex(loc, "SPO", "POS", "POS", 3*NodeId.SIZE) ;
        TupleIndex PSO = SetupTDB.makeTupleIndex(loc, "SPO", "PSO", "PSO", 3*NodeId.SIZE) ;
        
        TupleIndex [] indexes = { POS, PSO } ;

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
        TupleIndex i1 = action.index1 ;
        TupleIndex i2 = action.index2 ;
        
        if ( !index1.equals(i1) || !index2.equals(i2) )
            System.out.println("** Expected: "+index1+"-"+index2+" : Got "+i1+"-"+i2) ;
        else
            System.out.println("** "+action) ;
        System.out.println() ;
    }
     
    
    private static MergeAction choose(Triple triple1, Triple triple2, TupleIndex[] indexes)
    {
        Tuple<String> const1 = constants(triple1) ;
        Tuple<String> const2 = constants(triple2) ;
        
//        int x1 = const1.countNotNull() ;
//        int x2 = const2.countNotNull() ;

//        print(const1, indexes) ;
//        print(const2, indexes) ;
        
        // Assume P fixed.
        // Look for join vars.
        Set<Var> vars1 = VarUtils.getVars(triple1) ;
        Set<Var> vars2 = VarUtils.getVars(triple2) ;
        // Find join linkages.
        // Assume one for now.
        Pair<String, String> linkage = joinLinkages(triple1, triple2) ;

        // Const, vars, remainder
        
        if ( linkage == null )
        {
            System.out.println("No linkage") ;
            return null ;
        }

        Set<Var> _joinVars = SetUtils.intersection(vars1, vars2) ;
        Var joinVar = _joinVars.iterator().next(); 
        //System.out.println("Join: "+joinVars) ;
        
        // Base index for
        String prefix1 = constantIndexPrefix(triple1) ;
        String prefix2 = constantIndexPrefix(triple2) ;
//        System.out.print("Prefixes="+prefix1+"/"+prefix2) ;
//        System.out.println("  JoinVar="+joinVar+"  Linkage="+linkage) ;
        
        // .. conclusion.
        String idxPrefix1 = prefix1+linkage.getLeft();
        String idxPrefix2 = prefix2+linkage.getRight();
        //System.out.println("Calc: "+idx1+"-"+idx2) ;
            
        // 0 - contants
        // 1 - join var cols
        // 2 - rest
        // Make a class? JoinIndexAccess
        String[] joinIndex1 = new String[3] ;
        joinIndex1[0] = prefix1 ;
        joinIndex1[1] = linkage.getLeft();
        joinIndex1[2] = null ;
        String[] joinIndex2 = new String[3] ;
        joinIndex2[0] = prefix2 ;
        joinIndex2[1] = linkage.getRight();
        joinIndex2[2] = null ;
        
        TupleIndex idx1 = null ;
        TupleIndex idx2 = null ;
        
        for ( TupleIndex index : indexes )
        {
            String idxStr = index.getName() ;
            
            if ( idxStr.startsWith(idxPrefix1))
            {
                if ( joinIndex1[2] != null )
                    System.out.println("Choices! (1) : "+idxStr) ;
                else
                {
                    idx1 = index ;
                    joinIndex1[2] = idxStr.substring(idxPrefix1.length()) ;
                    
                }
            }
            if ( idxStr.startsWith(idxPrefix2))
            {
                if ( joinIndex2[2] != null )
                    System.out.println("Choices! (2) : "+idxStr) ;
                else
                {
                    idx2 = index ;
                    joinIndex2[2] = idxStr.substring(idxPrefix2.length()) ;
                }
            }
        }


//        String s1 = strJoinIndex(joinIndex1) ;
//        String s2 = strJoinIndex(joinIndex2) ;
        //System.out.println("Decision: "+s1+" "+s2) ;
        
        return new MergeAction(idx1, idx2,
                               prefix1, prefix2,
                               joinVar
                               )  ;

    }
    
    private static Pair<String, String> joinLinkages(Triple triple1, Triple triple2)
    {
        String x = joinLinkage(triple1.getSubject(), triple2) ;
        if ( x != null ) return Pair.create("S", x) ;
        x = joinLinkage(triple1.getPredicate(), triple2) ;
        if ( x != null ) return Pair.create("P", x) ;
        x = joinLinkage(triple1.getObject(), triple2) ;
        if ( x != null ) return Pair.create("O", x) ;
        return null ;
    }

    private static String joinLinkage(Node x, Triple triple)
    {
        if ( ! Var.isVar(x) ) return null ;

        if ( triple.getSubject().equals(x) )    return "S" ;
        if ( triple.getPredicate().equals(x) )  return "P" ;
        if ( triple.getObject().equals(x) )     return "O" ;
        return null ;
    }

    private static String constantIndexPrefix(Triple triple)
    {
        // Rework!
        String x = "" ;
        Tuple<String> tuple = constants(triple) ;
        if ( tuple.get(1) != null ) x = "P" ;
        if ( tuple.get(0) != null ) x = x+ "S" ;
        if ( tuple.get(2) != null ) x = x+ "O" ;
        return x  ;
    }


    private static Tuple<String> constants(Triple triple)
    {
        String[] colNames = new String[3] ;
        if ( constants(triple.getSubject()) != null )   colNames[0] = "S" ;
        if ( constants(triple.getPredicate()) != null ) colNames[1] = "P" ;
        if ( constants(triple.getObject()) != null )    colNames[2] = "O" ;
        return Tuple.create(colNames) ;
    }

    private static Node constants(Node node)
    {
        return node.isConcrete() ? node : null ; 
    }

//    private static String strJoinIndex(String[] joinIndex)
//    {
//        return joinIndex[0]+joinIndex[1]+joinIndex[2] ;
//        //return "["+joinIndex[0]+","+joinIndex[1]+","+joinIndex[2]+"]" ;
//    }
}

