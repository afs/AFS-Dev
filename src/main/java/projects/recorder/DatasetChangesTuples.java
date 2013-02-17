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

import projects.recorder.tio.TokenOutputStream ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

/** Write changes to a tuples file */
public class DatasetChangesTuples implements DatasetChanges
{
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
    
    private void record(QuadAction action, Node g, Node s, Node p, Node o)
    {
        if ( false )
        {        
            PrintStream out = System.out ; 
            out.print(action.label) ;
            out.print(SEP1) ;
            print(out, g) ;
            out.print(SEP1) ;
            print(out, s) ;
            out.print(SEP1) ;
            print(out, p) ;
            out.print(SEP1) ;
            print(out, o) ;
            out.print(SEP2) ;
            return ;
        }
        
        out.startTuple() ;
        out.sendWord(action.label) ;
        out.sendNode(g) ;
        out.sendNode(s) ;
        out.sendNode(p) ;
        out.sendNode(o) ;
        out.endTuple() ;
    }
    
    private void print(PrintStream out, Node x)
    {
        String str = FmtUtils.stringForNode(x) ;
        out.print(str) ;
    }

}

