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

import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.ResultSetFormatter ;
import org.apache.jena.tdb.TDBFactory ;

public class RDFConnectionExample1 {
    public static void main(String ...args) {
        Query query = QueryFactory.create("SELECT * { {?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }") ;

        try ( RDFConnection conn = RDFConnectionFactory.connect(TDBFactory.createDataset()) ) {
            
            System.out.println("Load a file");
            Txn.executeWrite(conn, ()->conn.load("data.ttl")) ;
            
            conn.begin(ReadWrite.WRITE);
            conn.load("http://example/g0", "data.ttl") ;
            
            System.out.println("Inside multistep transaction - fetch dataset");
            conn.queryResultSet(query, ResultSetFormatter::out) ;
            
            conn.abort();
            conn.end() ;
            
            System.out.println("After abort") ;
            // Only default graph showing.
            conn.queryResultSet(query, ResultSetFormatter::out);
        }
    }
}

