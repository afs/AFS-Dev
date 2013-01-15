/*
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

package projects.recorder.tio;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.NoSuchElementException ;

import org.apache.jena.riot.tokens.PrintTokenizer ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.Tokenizer ;
import static org.apache.jena.riot.tokens.TokenType.* ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Tokenizer that sorts out prefixes and groups into sequences of token */
public class TokenInputStreamBase implements TokenInputStream
{
    private static Logger log = LoggerFactory.getLogger(TokenInputStreamBase.class) ;
    private boolean finished = false ;
    private final Tokenizer tokens ;
    private List<Token> list ;
    private Map<String, String> map = new HashMap<String, String>() ;
    private String label ;
    
    public TokenInputStreamBase(String label, Tokenizer tokens)
    {
        if ( false ) tokens = new PrintTokenizer("InputStream: ", tokens) ;
        this.tokens = tokens ;
        this.label = label ;
    }

    @Override
    public boolean hasNext()
    {
        if ( finished )
            return false ;
        
        if ( list != null ) // Already got the reply.
            return true ;

        try {
            if ( ! tokens.hasNext() )
            {
                finished = true ;
                return false ;
            }
            list = buildOneLine() ;
            if ( false && log.isDebugEnabled() )
                log.debug("Tokens: "+list) ;
            if ( list == null )
                finished = true ;
            return list != null ;
        } catch (Exception ex) { finished = true ; return false ; }
    }

    private List<Token> buildOneLine()
    {
        List<Token> tuple = new ArrayList<Token>() ;
        boolean isDirective = false ;
        for( ; tokens.hasNext() ; )
        {
            Token token = tokens.next() ;
            
            if ( token.hasType(DIRECTIVE) )
                isDirective = true ;
            
            if ( token.hasType(DOT) )
            {
                if ( tuple.size() > 0 &&  tuple.get(0).hasType(DIRECTIVE))
                {
                    directive(tuple) ;
                    tuple.clear();
                    isDirective = false ;
                    // Start again.
                    continue ;
                }
                return tuple ;
            }

            // Fixup prefix names.
            if ( !isDirective && token.hasType(PREFIXED_NAME) )
            {
                String ns = map.get(token.getImage()) ;
                String iri ;
                if ( ns == null)
                {
                    log.warn("Can't resolve '"+token.toString(false)+"'", ns) ;
                    iri = "unresolved:"+token.getImage()+":"+token.getImage2() ;
                }
                else
                    iri = ns+token.getImage2() ; 
                token.setType(IRI) ;
                token.setImage(iri) ;
                token.setImage2(null) ;
            }
            
            tuple.add(token) ;
        }

        // No final DOT
        return tuple ;
    }

    private void directive(List<Token> tuple)
    {
        if ( tuple.size() != 3 )
            throw new CommsException("Bad directive: "+tuple) ;
        
        String x = tuple.get(0).getImage() ;
        
        if ( x.equals("prefix") )
        {
            // Raw - unresolved prefix name.
            if ( ! tuple.get(1).hasType(PREFIXED_NAME) )
                throw new CommsException("@prefix requires a prefix (found '"+tuple.get(1)+"')") ;
            if ( tuple.get(1).getImage2().length() != 0 )
                throw new CommsException("@prefix requires a prefix and no suffix (found '"+tuple.get(1)+"')") ;
            String prefix = tuple.get(1).getImage() ;
            
            if ( ! tuple.get(2).hasType(IRI) )
                throw new CommsException("@prefix requires an IRI (found '"+tuple.get(1)+"')") ;
            String iriStr = tuple.get(2).getImage() ;
            map.put(prefix, iriStr) ;
            return ;
        }
        throw new CommsException("Unregcognized directive: "+x) ;
    }
    
    @Override
    public List<Token> next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        List<Token> r = list ;
        if ( log.isDebugEnabled() )
        {
            if ( label != null )
                log.debug("<< "+label+": "+r) ;
            else
                log.debug("<< "+r.toString()) ;
        }
        list = null ;
        return r ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException() ; }

    @Override
    public Iterator<List<Token>> iterator()
    {
        return this ;
    }

    @Override
    public void close()
    {}
}
