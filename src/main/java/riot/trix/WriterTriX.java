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
import java.io.Writer ;
import java.util.Iterator ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.WriterDatasetRIOT ;
import org.apache.jena.riot.WriterGraphRIOT ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.RiotLib ;

import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/** Write TriX.
 */
public class WriterTriX implements WriterDatasetRIOT, WriterGraphRIOT {
    private static String rdfXMLLiteral = XMLLiteralType.theXMLLiteralType.getURI() ;

    // Dataset
    @Override
    public void write(OutputStream out, DatasetGraph datasetGraph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, datasetGraph, prefixMap, baseURI, null) ;
    }

    @Override
    public void write(Writer out, DatasetGraph datasetGraph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = RiotLib.create(out) ;
        write(iOut, datasetGraph, prefixMap, baseURI, null) ;
    }

    private void write(IndentedWriter out, DatasetGraph datasetGraph, PrefixMap prefixMap, String baseURI, Context context) {
        startXML(out) ;
        startTag(out, TriX.tagTriX, "xmlns", TriX.NS) ;
        out.println() ;
        writeOneGraph(out, datasetGraph.getDefaultGraph(), prefixMap, null) ;
        Iterator<Node> iter = datasetGraph.listGraphNodes() ;
        while ( iter.hasNext() ) {
            Node gn = iter.next() ;
            Graph g = datasetGraph.getGraph(gn) ;
            writeOneGraph(out, g, prefixMap, gn) ;
        }
        
        endTag(out, TriX.tagTriX) ;
        out.println() ;
        //writeTrailer() ;
        out.flush();
    }

    @Override
    public Lang getLang() {
        return TriX.TRIX ;
    }
    
    // Graph
    @Override
    public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, graph, prefixMap, baseURI, null) ;
    }

    @Override
    public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = RiotLib.create(out) ;
        write(iOut, graph, prefixMap, baseURI, null) ;
    }

    static void write(IndentedWriter out, Graph graph, PrefixMap prefixMap, String baseURI, Object context) {
        startXML(out) ;
        startTag(out, TriX.tagTriX, "xmlns", TriX.NS) ;
        out.println() ;
        writeOneGraph(out, graph, prefixMap, null) ;
        endTag(out, TriX.tagTriX) ;
        out.println() ;
        out.flush();
    }
    
    static void writeOneGraph(IndentedWriter out, Graph graph, PrefixMap prefixMap, Node graphURI) {
        ExtendedIterator<Triple> iter = graph.find(null, null, null) ;
        if ( ! iter.hasNext() )
            return ;
        startTag(out, TriX.tagGraph) ;
        out.println() ;
        if ( graphURI != null ) {
            write(out, graphURI, prefixMap) ;
        }
        write(out, iter, prefixMap) ;
        endTag(out, TriX.tagGraph) ;
        out.println() ;
    }
    

    static void write(IndentedWriter out, Iterator<Triple> triples, PrefixMap prefixMap) {
        while(triples.hasNext()) {
            write(out, triples.next(), prefixMap) ;
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

