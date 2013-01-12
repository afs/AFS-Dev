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

import java.io.PrintStream ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphWrapper ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

public class DatasetGraphRecorder extends DatasetGraphWrapper
{
    private boolean CheckFirst = true ;
    private boolean RecordNoAction = true ;
    
    public DatasetGraphRecorder(DatasetGraph dsg)
    {
        super(dsg) ;
    }

    @Override public void add(Quad quad)
    {
        if ( CheckFirst && contains(quad) )
        {
            if ( RecordNoAction )
                record(Action.NO_ADD, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
            return ;
        }
        add$(quad) ;
    }
    
    @Override public void add(Node g, Node s, Node p, Node o)
    {
        if ( CheckFirst && contains(g,s,p,o) )
        {
            if ( RecordNoAction )
                record(Action.NO_ADD,g,s,p,o) ; 
            return ;
        }
        
        add$(g,s,p,o) ;
    }
    
    private void add$(Node g, Node s, Node p, Node o)
    {
        super.add(g,s,p,o) ;
        record(Action.ADD,g,s,p,o) ; 
    }
    
    private void add$(Quad quad)
    {
        super.add(quad) ;
        record(Action.ADD, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    @Override public void delete(Quad quad)
    {
        if ( CheckFirst && ! contains(quad) )
        {
            if ( RecordNoAction )
                record(Action.NO_DELETE, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
            return ;
        }
        delete$(quad) ;
    }
    
    @Override public void delete(Node g, Node s, Node p, Node o)
    {
        if ( CheckFirst && ! contains(g,s,p,o) )
        {
            if ( RecordNoAction )
                record(Action.NO_DELETE, g,s,p,o) ;
            return ;
        }
        delete$(g,s,p,o) ;
    }
    
    private void delete$(Quad quad)
    {
        super.delete(quad) ;
        record(Action.DELETE, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }
    
    private void delete$(Node g, Node s, Node p, Node o)
    {
        super.delete(g,s,p,o) ;
        record(Action.DELETE,g,s,p,o) ; 
    }
    

    private static int SLICE = 1000 ;
    
    @Override
    public void deleteAny(Node g, Node s, Node p, Node o)
    {
        // Need to find otu what is actually deleted.
        // Could record explicit ..... super.deleteAny(g,s,p,o) ;
        // or record actually diff ...

        while (true)
        {
            Iterator<Quad> iter = find(g, s, p, o) ;

            // Read some
            List<Quad> some = take(iter, SLICE) ;
            for (Quad q : some)
                delete$(q) ;
            if (some.size() < SLICE) break ;
        }
    }
    
    static <T> List<T> take(Iterator<T> iter, int N)
    {
        iter = new IteratorN<T>(iter, N) ;
        List<T> x = new ArrayList<T>(N) ;
        for ( ; iter.hasNext() ; )
            x.add(iter.next()) ;
        return x ;
    }
    
    static class IteratorN<T> implements Iterator<T>
    {
        private final Iterator<T> iter ;
        private final int N ;
        private int count ;

        IteratorN(Iterator<T> iter, int N) {
            this.iter = iter ;
            this.N  = N ;
            this.count = 0 ;
        }

        @Override
        public boolean hasNext()
        {
            if ( count >= N )
                return false ;
            return iter.hasNext() ;
        }

        @Override
        public T next()
        {
            if ( count >= N )
                throw new NoSuchElementException() ;
            T x = iter.next() ;
            count++ ;
            return null ;
        }

        @Override
        public void remove()
        {
            // But leave the count as-is.
            iter.remove() ;
        }
    }

    @Override public void addGraph(Node gn, Graph g)
    {
        // Convert to quads.
        //super.addGraph(gn, g) ;
        ExtendedIterator<Triple> iter = g.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext(); )
        {
            Triple t = iter.next() ;
            add(gn, t.getSubject(), t.getPredicate(), t.getObject()) ;
        }
    }
    
    @Override public void removeGraph(Node gn)
    {
        //super.removeGraph(gn) ;
        deleteAny(gn, Node.ANY, Node.ANY, Node.ANY) ;
    }
    
    static enum Action { ADD("A"), DELETE("D"), NO_ADD("#A"), NO_DELETE("#D") ;
        final String label ;
        Action(String label) { this.label = label ; }
    
    }
    static final String SEP1 = ", " ;    // TAB is good. 
    static final String SEP2 = "\n" ; 
    private void record(Action action, Node g, Node s, Node p, Node o)
    {
        PrintStream out = System.out ; 
        out.print(action.label) ;
        out.print(SEP1) ;
        print(out, g) ;
        out.print(SEP1) ;
        print(out, s) ;
        out.print(SEP1) ;
        print(out, p) ;
        out.print(SEP1) ;
        print(out, o) ;
        out.print(SEP2) ;
    }
    
    private void print(PrintStream out, Node x)
    {
        String str = FmtUtils.stringForNode(x) ;
        out.print(str) ;
    }
}

