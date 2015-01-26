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

package inf.test;

import inf.InferenceProcessorStreamRDF ;
import inf.InferenceSetupRDFS ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.junit.BeforeClass ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.Model ;

/** Test of RDFS, with separate data and vocabulary, no RDFS in the deductions. */
public class TestExpandRDFS extends AbstractTestRDFS {
    static Model vocab ;
    static Model data ;

    static InferenceSetupRDFS setup ;
    // Jena graph to check results against.
    static Graph infGraph ;
    // The main test target
    static Graph testGraphExpanded ;
    
    static final String DIR = "testing/Inf" ;
    static final String DATA_FILE = DIR+"/rdfs-data.ttl" ;
    static final String VOCAB_FILE = DIR+"/rdfs-vocab.ttl" ;
    static final String RULES_FILE = DIR+"/rdfs-min.rules" ;
    
    @BeforeClass public static void setupClass() {
        vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
        data = RDFDataMgr.loadModel(DATA_FILE) ;
        setup = new InferenceSetupRDFS(vocab) ;
        
        infGraph = createRulesGraph(data, vocab, RULES_FILE) ;

        // Expansion Graph
        testGraphExpanded = Factory.createDefaultGraph() ;
        StreamRDF stream = StreamRDFLib.graph(testGraphExpanded) ;
        stream = new InferenceProcessorStreamRDF(stream, setup) ;
        RDFDataMgr.parse(stream, DATA_FILE) ;
    }

    @Override
    protected Graph getReferenceGraph() {
        return infGraph ;
    }

    @Override
    protected Graph getTestGraph() {
        return testGraphExpanded ;
    }

    @Override
    protected String getReferenceLabel() {
        return "Inference" ;
    }

    @Override
    protected String getTestLabel() {
        return "Expanded" ;
    }
}

