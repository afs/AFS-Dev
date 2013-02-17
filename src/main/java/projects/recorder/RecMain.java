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

import java.io.StringWriter ;
import java.io.Writer ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import projects.recorder.tio.TokenInputStream ;
import projects.recorder.tio.TokenInputStreamBase ;
import projects.recorder.tio.TokenOutputStreamWriter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraphSimpleMem ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class RecMain
{
    static { Log.setCmdLogging() ; }
    
    public static void main(String[] args)
    {
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        if ( false )
            dsg = new DatasetGraphSimpleMem() ;
        
        StringWriter sw = new StringWriter() ;
        Writer w = sw ; //new OutputStreamWriter(System.out) ;
        
        TokenOutputStreamWriter out = new TokenOutputStreamWriter(null, w) ;
        out.setPrefixMapping("", "http://example/") ;
        
        DatasetChanges changeLogger = new DatasetChangesTuples(out) ; 
        dsg = new DatasetGraphMonitor(dsg, changeLogger) ;
        
        Quad q = SSE.parseQuad("(:g <s> <p> <o>)") ;
        dsg.add(q) ;
        dsg.delete(q) ;
        dsg.delete(q) ;
        out.flush();
        
        Graph g = SSE.parseGraph("(graph (<s1> <p1> 1) (<s2> <p2> 2))") ;
        dsg.addGraph(Node.createURI("graph"), g) ;
        SSE.write(dsg) ;

        System.out.print(sw.toString()) ;

        Tokenizer t = TokenizerFactory.makeTokenizerString(sw.toString()) ;
        TokenInputStream in = new TokenInputStreamBase(null, t) ;
//        while(in.hasNext())
//        {
//            List<Token> line = in.next() ;
//            System.out.println(line) ;
//        }
        
        System.out.println() ;
        
        DatasetGraph dsg2 = DatasetGraphFactory.createMem() ;
        DatasetGraphPlayer.play(in, dsg2) ;
        SSE.write(dsg2) ;
        
        System.out.println("DONE") ; 
    }

}

