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

package projects.join2;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class Slot {
    // ?? Include nodeTable so self contained "to node"
    
    
    final NodeId id ;
    final Var var ;
    Slot(Var var) { this(var, NodeId.NodeIdAny) ; }
    Slot(NodeId id) { this(null, id) ; }
    
    private Slot(Var var, NodeId id)
    {
        this.id = id ;
        this.var = var ;
    }
    
    boolean isVar()     { return var != null ; }
    boolean isNodeId()  { return var == null ; }
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder() ;
        //builder.append("{") ;
        if ( var == null )
        {
            builder.append("id=") ;
            builder.append(id) ;
        }else {
            builder.append("var=") ;
            builder.append(var) ;
        }
        //builder.append("}") ;
        return builder.toString() ;
    }
    @Override
    public int hashCode()
    {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((id == null) ? 0 : id.hashCode()) ;
        result = prime * result + ((var == null) ? 0 : var.hashCode()) ;
        return result ;
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true ;
        if (obj == null) return false ;
        if (getClass() != obj.getClass()) return false ;
        Slot other = (Slot)obj ;
        if (id == null)
        {
            if (other.id != null) return false ;
        } else
            if (!id.equals(other.id)) return false ;
        if (var == null)
        {
            if (other.var != null) return false ;
        } else
            if (!var.equals(other.var)) return false ;
        return true ;
    }
}
