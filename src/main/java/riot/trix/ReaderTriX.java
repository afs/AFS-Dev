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

import java.io.InputStream ;
import java.io.Reader ;
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
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.resultset.ResultSetException ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class ReaderTriX implements ReaderRIOT {

    private ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd ;
    
    // RIOTLang
//    @Override
//    public ParserProfile getProfile() {
//        return null ;
//    }
//
//    @Override
//    public void setProfile(ParserProfile profile) {}
//
//    @Override
//    public void parse() {}
//
//    @Override
//    public Lang getLang() {
//        return null ;
//    }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        XMLInputFactory xf = XMLInputFactory.newInstance() ;
        XMLStreamReader xReader ;
        try {
            xReader = xf.createXMLStreamReader(in) ;
        } catch (XMLStreamException e) { throw new ResultSetException("Can't initialize StAX parsing engine", e) ; } 
//        } catch (Exception ex) {
//            throw new RiotException("Failed when initializing the StAX parsing engine", ex) ;
//        }
        read(xReader,  baseURI, output) ;
    }
        
    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {}

    
    private static String nsRDF = RDF.getURI() ;
    private static String nsXSD = XSDDatatype.XSD ; // No "#"
    private static String nsXML0 = "http://www.w3.org/XML/1998/namespace" ;
    private static int DepthTriX    = 0 ;
    private static int DepthGraph   = 1 ;
    private static int DepthTriple  = 2 ;
    private static int DepthTerms   = 3 ;
    
    private void read(XMLStreamReader parser, String baseURI, StreamRDF output) {
        int depth = 0 ;
        
        try { 
            while(parser.hasNext()) {
                int event = parser.next() ;
                String tag = null ;
                switch (event) {
                    case XMLStreamConstants.NAMESPACE:
                        System.out.println("namespace") ;
                        break ;
                    case XMLStreamConstants.START_DOCUMENT :
                        System.out.println("Start document") ;
                        break ;
                    case XMLStreamConstants.END_DOCUMENT :
                        System.out.println("END document") ;
                        if ( depth != 0 ) 
                            staxError(parser.getLocation(), "End of document while processing XML element: (depth="+depth+")") ;
                        return ;
                    case XMLStreamConstants.END_ELEMENT :
                        --depth ;
                        tag = parser.getLocalName() ;
//                        System.out.print("End:   ") ;
//                        for ( int i = 0 ; i < depth ; i++) System.out.print("  ") ;
//                        System.out.println(parser.getPrefix()+":"+tag) ;
                        
                        switch(tag) {
                            case TriX.tagTriple:
                                //output.triple() or quad()
                        }
                        
                        break ;
                        
                    case XMLStreamConstants.START_ELEMENT :
                        // Namespaces.
                        // Stack.
                        
                        int  j = parser.getNamespaceCount() ;
                        for ( int i = 0 ; i < j ; i++ ) {
                            String prefix = parser.getNamespacePrefix(i) ;
                            if ( prefix == null )
                                prefix = "" ;
                            String nsUri = parser.getNamespaceURI(i) ;
                            //output.prefix(prefix, nsUri) ;
                            System.out.println(prefix+ ": "+nsUri) ; 
                        }
                        
                        tag = parser.getLocalName() ;
//                        System.out.print("Start:   ") ;
//                        for ( int i = 0 ; i < depth ; i++) System.out.print("  ") ;
//                        String nsURI = parser.getNamespaceURI() ;
//                        System.out.println(parser.getPrefix()+":"+tag) ;
                        QName qname = parser.getName() ;
                        //qname.getLocalPart() ;
                        //qname.getNamespaceURI() ;
                        //qname.getPrefix() ;
                        
                        switch (tag) {
                            // structure
                            case TriX.tagGraph:
                                checkDepth(parser, qname, depth, DepthGraph) ;
                                depth++ ;
                                break ;
                            case TriX.tagTriple:
                                checkDepth(parser, qname, depth, DepthTriple) ;
                                depth++ ;
                                break ;
                            case TriX.tagTriX:
                                checkDepth(parser, qname, depth, DepthTriX) ;
                                depth++ ;
                                break ;
                            // nodes
                            case TriX.tagURI: {
                                // Two uses!
                                //checkDepth(parser, qname, depth, DepthTerms) ;
                                String x = parser.getElementText() ;
                                Node n = NodeFactory.createURI(x) ;
                                System.out.println("U ** "+x) ;
                                break ;
                            }
                            case TriX.tagId: {
                                checkDepth(parser, qname, depth, DepthTerms) ;
                                String x = parser.getElementText() ;
                                Node n = NodeFactory.createURI(x) ;
                                System.out.println("B ** "+x) ;
                                break ;
                            }
                            case TriX.tagPlainLiteral: {
                                checkDepth(parser, qname, depth, DepthTerms) ;
                                // xml:lang
                                
                                int x = parser.getAttributeCount() ;
                                if ( x > 1 )
                                    // Namespaces?
                                    staxError(parser.getLocation(), "Multiple attributes : only one allowed") ;
                                String lang = null ;
                                if ( x == 1 )
                                    lang = attribute(parser, nsXML0, TriX.attrXmlLang) ;
//                                    String attrPX =  parser.getAttributePrefix(0) ;
//                                    String attrLN = parser.getAttributeLocalName(0) ;
//                                    String attrVal = parser.getAttributeValue(0) ;
//                                    System.out.println("   Attr "+attrPX+":"+attrLN+"="+attrVal) ;
                                
                                
                                String lex = parser.getElementText() ;
                                Node n = (lang == null ) ? NodeFactory.createLiteral(lex) : NodeFactory.createLiteral(lex, lang, null) ; 
                                System.out.println("L ** "+n) ;
                                break ;
                            }
                            case TriX.tagTypedLiteral: {
                                checkDepth(parser, qname, depth, DepthTerms) ;
                                int nAttr = parser.getAttributeCount() ;
                                if ( nAttr != 1 )
                                    staxError(parser.getLocation(), "Multiple attributes : only one allowed") ;
                                String dt = attribute(parser, TriX.NS, TriX.attrDatatype) ;
                                if ( dt == null )
                                    staxError(parser.getLocation(), "No datatype attribute") ;
                                RDFDatatype rdt = NodeFactory.getType(dt) ;
                                String lex = parser.getElementText() ;
                                Node n = NodeFactory.createLiteral(lex, rdt) ;
                                System.out.println("T ** "+n) ;
                                break ;
                            }
                            default:
                                staxError(parser.getLocation(), "Unrecognized tag -- "+qname.getPrefix()+":"+qname.getLocalPart()) ;
                        }
                        break ;
                    default :
                }
            }
            staxError(-1, -1, "Premature end of file") ;
            return  ;
        } catch (XMLStreamException ex) {
            ex.printStackTrace(System.err) ;
        }
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

