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
 
package projects.dsg2.storage;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/** A wrapper for {@link StorageRDF}. */
public class StorageRDF_Wrapper implements StorageRDF {

    private final StorageRDF other;

    public StorageRDF_Wrapper(StorageRDF other) {
        this.other = other;
    }
    
    protected StorageRDF get() { return other ; }
    
    @Override
    public void add(Triple triple) {
        get().add(triple);
    }

    @Override
    public void add(Quad quad) {
        get().add(quad);
    }

    @Override
    public void delete(Triple triple) {
        get().delete(triple);
    }

    @Override
    public void delete(Quad quad) {
        get().delete(quad);
    }

    @Override
    public void add(Node s, Node p, Node o) {
        get().add(s, p, o);
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        get().add(s, p, o);
    }

    @Override
    public void delete(Node s, Node p, Node o) {
        get().delete(s, p, o);
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        get().delete(g, s, p, o);
    }

    @Override
    public void removeAll(Node s, Node p, Node o) {
        get().removeAll(s, p, o);
    }

    @Override
    public void removeAll(Node g, Node s, Node p, Node o) {
        get().removeAll(g, s, p, o);
    }

    @Override
    public Stream<Triple> find(Triple triple) {
        return get().find(triple);
    }
    
    @Override
    public Stream<Quad> find(Quad quad) {
        return get().find(quad);
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        return get().find(g, s, p, o);
    }

    @Override
    public Stream<Triple> findUnionGraph(Node s, Node p, Node o) {
        return get().findUnionGraph(s, p, o);
    }
    
    @Override
    public Stream<Triple> find(Node s, Node p, Node o) {
        return get().find(s, p, o);
    }

    @Override
    public boolean contains(Triple triple) {
        return get().contains(triple);
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        return get().contains(s, p, o);
    }

    @Override
    public boolean contains(Quad quad) {
        return get().contains(quad);
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        return get().contains(g, s, p, o);
    }
}
