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
import org.apache.jena.riot.ReaderRIOT ;

public class MainTriX {

    public static void main(String[] args) {
        ReaderRIOT r = new ReaderTriX() ;
        InputStream in = IO.openFile("trix-ex-0.xml") ;
        r.read(in, null, null, null, null) ;
        System.out.println("DONE") ;
    }

}

