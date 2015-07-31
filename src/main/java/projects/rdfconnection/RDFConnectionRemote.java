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
import static java.util.Objects.requireNonNull;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.atlas.web.auth.HttpAuthenticator ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.web.HttpCaptureResponse ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.riot.web.HttpResponseLib ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateProcessor ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.web.HttpSC ;

public class RDFConnectionRemote implements RDFConnection {
    private static final String fusekiDftSrvQuery = "query" ;
    private static final String fusekiDftSrvUpdate = "update" ;
    private static final String fusekiDftSrvGSP = "data" ;
    
    private final String destination ;
    private final String svcQuery ;
    private final String svcUpdate ;
    private final String svcGraphStore ;
    private HttpAuthenticator authenticator = null ;
    
    public RDFConnectionRemote(String destination) {
        this(requireNonNull(destination),
             formServiceURL(destination, fusekiDftSrvQuery), 
             formServiceURL(destination, fusekiDftSrvUpdate),
             formServiceURL(destination, fusekiDftSrvGSP)) ;
    }

    public RDFConnectionRemote(String sQuery, String sUpdate, String sGSP) {
        this(null, 
             requireNonNull(sQuery),
             requireNonNull(sUpdate),
             requireNonNull(sGSP)) ;
    }
    
    public RDFConnectionRemote(String destination, String sQuery, String sUpdate, String sGSP) {
        if ( destination.endsWith("/") )
            destination = destination.substring(0, destination.length()-1) ;
        this.destination = destination ;
        this.svcQuery = formServiceURL(destination,sQuery) ;
        this.svcUpdate = formServiceURL(destination,sUpdate) ;
        this.svcGraphStore = formServiceURL(destination,sGSP) ;
    }
    
    private static String formServiceURL(String destination, String srvEndpoint) {
        if ( destination.endsWith("/") )
            destination.substring(0, destination.length()-1) ;
        return destination+"/"+srvEndpoint ;
    }

    @Override
    public QueryExecution query(Query query) {
        checkQuery();
        return QueryExecutionFactory.createServiceRequest(svcQuery, query) ;
    }

    @Override
    public void update(UpdateRequest update) {
        checkUpdate();
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(update, svcUpdate) ;
        proc.execute();
    }
    
    @Override
    public Model fetchModel(String graphName) {
        checkGSP() ;
        String url = RDFConn.urlForGraph(svcGraphStore, graphName) ;
        Graph graph = fetch$(url) ;
        return ModelFactory.createModelForGraph(graph) ;
    }
    
    @Override
    public Model fetchModel() {
        checkGSP() ;
        return fetchModel(null) ;
    }
    
    private Graph fetch$(String url) {
        HttpCaptureResponse<Graph> graph = HttpResponseLib.graphHandler() ;
        try {
            HttpOp.execHttpGet(url, WebContent.defaultGraphAcceptHeader, graph, this.authenticator) ;
        } catch (HttpException ex) {
            if ( ex.getResponseCode() == HttpSC.NOT_FOUND_404 )
                return null ;
            throw ex ;
        }
        return graph.get() ;
    }


    @Override
    
    
    public void load(String graph, String file) {
        checkGSP() ;
        upload(graph, file, false) ;
    }
    
    private void upload(String graph, String file, boolean replace) {
        // if triples
        Lang lang = RDFLanguages.filenameToLang(file) ;
        if ( RDFLanguages.isQuads(lang) )
            throw new ARQException("Can't load quads into a graph") ;
        if ( ! RDFLanguages.isTriples(lang) )
            throw new ARQException("Not an RDF format: "+file+" (lang="+lang+")") ;
        String url = RDFConn.urlForGraph(svcGraphStore, graph) ;
        doPutPost(url, file, lang, replace) ;
    }
    
    @Override
    public void load(String file) {
        checkGSP() ;
        upload(file, false) ;
    }
    
    private void upload(String file, boolean replace) {
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
        doPutPost(url, file, lang, replace) ;
    }
    
    private void doPutPost(String url, String file, Lang lang, boolean replace) {
        File f = new File(file) ;
        long length = f.length() ; 
        InputStream source = IO.openFile(file) ;
        // Charset.
        if ( replace )
            HttpOp.execHttpPut(url, lang.getContentType().getContentType(), source, length, null, null, this.authenticator) ;
        else    
            HttpOp.execHttpPost(url, lang.getContentType().getContentType(), source, length, null, null, null, null, this.authenticator) ; 
    }

    @Override
    public void setReplace(String graph, String file) {
        checkGSP() ;
        upload(graph, file, true) ;
    }
    
    @Override
    public void setReplace(String file) { 
        checkGSP() ;
        upload(file, true) ; 
    }
    
    @Override
    public void delete(String graph) {
        checkGSP() ;
        String url = RDFConn.urlForGraph(svcGraphStore, graph) ;
        HttpOp.execHttpDelete(url);
    }

    @Override
    public void delete() {
        checkGSP() ;
        delete(null) ;
    }

    @Override
    public Dataset fetchDataset() {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URl provided") ; 
        Dataset ds = DatasetFactory.createMem() ;
        TypedInputStream s = HttpOp.execHttpGet(destination, WebContent.defaultDatasetAcceptHeader) ;
        Lang lang = RDFLanguages.contentTypeToLang(s.getContentType()) ;
        RDFDataMgr.read(ds, s, null) ;
        return ds ;
    }

    @Override
    public void loadDataset(String file) 
    { 
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URl provided") ; 
        doPutPostDataset(file, false) ; 
    }
    
    private void doPutPostDataset(String file, boolean replace) {
        Lang lang = RDFLanguages.filenameToLang(file) ;
        File f = new File(file) ;
        long length = f.length() ; 
        InputStream source = IO.openFile(file) ;
        if ( replace )
            HttpOp.execHttpPut(destination, lang.getContentType().getContentType(), source, length, null, null, this.authenticator) ;
        else    
            HttpOp.execHttpPost(destination, lang.getContentType().getContentType(), source, length, null, null, null, null, this.authenticator) ; 
    }

    @Override
    public void setReplaceDataset(String file) {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URl provided") ; 
        doPutPostDataset(file, true) ;
    }
    
    private void checkQuery() {
        if ( svcQuery == null )
            throw new ARQException("No query service defined for this RDFConnection") ;
    }
    
    private void checkUpdate() {
        if ( svcUpdate == null )
            throw new ARQException("No update service defined for this RDFConnection") ;
    }
    
    private void checkGSP() {
        if ( svcGraphStore == null )
            throw new ARQException("No SPARQL Graph Store service defined for this RDFConnection") ;
    }
    private void checkDataset() {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URL provided") ; 
    }


}

