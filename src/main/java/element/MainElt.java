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

package element;


import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class MainElt
{
    public static void main(String[] args)
    {
        String x = StrUtils.strjoinNL
            ( "PREFIX : <http://example/>"
              //, "SELECT * { ?s :p ?x . NOT EXISTS { ?x :r ?x }  FILTER NOT EXISTS { ?x :r ?x } FILTER ( ?x > ?y ) OPTIONAL { ?x :q ?y } }"
            //, "SELECT ?x { ?s :p ?x . FILTER NOT EXISTS { ?x :r ?x }} GROUP BY ?x ORDER BY ?x"
            , "ASK { FILTER (?x = <http://example/X>) }"
            );
        
        Query q = QueryFactory.create(x, Syntax.syntaxARQ) ;

        Map<Var, Node> map = new HashMap<Var, Node>() ;
        map.put(Var.alloc("x"), NodeFactory.createURI("http://example/X")) ; 
        
        Query q2 = QueryTransformOps.transform(q, map) ;
        System.out.print(q) ;
        System.out.println("-------------");
        System.out.print(q2) ;
        System.out.println("-------------");
        
        String z = StrUtils.strjoinNL
            ( "PREFIX : <http://example/>"
            , "DELETE { ?s :p ?x } WHERE {}" 
            );
        UpdateRequest req = UpdateFactory.create(z) ;
        UpdateRequest req2 = UpdateTransformOps.transform(req, map) ;
        System.out.print(req) ;
        System.out.println("-------------");
        System.out.print(req2) ;
        System.out.println("-------------");
        
        
    }
    
}

