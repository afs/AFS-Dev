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

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.DatasetGraphTriplesQuads ;
import org.apache.jena.sparql.core.Quad ;

public class DatasetGraphMemCompact extends DatasetGraphTriplesQuads {
    
    // Storage
    // Triples
    
    // Quads.
    
    static class CompactTriples {
        // S -> PO : hash table? Or binaray search on hash.
        //   To an array of adjacent nodes.  Don't worry about repeated P 
        // O -> PS
        //  To an array of adjacent nodes.  Do worry about repeated P. 
        //  Or OP->S ?
        // Hash table on P -> array S then look in S->PO 
        // P -> S, 
        // P -> O
        
        // ?s P ?o ==> 
        // ?s P O  especially if IFP 
        // S P ?o  ==>
        
        // ---- Always P->SO, P->OS
        // 
    }
    

    @Override
    public Iterator<Node> listGraphNodes() {
        return null;
    }

    @Override
    protected void addToDftGraph(Node s, Node p, Node o) {}

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o) {}

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o) {}

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o) {}

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        return null;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        return null;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        return null;
    }

    @Override
    public Graph getDefaultGraph() {
        return null;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return null;
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {}

    @Override
    public void removeGraph(Node graphName) {}

}

