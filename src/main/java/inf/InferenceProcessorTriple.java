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

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/** An inference processor that deals with one triple and adds any derived triples to an accumulator */
public class InferenceProcessorTriple {
    
    Collection<Triple> acc ;
    private InferenceSetupRDFS setup ;
    private InferenceProcessorRDFS infEngine ;
    
    InferenceProcessorTriple(InferenceSetupRDFS setup) {
        this.setup = setup ;
        this.infEngine = new InferenceProcessorRDFS(setup) {
            @Override
            public void derive(Node s, Node p, Node o) {
                acc.add(Triple.create(s, p, o)) ;
            } };
    }
    
    public void process(Collection<Triple> acc, Triple t) {
        process(acc, t.getSubject(), t.getPredicate(), t.getObject()) ;
    }

    public void process(Collection<Triple> acc, Node s, Node p, Node o) {
        this.acc = acc ;
        infEngine.process(s, p, o) ;
    }
}
