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

package projects.join2;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;

public class IndexAccess
{
    // Special cases:
    // Two vars: ?x <p> ?x
    // Three vars: ?x ?x ?x
    private TupleIndex index ;
    private int prefixLen ;
    private Var var ;

    public IndexAccess(TupleIndex index, int prefixLen, Var var)
    {
        this.index = index ;
        this.prefixLen = prefixLen ;
        this.var = var ;
    }

    public int getPrefixLen()       { return prefixLen ; }

    public TupleIndex getIndex()    { return index ; } 

    public Var getVar()             { return var ; }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder() ;
        builder.append("[") 
               .append(index.getName())
               .append("/")
               .append(index.getName().substring(0, prefixLen))
               .append("->")
               .append(var)
               .append("]") ;
        return builder.toString() ;
    }
}
