/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
 
package projects.dsg2;

import java.util.stream.Stream ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.sparql.core.Quad ;

/**
 * A minimal interface for RDF storage. This is less that {{DatasetGraph}} or
 * any of it's derived classes and it just concerned with {@link Triple}s and
 * {@link Quad}s, not {@link Graph}s.
 * <p>
 * Storage is split into the triples for the default graph and quads for the
 * named graphs. In {@link #find(Node, Node, Node, Node)} ({@code find} on the
 * named graphs), {@code null} for the graph slot does not match the default
 * graph.
 */
interface StorageRDF {
    default void add(Triple triple)     { add(triple.getSubject(), triple.getPredicate(), triple.getObject()) ; }
    default void add(Quad quad)         { add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }
    
    default void delete(Triple triple)  { delete(triple.getSubject(), triple.getPredicate(), triple.getObject()) ; }
    default void delete(Quad quad)      { delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }
    
    void add(Node s, Node p, Node o) ;
    void add(Node g, Node s, Node p, Node o) ;

    void delete(Node s, Node p, Node o) ;
    void delete(Node g, Node s, Node p, Node o) ;

    /** Delete all triples matching a {@code find}-like pattern */ 
    void removeAll(Node s, Node p, Node o) ;
    /** Delete all quads matching a {@code find}-like pattern */ 
    void removeAll(Node g, Node s, Node p, Node o) ;
    
    // NB Quads
    Stream<Quad>   findDftGraph(Node s, Node p, Node o) ;
    Stream<Quad>   findUnionGraph(Node s, Node p, Node o) ;
    Stream<Quad>   find(Node g, Node s, Node p, Node o) ;
    // For findUnion.
    Stream<Quad>   findDistinct(Node g, Node s, Node p, Node o) ;
    
    // triples
    Stream<Triple> find(Node s, Node p, Node o) ;
    
//    default Stream<Triple> find(Node s, Node p, Node o) { 
//        return findDftGraph(s,p,o).map(Quad::asTriple) ;
//    }
    
//    Iterator<Quad>   findUnionGraph(Node s, Node p, Node o) ;
//    Iterator<Quad>   find(Node g, Node s, Node p, Node o) ;
    
    
    // contains
    
    default boolean contains(Node s, Node p, Node o)            { return find(s,p,o).findAny().isPresent() ; }
    default boolean contains(Node g, Node s, Node p, Node o)    { return find(g,s,p,o).findAny().isPresent() ; }
     
    // Prefixes ??
}
