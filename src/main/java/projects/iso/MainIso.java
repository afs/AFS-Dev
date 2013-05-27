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

package projects.iso;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class MainIso
{
    static public void main(String ... args) throws Exception
    { 
        String x1 = "(graph (_:a _:a _:a))" ;
        Graph g1 = SSE.parseGraph(x1) ;
        SSE.write(g1) ;
        System.out.println("DONE") ;
    }
    
    static public void exec(String s1, String s2, boolean iso) { 
        Graph g1 = SSE.parseGraph(s1) ;
        Graph g2 = SSE.parseGraph(s2) ;
        boolean b = IsoMatcher.isomorphic(g1, g2) ;
        
        if ( b != iso ) {
            System.out.println("====") ;
            SSE.write(g1) ;
            System.out.println("----") ;
            SSE.write(g2) ;
            System.out.println(b+" / "+iso) ;
        }
    }
}
