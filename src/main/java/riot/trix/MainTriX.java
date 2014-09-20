/*
 *  Copyright 2013, 2014 Andy Seaborne
 *
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
 */

package riot.trix;

import java.io.InputStream ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.RDFDataMgr ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class MainTriX {
    
    // TODO
    // Namespaces in reader
    // Writer escape XML (x2)
    //    tests
    // writer tests
    // RDFDataMgr.parser - soft hints.
    // Bug - trix-14 XML attributes 

    public static void main(String[] args) {
        
        riotx.main("trix-ex-0.trix") ;
        System.exit(0) ;
        TriX.init() ;
        // Does not override .xml.
        // Strong and weak/default hints.
        // DatasetGraph dsg = RDFDataMgr.loadDatasetGraph("trix-ex-2.xml", TriX.TRIX) ;
        InputStream in = IO.openFile("trix-ex-0.trix") ;
        DatasetGraph dsg = DatasetGraphFactory.createMem() ; 
        RDFDataMgr.read(dsg, in, TriX.TRIX) ;
        SSE.write(dsg) ;
        System.out.println() ;
        RDFDataMgr.write(System.out, dsg, TriX.TRIX) ;
        //StreamRDFWriter.write(System.out, dsg, TriX.TRIX) ;
    }

}

