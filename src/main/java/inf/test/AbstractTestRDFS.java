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

package inf.test;

import inf.* ;

import java.io.IOException ;
import java.util.Collection ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
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

public abstract class AbstractTestRDFS extends BaseTest {
    static { LogCtl.setCmdLogging(); }
    
    static Model vocab ;
    static Model data ;
    static Node rt = NodeConst.nodeRDFType ;

    static InferenceSetupRDFS setup ;
    // Jena graph to check results against.
    static Graph infGraph ;
    static Graph testGraphRDFS ;
    static Graph testGraphExpanded ;
    static Graph testGraphFilterAll ;
    static Graph testGraphDataVocab ;
    
    static Node node(String str) { return NodeFactory.createURI("http://example/"+str) ; }
    
    static final String DATA_FILE = "rdfs-data.ttl" ;
    static final String VOCAB_FILE = "rdfs-vocab.ttl" ;
    
    @BeforeClass public static void setupClass() {
        try { 
            boolean VOCAB_IN_DATA = false ;
            if ( VOCAB_IN_DATA )
                InfGlobal.includeDerivedDataRDFS = true ;
            
            vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
            data = RDFDataMgr.loadModel(DATA_FILE) ;
            setup = new InferenceSetupRDFS(vocab) ;
            
            {
                String rules = FileUtils.readWholeFileAsUTF8("rdfs-min.rules") ;
                rules = rules.replaceAll("#[^\\n]*", "") ;
                Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
                //Model m = ModelFactory.createInfModel(reasoner, data);
                /** Rules way */ 
                InfModel m = ModelFactory.createInfModel(reasoner, vocab, data);
                infGraph = m.getGraph() ;
            }
            
            /** Compute way */
            testGraphRDFS = new GraphRDFS(setup, data.getGraph()) ;
            
            /** Expansion way (no vocab in the data) */
            testGraphExpanded = Factory.createDefaultGraph() ;
            StreamRDF stream = StreamRDFLib.graph(testGraphExpanded) ;
            stream = new InferenceProcessorStreamRDF(stream, setup) ;
            RDFDataMgr.parse(stream, DATA_FILE) ;
            //Vocab in data.
            if ( VOCAB_IN_DATA )
                RDFDataMgr.parse(stream, VOCAB_FILE) ;
            
            /** Whole graph expansion, filter  (no vocab in the data) */
            testGraphFilterAll = Factory.createDefaultGraph() ;
            testGraphFilterAll = new GraphRDFS3(setup, data.getGraph()) ;
            if ( VOCAB_IN_DATA )
                RDFDataMgr.read(testGraphFilterAll, "rdfs-vocab.ttl") ;
            
            /* Combined data and vocab files as data. */
            Model mAll = ModelFactory.createDefaultModel() ;
            StreamRDF streamModelAll = StreamRDFLib.graph(mAll.getGraph()) ;
            
            //StreamRDF s = new InferenceProcessorStreamRDF(streamModelAll, setup) ;
            RDFDataMgr.parse(streamModelAll, VOCAB_FILE) ;             //Expand (subclass foo) ** ?? **
            RDFDataMgr.parse(streamModelAll, DATA_FILE) ; //Data direct
            testGraphDataVocab = new GraphRDFS(setup, mAll.getGraph()) ;
            
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
    
    @Test public void test_calc_rdfs_16a()       { test(null, null, node("X")) ; }
    @Test public void test_calc_rdfs_16b()       { test(null, rt, node("X")) ; }

    static public Set<Triple> filterRDFS(Set<Triple> x) {
        Set<Triple> x0 = new HashSet<>() ;
        x.stream().forEach(triple -> {
            Node pred = triple.getPredicate() ;
            if ( InfGlobal.includeDerivedDataRDFS ) {
//                if ( ! pred.equals(RDFS.domain.asNode()) &&
//                     ! pred.equals(RDFS.range.asNode()) )
                    x0.add(triple) ;
            } else {
                if (! pred.getNameSpace().equals(RDFS.getURI()) ) 
                    x0.add(triple) ;
            }
        }) ;
        return x0 ;
    }
    
    private void test(Node s, Node p, Node o) {
        Set<Triple> x0 = infGraph.find(s,p,o).toSet() ;
        x0 = filterRDFS(x0) ;
        
        //Set<Triple> x2 = Iter.toSet(testGraph2.find(s,p,o)) ;
        
//        Model vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
//        Model data = RDFDataMgr.loadModel(DATA_FILE) ;
//        InferenceSetupRDFS setup = new InferenceSetupRDFS(vocab) ;
//        testGraphRDFS = new GraphRDFS(setup, data.getGraph()) ;
        
        
        
        List<Triple> x1 = Iter.toList(testGraphRDFS.find(s,p,o)) ;
        List<Triple> x2 = Iter.toList(testGraphExpanded.find(s,p,o)) ;
        List<Triple> x3 = Iter.toList(testGraphFilterAll.find(s,p,o)) ;
        if ( ! equals(x0, x1) || ! equals(x0, x2) || ! equals(x0, x3) ) {
            System.err.println("Expected: find("+s+", "+p+", "+o+")") ;
            x0.stream().forEach(triple -> {System.err.println("  "+triple) ; }) ;
            if ( ! equals(x0, x1) ) {
                System.err.println("Got (GraphRDFS):") ;
                print(x1) ;
            }
            if ( !equals(x0, x2) ) {
                System.err.println("Got (Expansion):") ;
                print(x2) ;
            }
            if ( ! equals(x0, x3) ) {
                System.err.println("Got (GraphRDFS3):") ;
                print(x3) ;
            }
            System.err.println() ;
        }
        
        Assert.assertTrue("GraphRDFS", equals(x0, x1)) ;
        Assert.assertTrue("Expansion stream", equals(x0, x2)) ;
        Assert.assertTrue("Expand/filter", equals(x0, x3)) ;

        if ( false ) {
            //Inf graph and data+vocab

            Set<Triple> x8 = Iter.toSet(infGraph.find(s,p,o)) ;
            Set<Triple> x9 = Iter.toSet(testGraphDataVocab.find(s, p, o)) ;
            if ( ! equals(x8, x9) ) {
                System.err.println("Expected (inf): find("+s+", "+p+", "+o+")") ;
                x8.stream().forEach(triple -> {System.err.println("  "+triple) ; }) ;
                System.err.println("Got (combined):") ;
                print(x9) ;
                System.err.println("Missed:") ;
                diff(x8, x9) ;
            }
            Assert.assertTrue("Data+vocab", equals(x8, x9)) ;
        }
    }
    
    static void diff(Set<Triple> A, Set<Triple> B) {
        for ( Triple t : A ) {
            if ( ! B.contains(t) )
                System.err.println("> "+t) ;
        }
    }
    
    static private boolean equals(Collection<Triple> triples1, Collection<Triple> triples2) {
        if ( triples1.size() != triples2.size() ) 
            return false ;
        for ( Triple t : triples1 )
            if ( !triples2.contains(t) )
                return false ;
        for ( Triple t : triples2 )
            if ( !triples1.contains(t) )
                return false ;
        return true ;
    }
    
    static private void print(Collection<Triple> x) {
        if ( x.isEmpty() )
            System.err.println("  {}") ;
        else
            x.stream().forEach(triple -> {System.err.println("  "+triple) ; }) ;
    }
}

