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

package lib;

import static lib.CSVParser.TokenType.COMMA ;
import static lib.CSVParser.TokenType.EOF ;
import static lib.CSVParser.TokenType.NL ;
import static lib.CSVParser.TokenType.QSTRING ;
import static lib.CSVParser.TokenType.STRING ;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.io.PeekReader ;
import org.openjena.atlas.iterator.IteratorSlotted ;
import org.openjena.atlas.iterator.PeekIterator ;
import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.StrUtils ;
// Scala?
public class CSVParser
{
    public static void main(String ... argv)
    {
        Sink<List<String>> sink = new Sink<List<String>>(){

            @Override
            public void close()
            {}

            @Override
            public void send(List<String> item)
            {
                System.out.println(item.size()+": "+StrUtils.strjoin(",",item)) ;
            }

            @Override
            public void flush()
            {}} ;
        
        parse("X.csv", sink) ;
        
    }

    
    public static void parse(String filename, Sink<List<String>> sink)
    {
        InputStream _in = IO.openFile(filename) ;
        TokenIterator iter = new TokenIterator(_in) ;
        CSVParser parser = new CSVParser(iter) ;
        parser.parse(sink) ;
    }
    
    // ----
    
    private TokenIterator iter ;

    public CSVParser(TokenIterator iter) { this.iter = iter ; }
    
    enum TokenType { STRING, QSTRING, COMMA, NL, EOF }
    
    static class Token
    {
        final TokenType type ;
        final String image ;
        final long line ;
        final long col ;
        
        public boolean same(Token obj)
        {
            if (this == obj) return true ;
            if (obj == null) return false ;
            if (type != obj.type) return false ;
            if ( type == COMMA || type == NL || type == EOF )
                return true ;
            
            if (image == null && obj.image != null) return false ;
            return Lib.equal(this.image, obj.image) ;
        }

        public Token(long line, long col, TokenType type, String image)
        {
            super() ;
            this.type = type ;
            this.image = image ;
            this.line = line ;
            this.col = col ;
        }

        @Override
        public String toString()
        {
            switch (type)
            {
                case STRING: case QSTRING:
                    return "Token ["+line+", "+col+"] " + type + " |" + image +"|" ;
                default:
                    return "Token ["+line+", "+col+"] " + type ;
            }
        }
       
    }
    
    
    public void parse(Sink<List<String>> sink)
    {
        // Optional checking on line length
        
        // Header?
        PeekIterator<Token> pIter = new PeekIterator<>(iter) ;

        List<String> line = null ;
        
        loop: while(pIter.hasNext())
        {
            // Get rid of switches.  break problems.
            Token t = pIter.next() ;
            if ( line == null )
                line = new ArrayList<>(100) ;
            switch(t.type)
            {
                case EOF: 
                case NL:
                    // Blank line = one or none?
                    line.add("") ;
                    sink.send(line) ;
                    line = null ;
                    if ( t.type == EOF)
                        return ;
                    continue loop ;
            }

            // Immediate COMMA is an empty term.
            switch (t.type )
            {
                case STRING:
                case QSTRING:
                    line.add(t.image) ;
                    break ;
                case COMMA:
                    line.add("") ;
                    continue loop ;
                default:
                    exception("Syntax error: expected a string or comma.", t) ;
            }
            // Expect COMMA or NL
            if  (!pIter.hasNext() )
            {
                // Short line
                sink.send(line) ;
                return ;
            }
            Token t2 = pIter.peek() ;
            switch(t2.type)
            {
                case COMMA:
                    pIter.next() ;
                    continue loop;
                case NL:
                case EOF:
                {
                    sink.send(line) ;
                    pIter.next() ;
                    line = null ;
                    if ( t.type == TokenType.EOF)
                        return ;

                    continue loop ;
                }
                default:
                    exception("Syntax error: expect comma or end of line.", t) ;
            }
        }
    }

    static void exception(String msg, Token t)
    {
        if ( t != null && t.line >= 0 && t.col > 0 )
            msg = String.format("[%s, %s] %s", t.line, t.col, msg) ;
        throw new CSVParseException(msg) ;
    }
    
    static void exception(String msg, long line, long col)
    {
        if ( line >= 0 && col > 0 )
            msg = String.format("[%s, %s] %s", line, col, msg) ;
        throw new CSVParseException(msg) ;
    }

    static class CSVParseException extends RuntimeException
    {
        public CSVParseException(String msg, Throwable cause)    { super(msg, cause) ; }
        public CSVParseException(String msg)                     { super(msg) ; }
        public CSVParseException(Throwable cause)                { super(cause) ; }
        public CSVParseException()                               { super() ; }
    }
    
    static class TokenIterator extends IteratorSlotted<Token>
    {
        private PeekReader in ;
        // One EOF?

        TokenIterator(InputStream input)
        {
            this.in = PeekReader.makeUTF8(input) ;
        }
        
        @Override
        protected Token moveToNext()
        {
            int ch = in.peekChar() ;
            if ( ch == '\r' )
            {
                in.readChar() ;
                ch = in.peekChar() ;
                if ( ch != '\n' )
                    return new Token(in.getLineNum(), in.getColNum(), NL, "\r") ;
                // '\n' = drop through.
            }
                
            if ( ch == '\n' )
            {
                in.readChar() ;
                return new Token(in.getLineNum(), in.getColNum(), NL, "\n") ;
            }
            
            if ( ch == ',')
            {
                in.readChar() ;
                return new Token(in.getLineNum(), in.getColNum(), COMMA, ",") ;
            }
                
            long line = in.getLineNum() ;
            long col = in.getColNum() ;
            
            // Not -1
            if ( ch == '"' || ch == '\'' )
                return new Token(line, col, QSTRING, readQuotedString()) ;
            else
                return new Token(line, col, STRING, readUnquotedString()) ;
        }
        
        
        StringBuilder builder = new StringBuilder() ; 
        
        private String readQuotedString()
        {
            builder.setLength(0) ;
            int qCh = in.readChar() ;
            int ch = qCh ;
            while(true)
            {
                ch = in.readChar() ;
                if ( ch == -1 )
                    exception("Unterminated quoted string at end-of-file", in.getLineNum(), in.getColNum()) ;
                // Newlines are allowed in quoted strings.
//                if ( ch == '\r' || ch == '\n'  )
//                    exception("Unterminated quoted string", in.getLineNum(), in.getColNum()) ;
                if ( ch == qCh )
                {
                    int ch2 = in.peekChar() ;
                    if ( ch2 != qCh )
                        break ; 
                    // Escaped quote
                    in.readChar() ;
                    // Fall through.//        while( iter.hasNext() )
//                  {
//                  Token t = iter.next() ;
//                  System.out.println(t) ;
//              }
                }
                builder.append((char)ch) ;
            }
            return builder.toString() ;
        }

        private String readUnquotedString()
        {
            builder.setLength(0) ;
            while(true)
            {
                int ch = in.peekChar() ;
                if ( ch == -1 || ch == '\r' || ch == '\n'  )
                    break ;
                if ( ch == ',' )
                    break ;
                in.readChar() ;
                builder.append((char)ch) ;
            }
            return builder.toString() ;
        }

        @Override
        protected boolean hasMore()
        {
            return ! in.eof() && in.peekChar() != -1 ;
        }
    }
}

