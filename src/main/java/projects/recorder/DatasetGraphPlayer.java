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

import java.util.List ;

import org.apache.jena.riot.tokens.Token ;
import projects.recorder.tio.TokenInputStream ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

public class DatasetGraphPlayer
{
    // Play tuples.
    
    public static void play(TokenInputStream input, DatasetGraph dsg)
    {
        long count = 0 ; 
        while(input.hasNext())
        {
            List<Token> line = input.next() ;
            count++ ;
            if ( line.size() == 0 )
                continue ;
            if ( line.size() != 5 )
                error("[%d] Bad tuple - length %d", count, line.size() ) ; 

            Token ctl = line.get(0) ;
            if ( ! ctl.isWord() )
                error("[%d] No start word",count) ; 
            String str = ctl.getImage() ;
            if ( str.startsWith("#") )
                continue ;
            Node g = line.get(1).asNode() ;
            Node s = line.get(2).asNode() ;
            Node p = line.get(3).asNode() ;
            Node o = line.get(4).asNode() ;
            dsg.add(g,s,p,o) ;
        }
    }

    private static void error(String fmt, Object...args)
    {
        throw new RuntimeException(String.format(fmt,args)) ;
    }
    
}

