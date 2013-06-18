/**
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

package projects.recorder;

import java.io.PrintStream ;

import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import projects.recorder.tio.TokenOutputStream ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

/** Write changes to a tuples file */
public class DatasetChangesTuples implements DatasetChanges
{
    // or generalise with a tuple-writer.
    
    
    private TokenOutputStream out ;
    
    public DatasetChangesTuples(TokenOutputStream out) { this.out = out; }
    
    @Override
    public void start()
    {}

    @Override
    public void finish()
    {}

    @Override
    public void change(QuadAction action, Node g, Node s, Node p, Node o)
    {
        record(action, g, s, p, o) ;
    }

    static final String SEP1 = ", " ;    // TAB is good. 
    static final String SEP2 = "\n" ; 
    
    private Node prev_g = null ;
    private Node prev_s = null ;
    private Node prev_p = null ;
    private Node prev_o = null ;
    
    private void record(QuadAction action, Node g, Node s, Node p, Node o)
    {
        out.startTuple() ;
        out.sendWord(action.label) ;
        send(g, prev_g) ;
        send(s, prev_s) ;
        send(p, prev_p) ;
        send(o, prev_o) ;
        prev_g = g ;
        prev_s = s ;
        prev_p = p ;
        prev_o = o ;
        out.endTuple() ;
    }
    
    static final Token REPEAT ;
    static {
        // Hack
        Tokenizer t = TokenizerFactory.makeTokenizerString("R") ;
        REPEAT = t.next() ;
    }
    
    private void send(Node n, Node prev_n)
    {
//        if ( n.equals(prev_n) )
//            out.sendToken(REPEAT) ;
//        else
            out.sendNode(n) ;
    }

    private void print(PrintStream out, Node x)
    {
        String str = FmtUtils.stringForNode(x) ;
        out.print(str) ;
    }

}

