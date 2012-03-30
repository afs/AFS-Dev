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

package projects.merge;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;

public class MergeAction
{
    public TupleIndex index1 ;
    public TupleIndex index2 ;
    public String prefix1 ;
    public String prefix2 ;
    public Var joinVar ;        // This is the first col after the prefix
    
    public MergeAction(TupleIndex index1, TupleIndex index2, 
                       String prefix1, String prefix2,
                       Var joinVar)
    {
        super() ;
        this.index1 = index1 ;
        this.index2 = index2 ;
        this.prefix1 = prefix1 ;
        this.prefix2 = prefix2 ;
        this.joinVar = joinVar ;
    }
    
    @Override
    public String toString() 
    {
        return "[Join("+index1.getName()+","+index2.getName()+") ("+prefix1+","+prefix2+") "+joinVar+"]" ;
    }
}
