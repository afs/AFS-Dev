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

import static inf.InfGlobal.rdfType ;
import static inf.InfGlobal.rdfsDomain ;
import static inf.InfGlobal.rdfsRange ;
import static inf.InfGlobal.rdfsSubClassOf ;

import java.util.* ;
import java.util.stream.Stream ;
import java.util.stream.StreamSupport ;

import org.apache.jena.atlas.iterator.* ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.sparql.graph.GraphWrapper ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NullIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

public class GraphRDFS_Iter extends GraphWrapper {
    private static Logger log = LoggerFactory.getLogger(GraphRDFS_Iter.class) ;
    
    private Find3_Graph fGraph ;
    private InferenceSetupRDFS setup ;
    private boolean infDomain ;
    private boolean infRange ;

    public GraphRDFS_Iter(InferenceSetupRDFS setup, Graph graph) {
        super(graph) ;
        this.setup = setup ;
        infDomain = ! setup.domainList.isEmpty() ;
        infRange =  ! setup.rangeList.isEmpty() ;
        fGraph = new Find3_Graph(setup, graph) ;
    }
    
    @Override
    public ExtendedIterator<Triple> find(TripleMatch m) {
        return find(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        Iterator<Triple> iter = fGraph.find(s, p, o) ;
        return WrappedIterator.create(iter) ;
    }

    public static <T> Stream<T> stream(Iterator<? extends T> iterator) {
        int characteristics = Spliterator.ORDERED | Spliterator.IMMUTABLE;
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, characteristics), false);
    }
    
    interface StreamGraph<Y, X> {
        // Stream?
        Iterator<Y> find(X s, X p, X o) ;
    }

    static class Find3_Graph implements StreamGraph<Triple, Node> {
        private final Graph graph ;
        private final InferenceSetupRDFS setup ;
        private final boolean infRange ;
        private final boolean infDomain ;

        Find3_Graph(InferenceSetupRDFS setup, Graph graph) {
            this.setup = setup ;
            this.graph = graph ;
            this.infRange =  ! setup.rangeList.isEmpty() ;
            this.infDomain = ! setup.domainList.isEmpty() ;
        }

        @Override
        public Iterator<Triple> find(Node s, Node p, Node o) {
            // Quick route to conversion.
            return find2(s,p,o) ;
        }

        // Iter?
        protected Iterator<Triple> sourceFind(Node s, Node p, Node o) {
            ExtendedIterator<Triple> iter = graph.find(s,p,o) ;
            return new ExtendedIteratorAutoClose<>(iter) ;
        }


        private Iterator<Triple> find2(Node _subject, Node _predicate, Node _object) {
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
                return sourceFind(subject, predicate, object) ;

            // Hard work, not scalable.
            //Don't forget predicate itself!
            Set<Triple> triples = Iter.toSet(sourceFind(subject, predicate, object)) ;

            for ( Node p : predicates ) {
                Iterator<Triple> iter = sourceFind(subject, p, object) ;

                // replace p by predicate
                Transform<Triple, Triple> map = new Transform<Triple, Triple>(){
                    @Override
                    public Triple convert(Triple triple) {
                        return Triple.create(triple.getSubject(), predicate, triple.getObject()) ;
                    }
                } ;

                Iterator<Triple> iter2 = Iter.map(iter, map) ;
                iter2.forEachRemaining(triple->triples.add(triple)) ;
            }
            return triples.iterator() ;
        }

        private Iterator<Triple> singletonIterator(Node s, Node p, Node o) {
            return new SingletonIterator<>(Triple.create(s, p, o)) ;
        }

        private Iterator<Triple> nullIterator() {
            return new NullIterator<>() ;
        }

        private Iterator<Triple> find_X_type_T(Node subject, Node object) {
            if (graph.contains(subject, rdfType, object) )
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

        private Iterator<Triple> find_X_type_ANY(Node subject) {
            Set<Node> types = new HashSet<>() ;
            accTypesRange(types, subject) ;
            accTypesDomain(types, subject) ;
            accTypes(types, subject) ;
            // expand supertypes
            types = superTypes(types) ;
            return types.stream().map( type -> Triple.create(subject, rdfType, type)).iterator() ; 
        }

        private Iterator<Triple> find_ANY_type_T(Node type) {
            // Fast path no subClasses
            Set<Node> types = subTypes(type) ;
            
            // Stream / distinct ?
            Set<Triple> triples = new HashSet<>() ;

            accInstances(triples, types, type) ;
            accInstancesRange(triples, types, type) ;
            accInstancesDomain(triples, types, type) ;

            return triples.iterator() ;
        }

        private Iterator<Triple> find_ANY_type_ANY() {
            // Better?
            Iterator<Triple> iter = sourceFind(Node.ANY, Node.ANY, Node.ANY) ;
            Iterator<Triple> iter2 = new InferenceProcessorIteratorRDFS(setup, iter) ;
            Stream<Triple> stream = stream(iter2) ;
            stream = stream.filter(triple -> triple.getPredicate().equals(rdfType)) ;
            return stream.iterator() ;
        }

        private Iterator<Triple> find_X_ANY_T(Node subject, Node object) {
            // Start at X.
            // (X ? ?) - inference - project "X ? T"
            // also (? ? X) if there is a range clause. 
            Iterator<Triple> iter = sourceFind(subject, Node.ANY, Node.ANY) ;
            // + reverse (used in object position and there is a range clause)
            if ( infRange )
                iter = Iter.concat(iter, sourceFind(Node.ANY, Node.ANY, subject)) ;
            return infFilterSubjObj(iter, subject, Node.ANY, object, false) ;
        }

        private Iterator<Triple> find_X_ANY_ANY(Node subject) {
            // Can we do better?
            return find_X_ANY_T(subject, Node.ANY) ;
        }

        private Iterator<Triple> find_ANY_ANY_T(Node object) {
            Iterator<Triple> iter = sourceFind(Node.ANY, Node.ANY, object) ;
            iter = Iter.filter(iter, filterDropRdfType) ;
            // and get via inference.
            // Exclude rdf:type and do by inference?
            iter = Iter.concat(iter, find_ANY_type_T(object)) ;

            // ? ? P (range) does not find :x a :P when :P is a class
            // and "some p range P"
            // Include from setup?
            boolean ensureDistinct = false ;
            if ( setup.includeDerivedDataRDFS ) {
                // These cause duplicates.
                ensureDistinct = true ;
                // Java7 compatibility :-(
                IteratorConcat<Triple> iter2 = new IteratorConcat<>() ;
                iter2.add(setup.vocabGraph.find(Node.ANY, rdfsRange, object)) ;
                iter2.add(setup.vocabGraph.find(Node.ANY, rdfsDomain, object)) ;
                iter2.add(setup.vocabGraph.find(Node.ANY, rdfsRange, object)) ;
                iter2.add(setup.vocabGraph.find(object, rdfsSubClassOf, Node.ANY)) ;
                iter2.add(setup.vocabGraph.find(Node.ANY, rdfsSubClassOf, object)) ;
                iter = iter2 ;
            }
            return infFilterSubjObj(iter, Node.ANY, Node.ANY, object, ensureDistinct) ;
        }

        //    private static <X> Iterator<X> distinct(Iterator<X> iter) {
        //        return Iter.distinct(iter) ;
        //    }

        private Iterator<Triple> find_ANY_ANY_ANY() {
            Iterator<Triple> iter = sourceFind(Node.ANY, Node.ANY, Node.ANY) ;
            Iterator<Triple> iter2 = new InferenceProcessorIteratorRDFS(setup, iter) ;
            if ( setup.includeDerivedDataRDFS )
                iter2 = Iter.distinct(iter2) ;
            return iter2 ;
        }

        private Iterator<Triple> infFilterSubjObj(Iterator<Triple> iter, Node subject, Node predicate, Node object, boolean ensureDistinct) {
            Iterator<Triple> iter2 = new InferenceProcessorIteratorRDFS(setup, iter) ;

            Stream<Triple> stream = stream(iter2) ;
            if ( isTerm(predicate) )
                stream = stream.filter(triple -> { return triple.getPredicate().equals(predicate) ; } ) ;

            if ( isTerm(object) )
                stream = stream.filter(triple -> { return triple.getObject().equals(object) ; } ) ;
            if ( isTerm(subject) )
                stream = stream.filter(triple -> { return triple.getSubject().equals(subject) ; } ) ;
            // When needed?
            if ( ensureDistinct ) 
                stream = stream.distinct() ;
            return stream.iterator() ;
        }

        private void accInstances(Set<Triple> triples, Set<Node> types, Node requestedType) {
            for ( Node type : types ) {
                Iterator<Triple> iter = sourceFind(Node.ANY, rdfType, type) ;
                iter.forEachRemaining(triple -> triples.add(Triple.create(triple.getSubject(), rdfType, requestedType)) );
            }
        }

        private void accInstancesDomain(Set<Triple> triples, Set<Node> types, Node requestedType) {
            for ( Node type : types ) {
                List<Node> predicates = setup.domainPropertyList.get(type) ;
                if ( predicates == null )
                    continue ;
                predicates.forEach(p -> {
                    Iterator<Triple> iter = sourceFind(Node.ANY, p, Node.ANY) ;
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
                    Iterator<Triple> iter = sourceFind(Node.ANY, p, Node.ANY) ;
                    iter.forEachRemaining(triple -> triples.add(Triple.create(triple.getObject(), rdfType, requestedType)) );
                }) ;
            }
        }

        private void accTypes(Set<Node> types, Node subject) {
            Iterator<Triple> iter = sourceFind(subject, rdfType, Node.ANY) ;
            iter.forEachRemaining(triple -> types.add(triple.getObject())) ;
        }

        private void accTypesDomain(Set<Node> types, Node node) {
            Iterator<Triple> iter = sourceFind(node, Node.ANY, Node.ANY) ;
            iter.forEachRemaining(triple -> {
                Node p = triple.getPredicate() ;
                List<Node> x = setup.domainList.get(p) ;
                if ( x != null )
                    types.addAll(x) ;
            }) ;
        }

        private void accTypesRange(Set<Node> types, Node node) {
            Iterator<Triple> iter = sourceFind(Node.ANY, Node.ANY, node) ;
            iter.forEachRemaining(triple -> {
                Node p = triple.getPredicate() ;
                List<Node> x = setup.rangeList.get(p) ;
                if ( x != null )
                    types.addAll(x) ;
                x = setup.domainList.get(p) ;
                if ( x != null )
                    types.addAll(x) ;
            }) ;
        }

        private Set<Node> X_subTypes(Set<Node> types) {
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

        private Set<Node> X_superTypes(Node type) {
            Set<Node> x = new HashSet<>() ;
            x.add(type) ;
            List<Node> y = setup.superClasses.get(type) ;
            if ( y != null )
                x.addAll(y) ;
            return x ;
        }
    }
    
    static private Filter<Triple> filterDropRdfType = new Filter<Triple>() {
        @Override
        public boolean accept(Triple triple) {
            return ! triple.getPredicate().equals(rdfType) ;
        } } ;
    

    static public Iterator<Triple> print(Iterator<Triple> iter) {
        List<Triple> triples = new ArrayList<>() ; 
        for ( ; iter.hasNext() ;)
            triples.add(iter.next()) ;
        triples.stream().forEach(t -> System.out.println("# "+t)) ;
        return triples.iterator() ;
    }

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

