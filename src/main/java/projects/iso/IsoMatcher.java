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

package projects.iso ;

import java.util.* ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Pair ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;
import com.hp.hpl.jena.sparql.util.NodeUtils.EqualityTest ;

// Needs to be back tracking?

/** Simple isomorphism testing */
public class IsoMatcher
{
    static boolean DEBUG = true ;
    private final Graph        graph1 ;
    private final Graph        graph2 ;
    
    private final Map<Node, Node>           mapping = new HashMap<>();
    private final Queue<Pair<Node, Node>>   causes  = new LinkedList<>() ;  
    private final EqualityTest nodeTest ;

    static class Mapping
    {
        final Node    node1 ;
        final Node    node2 ;
        final Mapping parent ;

        static Mapping rootMapping = new Mapping(null, null, null) ;
        
        public Mapping(Mapping parent, Node node1, Node node2)
        {
            super() ;
            this.parent = parent ;
            this.node1 = node1 ;
            this.node2 = node2 ;
        }

        public boolean mapped(Node node)    { return map(node) != null ; } 
        public boolean revmapped(Node node) { return revmap(node) != null ; }
        
        public Node map(Node node)
        {
            Mapping mapping = this ;
            while (mapping != rootMapping)
            {
                if (mapping.node1.equals(node))
                    return mapping.node2 ;
                mapping = mapping.parent ;
            }
            return null ;
        }
        
        // Reverse mapping.
        public Node revmap(Node node)
        {
            Mapping mapping = this ;
            while (mapping != rootMapping)
            {
                if (mapping.node2.equals(node))
                    return mapping.node1 ;
                mapping = mapping.parent ;
            }
            return null ;
        }

        @Override
        public String toString() {
            StringBuilder sbuff = new StringBuilder() ;
            Mapping mapping = this ;
            while (mapping != rootMapping)
            {
                sbuff.append("{"+mapping.node1+" => "+mapping.node2+"}") ;
                mapping = mapping.parent ;
            }
            sbuff.append("{}") ;
            return sbuff.toString() ;
        }
    }
    
    static class Cause
    {
        final Triple  triple ;
        final Mapping mapping ;

        public Cause(Triple triple, Mapping mapping)
        {
            super() ;
            this.triple = triple ;
            this.mapping = mapping ;
        }
    }


    public static boolean isomorphic(Graph g1, Graph g2)
    {
        IsoMatcher matcher = new IsoMatcher(g1, g2, NodeUtils.sameTerm) ;
        return matcher.match() ;
    }

    private IsoMatcher(Graph g1, Graph g2, EqualityTest nodeTest)
    {
        this.graph1 = g1 ;
        this.graph2 = g2 ;
        this.nodeTest = nodeTest ;
    }

    public boolean match()
    {
        // Mutated.
        List<Triple> g1 = Iter.toList(graph1.find(null, null, null)) ;
        List<Triple> g2 = Iter.toList(graph2.find(null, null, null)) ;
        return match(g1, g2, Mapping.rootMapping) ;
    }

    public boolean match(List<Triple> triples1, List<Triple> triples2, Mapping mapping)
    {
        if ( DEBUG ) {
            System.out.println("match: ") ;
            System.out.println("  "+triples1) ;
            System.out.println("  "+triples2) ;
            System.out.println("  "+mapping) ; 
        }
        if ( triples1.size() != triples2.size() )
            return false;
        
        List<Triple> triples = new ArrayList<>(triples1) ;  // Copy, mutate
        for ( Triple t1 : triples1 ) {
            if ( DEBUG )
                System.out.println("  t1 = "+t1) ;
            triples.remove(t1) ;
            List<Cause> causes = match(t1, triples2, mapping) ;
            for ( Cause c : causes ) {
                if ( DEBUG ) 
                    System.out.println("  Try: "+c.mapping) ;
                // Try t1 -> t2
                Triple t2 = c.triple ;
                triples2.remove(t2) ;
                if ( triples2.isEmpty() )
                    return true ;
                if ( match(triples, triples2, c.mapping) ) {
                    if ( DEBUG ) 
                        System.out.println("Yes") ;
                    return true ;
                }
                if ( DEBUG ) 
                    System.out.println("No") ;
                triples2.add(t2) ;
            }
            return false ;
        }
        return false ;
    }

    private List<Cause> match(Triple t1, Collection<Triple> g2, Mapping mapping)
    {
        List<Cause> matches = new ArrayList<>() ;
        for (Triple t2 : g2)
        {
            // No - multiple bNodes.
            Mapping step = gen(t1, t2, mapping) ;
            if (step != null) { 
                Cause c = new Cause(t2, step) ;
                matches.add(c) ;
            }
        }
        return matches ;
    }

    // -------------------------------
    
//    private Triple subtitute(Triple t, Mapping mapping) {
//        if ( mapping == null )
//            return t ;
//        Node s = mapping.map(t.getSubject()) ;
//        Node p = mapping.map(t.getPredicate()) ;
//        Node o = mapping.map(t.getPredicate()) ;
//        return Triple.create(s,p,o) ;
//    }

    // Maybe several mappings!
    private Mapping gen(Triple t1, Triple t2, Mapping _mapping)
    {
        Mapping mapping = _mapping ;
        Node s2 = t2.getSubject() ;
        Node p2 = t2.getPredicate() ;
        Node o2 = t2.getObject() ;
        
        Node s1 = t1.getSubject() ;
        if ( ! nodeTest.equal(s1, s2) )
        {
            mapping = gen(s1, s2, mapping) ;
            if ( mapping == null )
                return null ;
        }
        
        Node p1 = t1.getPredicate() ;
        if ( ! nodeTest.equal(p1, p2) )
        {
            mapping = gen(p1, p2, mapping) ;
            if ( mapping == null )
                return null ;
        }
        
        
        Node o1 = t1.getObject() ;
        if ( ! nodeTest.equal(o1, o2) )
        {
            mapping = gen(o1, o2, mapping) ;
            if ( mapping == null )
                return null ;
        }
        return mapping ;
    }

    private Mapping gen(Node x1, Node x2, Mapping mapping) {
        if ( x1.isBlank() && x2.isBlank() ) {
            // Is x1 already mapped?
            Node z = mapping.map(x1) ;
            if ( z != null )
                // Already mapped
                return (nodeTest.equal(x2, z)) ? mapping : null ;
            // Check reverse
            if ( mapping.revmapped(x2) )
                return null ;
            return new Mapping(mapping, x1, x2) ;
            
        }
        return null ;
    }
    
    private Node substitute(Node n, Mapping mapping) {
        if ( ! n.isBlank() )
            return n ;
//        if ( mapping == null )
//            return n ;
        Node n2 = mapping.map(n) ;
        if ( n2 == null )
            return n ;
        return n2 ;
    }
    
    
    // add must be bNode in ANY slots
    public static List<Triple> findC(Collection<Triple> triples, Triple t)
    {
        if (t.isConcrete())
        {
            if (triples.contains(t))
            {
                List<Triple> matches = new ArrayList<Triple>() ;
                matches.add(t) ;
                return matches ;
            }
        }

        Node s = t.getSubject() ;
        Node p = t.getPredicate() ;
        Node o = t.getObject() ;

        if (!s.isConcrete()) s = Node.ANY ;
        if (!p.isConcrete()) p = Node.ANY ;
        if (!o.isConcrete()) o = Node.ANY ;

        List<Triple> matches = new ArrayList<Triple>() ;
        for (Triple t2 : triples)
        {
            if (t2.matches(s, p, o)) matches.add(t2) ;
        }
        return matches ;
    }

}
