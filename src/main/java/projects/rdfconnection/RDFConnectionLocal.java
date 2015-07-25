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

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateRequest ;

public class RDFConnectionLocal implements RDFConnection {

    private final Dataset dataset ;

    public RDFConnectionLocal(Dataset dataset) {
        this.dataset = dataset ;
    }
    
    @Override
    public QueryExecution query(Query query) {
        return QueryExecutionFactory.create(query, dataset) ;
    }

    @Override
    public void update(UpdateRequest update) {
        UpdateExecutionFactory.create(update, dataset).execute() ; 
    }

    @Override
    public void load(String graph, String file) {
        Model m =   
            ( graph == null ) || ( graph.equals("default") ) 
            ? dataset.getDefaultModel()
            : dataset.getNamedModel(graph) ;
        RDFDataMgr.read(dataset.getNamedModel(file), file); 
    }

    @Override
    public void load(String file) {
        RDFDataMgr.read(dataset, file) ;
    }

}

