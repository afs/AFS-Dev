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

package projects.rdfconnection;

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.update.Update ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;

/** Interface for SPARQL operations on a datsets, whether local or remote.
 * 
 * <ul>
 * <li>query
 * <li>update
 * <li>graph store protocol   
 * </ul>
 * 
 * <p>
 * SPARQL Protocol 

 * <p>
 * SPARQL Graph Store Protocol 
 *  
 * 
 */  
public interface RDFConnection extends AutoCloseable {
    // Autoclosable.
    // This sort of "competes" with transactions in terms of: 
    /*
     * Txn.write(rdfConnection, ()->{
     *    // Writes
     *    }) ;
     *    
     * and leading to multiple nesting in some usages.
     */
    
    // ---- Query
    // Maybe more query forms: querySelect(Query)? select(Query)?
    // Closing?
//     public ResultSet querySelect(Query query) {
//      return QueryExecutionFactory.createServiceRequest(svcQuery, query).execSelect() ;
//  }
    // Model queryConstruct
    //  
    
    // Also AutoClosable.
    public QueryExecution query(Query query) ;
    
    // Override to allow non-standard syntax.
    public default QueryExecution query(String queryString) {
        return query(QueryFactory.create(queryString)) ;
    }

    // ---- Update
    public default void update(Update update) {
        update(new UpdateRequest(update)) ;
    }

    public void update(UpdateRequest update) ; 
    
    public default void update(String updateString) {
        update(UpdateFactory.create(updateString)) ;
    }
    
    // GraphStore Protocol
    
    
    /** Fetch a named graph.
     * This is SPARQL Graph Store Protocol HTTP GET or equivalent. 
     * 
     * @param graphName URI string for the graph name (null or "default" for the default graph)
     * @return Model
     */
    public Model fetch(String graphName) ;
    
    /** Fetch the default graph.
     * This is SPARQL Graph Store Protocol HTTP GET or equivalent. 
     * @return Model
     */
    public Model fetch() ;
    
    /** Load (add, append) RDF into a named graph in a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent. 
     * 
     * @param graphName Graph name (null or "default" for the default graph)
     * @param file File of the data.
     */
    public void load(String graphName, String file) ;
    
    /** Load (add, append) RDF into the default graph of a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent. 
     * 
     * @param file File of the data.
     */
    public void load(String file) ;

    /** Load (add, append) RDF into a named graph in a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent. 
     * 
     * @param graphName Graph name (null or "default" for the default graph)
     * @param model Data.
     */
    public void load(String graphName, Model model) ;
    
    /** Load (add, append) RDF into the default graph of a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent. 
     * 
     * @param model Data.
     */
    public void load(Model model) ;

    /** Set the contents of a named graph of a dataset.
     * Any existing data is lost. 
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent. 
     *
     * @param graphName Graph name (null or "default" for the default graph)
     * @param file File of the data.
     */
    public void put(String graphName, String file) ;
    
    /** Set the contents of the default graph of a dataset.
     * Any existing data is lost. 
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent. 
     * 
     * @param file File of the data.
     */
    public void put(String file) ;
        
    /** Set the contents of a named graph of a dataset.
     * Any existing data is lost. 
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent. 
     *
     * @param graphName Graph name (null or "default" for the default graph)
     * @param model Data.
     */
    public void put(String graphName, Model model) ;
    
    /** Set the contents of the default graph of a dataset.
     * Any existing data is lost. 
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent. 
     * 
     * @param model Data.
     */
    public void put( Model model) ;
        
    /**
     * Delete a graph from the dataset.
     * Null or "default" measn the default graph, which is cleared, not removed.
     * 
     * @param graphName
     */
    public void delete(String graphName) ;

    /**
     * Remove all data from the default graph.
     */ 
    public void delete() ;

    // Whole datsets operations
    // Not SPARQL Graph Store Protocol

    /* Load (add, append) RDF triple or quad data into a dataset. Triples wil go into the default graph.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP POST equivalent to the dataset.
     */
    public void loadDataset(String file) ;

    /* Load (add, append) RDF triple or quad data into a dataset. Triples wil go into the default graph.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP POST equivalent to the dataset.
     */
    public void loadDataset(Dataset dataset) ;

    /* Set RDF triple or quad data as the dataset contents.
     * Triples will go into the default graph, quads in named graphs.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP PUT equivalent to the dataset.
     */
    public void putDataset(String file) ;
    
    /* Set RDF triple or quad data as the dataset contents.
     * Triples will go into the default graph, quads in named graphs.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP PUT equivalent to the dataset.
     */
    public void putDataset(Dataset dataset) ;

    //    /** Clear the dataset - remove all named graphs, clear the default graph. */
//    public void clearDataset() ;
    
    /** Fetch the contents of the dataset */ 
    public Dataset fetchDataset() ;
    
    public boolean isClosed() ;
    
    @Override public void close() ;

}

