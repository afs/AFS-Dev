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

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
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
public interface StorageRDF /*extends Transactional*/ {
    // Prefixes per dataset : and per graph
    // DatasetPrefixStorage
    
    public default void add(Triple triple)
    { add(triple.getSubject(), triple.getPredicate(), triple.getObject()) ; }
    
    public default void add(Quad quad)
    { add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }
    
    public default void delete(Triple triple)
    { delete(triple.getSubject(), triple.getPredicate(), triple.getObject()) ; }
    
    public default void delete(Quad quad)
    { delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }
    
    public void add(Node s, Node p, Node o) ;
    
    public void add(Node g, Node s, Node p, Node o) ;

    public void delete(Node s, Node p, Node o) ;
    
    public void delete(Node g, Node s, Node p, Node o) ;

    /** Delete all triples matching a {@code find}-like pattern */ 
    public void removeAll(Node s, Node p, Node o) ;
    
    /** Delete all quads matching a {@code find}-like pattern */ 
    public void removeAll(Node g, Node s, Node p, Node o) ;
    
//    // ??
//    /** Find in the default graph - return as quads (graph name {@link Quad#defaultGraphIRI}) */
//    default Stream<Quad> xfindDftGraph(Node s, Node p, Node o) {
//        return find(s, p, o).map(t -> Quad.create(Quad.defaultGraphIRI, t)) ;
//    }
//    
    /** Find in the union graph (union of all named graphs, not the default graph) */
    public default Stream<Triple> findUnionGraph(Node s, Node p, Node o) {
        return find(Node.ANY, s, p, o).map(Quad::asTriple).distinct() ;
    }
    
    /** Find in named graphs: does not look in the default graph */
    public Stream<Quad> find(Node g, Node s, Node p, Node o) ;
    
    /** Find in the default graph */
    public Stream<Triple> find(Node s, Node p, Node o) ;

    /** Does the default graph contain the match the triple? (s,p,o must be concrete) */ 
    public default boolean contains(Triple triple) {
        return contains(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }

    /** Does the default graph contain the match the triple? (s,p,o must be concrete) */ 
    public boolean contains(Node s, Node p, Node o) ;
    
    public default boolean contains(Quad quad) {
        return contains(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    /** Do any of the named graphs match the quad? (g,s,p,o must be concrete) */
    boolean contains(Node g, Node s, Node p, Node o) ;
     
    // Prefixes ??
}
