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
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

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
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.util.FileUtils ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.Filter ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;
import com.hp.hpl.jena.vocabulary.RDFS ;
public class DevRDFS {
    static { LogCtl.setLog4j() ; }
    // Think through the cases.
    
    // More tests
    //  - run with and without vocab in data
    //  - run with different files (e.g empty data). 
    
    // Tests for:
    //   InfererenceProcessTriple - build on InferenceProcessStreamRDF
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
        InferenceSetupRDFS setup = new InferenceSetupRDFS(vocab) ;
        
        System.out.println("Inf") ;
        InferenceProcessorTriple proc = new InferenceProcessorTriple(setup) ;
        List<Triple> triples = new ArrayList<>() ;
        //InfGlobal.includeDerivedDataRDFS = true ;
        proc.process(triples, node("x1"),  node("p"), node("123")) ;
        triples.forEach(t -> System.out.println(t)) ;
        System.exit(0) ;
        
        if ( false ) {
            // Treat as data.
            Graph g = new GraphRDFS3(setup, vocab.getGraph()) ;
            Iterator<Triple> iter = g.find(null, null, node("P")) ;
//            Iterator<Triple> iter = new InferenceProcessorIteratorRDFS(setup, vocab.getGraph().find(null, null, null)) ;
            while(iter.hasNext())
                System.out.println("-- "+iter.next()) ;
            System.exit(0) ;
        }
        
        String rules = FileUtils.readWholeFileAsUTF8("rdfs-min.rules") ;
        rules = rules.replaceAll("#[^\\n]*", "") ;
        //System.out.println(rules) ;
        
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        //Model m = ModelFactory.createInfModel(reasoner, data);  
        InfModel m = ModelFactory.createInfModel(reasoner, vocab, data);
        inf = m.getGraph() ;
        
//        Model m2 = ModelFactory.createDefaultModel() ;
//        m2.setNsPrefixes(m) ;
//        m2.add(m.getDeductionsModel()) ;

        Node rt = NodeConst.nodeRDFType ;
        g_rdfs2 = new GraphRDFS(setup, data.getGraph()) ;
        
        g_rdfs3 = new GraphRDFS3(setup, data.getGraph()) ;
        
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
        System.out.println("** Graph (test):") ; 
        dwim$(gTest, s,p,o, false) ;
        System.out.println("** Graph (inference):") ; 
        dwim$(gInf, s,p,o, true) ;
        System.out.println() ;
    }
    
    
    
    static Filter<Triple> filterRDFS = new Filter<Triple>() {
        @Override
        public boolean accept(Triple triple) {
            if ( InfGlobal.includeDerivedDataRDFS ) {
                Node p = triple.getPredicate() ;
                return ! p.equals(RDFS.Nodes.domain) && ! p.equals(RDFS.Nodes.range) ; 
            }
            return  
                ! triple.getPredicate().getNameSpace().equals(RDFS.getURI()) ; 
            }
    } ;
    
    
    static void dwim$(Graph g, Node s, Node p , Node o, boolean filter) {
        System.out.printf("find(%s, %s, %s)\n", s,p,o) ; 
        ExtendedIterator<Triple> iter = g.find(s, p, o) ;
        if ( filter ) {
            Set<Triple> x = TestRDFS.filterRDFS(iter.toSet()) ;
            iter = WrappedIterator.create(x.iterator()) ;
            //iter = iter.filterKeep(filterRDFS) ;
        }
        for ( ; iter.hasNext() ; )
            System.out.println("    "+iter.next()) ;
    }
}

