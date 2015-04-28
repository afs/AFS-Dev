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

package projects.riot_sse;

import java.io.InputStream ;
import java.io.Reader ;
import java.io.StringReader ;

import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.sse.Item ;
import org.apache.jena.sparql.sse.lang.ParseHandler ;
import org.apache.jena.sparql.sse.lang.ParseHandlerPlain ;
import org.apache.jena.sparql.sse.lang.ParseHandlerResolver ;

public class ParserSSE
{
    // TODO PrefixMapping => PrefixMap
    // Prologue => RIOT Prologue
    
    public static Item parse(String string)
    {
        return parse(new StringReader(string), null) ;
    }
    
    public static Item parse(InputStream input, Prologue prologue)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
        return parseWorker(tokenizer, prologue) ;
    }
    
    public static Item parse(Reader reader, Prologue prologue)
    {
        @SuppressWarnings("deprecation")
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(reader) ;
        return parseWorker(tokenizer, prologue) ;
    }
    
 // Short prefix map for convenience (used in parsing, not in writing).
    protected static PrefixMapping defaultDefaultPrefixMapRead = new PrefixMappingImpl() ;
    static {
        defaultDefaultPrefixMapRead.setNsPrefix("rdf",  ARQConstants.rdfPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("rdfs", ARQConstants.rdfsPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("xsd",  ARQConstants.xsdPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("owl" , ARQConstants.owlPrefix) ;
        defaultDefaultPrefixMapRead.setNsPrefix("fn" ,  ARQConstants.fnPrefix) ; 
        defaultDefaultPrefixMapRead.setNsPrefix("ex" ,  "http://example/ns#") ;
        defaultDefaultPrefixMapRead.setNsPrefix("" ,    "http://example/") ;
    }
    
    public static PrefixMapping defaultPrefixMapRead = defaultDefaultPrefixMapRead ;
    public static PrefixMapping getDefaultPrefixMapRead() { return defaultPrefixMapRead ; }
    
    private static Item parseWorker(Tokenizer tokenizer, Prologue prologue)
    {
        if ( prologue == null ) 
            prologue = new Prologue(defaultDefaultPrefixMapRead) ;
        
        ParseHandler handler = createParseHandler(prologue.getPrefixMapping()) ;
        ErrorHandler errHandler = ErrorHandlerFactory.getDefaultErrorHandler() ;
        ParserProfile profile = RiotLib.profile(null, false, true, errHandler) ;
        
        LangSSE parser = new LangSSE(tokenizer, profile, handler) ;
        parser.parse() ;
        return handler.getItem() ;
    }
    
    private static ParseHandler createParseHandler(PrefixMapping pmap)
    {
        if ( true )
        {
            Prologue prologue = new Prologue(pmap) ;
            return new ParseHandlerResolver(prologue) ;
        }
        else
            return new ParseHandlerPlain() ;
    }
}
