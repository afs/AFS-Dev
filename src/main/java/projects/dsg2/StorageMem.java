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

package projects.dsg2;

import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;
import java.util.stream.Collectors ;
import java.util.stream.Stream ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Quad ;

/** Very simple {@link StorageRDF} */
public class StorageMem implements StorageRDF {
    private Set<Triple> triples = new HashSet<>() ;
    private Set<Quad> quads = new HashSet<>() ;
    
    
    @Override
    public void add(Node s, Node p, Node o) { triples.add(Triple.create(s, p, o)) ; }

    @Override
    public void add(Node g, Node s, Node p, Node o) { quads.add(Quad.create(g, s, p, o)) ; }

    @Override
    public void delete(Node s, Node p, Node o) { triples.remove(Triple.create(s, p, o)) ; }

    @Override
    public void delete(Node g, Node s, Node p, Node o) { quads.remove(Quad.create(g, s, p, o)) ; }

    @Override
    public void removeAll(Node s, Node p, Node o) {
        List<Triple> acc = match(s, p, o).collect(Collectors.toList()) ;
        acc.stream().forEach(this::delete);
    }

    @Override
    public void removeAll(Node g, Node s, Node p, Node o) {
        List<Quad> acc = match(g, s, p, o).collect(Collectors.toList()) ;
        acc.stream().forEach(this::delete);
    }

    private Stream<Triple> match(Node s, Node p, Node o) {
        return triples.stream()
            .filter(t-> match(t, s,p,o)) ; 
    }
    
    private Stream<Quad> match(Node g, Node s, Node p, Node o) {
        return quads.stream()
            .filter(q-> match(q, g,s,p,o)) ; 
    }
    

    private boolean match(Quad quad, Node g, Node s, Node p, Node o) {
        return 
            match(quad.getGraph(), g) &&
            match(quad.getSubject(), s) &&
            match(quad.getPredicate(), p) &&
            match(quad.getObject(), o) ;
    }

    private boolean match(Triple triple, Node s, Node p, Node o) {
        return
            match(triple.getSubject(), s) && 
            match(triple.getPredicate(), p) &&
            match(triple.getObject(), o) ;
    }

    private boolean match(Node node, Node pattern) {
        return pattern == null || pattern == Node.ANY || pattern.equals(node) ; 
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        return match(g, s, p, o) ;
    }

    @Override
    public Stream<Triple> find(Node s, Node p, Node o) {
        return match(s, p, o) ;
    }
    
    @Override
    public boolean contains(Node s, Node p, Node o) {
        checkConcrete(s,p,o) ;
        return find(s,p,o).findAny().isPresent() ;
    }
    
    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        checkConcrete(g,s,p,o) ;
        return find(g,s,p,o).findAny().isPresent() ;
    }

    private void checkConcrete(Node...nodes) {
        for ( Node n : nodes )
            checkConcrete(n);
    }

    private void checkConcrete(Node n) {
        if ( ! n.isConcrete() ) throw new StorageRDFException("Not concrete: "+n) ;
    }
}
