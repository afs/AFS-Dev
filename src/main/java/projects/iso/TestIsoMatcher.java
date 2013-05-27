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

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestIsoMatcher extends BaseTest
{
    @Test public void iso_00() { testGraph("",
                                      "",
                                      true) ; }

    
    @Test public void iso_01() { testGraph("(<x> <p> 1)",
                                      "(<x> <p> 1)",
                                      true) ; }

    @Test public void iso_02() { testGraph("(<x> <p> 1)",
                                      "(<x> <p> 2)",
                                      false) ; }

    @Test public void iso_03() { testGraph("(<x> <p> 1) (<x> <p> 2)",
                                      "(<x> <p> 2)",
                                      false) ; }
    
    @Test public void iso_04() { testGraph("(<x> <p> _:a)",
                                      "(<x> <p> 2)",
                                      false) ; }

    @Test public void iso_05() { testGraph("(<x> <p> _:a)",
                                      "(<x> <p> _:b)",
                                      true) ; }

    @Test public void iso_06() { testGraph("(_:a <p> _:a)",
                                      "(_:b <p> _:b)",
                                      true)  ; }

    @Test public void iso_07() { testGraph("(_:a1 <p> _:a2)",
                                      "(_:bb <p> _:bb)",
                                      false)  ; }
    
    @Test public void iso_10() { testGraph("(_:a _:a _:a)",
                                      "(_:b _:b _:b)",
                                      true)  ; }
    
    @Test public void iso_11() { testGraph("(_:a _:a _:a)",
                                      "(_:z _:b _:b)",
                                      false)  ; }

    @Test public void iso_12() { testGraph("(_:a _:a _:a)",
                                      "(_:b _:z _:b)",
                                      false)  ; }

    @Test public void iso_13() { testGraph("(_:a _:a _:a)",
                                      "(_:b _:b _:z)",
                                      false)  ; }

    @Test public void iso_14() { testGraph("(_:a _:a _:b)",
                                      "(_:b _:b _:z)",
                                      true)  ; }
    
    @Test public void iso_15() { testGraph("(_:a _:x _:a)",
                                      "(_:b _:z _:b)",
                                      true)  ; }

    @Test public void iso_16() { testGraph("(_:x _:a _:a)",
                                      "(_:z _:b _:b)",
                                      true)  ; }

    @Test public void iso_20() { testGraph("(<x> <p> _:a) (<z> <p> _:a)",
                                      "(<x> <p> _:b) (<z> <p> _:b)",
                                      true)  ; }

    @Test public void iso_21() { testGraph("(<x> <p> _:a1) (<z> <p> _:a2)",
                                      "(<x> <p> _:b) (<z> <p> _:b)",
                                      false)  ; }

    @Test public void iso_22() { testGraph("(_:a <p> _:a) (<s> <q> _:a)",
                                      "(_:b <p> _:b) (<s> <q> _:b)",
                                      true)  ; }

    @Test public void iso_23() { testGraph("(_:a <p> _:a) (<s> <q> _:a)",
                                      "(_:b <p> _:b) (<s> <q> _:c)",
                                      false)  ; }

    @Test public void iso_24() { testGraph("(_:a <p> _:a) (<s> <q> _:a) (_:b <q> _:b)",
                                      "(_:b <p> _:b) (<s> <q> _:b) (_:b <q> _:b)",
                                      false)  ; }

    @Test public void iso_50() { testDSG("(graph (_:a <p> _:a)) (graph <g> (<s> <q> _:a))" ,
                                         "(graph (_:a <p> _:a)) (graph <g> (<s> <q> _:a))" ,
                                         true)  ; }
    
    // Graphs separately isomorphic.
    @Test public void iso_51() { testDSG("(graph (_:a <p> _:a)) (graph <g> (<s> <q> _:a))" ,
                                         "(graph (_:a <p> _:a)) (graph <g> (<s> <q> _:b))" ,
                                         false)  ; }

    
    private void testGraph(String s1, String s2, boolean iso) {
        testGraph$(s1, s2, iso) ;
        testGraph$(s2, s1, iso) ;
    }
    
    private void testGraph$(String s1, String s2, boolean iso) {
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
    
    private void testDSG(String s1, String s2, boolean iso) {
        testDSG$(s1, s2, iso) ;
        testDSG$(s2, s1, iso) ;
    }

    private void testQuads(String s1, String s2, boolean iso) {
        //XXX
//        testDSG$(s1, s2, iso) ;
//        testDSG$(s2, s1, iso) ;
    }

    private void testDSG$(String s1, String s2, boolean iso) {
        s1 = "(dataset "+s1+")" ;
        s2 = "(dataset "+s2+")" ;
        
        DatasetGraph dsg1 = SSE.parseDatasetGraph(s1) ;
        DatasetGraph dsg2 = SSE.parseDatasetGraph(s2) ;
        boolean b = IsoMatcher.isomorphic(dsg1, dsg2) ;
        if ( b != iso ) {
            System.out.println("====") ;
            SSE.write(dsg1) ;
            System.out.println("----") ;
            SSE.write(dsg2) ;
            System.out.println("Expected: "+iso+" ; got: "+b) ;
        }
        assertEquals(iso, b) ;
    }
    

}
