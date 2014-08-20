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

package inf;

import java.io.IOException ;
import java.util.List ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.riot.RDFDataMgr ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.InfModel ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.reasoner.Reasoner ;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner ;
import com.hp.hpl.jena.reasoner.rulesys.Rule ;
import com.hp.hpl.jena.util.FileUtils ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
public class DevRDFS {
    static { LogCtl.setLog4j() ; }
    // Think through the cases.
    
    /*
     * rdfs:member, list:member
     *  
     * OWL 2 RL / OWL 2 QL.
     * http://www.w3.org/TR/owl2-profiles/#OWL_2_QL
     * http://www.w3.org/TR/owl2-profiles/#OWL_2_RL

     * RDFS 3.0 / RDFS Plus / 
     * http://www.w3.org/2009/12/rdf-ws/papers/ws31
        rdfs:domain
        rdfs:range
        rdfs:subClassOf
        rdfs:subPropertyOf
        
        owl:equivalentClass
        owl:equivalentProperty
        
        owl:sameAs
        owl:inverseOf
        (no reflexive)
        owl:TransitiveProperty 
        owl:SymmetricProperty

        owl:FunctionalProperty
        owl:InverseFunctionalProperty 
*/
    
    // More tests
    //   Coverage
    //   find_X_rdfsSubClassOf_Y
    
    // Tests for:
    //   InfererenceProcessTriple
    //   InferenceProcessStreamRDF
    //   InfererenceProcessIteratorRDFS
    
    // Test (D,V) , (D,-), (-, V), (D+V, D+V)
    //   Mode D-extract-V.
    
    // ANY_ANY_T - filter rdf:type and replace - no distinct needed.
    
    // Use InfFactory.
    
    // test(null, null, node("P")) misses when inclued RDFS is on.
    //     http://example/q @rdfs:range http://example/P
    
    // Tests - also with vocab in data (and separate).
    // Processor flag for "no machinary" - only rdf:type derivations  - when data includes vocab.
    // == hide rdfs: derived stuff from data.
    
    // If data inference - tests for X subClassOf X
    
    static Graph inf ;
    static Graph g_rdfs2 ; 
    static Graph g_rdfs3 ;
    
    public static void main(String...argv) throws IOException {
        
        String DATA_FILE = "rdfs-data.ttl" ;
        String VOCAB_FILE = "rdfs-vocab.ttl" ;

        Model vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
        Model data = RDFDataMgr.loadModel(DATA_FILE) ;
        Model dataAndVocab = ModelFactory.createDefaultModel() ;
        dataAndVocab.add(vocab) ;
        dataAndVocab.add(data) ;
        
        String rules        //iter = printExtended(iter) ;
 = FileUtils.readWholeFileAsUTF8("rdfs-min.rules") ;
        rules = rules.replaceAll("#[^\\n]*", "") ;
        //System.out.println(rules) ;

        InferenceSetupRDFS setup = new InferenceSetupRDFS(dataAndVocab, false) ;
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        //Model m = ModelFactory.createInfModel(reasoner, data);  
        InfModel m = ModelFactory.createInfModel(reasoner, vocab, data);
        inf = m.getGraph() ;
        g_rdfs2 = new GraphRDFS(setup, data.getGraph()) ;
        
        test(null, null, node("P")) ;
    }
    
    private static void test(Node s, Node p, Node o) {
        dwim(g_rdfs2, inf, s,p,o) ;
    }

    private static void test3(Node s, Node p, Node o) {
        System.out.println("** GraphRDFS3 **") ;
        dwim(g_rdfs3, inf, s,p,o) ;
    }

    static Node node(String str) { return NodeFactory.createURI("http://example/"+str) ; }
    
    static void dwim(Graph gTest, Graph gInf, Node s, Node p , Node o) {
        dwim$("test", gTest, s,p,o, false) ;
        dwim$("inference", gInf, s,p,o, true) ;
        System.out.println() ;
    }
    
    static void dwim(Graph graph, Node s, Node p , Node o) {
        dwim$(null, graph, s,p,o, false) ;
    }
    
    
//    static Filter<Triple> filterRDFS = new Filter<Triple>() {
//        @Override
//        public boolean accept(Triple triple) {
//            if ( InfGlobal.includeDerivedDataRDFS ) {
//                Node p = triple.getPredicate() ;
//                return ! p.equals(RDFS.Nodes.domain) && ! p.equals(RDFS.Nodes.range) ; 
//            }
//            return  
//                ! triple.getPredicate().getNameSpace().equals(RDFS.getURI()) ; 
//            }
//    } ;
    
    
    static void dwim$(String label, Graph g, Node s, Node p , Node o, boolean filter) {
        if ( label != null )
            System.out.println("** Graph ("+label+"):") ;
        System.out.printf("find(%s, %s, %s)\n", s,p,o) ; 
        ExtendedIterator<Triple> iter = g.find(s, p, o) ;
        List<Triple> x = iter.toList() ;
        if ( filter )
            x = InfGlobal.removeRDFS(x) ;
        x.forEach(t -> System.out.println("    "+t)) ;
        System.out.println() ;
    }
}

