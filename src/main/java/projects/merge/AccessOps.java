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

package projects.merge;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.InternalErrorException ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.solver.BindingNodeId ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Access operators - algebra operations on the TDB storage objects liek indexes and node tables.
 */
public class AccessOps
{
    // Need to indicate the sortedness coming out.
    public static Iterator<BindingNodeId> mergeJoin(Tuple<Slot> triple1, Tuple<Slot> triple2, TupleIndex[] indexes)
    {
        return null ;
    }
    
    static boolean PRINT = false ;
    
    // Should these be "actions"?
    // Should actions include mapping to variables?
    
    // Need to indicate the join key.
    public static Iterator<BindingNodeId> hashJoin(Tuple<Slot> triple1, Tuple<Slot> triple2, TupleIndex[] indexes)
    {
        return null ;
    }

    // Need to indicate the join key.
    public static Iterator<BindingNodeId> hashJoin(Iterator<BindingNodeId> incoming, Tuple<Slot> triple2, TupleIndex[] indexes)
    {
        return null ;
    }

    // Need to indicate the join key.
    public static Iterator<BindingNodeId> indexJoin(Iterator<BindingNodeId> incoming, Tuple<Slot> triple2, TupleIndex[] indexes)
    {
        return null ;
    }
    
    // --------

    // This could be the primitive for mergeJoin -- it churns BindingNodeIds - can that be fixed? 
    // Need to indicate the sortedness coming out.
    public static Iterator<BindingNodeId> mergeJoin(Iterator<BindingNodeId> iter1, Iterator<BindingNodeId> iter2, Var key)
    {
        List<BindingNodeId> results = new ArrayList<>() ;
        List<BindingNodeId> tmp1 = new ArrayList<>() ;
        List<BindingNodeId> tmp2 = new ArrayList<>() ;
        BindingNodeId row1 = null ;
        BindingNodeId row2 = null ;
        
        if ( PRINT )
        {
            List<BindingNodeId> x = Iter.toList(iter1) ;
            System.out.println("-- Left:") ;
            Iter.print(x.iterator()) ;
            iter1 = x.iterator() ;
        }
        
        if ( PRINT )
        {
            List<BindingNodeId> x = Iter.toList(iter2) ;
            System.out.println("-- Right:") ;
            Iter.print(x.iterator()) ;
            iter2 = x.iterator() ;
        }
        
        
        for(;;)
        {
            if ( row1 == null )
            {
                if ( ! iter1.hasNext() )
                    break ;
                row1 = iter1.next() ;
            }
            if ( row2 == null )
            {
                if ( ! iter2.hasNext() )
                    break ;
                row2 = iter2.next() ;
            }
            
            if ( PRINT )
            {
                System.out.println("row1 = "+row1) ;
                System.out.println("row2 = "+row2) ;
            }
            
            NodeId join1 = row1.get(key) ;
            NodeId join2 = row2.get(key) ;
            
            long v1 = join1.getId() ;
            long v2 = join2.getId() ;
            
            if ( v1 > v2 )
            {
                row2 = null ;
                continue ;
            }
            if ( v1 < v2 )
            {
                row1 = null ;
                continue ;
            }
            
            // if ( v1 == v2 )
            long v = v1 ;
            row1 = advance(v, iter1, key, tmp1, row1) ;
            row2 = advance(v, iter2, key, tmp2, row2) ;
            join(results, key, tmp1, tmp2) ;
        }
        return results.iterator() ;
    }

    // XXX When the key is missing.
    private static BindingNodeId advance(long v, Iterator<BindingNodeId> iter, Var key, List<BindingNodeId> acc, BindingNodeId row)
    {
        for (;;)
        {
            long v1 = row.get(key).getId() ;
            if ( v != v1 )
                break ;
            acc.add(row) ;
            if ( ! iter.hasNext() )
                return null ;
            row = iter.next() ;
            if ( ! row.containsKey(key) )
                return null ;
        }
        return row ;
    }

    private static void join(List<BindingNodeId> results, Var joinVar, List<BindingNodeId> tmp1, List<BindingNodeId> tmp2)
    {
        if ( PRINT )
            System.out.println("join left="+tmp1.size()+" right="+tmp2.size()) ;
        for ( BindingNodeId row1 : tmp1 )
            for ( BindingNodeId row2 : tmp2 )
            {
                if ( PRINT )
                    System.out.println("Join: "+row1+" "+row2) ;
                long j1 = row1.get(joinVar).getId() ; 
                long j2 = row2.get(joinVar).getId() ;
                if ( j1 != j2 )
                    continue ;
                
                BindingNodeId b = new BindingNodeId((Binding)null) ;
                b.putAll(row1) ;
                b.putAll(row2) ;
                if ( PRINT )
                    System.out.println("Bind => "+b) ;
                results.add(b) ;
            }
        tmp1.clear() ;
        tmp2.clear() ;
    }

    static BindingNodeId bind(Tuple<NodeId> row, Tuple<Slot> vars)
    {
        return bind(new BindingNodeId(), row, vars) ;
    }
    
    static BindingNodeId bind(BindingNodeId b, Tuple<NodeId> row, Tuple<Slot> vars)
    {
        // Tuples from indexes are in natural order.
        if ( PRINT ) 
            System.out.println("Bind: "+vars+" "+row) ;

        if ( row.size() != vars.size() )
            throw new InternalErrorException("Not aligned: "+row+" "+vars) ;
        
        for ( int i = 0 ; i < vars.size() ; i++ )
        {
            Slot slot = vars.get(i) ; 
            if ( ! slot.isVar() )
                continue ;
            NodeId id = row.get(i) ;
            NodeId id2 = b.get(slot.var) ;
            if ( id2 != null )
            {
                // already bound ... test compatibility.
                if ( id2.getId() == id.getId() )
                    continue ;
                return null ;
            }
            b.put(slot.var, id) ;
        }
        return b ;
    }
}

