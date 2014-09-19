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

import static riot.trix.ReaderTriX.State.GRAPH ;
import static riot.trix.ReaderTriX.State.OUTER ;
import static riot.trix.ReaderTriX.State.TRIPLE ;
import static riot.trix.ReaderTriX.State.TRIX ;

import java.io.InputStream ;
import java.io.Reader ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Objects ;

import javax.xml.namespace.QName ;
import javax.xml.stream.* ;

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.resultset.ResultSetException ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** read TriX */
public class ReaderTriX implements ReaderRIOT {

    private ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd ;

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        XMLInputFactory xf = XMLInputFactory.newInstance() ;
        XMLStreamReader xReader ;
        try {
            xReader = xf.createXMLStreamReader(in) ;
        } catch (XMLStreamException e) { throw new RiotException("Can't initialize StAX parsing engine", e) ; }
        read(xReader,  baseURI, output) ;
    }
        
    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        XMLInputFactory xf = XMLInputFactory.newInstance() ;
        XMLStreamReader xReader ;
        try {
            xReader = xf.createXMLStreamReader(reader) ;
        } catch (XMLStreamException e) { throw new ResultSetException("Can't initialize StAX parsing engine", e) ; } 
        read(xReader,  baseURI, output) ;
    }
    
    private static String nsRDF = RDF.getURI() ;
    private static String nsXSD = XSDDatatype.XSD ; // No "#"
    private static String nsXML0 = "http://www.w3.org/XML/1998/namespace" ;
    private static String rdfXMLLiteral = XMLLiteralType.theXMLLiteralType.getURI() ;
    
    enum State { OUTER, TRIX, GRAPH, TRIPLE }
    
    private void read(XMLStreamReader parser, String baseURI, StreamRDF output) {
        State state = OUTER ;
        Node g = null ;
        List<Node> terms = new ArrayList<>() ; 
        
        try { 
            while(parser.hasNext()) {
                int event = parser.next() ;
                switch (event) {
                    case XMLStreamConstants.NAMESPACE:
                        break ;
                    case XMLStreamConstants.START_DOCUMENT :
                        break ;
                    case XMLStreamConstants.END_DOCUMENT :
                        if ( state != OUTER ) 
                            staxError(parser.getLocation(), "End of document while processing XML element") ;
                        return ;
                    case XMLStreamConstants.END_ELEMENT : {
                        String tag = parser.getLocalName() ;
                        switch(tag) {
                            case TriX.tagTriple:
                                if ( terms.size() != 3 )
                                    staxError(parser.getLocation(), "Wriong number of terms for a triple. Got "+terms.size()) ;
                                Node s = terms.get(0) ;
                                Node p = terms.get(1) ;
                                Node o = terms.get(2) ;
                                if ( p.isLiteral() )
                                    staxError(parser.getLocation(), "Predicate is a literal") ;
                                if ( s.isLiteral() )
                                    staxError(parser.getLocation(), "Subject is a literal") ;
                                if ( g == null )
                                    output.triple(Triple.create(s, p, o)) ;
                                else {
                                    if ( g.isLiteral() )
                                        staxError(parser.getLocation(), "graph name is a literal") ;
                                    output.quad(Quad.create(g, s, p, o)) ;
                                }
                                terms.clear();
                                // Next is either end of <graph> or another <triple>
                                state = GRAPH ; 
                                break ;
                            case TriX.tagGraph:
                                state = TRIX ;
                                g = null ;
                                break ;
                            case TriX.tagTriX:
                                state = OUTER ;
                                break ;
                        }
                        break ;
                    }
                    case XMLStreamConstants.START_ELEMENT : {
                        String tag = parser.getLocalName() ;
                        
                        switch (tag) {
                            case TriX.tagTriX:
                                if ( state != OUTER )
                                    staxErrorOutOfPlaceElement(parser) ;
                                state = TRIX ;
                                break ;
                            // structure
                            case TriX.tagGraph:
                                if ( state != TRIX )
                                    staxErrorOutOfPlaceElement(parser) ;
                                // URI?
                                state = GRAPH ;
                                break ;
                            case TriX.tagTriple: {
                                if ( state != GRAPH )
                                    staxErrorOutOfPlaceElement(parser) ;
                                state = TRIPLE ;
                                break ;
                            }
                            case TriX.tagQName:
                            case TriX.tagURI: {
                                if ( state != GRAPH && state != TRIPLE )
                                    staxErrorOutOfPlaceElement(parser) ;
                                Node n = term(parser) ;
                                if ( state == GRAPH ) {
                                    g = n ;
                                    if ( g.isLiteral() )
                                        staxError(parser.getLocation(), "graph name is a literal") ;
                                }
                                else
                                    terms.add(n) ;
                                break ;
                            }
                            case TriX.tagId:
                            case TriX.tagPlainLiteral:
                            case TriX.tagTypedLiteral: {    
                                if ( state != TRIPLE )
                                    staxErrorOutOfPlaceElement(parser) ;
                                Node n = term(parser) ;
                                terms.add(n) ;
                                break ;
                            }
                            default:
                                staxError(parser.getLocation(), "Unrecognized XML element: "+qnameAsString(parser.getName())) ; 
                                break ;
                        }
                    }
                }
            }
            staxError(-1, -1, "Premature end of file") ;
            return  ;
        } catch (XMLStreamException ex) {
            throw new RiotException("XML error", ex) ;
        }
    }

    private void staxErrorOutOfPlaceElement(XMLStreamReader parser) {
        QName qname = parser.getName() ;
        staxError(parser.getLocation(), "Out of place XML element: "+qname.getPrefix()+":"+qname.getLocalPart()) ; 
    }    

    private Node term(XMLStreamReader parser) throws XMLStreamException {
        String tag = parser.getLocalName() ;
        switch(tag) {
            case TriX.tagURI: {
                // Two uses!
                String x = parser.getElementText() ;
                Node n = NodeFactory.createURI(x) ;
                return n ; 
            }
            case TriX.tagQName: {
                String x = parser.getElementText() ;
                int idx = x.indexOf(':') ;
                if ( idx == -1 )
                    staxError(parser.getLocation(), "Expected ':' in prefixed name.  Found "+x) ;
//                int idx2 = x.lastIndexOf(':') ;
//                if ( idx != idx2 )
//                    staxError(parser.getLocation(), "Exactly one ':' expected in prefixed name.  Found "+x) ;
                String[] y = x.split(":", 2) ;  // Allows additional ':'
                
                String prefUri = parser.getNamespaceURI(y[0]) ;
                String local = y[1] ; 
                Node n = NodeFactory.createURI(prefUri+local) ;
                return n ;
            }
            case TriX.tagId: {
                String x = parser.getElementText() ;
                Node n = NodeFactory.createURI(x) ;
                // XXX MAP bnode.
                return NodeFactory.createAnon(new AnonId(x)) ;
            }
            case TriX.tagPlainLiteral: {
                // xml:lang
                int x = parser.getAttributeCount() ;
                if ( x > 1 )
                    // Namespaces?
                    staxError(parser.getLocation(), "Multiple attributes : only one allowed") ;
                String lang = null ;
                if ( x == 1 )
                    lang = attribute(parser, nsXML0, TriX.attrXmlLang) ;
                String lex = parser.getElementText() ;
                Node n = (lang == null ) ? NodeFactory.createLiteral(lex) : NodeFactory.createLiteral(lex, lang, null) ; 
                return n ;
            }
            case TriX.tagTypedLiteral: {
                int nAttr = parser.getAttributeCount() ;
                if ( nAttr != 1 )
                    staxError(parser.getLocation(), "Multiple attributes : only one allowed") ;
                String dt = attribute(parser, TriX.NS, TriX.attrDatatype) ;
                if ( dt == null )
                    staxError(parser.getLocation(), "No datatype attribute") ;
                RDFDatatype rdt = NodeFactory.getType(dt) ;

                String lex = (rdfXMLLiteral.equals(dt)) 
                    ? slurpRDFXMLLiteral(parser)
                    : parser.getElementText() ;
                Node n = NodeFactory.createLiteral(lex, rdt) ;
                return n ; 
            }
            default: {
                QName qname = parser.getName() ;
                staxError(parser.getLocation(), "Unrecognized tag -- "+qnameAsString(qname)) ;
                return null ;
            }
        }
    }
    
    private String slurpRDFXMLLiteral(XMLStreamReader parser) throws XMLStreamException {
        StringBuffer content = new StringBuffer();
        int depth = 0 ;
        
        while(parser.hasNext()) {
            int event = parser.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT: {
                    QName qname = parser.getName() ;
                    String x = qnameAsString(qname) ;
                    content.append("<"+x+">") ;
                    depth++ ;
                    break ;
                }
                case XMLStreamConstants.END_ELEMENT: {
                    depth-- ;
                    if ( depth == -1 ) {
                        // Close tag of typed Literal.
                        return content.toString();
                    }
                    QName qname = parser.getName() ;
                    String x = qnameAsString(qname) ;
                    content.append(x) ;
                    content.append("</"+x+">") ;
                    // Final whitespace?
                    break ;
                }
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.CDATA:
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.ENTITY_REFERENCE:
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                case XMLStreamConstants.COMMENT:
                    content.append(parser.getText()) ;
                    break ;
                case XMLStreamConstants.END_DOCUMENT:
                    staxError(parser.getLocation(), "End of file") ;
            }
        }
        staxError(parser.getLocation(), "End of file") ;
        return null ;
    }
    
    
//    public String getElementText() throws XMLStreamException {
//
//        if(getEventType() != XMLStreamConstants.START_ELEMENT) {
//            throw new XMLStreamException(
//            "parser must be on START_ELEMENT to read next text", getLocation());
//        }
//        int eventType = next();
//        StringBuffer content = new StringBuffer();
//        while(eventType != XMLStreamConstants.END_ELEMENT ) {
//            if(eventType == XMLStreamConstants.CHARACTERS
//            || eventType == XMLStreamConstants.CDATA
//            || eventType == XMLStreamConstants.SPACE
//            || eventType == XMLStreamConstants.ENTITY_REFERENCE) {
//                content.append(getText());
//            } else if(eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
//            || eventType == XMLStreamConstants.COMMENT) {
//                // skipping
//            } else if(eventType == XMLStreamConstants.END_DOCUMENT) {
//                throw new XMLStreamException("unexpected end of document when reading element text content");
//            } else if(eventType == XMLStreamConstants.START_ELEMENT) {
//                throw new XMLStreamException(
//                "elementGetText() function expects text only elment but START_ELEMENT was encountered.", getLocation());
//            } else {
//                throw new XMLStreamException(
//                "Unexpected event type "+ eventType, getLocation());
//            }
//            eventType = next();
//        }
//        return content.toString();
//    }
    
    
    private String qnameAsString(QName qname) {
        String x = qname.getPrefix() ;
        if ( x == null || x.isEmpty() )
            return qname.getLocalPart() ;
        return x+":"+qname.getLocalPart() ;
    }


    private String attribute(XMLStreamReader parser, String nsURI, String localname) {
        int x = parser.getAttributeCount() ;
        if ( x > 1 )
            // Namespaces?
            staxError(parser.getLocation(), "Multiple attributes : only one allowed : "+tagName(parser)) ;
        if ( x == 0 )
            return null ;
        
        String attrPX =  parser.getAttributePrefix(0) ;
        String attrNS =  parser.getAttributeNamespace(0) ;
        if ( attrNS == null )
            attrNS = parser.getName().getNamespaceURI() ;
        String attrLN = parser.getAttributeLocalName(0) ;
        if ( ! Objects.equals(nsURI, attrNS) || ! Objects.equals(attrLN, localname) ) {
            staxError(parser.getLocation(), "Unexpected attribute : "+attrPX+":"+attrLN+" at "+tagName(parser)) ;
        }
        String attrVal = parser.getAttributeValue(0) ;
        return attrVal ;  
    }
    
    private String tagName(XMLStreamReader parser) {
        String prefix = parser.getPrefix() ;
        if ( prefix == null )
            prefix = "" ;
        return prefix+":"+parser.getLocalName() ;
    }
    
    private void checkDepth(XMLStreamReader parser, QName qname, int depth, int expectedDepth) {
        if ( depth != expectedDepth) {
            String x = "L:"+parser.getLocation().getLineNumber() ;
            throw new RiotException(x+"  Out of place: "+qname.getPrefix()+":"+qname.getLocalPart()) ; 
        }
    }

    private boolean isTag(String localName, String expectedName) {
//        if ( !parser.getNamespaceURI().equals(XMLResults.baseNamespace) )
//            return false ;
        return localName.equals(expectedName) ;
    }

    private void staxError(Location loc, String msg) {
        staxError(loc.getLineNumber(), loc.getColumnNumber(), msg) ;
    }

    private void staxError(int line, int col, String msg) {
        getErrorHandler().error(msg, line, col) ;
        throw new RiotException(msg) ;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler ;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler ; }

    @Override
    public ParserProfile getParserProfile() {
        return null ;
    }

    @Override
    public void setParserProfile(ParserProfile profile) {}
}

