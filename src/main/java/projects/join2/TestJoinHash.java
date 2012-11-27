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

import java.util.Iterator ;
import java.util.List ;

import org.junit.Test ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.tdb.solver.BindingNodeId ;

public class TestJoinHash extends BaseTest
{
    static List<BindingNodeId> table0 = Support.parseTableNodeId("(table)") ;
    static List<BindingNodeId> table1 = Support.parseTableNodeId("(table",
                                                                 "   (row (?a 1) (?b 2))",
                                                                 "   (row (?a 1) (?b 3))",
                                                                 ")") ;
    
    static List<BindingNodeId> table2 = Support.parseTableNodeId("(table",
                                                                 "   (row (?a 0) (?d 8))",
                                                                 "   (row (?a 1) (?c 9))",
                                                                 ")") ;
    static List<BindingNodeId> table3 = Support.parseTableNodeId("(table",
                                                                 "   (row (?a 1) (?c 9) (?b 2))",
                                                                 "   (row (?a 1) (?c 9) (?b 3))",
                                                                 ")") ;

    @Test public void hash_00() { testHashJoin(Var.alloc("a"), table0, table2, table0) ; }
    @Test public void hash_01() { testHashJoin(Var.alloc("a"), table1, table0, table0) ; }

    @Test public void hash_05() { testHashJoin(Var.alloc("a"), table1, table2, table3) ; }
    
    private static void testHashJoin(Var key, List<BindingNodeId> table1, List<BindingNodeId> table2, List<BindingNodeId> table3)
    {
        Iterator<BindingNodeId> iter =  AccessOps.hashJoin(table1.iterator(), table2.iterator(), key) ;
        List<BindingNodeId> results = Iter.toList(iter) ;
        assertTrue(Support.equals(table3, results)) ;
    }
}

