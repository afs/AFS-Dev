/*
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

package projects.mvccds;

import org.openjena.atlas.io.IndentedLineBuffer ;
import org.openjena.atlas.io.IndentedWriter ;
import structure.tree.TreeException ;


/** Simple binary tree nodes, including operations that apply to all trees */  

public class TNode<R extends Comparable<R>> //implements Printable
{
    int generation ;
    final long id ;
    // Ideally final but currently we make parent then child so
    // need to set down pointer in parent after the event.
    long left ;
    long right ;
    R record ;
    
    TNode(long id)
    {
        this(id, null, -1, -1, -99) ;
    }
    
    TNode(long id, R record, int generation)
    {
        this(id, record, -1, -1, generation) ;
    }
    
    TNode(long id, R record, long left, long right, int generation)
    {
        this.id = id ;
        this.generation = generation ;
        this.record = record ;
        this.left = left ;
        this.right = right ;
    }
    
    public boolean isNullLeft() { return left < 0 ; }
    public boolean isNullRight() { return right < 0 ; }

    @Override
    public String toString()
    { 
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        output(buff) ;
        return buff.toString() ;
    }
    
    final private static void checkNotNull(Object object)
    {
        if ( object == null )
            throw new TreeException("Null") ;
    }

    public void output(IndentedWriter out)
    {
        out.printf("(id=%-2d, G:%02d) [L:%s R:%s] -- %s", id, generation, 
                   idStr(left), idStr(right), record ) ;
    }
    
    private static <R extends Comparable<R>> String idStr(long node)
    {
        if ( node < 0 ) return "null" ;
        return String.format("%d", node) ;
    }
}
