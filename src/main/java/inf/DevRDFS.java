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

public class DevRDFS {
    static { LogCtl.setLog4j() ; }
    
    
    // Think through the cases.
    
    // TODO : GraphRDFS2 -- implicit rdf:type (range/domain)
    // GraphRDFS2::stream(s,p,o) -> Stream<Triple>
    
    // Special case more cases.
    // TODO: Check all inf.* needed
    
    static Graph inf ;
    static Graph g_rdfs2 ; 
    
    public static void main(String...argv) throws IOException {
        Model vocab = RDFDataMgr.loadModel("D-vocab.ttl") ;
        Model data = RDFDataMgr.loadModel("D-data.ttl") ;

        
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

        InferenceSetupRDFS setup = new InferenceSetupRDFS(vocab) ;
        
        Node rt = NodeConst.nodeRDFType ;
        g_rdfs2 = new GraphRDFS(setup, data.getGraph()) ;
        
        test(null, null, node("P")) ;
    }
    
    private static void test(Node s, Node p, Node o) {
        dwim(g_rdfs2, inf, s,p,o) ;
    }

    static Node node(String str) { return NodeFactory.createURI("http://example/"+str) ; }
    
    static void dwim(Graph g1, Graph g2, Node s, Node p , Node o) {
        System.out.println("** Graph 1:") ; 
        dwim$(g1, s,p,o) ;
        System.out.println("** Graph 2:") ; 
        dwim$(g2, s,p,o) ;
        System.out.println() ;
    }
    
    static void dwim(Graph g1, Node s, Node p , Node o) {
        dwim$(g1, s,p,o) ;
        System.out.println() ;
    }
    
    static void dwim$(Graph g, Node s, Node p , Node o) {
        System.out.printf("find(%s, %s, %s)\n", s,p,o) ; 
        ExtendedIterator<Triple> iter = g.find(s, p, o) ;
        for ( ; iter.hasNext() ; )
            System.out.println("    "+iter.next()) ;
    }
}

