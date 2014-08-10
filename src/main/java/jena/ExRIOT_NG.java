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

package jena ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFBase ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Reading one named graph from a N-Quads/TriG/JSON-LD source */ 
public class ExRIOT_NG {

    public static void main(String[] args) {
        
        // The storage destination.
        Model model = ModelFactory.createDefaultModel() ;
        StreamRDF dest = StreamRDFLib.graph(model.getGraph()) ;
        Node gn = NodeFactory.createURI("http://example/graphXYZ") ;
        
        // Set up filter for the parser process to catch a named graph
        // and direct to the graph for 'model'. 
        StreamRDF destParsed = new StreamRDF_PickOneGraph(gn, dest) ;
        RDFDataMgr.parse(destParsed, "data.trig") ;
        
        // Output ...
        RDFDataMgr.write(System.out, model, Lang.TTL) ;
    }

    /** Filter quads to pick those with particular */
    public static class StreamRDF_PickOneGraph extends StreamRDFBase {
        
        private final StreamRDF dest ;
        private final Node graphName ;

        public StreamRDF_PickOneGraph(Node graphName, StreamRDF dest) {
            this.dest = dest ;
            this.graphName = graphName ;
        }
        
        @Override
        public void triple(Triple triple) {
            // Triples - default graph of the parsed source.
            // For convenience, graph name of null is
            // the default graph. Not a named graph.  
            if ( graphName == null )
                dest.triple(triple) ;
        }

        @Override
        public void quad(Quad quad) {
            if ( Lib.equal(graphName, quad.getGraph()) )
                dest.triple(quad.asTriple()) ;
        }

        /** Send base to the destination */
        @Override
        public void base(String base) {
            dest.base(base) ;
        }

        /** Send prefixes to the destination */
        @Override
        public void prefix(String prefix, String iri) {
            dest.prefix(prefix, iri) ;
        }
    }
}

