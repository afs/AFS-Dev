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

import java.util.Iterator ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Quad ;

/** A minimal interface that can be plusgged into a 
 * 
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

    void removeAll(Node s, Node p, Node o) ;
    void removeAll(Node g, Node s, Node p, Node o) ;
    
    // NB Quads
    Iterator<Quad>   findDftGraph(Node s, Node p, Node o) ;
    Iterator<Quad>   find(Node g, Node s, Node p, Node o) ;
    
    // Prefixes ??
}
