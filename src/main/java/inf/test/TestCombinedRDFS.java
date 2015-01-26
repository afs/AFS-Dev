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

import inf.GraphRDFS ;
import inf.InferenceSetupRDFS ;

import java.util.List ;

import org.apache.jena.riot.RDFDataMgr ;
import org.junit.BeforeClass ;

import com.hp.hpl.jena.graph.* ;

/** Test of RDFS, with combined data and vocabulary. */
public class TestCombinedRDFS extends AbstractTestGraphRDFS {

    static Graph testGraphRDFS_X ;

    @BeforeClass public static void setupClass() {
        vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
        data = RDFDataMgr.loadModel(DATA_FILE) ;
        // And the vocabulary
        RDFDataMgr.read(data, VOCAB_FILE) ;
        
        infGraph = createRulesGraph(data, vocab, RULES_FILE) ;
        setup = new InferenceSetupRDFS(vocab, true) ;
        /** Compute way */
        testGraphRDFS_X = new GraphRDFS(setup, data.getGraph()) ;
    }
    
    @Override
    protected Graph createGraphRDFS(InferenceSetupRDFS setup, Graph data) {
        return testGraphRDFS_X ;
    }
    
    @Override
    protected List<Triple> findInTestGraph(Node s, Node p, Node o) {
        List<Triple> x = getTestGraph().find(s,p,o).toList() ;
        // This should not be necessary.
        //x = Lib8.toList(x.stream().distinct()) ;
        return x ;
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
        return "Combined GraphRDFS" ;
    }
}


