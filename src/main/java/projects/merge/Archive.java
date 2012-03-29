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
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Var ;

public class Archive
{
    // Another way of thinking about it.
    private static Pair<String,String> calcMergeJoin2(Triple triple1, Triple triple2, String[] indexes)
    {
        if ( constants(triple1.getPredicate()) == null ||
            constants(triple2.getPredicate()) == null )
        {
            System.out.println("Not a P-P pair") ;
            return null ;
        }

        String i1 ;
        String i2 ; 

        // Iterate possibilities.
        // Assuming P is fixed ...
        
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

        //      Var[] vars = new Var[3] ;
        //      vars[0] = vars(t1.getSubject()) ; 
        //      vars[1] = vars(t1.getPredicate()) ; 
        //      vars[2] = vars(t1.getObject()) ;
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

