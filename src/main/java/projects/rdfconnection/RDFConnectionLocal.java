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

import java.util.Objects ;

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateRequest ;

public class RDFConnectionLocal implements RDFConnection {

    private final Dataset dataset ;

    public RDFConnectionLocal(Dataset dataset) {
        // Add read-only wrapper?
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
        doPutPost(graph, file, false) ;
    }

    @Override
    public void load(String file) {
        doPutPost(null, file, false) ;
    }

    /**
     * There may be differences between local and remote behaviour. A local
     * connection may return direct references to a dataset so updates on
     * returned
     */

    @Override
    public Model fetchModel(String graph) {
        if ( RDFConn.isDefault(graph)) 
            return dataset.getDefaultModel() ;
        return dataset.getNamedModel(graph) ;
    }

    @Override
    public Model fetchModel() {
        return fetchModel(null) ;
    }

    @Override
    public void setReplace(String file) {
        doPutPost(null, file, true) ;
    }

    @Override
    public void setReplace(String graph, String file) {
        doPutPost(graph, file, true) ;
    }

    private void doPutPost(String graph, String file, boolean replace) {
        Objects.requireNonNull(file) ;
        Lang lang = RDFLanguages.filenameToLang(file) ;
        String url = null ;
        if ( RDFLanguages.isTriples(lang) ) {
            Model model = RDFConn.isDefault(graph) ? dataset.getDefaultModel() : dataset.getNamedModel(graph) ;
            if ( replace )
                model.removeAll() ;
            RDFDataMgr.read(model, file); 
        }
        else if ( RDFLanguages.isQuads(lang) ) {
            if ( replace )
                dataset.asDatasetGraph().clear(); 
            // Try to POST to the dataset.
            RDFDataMgr.read(dataset, file); 
        }
        else
            throw new ARQException("Not an RDF format: "+file+" (lang="+lang+")") ;
    }

    @Override
    public Dataset fetchDataset() {
        return dataset ;
    }

    @Override
    public void loadDataset(String file) {
        RDFDataMgr.read(dataset, file);
    }

    @Override
    public void setReplaceDataset(String file) {
        dataset.asDatasetGraph().clear();
        RDFDataMgr.read(dataset, file);
    }

    @Override
    public void delete(String graph) {
        if ( RDFConn.isDefault(graph) ) 
            dataset.getDefaultModel().removeAll();
        else { 
            dataset.removeNamedModel(graph);
        }
    }

    @Override
    public void delete() { delete(null) ; }
}

