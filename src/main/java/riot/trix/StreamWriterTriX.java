/*
 *  Copyright 2013, 2014 Andy Seaborne
 *
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
 */

package riot.trix;

import static riot.trix.WriterTriX.endTag ;
import static riot.trix.WriterTriX.startTag ;
import static riot.trix.WriterTriX.startXML ;
import static riot.trix.WriterTriX.write ;

import java.io.OutputStream ;
import java.util.Objects ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Write TriX.
 */
public class StreamWriterTriX implements StreamRDF {
    private static String rdfXMLLiteral = XMLLiteralType.theXMLLiteralType.getURI() ;
    private IndentedWriter out ;
    private Node gn = null ;
    private boolean inGraph = false ; 
    private PrefixMap pmap = PrefixMapFactory.create() ;

    public StreamWriterTriX(OutputStream out) { this.out = new IndentedWriter(out) ; }
    public StreamWriterTriX(IndentedWriter out) { this.out = out ; }

    // Batching.
    @Override public void start() {
        startXML(out) ;
        startTag(out, TriX.tagTriX, "xmlns", TriX.NS) ;
        out.println() ;
    }
    
    @Override public void base(String base) {} // Ignore.
    
    @Override public void prefix(String prefix, String iri) {
        pmap.add(prefix, iri) ;
    }
    
    @Override public void finish() {
        if ( inGraph ) {
            endTag(out, TriX.tagGraph) ;
            out.println() ;
        }
        endTag(out, TriX.tagTriX) ;
        out.println() ;
        out.flush() ;
    }
    
    @Override
    public void triple(Triple triple) {
        if ( inGraph && gn != null ) {
            endTag(out, TriX.tagGraph) ;
            out.println() ;
            inGraph = false ;
        }
        
        if ( ! inGraph ) {
            startTag(out, TriX.tagGraph) ;
            out.println() ;
        }
        inGraph = true ;
        gn = null ;
        //end graph?
        write(out, triple, pmap) ;
    }

    @Override
    public void quad(Quad quad) {
        Node g = quad.getGraph() ;
        
        if ( inGraph ) {
            if ( Objects.equals(g, gn) ) {
                endTag(out, TriX.tagGraph) ;
                out.println() ;
                inGraph = false ;
            }
        }
        if ( ! inGraph ) {
            startTag(out, TriX.tagGraph) ;
            out.println() ;
            if ( gn != null && ! Quad.isDefaultGraph(gn) ) {
                gn = quad.getGraph() ;
                write(out, gn, pmap) ;
            }
        }
        inGraph = true ;
        gn = g ;
        write(out, quad.asTriple(), pmap) ;

    }
    
    private void graph(Node gn2) {
        if ( ! Objects.equals(gn, gn2) ) {
            
        }
    }
}

