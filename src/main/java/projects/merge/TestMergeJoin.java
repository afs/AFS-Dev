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

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

public class TestMergeJoin extends BaseTest
{
    static Location loc = Location.mem() ;

    static TupleIndex SPO = SetupTDB.makeTupleIndex(loc, "SPO", "SPO", "SPO", 3*NodeId.SIZE) ;
    static TupleIndex POS = SetupTDB.makeTupleIndex(loc, "SPO", "POS", "POS", 3*NodeId.SIZE) ;
    static TupleIndex PSO = SetupTDB.makeTupleIndex(loc, "SPO", "PSO", "PSO", 3*NodeId.SIZE) ;
    static TupleIndex OSP = SetupTDB.makeTupleIndex(loc, "SPO", "OSP", "OSP", 3*NodeId.SIZE) ;

    static TupleIndex [] indexesP2 = { POS, PSO } ;
    static TupleIndex [] indexes1 =  { SPO, POS, OSP } ;
    
    // tests with partial index coverage.
    
    @Test public void chooseMerge_01()      { test("(?s <p> ?o)", "(?s <q> 123)", indexesP2, PSO, POS) ; }
    @Test public void chooseMerge_02()      { test("(?s <p> ?o)", "(?s <q> ?v)",  indexesP2, PSO, PSO) ; }
    @Test public void chooseMerge_03()      { test("(?s <p> ?z)", "(?z <q> ?v)",  indexesP2, POS, PSO) ; }
    @Test public void chooseMerge_04()      { test("(?s <p> ?z)", "(?z <q> 123)", indexesP2, POS, POS) ; }
    @Test public void chooseMerge_05()      { test("(?x <p> ?x)", "(?x <q> ?v)",  indexesP2, PSO, PSO) ; }
    @Test public void chooseMerge_06()      { test("(?a <p> ?b)", "(?c <q> ?d)",  indexesP2, null, null) ; }
    
    @Test public void chooseMerge_10()      { test("(?s <p> ?o)", "(?s <q> 123)", indexes1, PSO, POS) ; }

    private static void test(String tripleStr1, String tripleStr2, TupleIndex[] indexes, TupleIndex index1, TupleIndex index2)
    {
        Triple triple1 = SSE.parseTriple(tripleStr1) ;
        Triple triple2 = SSE.parseTriple(tripleStr2) ;
        MergeActionIdxIdx action = MergeLib.calcMergeAction(triple1, triple2, indexes) ;
        
        if ( index1 != null )
            assertNotNull("No match", action) ;
        else
        {
            assertNull("Match, none expected", action) ;
            return ;
        }

        TupleIndex i1 = action.getIndexAccess1().getIndex() ;
        TupleIndex i2 = action.getIndexAccess2().getIndex() ;

        assertEquals(index1, i1) ;
        assertEquals(index2, i2) ;
    }

}

