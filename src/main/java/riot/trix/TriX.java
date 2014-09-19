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

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFWriter ;
import org.apache.jena.riot.system.StreamRDFWriterFactory ;

public class TriX {
    /*
<!-- TriX: RDF Triples in XML -->
<!ELEMENT TriX (graph*)>
<!ATTLIST TriX xmlns CDATA #FIXED "http://www.w3.org/2004/03/trix/trix-1/">
<!ELEMENT graph (uri*, triple*)>
<!ELEMENT triple ((id|uri|plainLiteral|typedLiteral), uri, (id|uri|plainLiteral|typedLiteral))>
<!ELEMENT id (#PCDATA)>
<!ELEMENT uri (#PCDATA)>
<!ELEMENT plainLiteral (#PCDATA)>
<!ATTLIST plainLiteral xml:lang CDATA #IMPLIED>
<!ELEMENT typedLiteral (#PCDATA)>
<!ATTLIST typedLiteral datatype CDATA #REQUIRED> 
          -----------------
          
     */
    
    public static void init() {
        RDFLanguages.register(TRIX) ;
        ReaderRIOTFactory readerFactory = new ReaderRIOTFactory() {
            @Override
            public ReaderRIOT create(Lang language) {
                return new ReaderTriX() ;
            }} ;
        
        RDFParserRegistry.registerLangTriples(TRIX, readerFactory) ;
        RDFParserRegistry.registerLangQuads(TRIX, readerFactory) ;
        
        WriterGraphRIOTFactory wgFactory = new WriterGraphRIOTFactory() {

            @Override
            public WriterGraphRIOT create(RDFFormat syntaxForm) {
                return new WriterTriX() ;
            } } ;
        WriterDatasetRIOTFactory wdsgFactory = new WriterDatasetRIOTFactory() {

            @Override
            public WriterDatasetRIOT create(RDFFormat syntaxForm) {
                return new WriterTriX() ;
            } } ;
        
        RDFFormat trixFormat = new RDFFormat(TRIX) ;
        RDFWriterRegistry.register(TRIX, trixFormat) ;
        RDFWriterRegistry.register(trixFormat, wgFactory) ;
        RDFWriterRegistry.register(trixFormat, wdsgFactory) ;
        
        StreamRDFWriterFactory streamWriter = new StreamRDFWriterFactory() {
            @Override
            public StreamRDF create(OutputStream output, RDFFormat format) {
                return new StreamWriterTriX(output) ;
            }} ;
        StreamRDFWriter.register(TRIX, trixFormat) ;
        StreamRDFWriter.register(trixFormat, streamWriter) ;
    }
    
    public static final String contentTypeTriX      = "application/trix" ;
    public static final ContentType ctTriX          = ContentType.create(contentTypeTriX) ;
    
    public static Lang TRIX = LangBuilder.create("TriX", contentTypeTriX)
                                         .addAltNames("TRIX", "trix")
                                         .addFileExtensions("trix")
                                         .build() ;
    
    public final static String NS              = "http://www.w3.org/2004/03/trix/trix-1/" ;
    public final static String tagTriX         = "TriX" ;

    public final static String tagGraph        = "graph" ;
    public final static String tagTriple       = "triple" ;
    public final static String tagURI          = "uri" ;
    public final static String tagId           = "id" ;
    public final static String tagQName        = "qname" ;
    public final static String tagPlainLiteral = "plainLiteral" ;
    public final static String tagTypedLiteral = "typedLiteral" ;

    public final static String attrXmlLang     = "lang" ;
    public final static String attrDatatype    = "datatype" ;
}
    

