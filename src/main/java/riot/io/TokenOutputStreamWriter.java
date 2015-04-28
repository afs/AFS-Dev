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

package riot.io;

import java.io.IOException ;
import java.io.Writer ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.tokens.Token ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.util.FmtUtils ;

public class TokenOutputStreamWriter implements TokenOutputStream
{
    // Both token output stream and tuple abbreviation rules.
    // Separate out abbreviation/LastTuple support?
    
    // Will need rework to improve performance.
    // XXX TokenWriter. See tokenToString
    
    private static Logger log = LoggerFactory.getLogger(TokenOutputStreamWriter.class) ;
    
    // Whether to space out the tuples a bit for readability.
    private static final boolean GAPS = true ;
    
    private final Writer out ;
    private List<Object> lastTuple = null ;
    private boolean inTuple = false ;
    private boolean inSection = false ;
    private List<Object> thisTuple = new ArrayList<Object> () ;
    
    private PrefixMap pmap = PrefixMapFactory.create() ;

    private String label ;
    
    public TokenOutputStreamWriter(String label, Writer out)
    {
        this.out = out ;
        this.label = label ;
    }
    
    @Override
    public Writer getWriter()
    {
        return out ;
    }

    // Really want to directly insert in to the output byte buffer.
    // Optimization for later - get somethign working for now
    // (and it may well be fast enough anyway).
    
    public void setPrefixMapping(String prefix, String uri)
    {
        String pf = prefix ;
        if ( pf.endsWith(":") )
            pf = pf.substring(0, pf.length()-1) ;
        else
            prefix = prefix + ":" ;
        pmap.add(pf, uri) ;
        startTuple() ;
        lastTuple = null ;
        accumulate("@prefix") ;
        gap(true) ;
        accumulate(prefix) ;
        gap(true) ;
        accumulate("<") ;
        accumulate(uri) ;
        accumulate(">") ;
        gap(false) ;
        endTuple() ;
    }
    
    @Override
    public void sendToken(Token token)
    {
        remember(token) ;
        String string = tokenToString(token) ;
        accumulate(string) ;
        gap(true) ;
    }
    
    @Override
    public void sendNode(Node node)
    {
        remember(node) ;
        String x = NodeFmtLib.str(node, null, pmap) ;
        accumulate(x) ;
        gap(false) ;
    }

    @Override
    public void sendString(String string)
    {
        remember(string) ;
        accumulate("\"") ;
        accumulate(string) ; // escapes 
        accumulate("\"") ;
        gap(false) ;
    }
    
    @Override
    public void sendWord(String string)
    {
        remember(string) ;
        accumulate(string) ; // no escapes 
        gap(true) ;
    }
    
    @Override
    public void sendControl(char controlChar)
    {
        String x = TokenComms.cntrlAsString(controlChar) ;
        remember(x) ;
        accumulate(x) ; 
        gap(false) ;
    }

    @Override
    public void sendNumber(long number)
    {
        remember(number) ;
        accumulate(Long.toString(number)) ;
        gap(true) ;
    }
    
    @Override
    public void startTuple()
    {
        // Ensure endTuple.
        if ( log.isDebugEnabled() ) log.debug("Start tuple") ;
    }

    @Override
    public void endTuple()
    {
        if ( ! inTuple ) return ;
        accumulate(".") ;
        accumulate("\n") ;
        if ( log.isDebugEnabled() )
        {
            log.debug("End tuple") ;
            if ( label != null )
                log.debug(">> "+label+": "+thisTuple) ;
            else
                log.debug(">> "+thisTuple.toString()) ;
        }
        
        // Compression.
        lastTuple = thisTuple ;
        thisTuple = new ArrayList<Object>(lastTuple.size()) ;
        inTuple = false ; 
    }
    
    @Override
    public void startSection()
    {
        if ( log.isDebugEnabled() )
            log.debug("Start section") ;
        if ( inSection )
            log.warn("Already in a section") ;
        inSection = true ;
    }

    @Override
    public void endSection()
    {
        if ( log.isDebugEnabled() )
            log.debug("End section") ;
        if ( ! inSection )
            log.warn("Not in a section") ;
        startTuple() ;
        sendControl(TokenComms.endSectionMarker) ;
        endTuple() ;
        inSection = false ;
    }

    @Override
    public void close()
    {
        startTuple() ;
        sendControl(TokenComms.endSectionMarker) ;
        endTuple() ;
        IO.close(out);
    }
    
    @Override
    public void sync()
    { flush() ; }
    
    @Override
    public void flush() { 
        //System.out.println("Send: flush") ;
        try { out.flush(); } catch (IOException e) {} 
    }
    
    // --------
    
    private String tokenToString(Token token)
    {
        switch ( token.getType() )
        {
            // superclass case NODE:
            case IRI:
                return "<"+token.getImage()+">" ;
            case PREFIXED_NAME: 
                notImplemented(token) ;
                return null ;
            case BNODE:
                return "_:"+token.getImage() ;
            //BOOLEAN,
            // One kind of string?
            case STRING:
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
                // XXX
                //return "'"+NodeFmtLib.esc(token.getImage())+"'" ;
                return "\""+FmtUtils.stringEsc(token.getImage())+"\"" ;
            case LITERAL_LANG:
                return "\""+FmtUtils.stringEsc(token.getImage())+"\"@"+token.getImage2() ;
            case LITERAL_DT:
                return "\""+FmtUtils.stringEsc(token.getImage())+"\"^^"+tokenToString(token.getSubToken2()) ;
            case INTEGER:
            case DECIMAL:
            case DOUBLE:
                return token.getImage() ;
                
            // Not RDF
            case KEYWORD:
                return token.getImage() ;
            case CNTRL:
                if ( token.getCntrlCode() == -1 )
                    return "*" ; 
                return "*"+Character.toString((char)token.getCntrlCode()) ;
            case VAR:
            case HEX:
                
            // Syntax
            // COLON is only visible if prefix names are not being processed.
            case DOT:
            case COMMA:
            case SEMICOLON:
            case COLON: 
            case DIRECTIVE:
            // LT, GT, LE, GE are only visible if IRI processing is not enabled.
            case LT:
            case GT:
            case LE:
            case GE:
            // In RDF, UNDERSCORE is only visible if BNode processing is not enabled.
            case UNDERSCORE: 
            case LBRACE:    case RBRACE:    // {} 
            case LPAREN:    case RPAREN:    // ()
            case LBRACKET:  case RBRACKET:  // []
                
            case PLUS:
            case MINUS:
            case STAR:
            case SLASH:
            case RSLASH:
            default:
                notImplemented(token) ;
                return null ;
            //case EOF:
        }
    }

    private void remember(Object obj)
    { thisTuple.add(obj) ; }
    
    // A gap is always necessary for items that are not endLog-limited.
    // For example, numbers adjacent to numbers must have a gap but
    // quoted string then quoted string does not require a gap.  
    private void gap(boolean required)
    {
        if ( required || GAPS ) accumulate(" ") ;
    }

    // Beware of multiple stringing.
    private void accumulate(String string)
    { 
        //System.out.println("Send: "+string) ;
        inTuple = true ; 
        try { out.write(string) ; } catch (IOException e) {} 
    }
    
    private static void exception(IOException ex)
    { throw new CommsException(ex) ; }

    private void notImplemented(Token token)
    {
        throw new RiotException("Unencodable token: "+token) ;
    }
}
