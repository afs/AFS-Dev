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

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/** Iterator of triples that inserts any inferred triples */ 
class InferenceProcessorStreamRDFS implements Iterator<Triple> {
    // stream?
    private final Iterator<Triple> iter ;
    private List<Triple> current = new ArrayList<>() ;
    private int idx = -1 ;
    private InferenceProcessorAccRDFS infEngine ;
    
    public InferenceProcessorStreamRDFS(InferenceSetupRDFS setup, Iterator<Triple> iter) {
        this.iter = iter ;
        this.infEngine = new InferenceProcessorAccRDFS(current, setup) ;
    }
    
    @Override
    public boolean hasNext() {
        if ( idx >= 0 )
            return true ;
        return iter.hasNext() ; 
    }

    @Override
    public Triple next() {
        // Inference triples.
        if ( idx >= 0 ) {
            Triple t = current.get(idx) ;
            idx ++ ;
            if ( idx == current.size() ) {
                current.clear() ;
                idx = -1 ;
            }
            return t ;
        }
        // Base data triple.
        Triple t = iter.next() ;
        Node p = t.getPredicate() ;
        
        infEngine.process(t.getSubject(), t.getPredicate(), t.getObject()) ;
        idx = (current.size() != 0 ) ? 0 : -1 ; 
        return t ;
    }
}
