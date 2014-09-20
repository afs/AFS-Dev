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

import java.io.OutputStream ;
import java.util.Objects ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Write TriX. */
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
        StreamWriterTriX.startXML(out) ;
        StreamWriterTriX.startTag(out, TriX.tagTriX, "xmlns", TriX.NS) ;
        out.println() ;
    }
    
    @Override public void base(String base) {} // Ignore.
    
    @Override public void prefix(String prefix, String iri) {
        pmap.add(prefix, iri) ;
    }
    
    @Override public void finish() {
        if ( inGraph ) {
            StreamWriterTriX.endTag(out, TriX.tagGraph) ;
            out.println() ;
        }
        StreamWriterTriX.endTag(out, TriX.tagTriX) ;
        out.println() ;
        out.flush() ;
    }
    
    @Override
    public void triple(Triple triple) {
        if ( inGraph && gn != null ) {
            StreamWriterTriX.endTag(out, TriX.tagGraph) ;
            out.println() ;
            inGraph = false ;
        }
        
        if ( ! inGraph ) {
            StreamWriterTriX.startTag(out, TriX.tagGraph) ;
            out.println() ;
        }
        inGraph = true ;
        gn = null ;
        //end graph?
        StreamWriterTriX.write(out, triple, pmap) ;
    }

    @Override
    public void quad(Quad quad) {
        Node g = quad.getGraph() ;
        
        if ( inGraph ) {
            if ( ! Objects.equals(g, gn) ) {
                StreamWriterTriX.endTag(out, TriX.tagGraph) ;
                out.println() ;
                inGraph = false ;
            }
        }
        if ( ! inGraph ) {
            StreamWriterTriX.startTag(out, TriX.tagGraph) ;
            out.println() ;
            if ( gn == null || ! Quad.isDefaultGraph(gn) ) {
                gn = quad.getGraph() ;
                StreamWriterTriX.write(out, gn, pmap) ;
            }
        }
        inGraph = true ;
        gn = g ;
        StreamWriterTriX.write(out, quad.asTriple(), pmap) ;

    }
    
    private void graph(Node gn2) {
        if ( ! Objects.equals(gn, gn2) ) {
            
        }
    }

    static void write(IndentedWriter out, Triple triple, PrefixMap prefixMap) {
        out.println("<triple>") ;
        out.incIndent();
        write(out, triple.getSubject(), prefixMap) ;
        write(out, triple.getPredicate(), prefixMap) ;
        write(out, triple.getObject(), prefixMap) ;
        out.decIndent();
        out.println("</triple>") ;
    }
    
    static void write(IndentedWriter out, Node node, PrefixMap prefixMap) {
        // The decent use of TriX is very regular output as we do not use <qname>. 
        if ( node.isURI() ) {
            String uri = node.getURI() ;
            if ( prefixMap != null ) {
                String abbrev = prefixMap.abbreviate(uri) ;
                if ( abbrev != null ) {
                    startTag(out, TriX.tagQName) ;
                    writeStringEsc(out, abbrev) ;
                    endTag(out, TriX.tagQName) ;
                    return ;
                }
            }
            
            startTag(out, TriX.tagURI) ;
            writeStringEsc(out, node.getURI()) ;
            endTag(out, TriX.tagURI) ;
            out.println() ;
            return ;
        }
        
        if ( node.isBlank() ) {
            startTag(out, TriX.tagId) ;
            writeStringEsc(out, node.getBlankNodeLabel()) ;
            endTag(out, TriX.tagId) ;
            out.println() ;
            return ;
        }
        
        if ( node.isLiteral() ) {
            // RDF 1.1
            String lang = node.getLiteralLanguage() ;
            if ( lang != null && lang.isEmpty() )
                lang = null ;
            
            String dt = node.getLiteralDatatypeURI() ;
            if ( lang != null ) {
                startTag(out, TriX.tagPlainLiteral, TriX.attrXmlLang, lang) ;
                writeStringEsc(out, node.getLiteralLexicalForm()) ;
                endTag(out, TriX.tagPlainLiteral) ;
                out.println() ;
                return ;
            }
            
            if ( dt == null ) {
                startTag(out, TriX.tagPlainLiteral) ;
                writeStringEsc(out, node.getLiteralLexicalForm()) ;
                endTag(out, TriX.tagPlainLiteral) ;
                out.println() ;
                return ;
            }
            
            //if ( lang == null && dt != null )
    
            startTag(out, TriX.tagTypedLiteral, TriX.attrDatatype, dt) ;
            String lex = node.getLiteralLexicalForm() ;
            if ( rdfXMLLiteral.equals(dt) )
                out.print(lex) ;    // Write raw
            else
                writeStringEsc(out, lex) ;
            endTag(out, TriX.tagTypedLiteral) ;
            out.println() ;
            return ;
            //throw new RiotException("internal error") ;
        }
        
        throw new RiotException("Not a concrete node: "+node) ;
    }
    static void writeStringEsc(IndentedWriter out, String string) {
        //throw new NotImplementedException() ;
        out.print(string) ;
    }
    static void startXML(IndentedWriter out) {
        //out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") ;
    }
    static void startTag(IndentedWriter out, String text) {
        out.print("<") ;
        out.print(text) ;
        out.print(">") ;
        out.incIndent();
    }
    static void startTag(IndentedWriter out, String text, String attr, String attrValue) {
        out.print("<") ;
        out.print(text) ;
        out.print(" ") ;
        out.print(attr) ;
        out.print("=\"") ;
        out.print(attrValue) ;  // No need to escape.
        out.print("\"") ;
        out.print(">") ;
        out.incIndent();
    }
    static void endTag(IndentedWriter out, String text) {
        out.decIndent();
        out.print("</") ;
        out.print(text) ;
        out.print(">") ;
    }
}

