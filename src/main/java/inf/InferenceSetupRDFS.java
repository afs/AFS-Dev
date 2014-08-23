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

import java.util.* ;

import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class InferenceSetupRDFS implements InfSetupRDFS<Node>{
    public final Graph vocabGraph ;
    
    // Variants for with and without the key in the value side.
    // Adding to a list is cheap? List(elt, tail)
    
    private final Map<Node, Set<Node>> superClasses         = new HashMap<>() ;
    private final Map<Node, Set<Node>> superClassesInc      = new HashMap<>() ;
    private final Map<Node, Set<Node>> subClasses           = new HashMap<>() ;
    private final Map<Node, Set<Node>> subClassesInc        = new HashMap<>() ;
    private final Set<Node> classes                         = new HashSet<>() ;

    private final Map<Node, Set<Node>> superPropertiesInc   = new HashMap<>() ;
    private final Map<Node, Set<Node>> superProperties      = new HashMap<>() ;
    private final Map<Node, Set<Node>> subPropertiesInc     = new HashMap<>() ;
    private final Map<Node, Set<Node>> subProperties        = new HashMap<>() ;
    
    // Predicate -> type 
    private final Map<Node, Set<Node>> propertyRange        = new HashMap<>() ;
    private final Map<Node, Set<Node>> propertyDomain       = new HashMap<>() ;
    
    // Type -> predicate
    private final Map<Node, Set<Node>> rangeToProperty      = new HashMap<>() ;
    private final Map<Node, Set<Node>> domainToProperty     = new HashMap<>() ;

    private final boolean includeDerivedDataRDFS$ ;

    private static String preamble = StrUtils.strjoinNL
        ("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
         "PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
         "PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#>",
         "PREFIX  owl:    <http://www.w3.org/2002/07/owl#>",
         "PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>") ;

    
    public InferenceSetupRDFS(Graph vocab) {
        this(vocab, false) ;
    }

    public InferenceSetupRDFS(Graph vocab, boolean incDerivedDataRDFS) {
        this(ModelFactory.createModelForGraph(vocab), incDerivedDataRDFS) ;
    }
    public InferenceSetupRDFS(Model vocab) {
        this(vocab, false) ;
    }
    
    public InferenceSetupRDFS(Model vocab, boolean incDerivedDataRDFS) {
        includeDerivedDataRDFS$ = incDerivedDataRDFS ;
        vocabGraph = vocab.getGraph() ;
        
        // Find super classes - uses property paths
        exec("SELECT ?x ?y { ?x rdfs:subClassOf+ ?y }", vocab, superClasses, subClasses ) ;

        // Find properties
        exec("SELECT ?x ?y { ?x rdfs:subPropertyOf+ ?y }", vocab, superProperties, subProperties) ;

        // Find domain
        exec("SELECT ?x ?y { ?x rdfs:domain ?y }", vocab, propertyDomain, domainToProperty) ;

        // Find range
        exec("SELECT ?x ?y { ?x rdfs:range ?y }", vocab, propertyRange, rangeToProperty) ;
        
        // All mentioned classes
        classes.addAll(superClasses.keySet()) ;
        classes.addAll(subClasses.keySet()) ;
        classes.addAll(rangeToProperty.keySet()) ;
        classes.addAll(domainToProperty.keySet()) ;
        
        deepCopyInto(superClassesInc, superClasses) ;
        addKeysToValues(superClassesInc) ;
        
        deepCopyInto(subClassesInc, subClasses) ;
        addKeysToValues(subClassesInc) ;
        
        deepCopyInto(superPropertiesInc, superProperties) ;
        addKeysToValues(superPropertiesInc) ;
        
        deepCopyInto(subPropertiesInc, subProperties) ;
        addKeysToValues(subPropertiesInc) ;
    }

    private void deepCopyInto(Map<Node, Set<Node>> dest, Map<Node, Set<Node>> src) {
        src.entrySet().forEach(e -> {
            Set<Node> x = new HashSet<>(e.getValue()) ;
            dest.put(e.getKey(), x) ;
        }) ;
    }

    private void addKeysToValues(Map<Node, Set<Node>> map) {
        map.entrySet().forEach(e -> e.getValue().add(e.getKey()) ) ;
    }

    private static void exec(String qs, Model model, Map<Node, Set<Node>> multimap1, Map<Node, Set<Node>> multimap2) {
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
    
    public boolean includeDerivedDataRDFS() {
        return includeDerivedDataRDFS$ ;
    }

    private static void put(Map<Node, Set<Node>> multimap, Node n1, Node n2) {
        if ( !multimap.containsKey(n1) )
            multimap.put(n1, new HashSet<Node>()) ;
        multimap.get(n1).add(n2) ;
    }

    static private Set<Node> empty = Collections.emptySet() ;
    
    static private Set<Node> result(Map<Node, Set<Node>> map, Node elt) {
        Set<Node> x = map.get(elt) ;
        return x != null ? x : empty ;
    }
    
    @Override
    public Set<Node> getSuperClasses(Node elt) {
        return result(superClasses, elt) ;
    }

    @Override
    public Set<Node> getSuperClassesInc(Node elt) {
        return result(superClassesInc, elt) ;
    }

    @Override
    public Set<Node> getSubClasses(Node elt) {
        return result(subClasses, elt) ;
    }

    @Override
    public Set<Node> getSubClassesInc(Node elt) {
        return result(subClassesInc, elt) ;
    }

    @Override
    public Set<Node> getSuperProperties(Node elt) {
        return result(superProperties, elt) ;
    }

    @Override
    public Set<Node> getSuperPropertiesInc(Node elt) {
        return result(superPropertiesInc, elt) ;
    }

    @Override
    public Set<Node> getSubProperties(Node elt) {
        return result(subProperties, elt) ;
    }

    @Override
    public Set<Node> getSubPropertiesInc(Node elt) {
        return result(subPropertiesInc, elt) ;
    }

    @Override
    public boolean hasRangeDeclarations() {
        return ! propertyRange.isEmpty() ;
    }

    @Override
    public boolean hasDomainDeclarations() {
        return ! propertyDomain.isEmpty() ;
    }

    @Override
    public Set<Node> getRange(Node elt) {
        return result(propertyRange, elt) ;
    }

    @Override
    public Set<Node> getDomain(Node elt) {
        return result(propertyDomain, elt) ;
    }

    @Override
    public Set<Node> getPropertiesByRange(Node elt) {
        return result(rangeToProperty, elt) ;
    }

    @Override
    public Set<Node> getPropertiesByDomain(Node elt) {
        return result(domainToProperty, elt) ;
    }
}
