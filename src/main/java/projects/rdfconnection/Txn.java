/**
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

import java.util.function.Supplier ;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.Transactional ;

/** Application utilities for transactions. */
public class Txn {
    
    /** Execute the Runnable in a read transaction.
     *  Nested transactions are not supported.
     */
    public static <T extends Transactional> void executeRead(T txn, Runnable r) {
        txn.begin(ReadWrite.READ) ;
        r.run(); 
        txn.end() ;
    }

    /** Execute and return a value in a read transaction
     * Nested transactions are not supported.
     */

    public static <T extends Transactional, X> X executeReadReturn(T txn, Supplier<X> r) {
        txn.begin(ReadWrite.READ) ;
        X x = r.get() ;
        txn.end() ;
        return x ;
    }

    /** Execute the Runnable in a write transaction 
     *  Nested transaction are not supported.
     */
    public static <T extends Transactional> void executeWrite(T txn, Runnable r) {
        txn.begin(ReadWrite.WRITE) ;
        try { r.run(); }
        catch (Throwable th) {
            txn.abort();
            txn.end();
            throw th ; 
        }
        txn.commit() ;
        txn.end() ;
    }
    
    /** Execute the Runnable in a write transaction 
     *  Nested transaction are not supported.
     */
    public static <T extends Transactional, X> X executeWriteReturn(Transactional txn, Supplier<X> r) {
        txn.begin(ReadWrite.WRITE) ;
        X x = r.get() ;
        txn.commit() ;
        txn.end() ;
        return x ;
    }
}

