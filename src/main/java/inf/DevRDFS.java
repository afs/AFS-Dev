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
     * QueryExecutionDSG etc over Graphs/DatasetGraphs
     * 
     * Setup engine <X> -- abstract class with Node -> X function. 
     * StreamRDF versions of basic range/domain;
     *   SubProperty -> R/D -> SubClass 
     * 
     * When to materialize? Recursive calls into the source. 
     * GraphRDFS (stream version) needs cleaning.
     *  
     * coverage
     * Duplicates in (? ? ?)combined due to subClassOf
     *   Another rules file - RDFS graph without axioms.
     * 
     * rdfs:member, list:member
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
    
    static Graph inf ;
    static Graph g_rdfs2 ; 
    static Graph g_rdfs3 ;
    
    public static void main(String...argv) throws IOException {
        
        String DIR = "testing/Inf" ;
        String DATA_FILE = DIR+"/rdfs-data.ttl" ;
        String VOCAB_FILE = DIR+"/rdfs-vocab.ttl" ;
        String RULES_FILE = DIR+"/rdfs-min.rules" ;

        Model vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
        Model data = RDFDataMgr.loadModel(DATA_FILE) ;
        String rules = FileUtils.readWholeFileAsUTF8(RULES_FILE) ;
        rules = rules.replaceAll("#[^\\n]*", "") ;

        InferenceSetupRDFS setup = new InferenceSetupRDFS(vocab, false) ;
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        InfModel m = ModelFactory.createInfModel(reasoner, vocab, data);
        inf = m.getGraph() ;
        g_rdfs2 = new GraphRDFS(setup, data.getGraph()) ;
        test(node("c"), node("pTop"), null) ;
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
        dwim$("inference", gInf, s,p,o, true) ;
        dwim$("test", gTest, s,p,o, false) ;
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

