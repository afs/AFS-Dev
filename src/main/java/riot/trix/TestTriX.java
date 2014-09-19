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
import java.util.Arrays ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.junit.Assert ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameter ;
import org.junit.runners.Parameterized.Parameters ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.lib.DatasetLib ;

@RunWith(Parameterized.class)

public class TestTriX {
    
    @Parameters(name="{0}")
    public static Iterable<Object[]> data() {
              return Arrays.asList(new Object[][] { 
                  { "trix-ex-0.xml", null }, 
                  { "trix-ex-1.xml", null },
//                  //{ "trix-ex-2.xml", null },  // Contains <integer> 
                  { "trix-ex-3.xml", null },
                  { "trix-ex-4.xml", null },
                  { "trix-ex-5.xml", null }
                  });
    }
    
    @Parameter(0)
    public String fInput;
        
    @Parameter(1)
    public String fExpected;
    
    @Test
    public void trix() {
        ReaderRIOT r = new ReaderTriX() ;
        InputStream in = IO.openFile(fInput) ;
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        //StreamRDF stream = StreamRDFLib.writer(System.out) ;
        StreamRDF stream = StreamRDFLib.dataset(dsg) ;
        stream.start();
        r.read(in, null, null, stream, null) ;
        stream.finish();
        if ( fExpected != null ) {
            DatasetGraph dsg2 = RDFDataMgr.loadDatasetGraph(fExpected) ;
            boolean b = DatasetLib.isomorphic(dsg, dsg2) ;
            if ( ! b )
                Assert.fail("Not isomorphic") ;
        }
    }
    
}

