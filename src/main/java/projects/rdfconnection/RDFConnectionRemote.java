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

import java.io.File ;
import java.io.InputStream ;
import java.util.Objects ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.query.QueryExecutionFactory ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.update.Update ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateProcessor ;
import org.apache.jena.update.UpdateRequest ;

public class RDFConnectionRemote implements RDFConnection {
    private static final String fusekiDftSrvQuery = "query" ;
    private static final String fusekiDftSrvUpdate = "update" ;
    private static final String fusekiDftSrvGSP = "data" ;
    
    private final String destination ;
    private final String svcQuery ;
    private final String svcUpdate ;
    private final String svcGraphStore ;
    
    
    public RDFConnectionRemote(String destination) {
        this(destination, fusekiDftSrvQuery, fusekiDftSrvUpdate, fusekiDftSrvGSP) ;
    }

    public RDFConnectionRemote(String destination, 
                         String sQuery, String sUpdate, String sGSP) {
        Objects.requireNonNull(destination) ;
        Objects.requireNonNull(sQuery) ;
        Objects.requireNonNull(sUpdate) ;
        Objects.requireNonNull(sGSP) ;
        
        if ( destination.endsWith("/") )
            destination = destination.substring(0, destination.length()-1) ;
        this.destination = destination ;
        this.svcQuery = formServiceURL(destination,sQuery) ;
        this.svcUpdate = formServiceURL(destination,sUpdate) ;
        this.svcGraphStore = formServiceURL(destination,sGSP) ;
    }
    
    private static String formServiceURL(String destination, String service) {
        return destination+"/"+service ; 
    }

//    public RDFConnection(String destination, Map<String, String> services) {
//        
//    }

    // See Query
    @Override
    public QueryExecution query(Query query) {
        return QueryExecutionFactory.createServiceRequest(svcQuery, query) ;
    }

//    // try-resources
//    public ResultSet querySelect(Query query) {
//        return QueryExecutionFactory.createServiceRequest(svcQuery, query) ;
//    }

    @Override
    public void update(Update update) {
        update(new UpdateRequest(update)) ;
    }

    @Override
    public void update(UpdateRequest update) {
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(update, svcUpdate) ;
        proc.execute();
    }
    
    @Override
    public void load(String graph, String file) {
        // if triples
        Lang lang = RDFLanguages.filenameToLang(file) ;
        if ( RDFLanguages.isQuads(lang) )
            throw new ARQException("Can't load quads into a graph") ;
        if ( ! RDFLanguages.isTriples(lang) )
            throw new ARQException("Not an RDF format: "+file+" (lang="+lang+")") ;
        String queryString = 
             (graph == null || graph.equals("default") )
             ? "?default"
             : "?graph="+graph ;
        String url = svcGraphStore+queryString ;
        load$(url, file, lang) ;
    }
    
    @Override
    public void load(String file) {
        Lang lang = RDFLanguages.filenameToLang(file) ;
        String url = null ;
        if ( RDFLanguages.isTriples(lang) ) {
            url = svcGraphStore+"?default" ;
        }
        else if ( RDFLanguages.isQuads(lang) ) {
            // Try to POST to the dataset.
            // Non-standard
            url = svcGraphStore ;
        }
        if ( url == null )
            throw new ARQException("Not an RDF format: "+file+" (lang="+lang+")") ;
        load$(url, file, lang) ;
    }
    
    private static void load$(String url, String file, Lang lang) {
        File f = new File(file) ;
        long length = f.length() ; 
        InputStream source = IO.openFile(file) ;
        // Charset.
        HttpOp.execHttpPost(url, lang.getContentType().getContentType(), source, length) ; 
    }
}

