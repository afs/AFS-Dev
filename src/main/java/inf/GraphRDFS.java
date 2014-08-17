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

import java.util.* ;
import java.util.stream.Collectors ;
import java.util.stream.Stream ;
import java.util.stream.StreamSupport ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.sparql.graph.GraphWrapper ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.util.iterator.* ;

public class GraphRDFS extends GraphWrapper {
    private static Logger log = LoggerFactory.getLogger(GraphRDFS.class) ;
    private static final Node rdfType = NodeConst.nodeRDFType ;
    private InferenceSetupRDFS setup ;
    private boolean infDomain ;
    private boolean infRange ;

    public GraphRDFS(InferenceSetupRDFS setup, Graph graph) {
        super(graph) ;
        this.setup = setup ;
        infDomain = ! setup.domainList.isEmpty() ;
        infRange =  ! setup.rangeList.isEmpty() ;
    }
    
    @Override
    public ExtendedIterator<Triple> find(TripleMatch m)
    {
        return find2(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o)
    {
        return find2(s, p, o) ;
    }
    
    private static <T> Stream<T> stream(Iterator<? extends T> iterator) {
        int characteristics = Spliterator.ORDERED | Spliterator.IMMUTABLE;
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, characteristics), false);
    }
    
    private ExtendedIterator<Triple> find2(Node _subject, Node _predicate, Node _object) {
        //log.info("find("+_subject+", "+_predicate+", "+_object+")") ;
        Node subject = any(_subject) ;
        Node predicate = any(_predicate) ;
        Node object = any(_object) ;

        // Subproperties of rdf:type
        
        if ( rdfType.equals(predicate) ) {
            if ( isTerm(subject) ) {
                if ( isTerm(object) )
                    return find_X_type_T(subject, object) ;
                else
                    return find_X_type_ANY(subject) ;
            } else {
                if ( isTerm(object) )
                    return find_ANY_type_T(object) ;
                else
                    return find_ANY_type_ANY() ;
            }
        }

        if ( isANY(predicate) ) {
//          throw new NotImplemented("ANY predicate") ;
            if ( isTerm(subject) ) {
                if ( isTerm(object) )
                    return find_X_ANY_T(subject, object) ;
                else
                    return find_X_ANY_ANY(subject) ;
            } else {
                if ( isTerm(object) )
                    return find_ANY_ANY_T(object) ;
                else
                    return find_ANY_ANY_ANY() ;
            }
        }
            
        // Subproperty
        // We assume no subproperty of rdf:type
        
        List<Node> predicates = setup.subProperties.get(predicate) ;
        if ( predicates == null )
            return super.find(subject, predicate, object) ;
        
        // Hard work, not scalable.
        Set<Triple> triples = new HashSet<>() ;
        
        for ( Node p : predicates ) {
            ExtendedIterator<Triple> iter = super.find(subject, p, object) ;
            // replace p by predicate
            Map1<Triple, Triple> map = new Map1<Triple, Triple>(){
                @Override
                public Triple map1(Triple triple) {
                    return Triple.create(triple.getSubject(), predicate, triple.getObject()) ;
                }
            } ;

            ExtendedIterator<Triple> iter2 = iter.mapWith(map) ;
            iter2.forEachRemaining(triple->triples.add(triple)) ;
        }
        return WrappedIterator.create(triples.iterator()) ;

        /*
    { X rdf:type T }
    ==>
    {
      { X ?p [] . ?p rdfs:domain ?T1 }
        UNION
      { [] ?p X. ?p rdfs:range ?T1 }
        UNION
      { X rdf:type ?T1 }
    }
    { ?T1 rdfs:subClassOf T } -- maybe optional?         
    */
    }

    private ExtendedIterator<Triple> singletonIterator(Node s, Node p, Node o) {
        return new SingletonIterator<>(Triple.create(s, p, o)) ;
    }
    
    private ExtendedIterator<Triple> nullIterator() {
        return new NullIterator<>() ;
    }

    private ExtendedIterator<Triple> find_X_type_T(Node subject, Node object) {
        if ( get().contains(subject, rdfType, object) )
            return singletonIterator(subject, rdfType, object) ;
        Set<Node> types = new HashSet<>() ;
        accTypesRange(types, subject) ;
        if ( types.contains(object) )
            return singletonIterator(subject, rdfType, object) ;
        accTypesDomain(types, subject) ;
        if ( types.contains(object) )
            return singletonIterator(subject, rdfType, object) ;
        accTypes(types, subject) ;
        // expand supertypes
        types = superTypes(types) ;
        if ( types.contains(object) )
            return singletonIterator(subject, rdfType, object) ;
        return nullIterator() ;
    }

    private ExtendedIterator<Triple> find_X_type_ANY(Node subject) {
        Set<Node> types = new HashSet<>() ;
        accTypesRange(types, subject) ;
        accTypesDomain(types, subject) ;
        accTypes(types, subject) ;
        // expand supertypes
        types = superTypes(types) ;
        List<Triple> triples = types.stream().map( type -> Triple.create(subject, rdfType, type)).collect(Collectors.toList()) ; 
        return WrappedIterator.create(triples.iterator()) ; 
    }

    private ExtendedIterator<Triple> find_ANY_type_T(Node type) {
        // Fast path no subClasses
        Set<Node> types = subTypes(type) ;
        Set<Triple> triples = new HashSet<>() ;
        
        accInstances(triples, types, type) ;
        accInstancesRange(triples, types, type) ;
        accInstancesDomain(triples, types, type) ;
        
        return WrappedIterator.create(triples.iterator()) ;
    }

    private ExtendedIterator<Triple> find_ANY_type_ANY() {
        // Better?
        ExtendedIterator<Triple> iter = super.find(Node.ANY, Node.ANY, Node.ANY) ;
        Iterator<Triple> iter2 = new InferenceProcessorIteratorRDFS(setup, iter) ;
        Stream<Triple> stream = stream(iter2) ;
        stream = stream.filter(triple -> triple.getPredicate().equals(rdfType)) ;
        return new ExtendedIteratorCloser<>(stream.iterator(), iter) ;
    }

    private ExtendedIterator<Triple> find_X_ANY_T(Node subject, Node object) {
        // Start at X.
        // (X ? ?) - inference - project "X ? T"
        // also (? ? X) if there is a range clause. 
        ExtendedIterator<Triple> iter = super.find(subject, Node.ANY, Node.ANY) ;
        // + reverse (used in object position and there is a range clause)
        if ( infRange )
            iter = iter.andThen(super.find(Node.ANY, Node.ANY, subject)) ;
        //iter = print(iter) ;
        return infFilterSubjObj(iter, subject, object) ;
    }

    private ExtendedIterator<Triple> find_X_ANY_ANY(Node subject) {
        // XXX DO BETTER
        return find_X_ANY_T(subject, Node.ANY) ;
    }

    private ExtendedIterator<Triple> find_ANY_ANY_T(Node object) {
        ExtendedIterator<Triple> iter = super.find(Node.ANY, Node.ANY, object) ;
        // XXX BUG
        // ? ? P (range) does not find :x a :P.
        // :P is not in the target graph.
        //add ? rdf:type P and distinct.
        // And subclasses
        
        // + reverse (used in object position and there is a domain clause)
        if ( infDomain )
            iter = iter.andThen(super.find(object, Node.ANY, Node.ANY)) ;
        //iter = print(iter) ;
        return infFilterSubjObj(iter, Node.ANY, object) ;
    }

    private ExtendedIterator<Triple> infFilterSubjObj(ExtendedIterator<Triple> iter, Node subject, Node object) {
        Iterator<Triple> iter2 = new InferenceProcessorIteratorRDFS(setup, iter) ;
        Stream<Triple> stream = stream(iter2) ;
        if ( isTerm(object) )
            stream = stream.filter(triple -> { return triple.getObject().equals(object) ; } ) ;
        if ( isTerm(subject) )
            stream = stream.filter(triple -> { return triple.getSubject().equals(subject) ; } ) ;
        // When needed?
        stream = stream.distinct() ;
        return new ExtendedIteratorCloser<>(stream.iterator(), iter) ;
    }

    private ExtendedIterator<Triple> find_ANY_ANY_ANY() {
        ExtendedIterator<Triple> iter = super.find(Node.ANY, Node.ANY, Node.ANY) ;
        Iterator<Triple> iter2 = new InferenceProcessorIteratorRDFS(setup, iter) ;
        return new ExtendedIteratorCloser<>(iter2, iter) ;
    }

    private void accInstances(Set<Triple> triples, Set<Node> types, Node requestedType) {
        for ( Node type : types ) {
            ExtendedIterator<Triple> iter = super.find(Node.ANY, rdfType, type) ;
            iter.forEachRemaining(triple -> triples.add(Triple.create(triple.getSubject(), rdfType, requestedType)) );
            iter.close() ;
        }
    }
    
    private void accInstancesDomain(Set<Triple> triples, Set<Node> types, Node requestedType) {
        for ( Node type : types ) {
            List<Node> predicates = setup.domainPropertyList.get(type) ;
            if ( predicates == null )
                continue ;
            predicates.forEach(p -> {
                ExtendedIterator<Triple> iter = super.find(Node.ANY, p, Node.ANY) ;
                iter.forEachRemaining(triple -> triples.add(Triple.create(triple.getSubject(), rdfType, requestedType)) );
            }) ;
        }
    }
    
    private void accInstancesRange(Set<Triple> triples, Set<Node> types, Node requestedType) {
        for ( Node type : types ) {
            List<Node> predicates = setup.rangePropertyList.get(type) ;
            if ( predicates == null )
                continue ;
            predicates.forEach(p -> {
                ExtendedIterator<Triple> iter = super.find(Node.ANY, p, Node.ANY) ;
                iter.forEachRemaining(triple -> triples.add(Triple.create(triple.getObject(), rdfType, requestedType)) );
            }) ;
        }
    }


    private ExtendedIterator<Triple> print(ExtendedIterator<Triple> iter) {
        List<Triple> triples = new ArrayList<>() ; 
        for ( ; iter.hasNext() ;)
            triples.add(iter.next()) ;
        
        //iter.forEachRemaining(triples::add) ;
        iter.close() ;
        System.out.println(triples) ;
        return WrappedIterator.create(triples.iterator()) ;
    }
    
    private void accTypes(Set<Node> types, Node subject) {
        ExtendedIterator<Triple> iter = get().find(subject, rdfType, Node.ANY) ;
        iter.forEachRemaining(triple -> types.add(triple.getObject())) ;
        iter.close() ;
    }

    private void accTypesDomain(Set<Node> types, Node node) {
        ExtendedIterator<Triple> iter = get().find(node, Node.ANY, Node.ANY) ;
        iter.forEachRemaining(triple -> {
            Node p = triple.getPredicate() ;
            List<Node> x = setup.domainList.get(p) ;
            if ( x != null )
                types.addAll(x) ;
        }) ;
        iter.close(); 
    }

    private void accTypesRange(Set<Node> types, Node node) {
        ExtendedIterator<Triple> iter = get().find(Node.ANY, Node.ANY, node) ;
        iter.forEachRemaining(triple -> {
            Node p = triple.getPredicate() ;
            List<Node> x = setup.rangeList.get(p) ;
            if ( x != null )
                types.addAll(x) ;
            x = setup.domainList.get(p) ;
            if ( x != null )
                types.addAll(x) ;
        }) ;
        iter.close(); 
    }

    private Set<Node> subTypes(Set<Node> types) {
        Set<Node> x = new HashSet<>() ;
        for ( Node type : types ) {
            List<Node> y = setup.subClasses.get(type) ;
            if ( y != null )
                x.addAll(y) ;
            x.add(type) ;
        }
        return x ;
    }
    
    private Set<Node> superTypes(Set<Node> types) {
        Set<Node> x = new HashSet<>() ;
        for ( Node type : types ) {
            List<Node> y = setup.superClasses.get(type) ;
            if ( y != null )
                x.addAll(y) ;
            x.add(type) ;
        }
        return x ;
    }

    private Set<Node> subTypes(Node type) {
        Set<Node> x = new HashSet<>() ;
        x.add(type) ;
        List<Node> y = setup.subClasses.get(type) ;
        if ( y != null )
            x.addAll(y) ;
        return x ;
    }
    
    private Set<Node> superTypes(Node type) {
        Set<Node> x = new HashSet<>() ;
        x.add(type) ;
        List<Node> y = setup.superClasses.get(type) ;
        if ( y != null )
            x.addAll(y) ;
        return x ;
    }
//
//    private boolean rangeMatch(Node subject, Node object) {
//        ExtendedIterator<Triple> iter = super.find(Node.ANY, Node.ANY, subject) ;
//        return matchDomainOrRange(iter, setup.rangeList, object) ;
//    }
//    
//    private boolean domainMatch(Node subject, Node object) {
//        ExtendedIterator<Triple> iter = super.find(subject, Node.ANY, Node.ANY) ;
//        return matchDomainOrRange(iter, setup.domainList, object) ;
//    }
//
//    private boolean matchDomainOrRange(ExtendedIterator<Triple> iter, Map<Node, List<Node>> domainRange, Node object) {
//        while(iter.hasNext()) {
//            Triple triple = iter.next();
//            Node p = triple.getPredicate() ;
//            List<Node> x = domainRange.get(p) ;
//            if ( x != null ) {
//                for ( Node  t : x ) {
//                    if ( t.equals(object) ) {
//                        return true ;
//                    }
//                }
//            }
//        }
//        return false ;
//    }
//
//    // return matches for { S rdf:type O } ==> { S rdf:type ?T1 } { ?T1 rdfs:subClassOf O }
//    // Rework for efficiency later
//    private Iterator<Triple> find_type(Node s, Node o) {
//        if ( isANY(o) ) {
//            if ( isANY(s) )
//                return find_ANY_type_ANY() ;
//            else
//                return find_X_type_ANY(s) ;
//        }
//        
//        if ( isANY(s) )
//            return find_ANY_type_T(o) ;
//        else
//            return find_X_type_T(s,o) ;
//    }
//        
//    // Find with inference
//    private Iterator<Triple> find_ANY_type_T(Node o) {
//        log.info("find_ANY_type_T") ;
//        List<Node> x = setup.subClasses.get(o) ;
//        if ( x == null || x.isEmpty() )
//            // No inference.
//            return super.find(Node.ANY, rdfType, o) ;
//        
//        List<Triple> results = new ArrayList<>() ;
//        for ( Node type : x ) {
//            ExtendedIterator<Triple> iter = super.find(Node.ANY, rdfType, type) ;
//            iter.forEachRemaining(triple -> results.add(Triple.create(triple.getSubject(), rdfType, o)) );
//            iter.close() ;
//        }
//        return results.stream().distinct().iterator() ;
//    }

    private static Node any(Node node) {
        return ( node == null ) ? Node.ANY : node ;
    }

    private static boolean isANY(Node node) {
        return ( node == null ) || Node.ANY.equals(node) ;
    }

    private static boolean isTerm(Node node) {
        return ( node != null ) && ! Node.ANY.equals(node) ;
    }

}

