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

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;
import com.hp.hpl.jena.sparql.util.NodeUtils.EqualityTest ;

// Needs to be back tracking?

/** Simple isomorphism testing */
public class IsoMatcherOld
{
    private final Graph        graph1 ;
    private final Graph        graph2 ;
    private final EqualityTest nodeTest ;

    static class Mapping
    {
        final Node    node1 ;
        final Node    node2 ;
        final Mapping parent ;

        public Mapping(Mapping parent, Node node1, Node node2)
        {
            super() ;
            this.parent = parent ;
            this.node1 = node1 ;
            this.node2 = node2 ;
        }

        public Node map(Node node)
        {
            Mapping mapping = this ;
            while (mapping != null)
            {
                if (mapping.node1.equals(node))
                    return mapping.node2 ;
                mapping = mapping.parent ;
            }
            return null ;
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
        IsoMatcherOld matcher = new IsoMatcherOld(g1, g2, NodeUtils.sameTerm) ;
        return matcher.match() ;
    }

    private IsoMatcherOld(Graph g1, Graph g2, EqualityTest nodeTest)
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
        return match(g1, g2) ;
    }

    public boolean match(List<Triple> triples1, List<Triple> triples2)
    {
        
        Triple t1 = triples1.remove(0) ;
        List<Cause> causes = gen(t1, triples2) ;
        
        for ( Cause c : causes )
        {
            int idx = triples2.indexOf(c.triple) ;
            triples2.remove(idx) ;
            boolean b = match(triples1, triples2) ;
            if ( b )
                return true ;
            triples2.add(idx, c.triple) ;
        }
        return false ;
    }

    private List<Cause> gen(Triple t1, Collection<Triple> g2)
    {
        List<Cause> matches = new ArrayList<Cause>() ;
        for (Triple t2 : g2)
        {
            List<Cause> step = gen(t1, t2, nodeTest) ;
            if (step != null) matches.addAll(step) ;
        }
        return matches ;
    }

    // -------------------------------
    
    // Maybe several mappings!
    private static List<Cause> gen(Triple t1, Triple t2, EqualityTest test)
    {
        // Bnodes are either known, or not.

        return null ;

        // return test.equal(t1.getSubject(), t2.getSubject()) &&
        // test.equal(t1.getPredicate(), t2.getPredicate()) &&
        // test.equal(t1.getObject(), t2.getObject()) ;
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
