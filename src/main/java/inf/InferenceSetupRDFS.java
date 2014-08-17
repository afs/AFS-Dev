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

package inf ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;

public class InferenceSetupRDFS {
    public final Map<Node, List<Node>> superClasses    = new HashMap<>() ;
    public final Map<Node, List<Node>> subClasses      = new HashMap<>() ;

    public final Map<Node, List<Node>> superProperties = new HashMap<>() ;
    public final Map<Node, List<Node>> subProperties   = new HashMap<>() ;
    
    // Predicate -> type 
    public final Map<Node, List<Node>> domainList      = new HashMap<>() ;
    public final Map<Node, List<Node>> rangeList       = new HashMap<>() ;
    
    // Type -> predicate
    public final Map<Node, List<Node>> domainPropertyList      = new HashMap<>() ;
    public final Map<Node, List<Node>> rangePropertyList       = new HashMap<>() ;
    
    
    public InferenceSetupRDFS(Model vocab) {
        // Find super classes - uses property paths
        exec("SELECT ?x ?y { ?x rdfs:subClassOf+ ?y }", vocab, superClasses, subClasses ) ;

        // Find properties
        exec("SELECT ?x ?y { ?x rdfs:subPropertyOf+ ?y }", vocab, superProperties, subProperties) ;

        // Find domain
        exec("SELECT ?x ?y { ?x rdfs:domain ?y }", vocab, domainList, domainPropertyList) ;

        // Find range
        exec("SELECT ?x ?y { ?x rdfs:range ?y }", vocab, rangeList, rangePropertyList) ;
    }

    private static void exec(String qs, Model model, Map<Node, List<Node>> multimap1, Map<Node, List<Node>> multimap2) {
        String preamble = StrUtils.strjoinNL("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
                                             "PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
                                             "PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#>",
                                             "PREFIX  owl:    <http://www.w3.org/2002/07/owl#>",
                                             "PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>") ;
        Query query = QueryFactory.create(preamble + "\n" + qs, Syntax.syntaxARQ) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        ResultSet rs = qexec.execSelect() ;
        for ( ; rs.hasNext() ; ) {
            QuerySolution soln = rs.next() ;
            Node x = soln.get("x").asNode() ;
            Node y = soln.get("y").asNode() ;
            put(multimap1, x, y) ;
            put(multimap2, y, x) ;
        }
    }
    
    private static void put(Map<Node, List<Node>> multimap, Node n1, Node n2) {
        if ( !multimap.containsKey(n1) )
            multimap.put(n1, new ArrayList<Node>()) ;
        multimap.get(n1).add(n2) ;
    }
}
