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

package inf;

import java.util.Collection ;
import java.util.HashSet ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/** An inference processor that deals with one triple.
 * It can return the derived triples or adds them derived to an accumulator
 * The input triple is not added (but it may be derived).
 */
public class InferenceProcessorTriple {
    private InferenceSetupRDFS setup ;
    
    public InferenceProcessorTriple(InferenceSetupRDFS setup) {
        this.setup = setup ;
    }

    /** Calculate the set of triples from processing triple t */
    public Set<Triple> process(Triple t) {
        return process(t.getSubject(), t.getPredicate(), t.getObject()) ;
    }
    
    public Set<Triple> process(Node s, Node p, Node o) {
        Set<Triple> acc = new HashSet<>() ;
        process(acc, s, p, o) ;
        return acc ;
    }

    /** Accumulate the triples from processing triple t */
    public void process(Collection<Triple> acc, Triple t) {
        process(acc, t.getSubject(), t.getPredicate(), t.getObject()) ;
    }
    
    /** Accumulate the triples from processing triple t */
    public void process(Collection<Triple> acc, Node s, Node p, Node o) {
        InferenceProcessorAccRDFS inf = new InferenceProcessorAccRDFS(acc, setup) ;
        inf.process(s, p, o) ;
    }

}
