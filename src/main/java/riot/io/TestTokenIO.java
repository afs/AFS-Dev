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

package riot.io;

import static org.apache.jena.riot.tokens.Token.tokenForChar ;
import static org.apache.jena.riot.tokens.Token.tokenForNode ;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.Writer ;
import java.nio.ByteBuffer ;
import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.sse.SSE ;

public class TestTokenIO extends BaseTest
{
    static Node n1 = SSE.parseNode("<x>") ;
    static Node n2 = SSE.parseNode("<http://example/y>") ;
    static Node n3 = SSE.parseNode("<http://example/z>") ;
    
    static Node n4 = SSE.parseNode("'literal'") ;
    static Node n5 = SSE.parseNode("'literal'@en") ;
    static Node n6 = SSE.parseNode("123") ;
    
    static Node n7 = SSE.parseNode("'123'^^<http://example/myType>") ;

    // Where to send things.
    //private SinkToBytes sink = new SinkToBytes() ;
    private ByteArrayOutputStream bytesOut = new ByteArrayOutputStream() ;
    private Writer w = IO.asBufferedUTF8(bytesOut) ;
    private TokenOutputStreamWriter out = new TokenOutputStreamWriter("OUT", w) ;

    @BeforeClass public static void setupClass()
    {
        LogCtl.enable("Token") ;
    }
    
    @AfterClass public static void teardownClass()
    {
        LogCtl.set("Token", "warn") ;
    }

    
    @Before public void setup()
    { 
        
    }
    
    @Test public void tokens1()
    {
        send(n7) ;
        expect(tokenForNode(n7)) ;
    }
    
    @Test public void tokens2()
    {
        out.startTuple() ;
        send(n7) ;
        out.endTuple() ;
        expect(tokenForNode(n7), tokenForChar(Chars.CH_DOT)) ;
    }
    
    @Test public void tokens3()
    {
        send(n1, n4, n5, n6, n7) ;
        
        // Because tokenForNode sets the type as TokenType.STRING
        Token t4 = tokenForNode(n4) ;
        t4.setType(TokenType.STRING2) ;
        
        expect(tokenForNode(n1),
               t4,
               tokenForNode(n5),
               tokenForNode(n6),
               tokenForNode(n7)) ;
    }

    
    @Test public void comms1()
    {
        send(n1) ;
        expect(n1) ;
    }
    
    @Test public void comms2()
    {
        send(n1, n2, n3) ;
        expect(n1, n2, n3) ; 
    }
    
    // Prefixes.
    
    @Test public void prefix1()
    {
        out.setPrefixMapping("", "http://example/") ;
        send(n2) ;
        expect(n2) ; 
    }
    
    // ---- 
    
    private void send(Node... nodes)
    {
        for ( Node n : nodes )
            out.sendNode(n) ;
        out.flush() ;
        
    }
    
    private void expect(Node... nodes)
    {
        byte b[] = bytesOut.toByteArray() ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(new ByteArrayInputStream(b)) ;
        //tokenizer = new PrintTokenizer("Read", tokenizer) ;
        
        // Expand prefixed names.
        TokenInputStream in = new TokenInputStreamBase("IN", tokenizer) ;
        
        int idx = 0 ;
        while ( in.hasNext() )
        {
            List<Token> tokens = in.next() ;
            for ( Token t : tokens )
            {
                Node n = nodes[idx++] ;
                assertFalse("Directive", t.hasType(TokenType.DIRECTIVE)) ;
                assertEquals(n, t.asNode()) ;
            }
        }
        tokenizer.close();
    }
    
    private void expect(Token ...tokens)
    {
        out.flush() ;
        byte b[] = bytesOut.toByteArray() ;

        //String s = StrUtils.fromUTF8bytes(b) ;
        //System.out.println(s) ;
        
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(new ByteArrayInputStream(b)) ;
        
        for ( Token t : tokens )
        {
            
            Token t2 = tokenizer.next() ;
            //System.out.println(t2) ;
            assertEquals(t, t2) ;
        }
        
        assertFalse("Remaining tokens", tokenizer.hasNext()) ;
        tokenizer.close();
    }

    
    static class SinkToBytes implements Sink<ByteBuffer>
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        
        @Override
        public void send(ByteBuffer item)
        { 
            for ( int i = item.position() ; i < item.limit() ; i++ )
            {
                byte b = item.get() ;
                out.write(b) ;
            }
        }

        public byte[] bytes() { return out.toByteArray() ; }
        
        @Override
        public void close()
        { try { out.close() ; } catch (IOException ex) {} }

        @Override
        public void flush()
        { try { out.flush() ; } catch (IOException ex) {} }
        
       
         
    }
}
