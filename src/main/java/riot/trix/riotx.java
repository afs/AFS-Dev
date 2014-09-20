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

import org.apache.jena.riot.RIOT ;

public class riotx extends riotcmd.riot {

    static { RIOT.init() ; TriX.init(); }
    
    protected riotx(String[] argv) {
        super(argv) ;
    }

    public static void main(String... args) {
        new riotx(args).mainRun() ;
    }

}
