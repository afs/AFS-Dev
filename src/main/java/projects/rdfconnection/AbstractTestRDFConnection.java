/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
 
package projects.rdfconnection;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.util.IsoMatcher ;
import org.junit.Test ;

public abstract class AbstractTestRDFConnection extends BaseTest {
    // Testing data.
    static String DIR = "testing/RDFConnection/" ;
    
    protected abstract RDFConnection connection() ;

//    RDFConnection connection ;
//    
//    @Before public void before() {
//        connection = connection() ;
//    }
//    
//    @After public void after() {
//        connection.close() ;
//    }
    
    static String dsgdata = StrUtils.strjoinNL
        ("(dataset"
        ,"  (graph"
        ,"     (:s :p :o)"
        ,"     (:s0 :p0 :o)"
        ,"    )"
        ,"  (graph :g1"
        ,"     (:s :p :o)"
        ,"     (:s1 :p1 :o1)"
        ,"    )" 
        ,"  (graph :g2"
        ,"     (:s :p :o)"
        ,"     (:s2 :p2 :o)"
        ,"    )" 
        ,")"
        ) ;
    
    static DatasetGraph dsg = SSE.parseDatasetGraph(dsgdata) ;
    
    @Test public void connect_01() {
        @SuppressWarnings("resource")
        RDFConnection conn = connection() ;
        assertFalse(conn.isClosed()) ;
        conn.close() ;
        assertTrue(conn.isClosed()) ;
        // Alow multiple close()
        conn.close() ;
    }
    
    @Test public void connect_02() {
        try ( RDFConnection conn = connection() ) {
            conn.begin(ReadWrite.READ);
            try ( QueryExecution qExec = conn.query("ASK{}") ) {
                boolean b = qExec.execAsk() ;
                assertTrue(b) ;
            }
            conn.end() ;
        }
    }
    
    @Test public void dataset_01() {
        try ( RDFConnection conn = connection() ) {
            conn.load(DIR+"data.trig");
        }
    }
    
    @Test public void dataset_02() {
        String testDataFile = DIR+"data.trig" ; 
        try ( RDFConnection conn = connection() ) {
            conn.load(testDataFile);
            Dataset ds0 = RDFDataMgr.loadDataset(testDataFile) ;
            Dataset ds = conn.fetchDataset() ;
            assertTrue(isomorphic(ds0, ds)) ;
        }
    }
    
    @Test public void transaction_01() {
        String testDataFile = DIR+"data.trig" ; 
        try ( RDFConnection conn = connection() ) {
            conn.begin(ReadWrite.WRITE) ;
            conn.load(testDataFile);
            conn.abort();
            conn.end();
            conn.begin(ReadWrite.READ) ;
            Model m = conn.fetch() ;
            assertTrue(m.isEmpty()) ;
            conn.end() ;
        }
    }


    private static boolean isomorphic(Dataset ds1, Dataset ds2) {
        return IsoMatcher.isomorphic(ds1.asDatasetGraph(), ds2.asDatasetGraph()) ;
    }
    
}

