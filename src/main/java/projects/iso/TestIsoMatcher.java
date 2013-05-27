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

package projects.iso;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.sparql.sse.SSE ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestIsoMatcher extends BaseTest
{
    @Test public void iso_01() { test("(<x> <p> 1)",
                                      "(<x> <p> 1)",
                                      true) ; }

    @Test public void iso_02() { test("(<x> <p> 1)",
                                      "(<x> <p> 2)",
                                      false) ; }

    @Test public void iso_03() { test("(<x> <p> 1) (<x> <p> 2)",
                                      "(<x> <p> 2)",
                                      false) ; }
    
    @Test public void iso_04() { test("(<x> <p> _:a)",
                                      "(<x> <p> 2)",
                                      false) ; }

    @Test public void iso_10() { test("(<x> <p> _:a)",
                                      "(<x> <p> _:b)",
                                      true) ; }

    @Test public void iso_11() { test("(<x> <p> _:a) (<z> <p> _:a)",
                                      "(<x> <p> _:b) (<z> <p> _:b)",
                                      true)  ; }

    @Test public void iso_12() { test("(<x> <p> _:a1) (<z> <p> _:a2)",
                                      "(<x> <p> _:b) (<z> <p> _:b)",
                                      false)  ; }

    @Test public void iso_13() { test("(_:a <p> _:a)",
                                      "(_:b <p> _:b)",
                                      true)  ; }

    @Test public void iso_14() { test("(_:a1 <p> _:a2)",
                                      "(_:bb <p> _:bb)",
                                      false)  ; }
    
    @Test public void iso_15() { test("(_:a <p> _:a) (<s> <q> _:a)",
                                      "(_:b <p> _:b) (<s> <q> _:b)",
                                      true)  ; }

    @Test public void iso_16() { test("(_:a <p> _:a) (<s> <q> _:a)",
                                      "(_:b <p> _:b) (<s> <q> _:c)",
                                      false)  ; }

    @Test public void iso_20() { test("(_:a _:a _:a)",
                                      "(_:b _:b _:b)",
                                      true)  ; }
    
    @Test public void iso_21() { test("(_:a _:a _:a)",
                                      "(_:z _:b _:b)",
                                      false)  ; }

    @Test public void iso_22() { test("(_:a _:a _:a)",
                                      "(_:b _:z _:b)",
                                      false)  ; }

    @Test public void iso_23() { test("(_:a _:a _:a)",
                                      "(_:b _:b _:z)",
                                      false)  ; }

    @Test public void iso_24() { test("(_:a _:a _:b)",
                                      "(_:b _:b _:z)",
                                      true)  ; }
    
    @Test public void iso_25() { test("(_:a _:x _:a)",
                                      "(_:b _:z _:b)",
                                      true)  ; }

    @Test public void iso_26() { test("(_:x _:a _:a)",
                                      "(_:z _:b _:b)",
                                      true)  ; }

    private void test(String s1, String s2, boolean iso) {
        testWorker(s1, s2, iso) ;
        testWorker(s2, s1, iso) ;
    }
    
    private void testWorker(String s1, String s2, boolean iso) {
        s1 = "(graph "+s1+")" ;
        s2 = "(graph "+s2+")" ;
        
        Graph g1 = SSE.parseGraph(s1) ;
        Graph g2 = SSE.parseGraph(s2) ;
        boolean b = IsoMatcher.isomorphic(g1, g2) ;
            
        if ( b != iso ) {
            System.out.println("====") ;
            SSE.write(g1) ;
            System.out.println("----") ;
            SSE.write(g2) ;
            System.out.println("Expected: "+iso+" ; got: "+b) ;
        }
        
        assertEquals(iso, b) ;
        // Check with the other code.
        assertEquals(b, g1.isIsomorphicWith(g2)) ;
    }
}
