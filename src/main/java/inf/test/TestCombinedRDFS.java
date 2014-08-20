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

import inf.GraphRDFS ;
import inf.InfGlobal ;
import inf.InferenceSetupRDFS ;

import java.io.IOException ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.InfModel ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.reasoner.Reasoner ;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner ;
import com.hp.hpl.jena.reasoner.rulesys.Rule ;
import com.hp.hpl.jena.util.FileUtils ;

/** Test of RDFS, with combined data and vocabulary. */
public class TestCombinedRDFS extends AbstractTestRDFS {
    static Model vocab ;
    static Model data ;

    static InferenceSetupRDFS setup ;
    // Jena graph to check results against.
    static Graph infGraph ;
    // The main test target
    static Graph testGraphRDFS ;
    
    static final String DIR = "testing/Inf" ;
    static final String DATA_FILE = DIR+"/rdfs-data.ttl" ;
    static final String VOCAB_FILE = DIR+"/rdfs-vocab.ttl" ;
    
    static boolean original_includeDerivedDataRDFS ;
    
    @BeforeClass public static void setupClass() {
        try {
            /**/
            original_includeDerivedDataRDFS = InfGlobal.includeDerivedDataRDFS ;
            InfGlobal.includeDerivedDataRDFS = true ;
            
            vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
            data = RDFDataMgr.loadModel(DATA_FILE) ;
            
            RDFDataMgr.read(data, VOCAB_FILE) ;
            
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
            
            /* Combined data and vocab files as data. */
            Model mAll = ModelFactory.createDefaultModel() ;
            StreamRDF streamModelAll = StreamRDFLib.graph(mAll.getGraph()) ;
        } catch (IOException ex ) { IO.exception(ex) ; }
    }
    
    @AfterClass public static void resetClass() {
        InfGlobal.includeDerivedDataRDFS = original_includeDerivedDataRDFS ;
    }

    @Override
    protected boolean removeVocabFromReferenceResults()      { return false ; } 
    
    @Override
    protected Graph getReferenceGraph() {
        return infGraph ;
    }

    @Override
    protected Graph getTestGraph() {
        return testGraphRDFS ;
    }

    @Override
    protected String getReferenceLabel() {
        return "Inference" ;
    }

    @Override
    protected String getTestLabel() {
        return "GraphRDFS" ;
    }
}

