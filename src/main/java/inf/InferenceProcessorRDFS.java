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

import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

/**
 * Apply a fixed set of inference rules to a stream of triples. This is
 * inference on the A-Box (the data) with respect to a fixed T-Box (the
 * vocabulary, ontology).
 * <ul>
 * <li>rdfs:subClassOf (transitive)</li>
 * <li>rdfs:subPropertyOf (transitive)</li>
 * <li>rdfs:domain</li>
 * <li>rdfs:range</li>
 * </ul>
 * 
 * Usage: call process(Node, Node, Node), outputs to derive(Node, Node, Node).
 */

abstract class InferenceProcessorRDFS {
    // Todo:
    // rdfs:member
    // list:member ???

    static final Node rdfType           = RDF.type.asNode() ;
    static final Node rdfsSubClassOf    = RDFS.subClassOf.asNode() ;
    static final Node rdfsSubPropertyOf = RDFS.subPropertyOf.asNode() ;
    static final Node rdfsDomain        = RDFS.domain.asNode() ;
    static final Node rdfsRange         = RDFS.range.asNode() ;

    private final InferenceSetupRDFS setup ;
    
    public InferenceProcessorRDFS(InferenceSetupRDFS state) {
        this.setup = state ;
    }

    public void process(Node s, Node p, Node o) {
        subClass(s, p, o) ;
        subProperty(s, p, o) ;

        // domain() and range() also go through subClass processing.
        domain(s, p, o) ;
        range(s, p, o) ;
    }

    public abstract void derive(Node s, Node p, Node o) ;

    /*
     * [rdfs8: (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)]
     * [rdfs9: (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]
     */
    final private void subClass(Node s, Node p, Node o) {
        if ( p.equals(rdfType) ) {
            List<Node> x = setup.superClasses.get(o) ;
            if ( x != null )
                for ( Node c : x )
                    derive(s, rdfType, c) ;
            if ( setup.includeDerivedDataRDFS ) {
                subClass(o, rdfsSubClassOf, o) ;    // Recurse
            }
        }
        if ( setup.includeDerivedDataRDFS && p.equals(rdfsSubClassOf) ) {
            List<Node> superClasses = setup.superClasses.get(o) ;
            if ( superClasses != null )
                for ( Node c : superClasses )
                    derive(o, p, c) ;
            List<Node> subClasses = setup.subClasses.get(o) ;
            if ( subClasses != null )
                for ( Node c : subClasses )
                    derive(c, p, o) ;
            derive(s, p, s) ;
            derive(o, p, o) ;
        }
    }

    // Rule extracts from Jena's RDFS rules etc/rdfs.rules

    /*
     * [rdfs5a: (?a rdfs:subPropertyOf ?b), (?b rdfs:subPropertyOf ?c) -> (?a rdfs:subPropertyOf ?c)] 
     * [rdfs6: (?a ?p ?b), (?p rdfs:subPropertyOf ?q) -> (?a ?q ?b)]
     */
    private void subProperty(Node s, Node p, Node o) {
        List<Node> x = setup.superProperties.get(p) ;
        if ( x != null ) {
            for ( Node p2 : x )
                derive(s, p2, o) ;
            if ( setup.includeDerivedDataRDFS )
                subProperty(p, rdfsSubPropertyOf, p) ;
        }
        if ( setup.includeDerivedDataRDFS && p.equals(rdfsSubPropertyOf) ) {
            // ** RDFS extra
            List<Node> superProperties = setup.superProperties.get(o) ;
            if ( superProperties != null )
                for ( Node c : superProperties )
                    derive(o, p, c) ;
            List<Node> subProperties = setup.subProperties.get(o) ;
            if ( subProperties != null )
                for ( Node c : subProperties )
                    derive(c, p, o) ;
            derive(s, p, s) ;
            derive(o, p, o) ;
        }
    }

    /*
     * [rdfs2: (?p rdfs:domain ?c) -> [(?x rdf:type ?c) <- (?x ?p ?y)] ]
     * [rdfs9: (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]
     */
    final private void domain(Node s, Node p, Node o) {
        List<Node> x = setup.domainList.get(p) ;
        if ( x != null ) {
            for ( Node c : x ) {
                derive(s, rdfType, c) ;
                subClass(s, rdfType, c) ;
                if ( setup.includeDerivedDataRDFS )
                    derive(p, rdfsDomain, c) ;
            }
        }
    }

    /*
     * [rdfs3: (?p rdfs:range ?c) -> [(?y rdf:type ?c) <- (?x ?p ?y)] ]
     * [rdfs9: (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]
     */
    final private void range(Node s, Node p, Node o) {
        // Mask out literal subjects
        if ( o.isLiteral() )
            return ;
        // Range
        List<Node> x = setup.rangeList.get(p) ;
        if ( x != null ) {
            for ( Node c : x ) {
                derive(o, rdfType, c) ;
                subClass(o, rdfType, c) ;
                if ( setup.includeDerivedDataRDFS )
                    derive(p, rdfsRange, c) ;
            }
        }
    }
}
