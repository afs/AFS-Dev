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
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.junit.Assert ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.rdf.model.InfModel ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.reasoner.Reasoner ;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner ;
import com.hp.hpl.jena.reasoner.rulesys.Rule ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.util.FileUtils ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class TestRDFS {// extends BaseTest {
    static { LogCtl.setCmdLogging(); }
    
    
    static Model vocab ;
    static Model data ;
    static Node rt = NodeConst.nodeRDFType ;
    // Jena graph to check results against.
    static Graph infGraph ;
    static Graph testGraph1 ;
    static Graph testGraph2 ;
    static Graph testGraph3 ;
    
    static Node node(String str) { return NodeFactory.createURI("http://example/"+str) ; }
    
    @BeforeClass public static void setupClass() {
        try { 
            vocab = RDFDataMgr.loadModel("rdfs-vocab.ttl") ;
            data = RDFDataMgr.loadModel("rdfs-data.ttl") ;
            String rules = FileUtils.readWholeFileAsUTF8("rdfs-min.rules") ;
            rules = rules.replaceAll("#[^\\n]*", "") ;
            Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
            //Model m = ModelFactory.createInfModel(reasoner, data);
            /** Rules way */ 
            InfModel m = ModelFactory.createInfModel(reasoner, vocab, data);
            infGraph = m.getGraph() ;
            InferenceSetupRDFS setup = new InferenceSetupRDFS(vocab) ;
            
            /** Compute way */
            testGraph1 = new GraphRDFS(setup, data.getGraph()) ;
            
            /** Expansion way */
            testGraph2 = Factory.createDefaultGraph() ;
            StreamRDF stream = StreamRDFLib.graph(testGraph2) ; 
            stream = new InferenceProcessorStreamRDF(stream, setup) ;
            RDFDataMgr.parse(stream, "rdfs-data.ttl") ;
            
            /** Whole graph expansion, filter */
            /** Compute way */
            testGraph3 = new GraphRDFS3(setup, data.getGraph()) ;
            

            
        } catch (IOException ex ) { IO.exception(ex) ; }
    }
    
    // Check this is coverage.
    
    @Test public void test_calc_rdfs_01()       { test(node("a"), rt, null) ; }
    @Test public void test_calc_rdfs_02()       { test(node("a"), rt, node("T2")) ; }
    @Test public void test_calc_rdfs_03()       { test(null, rt, node("T2")) ; }

    @Test public void test_calc_rdfs_04()       { test(null, rt, node("T2")) ; }
    @Test public void test_calc_rdfs_05()       { test(null, rt, node("T")) ; }
      
    @Test public void test_calc_rdfs_06()       { test(node("c"), rt, null) ; }
      
    @Test public void test_calc_rdfs_07()       { test(null, rt, null) ; }
    @Test public void test_calc_rdfs_08()       { test(null, node("q"), null) ; }
      
    @Test public void test_calc_rdfs_09()       { test(node("z"), null, null) ;  }
    @Test public void test_calc_rdfs_10()       { test(node("z"), rt, null) ; }
      
    @Test public void test_calc_rdfs_11()       { test(null, null, null) ; }
      
    @Test public void test_calc_rdfs_12a()       { test(null, rt, node("P")) ; }
    @Test public void test_calc_rdfs_12b()       { test(null, rt, node("P1")) ; }
    @Test public void test_calc_rdfs_12c()       { test(null, rt, node("P2")) ; }
    @Test public void test_calc_rdfs_12d()       { test(null, null, node("P")) ; }
    @Test public void test_calc_rdfs_12e()       { test(null, null, node("P1")) ; }
    @Test public void test_calc_rdfs_12f()       { test(null, null, node("P2")) ; }

    @Test public void test_calc_rdfs_13a()       { test(null, rt, node("Q")) ; }
    @Test public void test_calc_rdfs_13b()       { test(null, rt, node("Q1")) ; }
    @Test public void test_calc_rdfs_13c()       { test(null, rt, node("Q2")) ; }
    @Test public void test_calc_rdfs_13d()       { test(null, null, node("Q")) ; }
    @Test public void test_calc_rdfs_13e()       { test(null, null, node("Q1")) ; }
    @Test public void test_calc_rdfs_13f()       { test(null, null, node("Q2")) ; }

    
    // all T cases.
    // all U cases.
    @Test public void test_calc_rdfs_14a()       { test(null, rt, node("T")) ; }
    @Test public void test_calc_rdfs_14b()       { test(null, rt, node("T1")) ; }
    @Test public void test_calc_rdfs_14c()       { test(null, rt, node("S2")) ; }
    @Test public void test_calc_rdfs_14d()       { test(null, null, node("T")) ; }
    @Test public void test_calc_rdfs_14e()       { test(null, null, node("T1")) ; }
    @Test public void test_calc_rdfs_14f()       { test(null, null, node("S2")) ; }

    @Test public void test_calc_rdfs_15a()       { test(null, rt, node("U")) ; }
    @Test public void test_calc_rdfs_15b()       { test(null, null, node("U")) ; }

    private void test(Node s, Node p, Node o) {
        Set<Triple> x0 = new HashSet<>() ;
        infGraph.find(s,p,o).forEachRemaining(triple -> {
            Node pred = triple.getPredicate() ;
            if ( ! pred.getNameSpace().equals(RDFS.getURI()) ) { 
                x0.add(triple) ;
            }
        }) ;
        
        //Set<Triple> x2 = Iter.toSet(testGraph2.find(s,p,o)) ;
        
        Set<Triple> x1 = Iter.toSet(testGraph1.find(s,p,o)) ;
        Set<Triple> x2 = Iter.toSet(testGraph2.find(s,p,o)) ;
        Set<Triple> x3 = Iter.toSet(testGraph3.find(s,p,o)) ;
        if ( ! x0.equals(x1) || ! x0.equals(x2) || ! x0.equals(x3) ) {
            System.err.println("Expected: find("+s+", "+p+", "+o+")") ;
            x0.stream().forEach(triple -> {System.err.println("  "+triple) ; }) ;
            if ( ! x0.equals(x1) ) {
                System.err.println("Got (GraphRDFS):") ;
                print(x1) ;
            }
            if ( ! x0.equals(x2) ) {
                System.err.println("Got (Expansion):") ;
                print(x2) ;
            }
            if ( ! x0.equals(x3) ) {
                System.err.println("Got (GraphRDFS3):") ;
                print(x3) ;
            }
            System.err.println() ;
        }
        
        Assert.assertEquals("GraphRDFS", x0, x1) ;
        Assert.assertEquals("Expansion stream", x0, x2) ;
        Assert.assertEquals("Expand/filter", x0, x3) ;
        
    }
    
    static private void print(Set<Triple> x) {
        if ( x.isEmpty() )
            System.err.println("  {}") ;
        else
            x.stream().forEach(triple -> {System.err.println("  "+triple) ; }) ;
    }
}

