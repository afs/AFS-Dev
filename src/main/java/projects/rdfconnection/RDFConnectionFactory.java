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

// Pool stuff
public class RDFConnectionFactory {
    /** Connection to a remote location by URL.
     * This is the URL for the dataset.
     * 
     * Assumed names
     *   private static final String fusekiDftSrvQuery = "query" ;
    private static final String fusekiDftSrvUpdate = "update" ;
    private static final String fusekiDftSrvGSP = "data" ;
     *     
     *     
     *  hence ...
     * @param destination
     * @return RDFConnection
     */
    public static RDFConnection connect(String destination) {
        return new RDFConnectionRemote(destination) ;
    }

    public static RDFConnection connect(String queryServiceEndpoint,
                                        String updateServiceEndpoint,
                                        String graphStoreProtocolEndpoint) {
        return new RDFConnectionRemote(queryServiceEndpoint, updateServiceEndpoint, graphStoreProtocolEndpoint) ;
    }

    
    public static RDFConnection connect(String daatsetURL,
                                        String queryServiceEndpoint,
                                        String updateServiceEndpoint,
                                        String graphStoreProtocolEndpoint) {
        return new RDFConnectionRemote(queryServiceEndpoint, updateServiceEndpoint, graphStoreProtocolEndpoint) ;
    }

    /**
     * Differences:
     * @param dataset
     * @return
     */
    static RDFConnection create(Dataset dataset) {
        return new RDFConnectionLocal(dataset) ;
    }

    //public RDFConnection getFromPool() ;
}

