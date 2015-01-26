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

package riot.trix;

import java.io.InputStream ;
import java.util.Arrays ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.junit.Assert ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameter ;
import org.junit.runners.Parameterized.Parameters ;

@RunWith(Parameterized.class)

public class TestBadTriX extends Assert /*BaseTest*/ {
    
    static { TriX.init() ; }
    
    static String DIR = "testing/RIOT/Lang/TriX" ;
    
    @Parameters(name="{0}")
    public static Iterable<Object[]> data() {
              return Arrays.asList(new Object[][] { 
                  { DIR+"/trix-bad-01.trix" } ,
                  { DIR+"/trix-bad-02.trix" } ,
                  { DIR+"/trix-bad-03.trix" } ,
                  { DIR+"/trix-bad-04.trix" } ,
                  { DIR+"/trix-bad-05.trix" } ,
                  { DIR+"/trix-bad-06.trix" } ,
                  { DIR+"/trix-bad-07.trix" } ,
                  { DIR+"/trix-bad-08.trix" } ,
                  { DIR+"/trix-bad-09.trix" } ,
                  });
    }
    
    @Parameter(0)
    public String fInput;
    
    @Test(expected=RiotException.class)
    public void trix_bad() {
        ErrorHandler err = ErrorHandlerFactory.getDefaultErrorHandler() ;
        try {
            ErrorHandlerFactory.setDefaultErrorHandler(ErrorHandlerFactory.errorHandlerSimple()) ;
            InputStream in = IO.openFile(fInput) ;
            StreamRDF sink = StreamRDFLib.sinkNull() ;
            RDFDataMgr.parse(sink, in, TriX.TRIX) ;
        } finally {
            ErrorHandlerFactory.setDefaultErrorHandler(err) ;
        }
    }
}

