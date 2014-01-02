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

package projects.riot_sse;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.lang.LangBase ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.sse.lang.ParseHandler ;

/** Parse the SSE (SPARQL/Lisp) syntax */

public class LangSSE extends LangBase
{
    // Tokenizer needs tuning to yield + - * < and > as tokens. 
    
    private static Logger log = LoggerFactory.getLogger(LangSSE.class) ;

    private ParseHandler handler ;
    
    public LangSSE(Tokenizer tokens,
                   ParserProfile profile,
                   ParseHandler handler)
    {
        super(tokens, profile, StreamRDFLib.sinkNull()) ;
        this.handler = handler ;
    }
    
    @Override
    public Lang getLang()
    {
        return null ;
    }

    @Override
    protected void runParser()
    {
        parseOne() ;
        if ( ! eof() )
            exception(peekToken(), "Excess characters") ;
    }
    
    private void parseList(TokenType opener, TokenType terminator)
    { 
        // On entry, looking at opening character.
        Token t = nextToken() ;
        handler.listStart((int)t.getLine(), (int)t.getColumn()) ;
        
        while(moreTokens())
        {
            // Do one
            if ( lookingAt(terminator) )
                break ;
            if ( lookingAt(TokenType.RPAREN) || lookingAt(TokenType.RBRACKET) || lookingAt(TokenType.RBRACE) )
            {
                Token t2 = nextToken() ;
                exception(t2, "Mismatched delimiters: %s %s ('%s')", opener, t2.getType(), t2.getImage()) ;
            }
            
            parseOne() ;
        }

        if ( eof() )
            exception(peekToken(), "EOF: Expected ')'") ;
        Token t2 = nextToken() ;
        
        handler.listFinish((int)t2.getLine(), (int)t2.getColumn()) ;
    }
    
    private void parseOne()
    {
        if ( lookingAt(TokenType.LPAREN) )
        {
            parseList(TokenType.LPAREN, TokenType.RPAREN) ;
            return ;
        }
        
        if ( lookingAt(TokenType.LBRACKET) )
        {
            parseList(TokenType.LBRACKET, TokenType.RBRACKET) ;
            return ;
        }
        
        if ( lookingAt(TokenType.LBRACE) )
        {
            parseList(TokenType.LBRACE, TokenType.RBRACE) ;
            return ;
        }

        if ( lookingAt(TokenType.STRING) || 
             lookingAt(TokenType.STRING1) || lookingAt(TokenType.STRING2) ||
             lookingAt(TokenType.LONG_STRING1) || lookingAt(TokenType.LONG_STRING2) )
        {
            Token t = nextToken() ;
            handler.emitLiteral((int)t.getLine(), (int)t.getColumn(), 
                                t.getImage(), null, null, null) ;
            return ;
        }

        if ( lookingAt(TokenType.LITERAL_DT) )
        {
            Token t = nextToken() ;
            Token t2 = t.getSubToken2() ;
            if ( t2.hasType(TokenType.PREFIXED_NAME) )
            {
                handler.emitLiteral((int)t.getLine(), (int)t.getColumn(), t.getImage(),
                                    null, null, t2.getImage()+":"+t2.getImage2()) ;
                return ;

            }
            if ( t2.hasType(TokenType.IRI) )
            {
                handler.emitLiteral((int)t.getLine(), (int)t.getColumn(), t.getImage(),
                                    null, t2.getImage(), null) ;
                return ;
            }
            else
               exception(t, "Unexpected token after '^^': %s", t) ; 
        }
        
        if ( lookingAt(TokenType.LITERAL_LANG) )
        {
            Token t = nextToken() ;
            handler.emitLiteral((int)t.getLine(), (int)t.getColumn(), 
                                t.getImage(), t.getImage2(), null, null) ;
            return ;
        }
        
        if ( lookingAt(TokenType.IRI) )
        {
            Token t = nextToken() ;
            handler.emitIRI((int)t.getLine(), (int)t.getColumn(), t.getImage()) ;
            return ;
        }

        if ( lookingAt(TokenType.BNODE) )
        {
            Token t = nextToken() ;
            handler.emitBNode((int)t.getLine(), (int)t.getColumn(), t.getImage()) ;
            return ;
        }
        
        if ( lookingAt(TokenType.KEYWORD) )
        {
            Token t = nextToken() ;
            handler.emitSymbol((int)t.getLine(), (int)t.getColumn(), t.getImage()) ;
            return ;
        }
        
        if ( lookingAt(TokenType.PLUS) )
        {
            Token t = nextToken() ;
            handler.emitSymbol((int)t.getLine(), (int)t.getColumn(), "+") ;
            return ;
        }

        if ( lookingAt(TokenType.MINUS) )
        {
            Token t = nextToken() ;
            handler.emitSymbol((int)t.getLine(), (int)t.getColumn(), "-") ;
            return ;
        }

        if ( lookingAt(TokenType.PREFIXED_NAME) )
        {
            Token t = nextToken() ;
            // FIXME - use tokenAsNode through out
            String x = t.getImage()+":"+t.getImage2() ;
            handler.emitPName((int)t.getLine(), (int)t.getColumn(), x) ;
            return ;
        }
        
        if ( lookingAt(TokenType.VAR) )
        {
            Token t = nextToken() ;
            handler.emitVar((int)t.getLine(), (int)t.getColumn(), t.getImage()) ;
            return ;
        }
        
        if ( lookingAt(TokenType.INTEGER) )
        {
            Token t = nextToken() ;
            handler.emitLiteral((int)t.getLine(), (int)t.getColumn(), 
                                t.getImage(), null, XSDDatatype.XSDinteger.getURI(), null) ;
            return ;
        }

        if ( lookingAt(TokenType.DECIMAL) )
        {
            Token t = nextToken() ;
            handler.emitLiteral((int)t.getLine(), (int)t.getColumn(), 
                                t.getImage(), null, XSDDatatype.XSDdecimal.getURI(), null) ;
            return ;
        }

        if ( lookingAt(TokenType.DOUBLE) )
        {
            Token t = nextToken() ;
            handler.emitLiteral((int)t.getLine(), (int)t.getColumn(), 
                                t.getImage(), null, XSDDatatype.XSDdouble.getURI(), null) ;
            return ;
        }
        
        if ( lookingAt(TokenType.DIRECTIVE) )
        {
            // What parse as directives (@word) are symbols.
            Token t = nextToken() ;
            String x = "@"+t.getImage() ;
            handler.emitSymbol((int)t.getLine(), (int)t.getColumn(), x) ; 
            return ;
        }
        
        Token token  = nextToken() ;
        
        exception(token, "Unexpected: %s", token) ;
    }

    //@Override
    protected Node tokenAsNode(Token token)
    {
        return profile.create(null, token) ;
    }
}
