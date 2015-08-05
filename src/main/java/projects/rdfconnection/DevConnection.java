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

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;

public class DevConnection {
    /*
     * jena-client - local/remote same
     * RDFConnection - "it's all remote" and works locally. 
     * why QueryStatement, not QueryExecution?
     */
    
    public static void main(String ... argv) {
        
        //String dest = "http://localhost:3030/ds/" ;
        // RDFConnection rConn = RDFConnectionFactory.create(dest) ;
        Dataset ds = DatasetFactory.createMem() ;
        RDFConnection rConn1 = RDFConnectionFactory.connect(ds) ;
        
        try ( RDFConnection rConn = RDFConnectionFactory.connect(ds) ) {
    //        rConn.load("/home/afs/tmp/D.trig") ;
            rConn.load("default", "/home/afs/tmp/D1.ttl") ;
            rConn.load("/home/afs/tmp/D2.ttl") ;
            
            rConn.put("/home/afs/tmp/D.trig") ;
            Model m = rConn.fetch() ;
            RDFDataMgr.write(System.out, m, Lang.TTL);  
            
            // Illegal
            // rConn.load("http://example/graph", "/home/afs/tmp/D.trig") ;
        }
        System.out.println("DONE") ;
    }
}

