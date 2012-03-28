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
import java.util.Arrays ;
import java.util.List ;
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

public class JoinMerge
{
    public static void main(String ... argv)
    {
        Log.setLog4j() ;
        ColumnMap colMap = new ColumnMap("SPO", "POS") ;
        String [] indexes = { "POS", "PSO"} ;
        Triple[] triples = { SSE.parseTriple("(?s <p> ?o)") ,
                             SSE.parseTriple("(?s <q> 123)") ,
                             SSE.parseTriple("(<x> <q> 123)") ,
                             } ;
//        for ( Triple t : triples )
//            choose(t, indexes) ;
        
        // Test cases.
        
        test("(?s <p> ?o)", "(?s <q> 123)", indexes, "PSO", "POS") ;
        test("(?s <p> ?o)", "(?s <q> ?v)",  indexes, "PSO", "PSO") ;
        test("(?s <p> ?z)", "(?z <q> ?v)",  indexes, "POS", "PSO") ;
        
        test("(?s <p> ?z)", "(?z <q> 123)", indexes, "POS", "POS") ;
        test("(?x <p> ?x)", "(?x <q> ?v)",  indexes, "PSO", "PSO") ;
        
        test("(?a <p> ?b)", "(?c <q> ?d)",  indexes, "PSO", "PSO") ;
        
    }

    // Find first sort order
    // When is there a choice?
    // Finger print: (Constants) (same vars).
    
    private static void test(String tripleStr1, String tripleStr2, String[] indexes, String index1, String index2)
    {
        Triple triple1 = SSE.parseTriple(tripleStr1) ;
        Triple triple2 = SSE.parseTriple(tripleStr2) ;
        
        System.out.print("Join: ") ;
        SSE.write(triple1) ;
        System.out.print("  ") ;
        SSE.write(triple2) ;
        System.out.println() ;
        
        //System.out.println("{"+triple1+"}    {"+triple2+"}") ;
        Pair<String, String> p = choose(triple1, triple2, indexes) ;
        
        if ( p == null )
        {
            System.out.println("** No match") ;
            return ;
        }
        String i1 = p.getLeft() ;
        String i2 = p.getRight() ;
        
        if ( !index1.equals(i1) || !index2.equals(i2) )
            System.out.println("** Expected: "+index1+"-"+index2+" : Got "+i1+"-"+i2) ;
        else
            System.out.println("** "+i1+"-"+i2) ;
        System.out.println() ;
    }
     
    
    private static Pair<String, String> choose(Triple triple1, Triple triple2, String[] indexes)
    {
        // Join vars.
        
        Tuple<String> const1 = constants(triple1) ;
        Tuple<String> const2 = constants(triple2) ;
        
        int x1 = const1.countNotNull() ;
        int x2 = const2.countNotNull() ;

//        print(const1, indexes) ;
//        print(const2, indexes) ;
        
        // Assume P fixed.
        // Look for join vars.
        Set<Var> vars1 = VarUtils.getVars(triple1) ;
        Set<Var> vars2 = VarUtils.getVars(triple2) ;
        Set<Var> joinVars = SetUtils.intersection(vars1, vars2) ;
        //System.out.println("Join: "+joinVars) ;
        
        // Find join linkages.
        // Assume one for now.
        Pair<String, String> linkage = joinLinkages(triple1, triple2) ;

        // There parts.
        // Const, vars, remainederr
        
        if ( linkage == null )
        {
            System.out.println("No linkage") ;
        }
        else
        {
            // Base index for
            String prefix1 = constantIndexPrefix(triple1) ;
            String prefix2 = constantIndexPrefix(triple2) ;
            System.out.print("Prefixes="+prefix1+"/"+prefix2) ;
            System.out.println("  JoinVars="+joinVars+"  Linkage="+linkage) ;
        
            // .. conclusion.
            String idxPrefix1 = prefix1+linkage.getLeft();
            String idxPrefix2 = prefix2+linkage.getRight();
            //System.out.println("Calc: "+idx1+"-"+idx2) ;
            
            String[] joinIndex1 = new String[3] ;
            joinIndex1[0] = prefix1 ;
            joinIndex1[1] = linkage.getLeft(); ;
            joinIndex1[2] = null ;
            String[] joinIndex2 = new String[3] ;
            joinIndex2[0] = prefix2 ;
            joinIndex2[1] = linkage.getRight(); ;
            joinIndex2[2] = null ;
            
            
            for ( String index : indexes )
            {
                if ( index.startsWith(idxPrefix1))
                {
                    if ( joinIndex1[2] != null )
                        System.out.println("Chocies! (1) : "+index) ;
                    else
                        joinIndex1[2] = index.substring(idxPrefix1.length()) ;
                }
                if ( index.startsWith(idxPrefix2))
                {
                    if ( joinIndex2[2] != null )
                        System.out.println("Chocies! (2) : "+index) ;
                    else
                        joinIndex2[2] = index.substring(idxPrefix2.length()) ;
                    
                }
            }
            
            
            String s1 = strJoinIndex(joinIndex1) ;
            String s2 = strJoinIndex(joinIndex2) ;
            System.out.println("Calc: "+s1+" "+s2) ;
        }

        // Another way of thinking about it.
        // Generate possibilities.
        
        if ( constants(triple1.getPredicate()) == null ||
             constants(triple2.getPredicate()) == null )
        {
            System.out.println("Not a P-P pair") ;
            return null ;
        }
        
        String i1 ;
        String i2 ; 
        
        if ( varMatch(triple1.getSubject(), triple2.getSubject()) )
        {
            i1 = chooseIndex(triple1.getObject(),  "PSO" , "POS") ;
            i2 = chooseIndex(triple2.getObject(),  "PSO" , "POS") ;
        }
        else if ( varMatch(triple1.getSubject(), triple2.getObject()) )  
        {
            i1 = chooseIndex(triple1.getObject(),  "PSO" , "POS") ;
            i2 = chooseIndex(triple2.getSubject(), "POS" , "PSO") ;
        }
        else if ( varMatch(triple1.getObject(), triple2.getSubject()) )  
        {
            i1 = chooseIndex(triple1.getSubject(), "POS" , "PSO") ;
            i2 = chooseIndex(triple2.getObject(),  "PSO" , "POS") ;
        }
        else if ( varMatch(triple1.getObject(), triple2.getObject()) )
        {
            i1 = chooseIndex(triple1.getSubject(), "POS" , "PSO") ;
            i2 = chooseIndex(triple2.getSubject(), "POS" , "PSO") ;
        }
        else
            return null ;
            
        return Pair.create(i1, i2) ;
    }


    private static String strJoinIndex(String[] joinIndex)
    {
        return "["+joinIndex[0]+","+joinIndex[1]+","+joinIndex[2]+"]" ;
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
    
    private static Set<Var> vars(Triple triple1)
    {
        return null ;
    }

    private static String chooseIndex(Node node, String idx1, String idx2)
    {
        return isVar(node) ? idx1 : idx2  ;
    }

    private static boolean isVar(Node node)
    {
        return Var.isVar(node) ;
    }

    private static boolean varMatch(Node node1, Node node2)
    {
        return Var.isVar(node1) && node1.equals(node2) ;
    }

    private static void print(Tuple<String> constants, String[] indexes)
    {
        System.out.println(constants) ;
        for ( String idx : indexes )
        {
            ColumnMap colmap = new ColumnMap("SPO", idx) ;
            Tuple<String> tuple = constants ;
            Tuple<String> tuple2 = tuple.map(colmap) ;
            System.out.println(idx+": "+tuple2) ;
        }
    }

    private static Tuple<String> constants(Triple triple)
    {
        String[] colNames = new String[3] ;
        if ( constants(triple.getSubject()) != null )   colNames[0] = "S" ;
        if ( constants(triple.getPredicate()) != null ) colNames[1] = "P" ;
        if ( constants(triple.getObject()) != null )    colNames[2] = "O" ;
        return Tuple.create(colNames) ;
    }
    
    
    private static void choose(Triple t1, String[] indexes)
    {
        System.out.println(t1) ;
        List<String> cols = new ArrayList<String>() ;
        Node[] constants = new Node[3] ;
        constants[0] = constants(t1.getSubject()) ; 
        constants[1] = constants(t1.getPredicate()) ; 
        constants[2] = constants(t1.getObject()) ;
        String[] colNames = new String[3] ;
        
        if ( constants[0] != null ) { cols.add("S") ; colNames[0] = "S" ; }
        if ( constants[1] != null ) { cols.add("P") ; colNames[1] = "P" ; }
        if ( constants[2] != null ) { cols.add("O") ; colNames[2] = "O" ; }
        
        Tuple<String> tuple = Tuple.create(colNames) ; 
        System.out.println(cols) ;
        System.out.println(Arrays.asList(colNames)) ;
        
        for ( String idx : indexes )
        {
            ColumnMap colmap = new ColumnMap("SPO", idx) ;
            Tuple<String> tuple2 = tuple.map(colmap) ;
            System.out.println(idx+": "+tuple2) ;
        }
        
//        Var[] vars = new Var[3] ;
//        vars[0] = vars(t1.getSubject()) ; 
//        vars[1] = vars(t1.getPredicate()) ; 
//        vars[2] = vars(t1.getObject()) ;
    }

    private static Node constants(Node node)
    {
        return node.isConcrete() ? node : null ; 
    }
    
    private static Var vars(Node node)
    {
        return Var.isVar(node) ? Var.alloc(node) : null ; 
    }

}

