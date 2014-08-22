/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package inf.test;

import java.util.Collection ;

import org.apache.jena.atlas.lib.Lib ;

public class InfTestLib {

    /** Test whether 2 collection have the same elements regardless of order */
    public static <X> boolean sameElts(Collection<X> items1, Collection<X> items2) {
        // Simple and inefficient.
        if ( items1.size() != items2.size() ) 
            return false ;
        // Need to account for duplicates
        // Make sure counts agree.
        for ( X t : items1 ) {
            int x1 = countElts(items1, t) ;
            int x2 = countElts(items2, t) ;            
            if ( x1 != x2 )
                return false ;
        }
        return true ;
    }

    private static <X> int countElts(Collection<X> items, X item) {
        int i = 0 ;
        for ( X x : items ) {
            if ( Lib.equal(x, item) )
                i++ ;
        }
        return i ;
    }
}


