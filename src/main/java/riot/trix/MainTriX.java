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
import org.apache.jena.riot.system.StreamRDFWriter ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class MainTriX {
    
    // TODO
    // <integer>??
    // RDFXMLLiterals (ex-3)
    // Namespaces in reader
    // Error cases.
    // Correct results.
    // Writer escape XML
    // tests

    public static void main(String[] args) {
        TriX.init() ;
        // Does not override .xml.
        // Strong and weak/default hints.
        // DatasetGraph dsg = RDFDataMgr.loadDatasetGraph("trix-ex-2.xml", TriX.TRIX) ;
        InputStream in = IO.openFile("trix-ex-2.xml") ;
        DatasetGraph dsg = DatasetGraphFactory.createMem() ; 
        RDFDataMgr.read(dsg, in, TriX.TRIX) ;
        SSE.write(dsg) ;
        System.out.println() ;
        //RDFDataMgr.write(System.out, dsg, TriX.TRIX) ;
        
        
        StreamRDFWriter.write(System.out, dsg, TriX.TRIX) ;
        
        
//        ReaderRIOT r = new ReaderTriX() ;
//        InputStream in = IO.openFile("trix-ex-2.xml") ;
//        //StreamRDF stream = StreamRDFLib.writer(System.out) ;
//        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
//        StreamRDF stream = StreamRDFLib.dataset(dsg) ;
//        stream.start();
//        r.read(in, null, null, stream, null) ;
//        stream.finish();
//        SSE.write(dsg) ;
//        
////        WriterGraphRIOT w = new WriterTriX() ;
////        w.write(System.out, dsg.getDefaultGraph(), null, null, null) ;
//        
//      WriterDatasetRIOT w = new WriterTriX() ;
//      w.write(System.out, dsg, null, null, null) ;
//      System.out.println("DONE") ;
    }

}

