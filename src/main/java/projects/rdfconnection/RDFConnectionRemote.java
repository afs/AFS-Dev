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

import static java.util.Objects.requireNonNull ;

import java.io.File ;
import java.io.InputStream ;
import java.util.function.Supplier ;

import org.apache.http.HttpEntity ;
import org.apache.http.entity.EntityTemplate ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.atlas.web.auth.HttpAuthenticator ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.web.HttpCaptureResponse ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.riot.web.HttpResponseLib ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.ARQNotImplemented ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateProcessor ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.web.HttpSC ;

public class RDFConnectionRemote implements RDFConnection {
    private static final String fusekiDftSrvQuery = "query" ;
    private static final String fusekiDftSrvUpdate = "update" ;
    private static final String fusekiDftSrvGSP = "data" ;
    
    private boolean isOpen = true ; 
    private final String destination ;
    private final String svcQuery ;
    private final String svcUpdate ;
    private final String svcGraphStore ;
    private HttpAuthenticator authenticator = null ;
    
    // Merge/share code with DatasetGraphAccessorHTTP
    
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
        return exec(()->QueryExecutionFactory.createServiceRequest(svcQuery, query)) ;
    }

    @Override
    public void update(UpdateRequest update) {
        checkUpdate();
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(update, svcUpdate) ;
        exec(()->proc.execute());
    }
    
    @Override
    public Model fetch(String graphName) {
        checkGSP() ;
        String url = RDFConn.urlForGraph(svcGraphStore, graphName) ;
        Graph graph = fetch$(url) ;
        return ModelFactory.createModelForGraph(graph) ;
    }
    
    @Override
    public Model fetch() {
        checkGSP() ;
        return fetch(null) ;
    }
    
    private Graph fetch$(String url) {
        HttpCaptureResponse<Graph> graph = HttpResponseLib.graphHandler() ;
        exec(()->HttpOp.execHttpGet(url, WebContent.defaultGraphAcceptHeader, graph, this.authenticator)) ;
        return graph.get() ;
    }

    @Override
    public void load(String graph, String file) {
        checkGSP() ;
        upload(graph, file, false) ;
    }
    
    @Override
    public void load(String file) {
        checkGSP() ;
        upload(null, file, false) ;
    }
    
    @Override
    public void load(Model model) {
        load(null, model) ;
    }
    
    @Override
    public void load(String graphName, Model model) {
        Graph graph = model.getGraph() ;
        HttpEntity entity = graphToHttpEntity(graph) ;
        String url = RDFConn.urlForGraph(svcGraphStore, graphName) ;
        exec(()->HttpOp.execHttpPut(url, entity, null, null, this.authenticator)) ;
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

    private void doPutPost(String url, String file, Lang lang, boolean replace) {
        File f = new File(file) ;
        long length = f.length() ; 
        InputStream source = IO.openFile(file) ;
        // Charset.
        exec(()->{
            if ( replace )
                HttpOp.execHttpPut(url, lang.getContentType().getContentType(), source, length, null, null, this.authenticator) ;
            else    
                HttpOp.execHttpPost(url, lang.getContentType().getContentType(), source, length, null, null, null, null, this.authenticator) ;
        }) ;
    }

    @Override
    public void put(String graph, String file) {
        checkGSP() ;
        upload(graph, file, true) ;
    }
    
    @Override
    public void put(String file) { 
        checkGSP() ;
        upload(null, file, true) ; 
    }
    
    @Override
    public void put(String graphName, Model model) {}

    @Override
    public void put(Model model) {}

    
    
    @Override
    public void delete(String graph) {
        checkGSP() ;
        String url = RDFConn.urlForGraph(svcGraphStore, graph) ;
        exec(()->HttpOp.execHttpDelete(url));
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
        TypedInputStream s = exec(()->HttpOp.execHttpGet(destination, WebContent.defaultDatasetAcceptHeader)) ;
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
    
    @Override
    public void loadDataset(Dataset dataset) {}

    private void doPutPostDataset(String file, boolean replace) {
        Lang lang = RDFLanguages.filenameToLang(file) ;
        File f = new File(file) ;
        long length = f.length() ;
        exec(()->{
            InputStream source = IO.openFile(file) ;
            if ( replace )
                HttpOp.execHttpPut(destination, lang.getContentType().getContentType(), source, length, null, null, this.authenticator) ;
            else    
                HttpOp.execHttpPost(destination, lang.getContentType().getContentType(), source, length, null, null, null, null, this.authenticator) ;
        });
    }

    @Override
    public void putDataset(String file) {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URl provided") ; 
        doPutPostDataset(file, true) ;
    }
    
    @Override
    public void putDataset(Dataset dataset) {}

    private void doPutPostDataset(Dataset dataset, boolean replace) {
        Lang lang = Lang.NQUADS ;
        exec(()->{
            if ( replace )
                HttpOp.execHttpPut(destination, datasetToHttpEntity(dataset.asDatasetGraph()),
                                   null, null, this.authenticator) ;
            else    
                HttpOp.execHttpPost(destination, datasetToHttpEntity(dataset.asDatasetGraph()),
                                    null, null, this.authenticator) ;
        });
    }


    private void checkQuery() {
        checkOpen() ;
        if ( svcQuery == null )
            throw new ARQException("No query service defined for this RDFConnection") ;
    }
    
    private void checkUpdate() {
        checkOpen() ;
        if ( svcUpdate == null )
            throw new ARQException("No update service defined for this RDFConnection") ;
    }
    
    private void checkGSP() {
        checkOpen() ;
        if ( svcGraphStore == null )
            throw new ARQException("No SPARQL Graph Store service defined for this RDFConnection") ;
    }
    
    private void checkDataset() {
        checkOpen() ;
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URL provided") ; 
    }

    private void checkOpen() {
        if ( ! isOpen )
            throw new ARQException("closed") ;
    }

    @Override
    public void close() {
        isOpen = false ;
    }

    @Override
    public boolean isClosed() {
        return ! isOpen ;
    }

    /** Create an HttpEntity for the graph */  
    protected HttpEntity graphToHttpEntity(final Graph graph) {
        final RDFFormat syntax = RDFFormat.NT ;
        EntityTemplate entity = new EntityTemplate((out)->RDFDataMgr.write(out, graph, syntax)) ;
        String ct = syntax.getLang().getContentType().getContentType() ;
        entity.setContentType(ct) ;
        return entity ;
    }

    /** Create an HttpEntity for the dataset */  
    protected HttpEntity datasetToHttpEntity(final DatasetGraph dataset) {
        final RDFFormat syntax = RDFFormat.NT ;
        EntityTemplate entity = new EntityTemplate((out)->RDFDataMgr.write(out, dataset, syntax)) ;
        String ct = syntax.getLang().getContentType().getContentType() ;
        entity.setContentType(ct) ;
        return entity ;
    }

    /** Convert HTTP status codes to exceptions */ 
    static void exec(Runnable action)  {
        try { action.run() ; }
        catch (HttpException ex) { handleHttpException(ex, false) ; }
    }

    /** Convert HTTP status codes to exceptions */ 
    static <X> X exec(Supplier<X> action)  {
        try { return action.get() ; }
        catch (HttpException ex) { handleHttpException(ex, true) ; return null ;}
    }

    private static void handleHttpException(HttpException ex, boolean ignore404) {
        if ( ex.getResponseCode() == HttpSC.NOT_FOUND_404 && ignore404 )
            return  ;
        throw ex ;
    }

    @Override
    public void begin(ReadWrite readWrite) { throw new ARQNotImplemented() ; }

    @Override
    public void commit() { throw new ARQNotImplemented() ; }

    @Override
    public void abort() { throw new ARQNotImplemented() ; }

    @Override
    public boolean isInTransaction() {
        return false ;
    }

    @Override
    public void end() { throw new ARQNotImplemented() ; }

}

