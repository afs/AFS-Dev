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

import java.util.Arrays ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.NotImplemented ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameter ;
import org.junit.runners.Parameterized.Parameters ;

@RunWith(Parameterized.class)
public class TestTriXWriter extends BaseTest {

    static { TriX.init() ; }

    static String DIR = "testing/RIOT/Lang/TriX" ;

    @Parameters(name="{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { 
            { DIR+"/trix-01.trix", DIR+"/trix-01.nq" } ,
            { DIR+"/trix-02.trix", DIR+"/trix-02.nq" } ,
            { DIR+"/trix-03.trix", DIR+"/trix-03.nq" } ,
            { DIR+"/trix-04.trix", DIR+"/trix-04.nq" } ,
            { DIR+"/trix-05.trix", DIR+"/trix-05.nq" } ,
            { DIR+"/trix-06.trix", DIR+"/trix-06.nq" } ,
            { DIR+"/trix-10.trix", DIR+"/trix-10.nq" } ,        
            { DIR+"/trix-11.trix", DIR+"/trix-11.nq" } ,        
            { DIR+"/trix-12.trix", DIR+"/trix-12.nq" } ,        
            { DIR+"/trix-13.trix", DIR+"/trix-13.nq" } ,        
            { DIR+"/trix-14.trix", DIR+"/trix-14.nq" } , 
            // The example from HPL-2004-56
            { DIR+"/trix-ex-1.trix", null },
            //                      //{ "trix-ex-2.trix", null },  // Contains <integer> 
            { DIR+"/trix-ex-3.trix", null },
            { DIR+"/trix-ex-4.trix", null },
            { DIR+"/trix-ex-5.trix", null }
        });
    }
    @Parameter(0)
    public String fInput;

    @Parameter(1)
    public String fExpected;
    
    @Test
    public void trix_writer() {
        // read in NQ, write TriX, read TriX, check.
        // Also add to "reader" loop
        throw new NotImplemented() ;
    }
}

