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

import java.util.Iterator ;
import java.util.Spliterator ;
import java.util.Spliterators ;
import java.util.stream.Stream ;
import java.util.stream.StreamSupport ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.graph.GraphWrapper ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

public class GraphRDFS3 extends GraphWrapper {
    private static Logger log = LoggerFactory.getLogger(GraphRDFS3.class) ;
    private static final Node rdfType = NodeConst.nodeRDFType ;
    private InferenceSetupRDFS setup ;
    private boolean infDomain ;
    private boolean infRange ;

    public GraphRDFS3(InferenceSetupRDFS setup, Graph graph) {
        super(graph) ;
        this.setup = setup ;
        infDomain = ! setup.domainList.isEmpty() ;
        infRange =  ! setup.rangeList.isEmpty() ;
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o)
    {
        // In efficient - whole graph scan
        Iterator<Triple> iter = new InferenceProcessorIteratorRDFS(setup, get().find(null, null, null)) ;        
        Stream<Triple> stream = stream(iter) ;
        stream = stream.filter(triple ->
        {
            boolean b1 = ( isANY(s) || s.equals(triple.getSubject())) ;
            boolean b2 = ( isANY(p) || p.equals(triple.getPredicate())) ;
            boolean b3 = ( isANY(o) || o.equals(triple.getObject())) ;
            return b1 && b2 && b3 ;
        } ) ;
        return WrappedIterator.create(stream.iterator()) ;
    }
    
    private static boolean isANY(Node node) {
        return ( node == null ) || Node.ANY.equals(node) ;
    }

    private static boolean isTerm(Node node) {
        return ( node != null ) && ! Node.ANY.equals(node) ;
    }
    
    private static <T> Stream<T> stream(Iterator<? extends T> iterator) {
        int characteristics = Spliterator.ORDERED | Spliterator.IMMUTABLE;
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, characteristics), false);
    }
}

