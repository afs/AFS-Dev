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

import java.util.* ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderLib ;
import com.hp.hpl.jena.tdb.solver.BindingNodeId ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class Support
{
    /* parse to create dadat for tests for AccessOps. 
     * (table
     *    (row (?x 1) (?y 2)) 
     *  )
     * 
     */
    
    public static void main(String ... argv)
    {
        Log.setLog4j() ;
        List<BindingNodeId> x = parseTableNodeId(
           "(table",
           "   (row (?x 1) (?y 2))",
           "   (row )",
           "   (row (?z 3))",
           ")") ;
           
        System.out.println(x) ;
        
        List<BindingNodeId> y = parseTableNodeId(
                                                 "(table",
                                                 "   (row )",
                                                 "   (row (?z 3))",
                                                 "   (row (?x 1) (?y 2))",
                                                 ")") ;
        
        System.out.println(equals(x,y)) ;
    }
    
    public static boolean equals(List<BindingNodeId> x, List<BindingNodeId> y)
    {
        if ( x.size() != y.size() )
            return false ;
        for ( int i = 0 ; i < x.size() ; i++ )
        {
            BindingNodeId b1 = x.get(i) ;
            BindingNodeId b2 = y.get(i) ;
            if ( ! equals(b1, b2) )
                return false ;
        }
        return true ;
        
    }
    
    private static boolean equals(BindingNodeId b1, BindingNodeId b2)
    {
        List<Var> vars1 = Iter.toList(b1.iterator()) ;
        List<Var> vars2 = Iter.toList(b2.iterator()) ;
        if ( vars1.size() != vars2.size() )
            return false ;
        Collections.sort(vars1, varComp) ;
        Collections.sort(vars2, varComp) ;
        
        for ( int i = 0 ; i < vars1.size() ; i++ )
        {
            Var v = vars1.get(i) ;
            NodeId n1 = b1.get(v) ;
            NodeId n2 = b1.get(v) ;
            if ( n1.getId() != n2.getId() )
                return false ;
        }
        return true ;
    }
    
    private static Comparator<Var> varComp = new Comparator<Var>() {

        @Override
        public int compare(Var v1, Var v2)
        {
            return v1.getName().compareTo(v2.getName()) ; 
        }} ;

    public static List<BindingNodeId> parseTableNodeId(String ... s)
    {
        List<BindingNodeId> bindings = new ArrayList<>() ;
        String x = StrUtils.strjoinNL(s) ;
        Item item = SSE.parse(x) ;
        
        ItemList list = item.getList() ;
        BuilderLib.checkTag(list, "table") ;
        list = list.cdr() ;
        
        for ( Item e : list )
        {
            BindingNodeId b = parseRow(e) ;
            bindings.add(b) ;
        }
        
        return bindings ;
    }
    
    private static BindingNodeId parseRow(Item item)
    {
        BindingNodeId row = new BindingNodeId() ;
        ItemList list = item.getList() ;
        BuilderLib.checkTag(list, "row") ;
        list = list.cdr() ;
        
        for ( Item e : list )
        {
            Pair<Var, NodeId> p = parse1(e) ;
            row.put(p.getLeft(), p.getRight()) ;
        }
        
        return row ;
    }
    
    private static Pair<Var, NodeId> parse1(Item e)
    {
        Var var = Var.alloc(e.getList().get(0).getNode()) ;
        long x = e.getList().get(1).getLong() ;
        return Pair.create(var, NodeId.create(x)) ;
    }
    

}

