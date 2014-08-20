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

package inf ;

import static lib.Lib8.toList ;

import java.util.List ;
import java.util.function.Predicate ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class InfGlobal {
    // Whether to include RDFS subClassOf/subPropertyOf derivation when
    // subClassOf/subPropertyOf is in the data.
    // Also covers rdf:type T --> T rdfs:subClassOf T
    // When false, inferences are only rdf:type stuff.

    public static /* final */ boolean includeDerivedDataRDFS = false ;

    public static final Node        rdfType                = NodeConst.nodeRDFType ;
    public static final Node        rdfsRange              = RDFS.Nodes.range ;
    public static final Node        rdfsDomain             = RDFS.Nodes.domain ;
    public static final Node        rdfsSubClassOf         = RDFS.Nodes.subClassOf ;
    public static final Node        rdfsSubPropertyOf      = RDFS.Nodes.subPropertyOf ;

    private static Predicate<Triple> filterRDFS = 
        triple -> triple.getPredicate().getNameSpace().equals(RDFS.getURI()) ;
    private static Predicate<Triple> filterNotRDFS = 
        triple -> ! triple.getPredicate().getNameSpace().equals(RDFS.getURI()) ;
        
    public static List<Triple> removeRDFS(List<Triple> x) {
        return toList(x.stream().filter(filterNotRDFS)) ;
    }
}
