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
    

