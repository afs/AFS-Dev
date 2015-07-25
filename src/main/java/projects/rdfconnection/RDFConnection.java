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

package projects.rdfconnection;

import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.update.Update ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;

public interface RDFConnection {

    // ---- Query
    //   execSelect()
    // Consider special for SELECT queries.  
    
    public QueryExecution query(Query query) ; 
    
    // Override to allow non-standard syntax.
    public default QueryExecution query(String queryString) {
        return query(QueryFactory.create(queryString)) ;
    }

    // ---- Update
    public void update(Update update) ;

    public void update(UpdateRequest update) ; 
    
    public default void update(String updateString) {
        update(UpdateFactory.create(updateString)) ;
    }
    
    // GraphStore Protocol
    
    // GET
    
    // POST
    public void load(String graph, String file) ;
    
    public void load(String file) ;
    
    // PUT
    
    // DELETE
    
    
    //public void load(String graph, String file) ;
}

