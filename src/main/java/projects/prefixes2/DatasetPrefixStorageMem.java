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

package projects.prefixes2;

import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Map ;
import java.util.Set ;

import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;

import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.DatasetPrefixStorage ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.graph.GraphPrefixesProjection ;

/** Abstract of prefix storage for graphs in an RDF dataset */

public class DatasetPrefixStorageMem implements DatasetPrefixStorage
{
    // DatasetPrefixStorage ==> DatasetPrefixes ??
    // Need remove whole per-graph mapping.? 
    
    // The default graph : preferred name is the explicitly used name.
    private static final String dftGraph =  Quad.defaultGraphIRI.getURI() ;
    // Also seen as:
    private static final String dftGraph2 = Quad.defaultGraphNodeGenerated.getURI() ;
    
    private Map<String, PrefixMap> prefixes = new HashMap<String,PrefixMap>() ;
    
    private static final PrefixMap emptyPrefixMap = PrefixMapFactory.create() ;
    
    public DatasetPrefixStorageMem() {}
    
    @Override
    public Set<String> graphNames()
    {
        // Default graph.
        Set<String> x = new HashSet<String>(prefixes.keySet()) ;
        x.remove(dftGraph) ;
        return x ;
    }

    @Override
    public String readPrefix(String graphName, String prefix)
    {
        return access(graphName).expand(prefix) ;
    }

    @Override
    public String readByURI(String graphName, String uriStr)
    {
        return access(graphName).abbreviate(uriStr) ;
    }

    @Override
    public Map<String, String> readPrefixMap(String graphName)
    {
        return access(graphName).getMappingCopyStr() ;
    }

    @Override
    public void insertPrefix(String graphName, String prefix, String uri)
    {
        graphName = canonical(graphName) ;
        PrefixMap pmap = prefixes.get(graphName) ;
        if ( pmap == null )
        {
            pmap = PrefixMapFactory.create() ;
            prefixes.put(graphName, pmap) ;
        }
        pmap.add(prefix, uri) ;
    }

    @Override
    public void loadPrefixMapping(String graphName, PrefixMapping pmap)
    {}

    @Override
    public void removeFromPrefixMap(String graphName, String prefix)
    {
        access(graphName).delete(prefix) ;
    }

    @Override
    public PrefixMapping getPrefixMapping()
    {
        return getPrefixMapping(dftGraph) ;
    }

    @Override
    public PrefixMapping getPrefixMapping(String graphName)
    {
        return new GraphPrefixesProjection(canonical(graphName), this) ;
    }

    @Override
    public void close()
    {}

    @Override
    public void sync()
    {}


    // Access or return the empty, dummy mapping.
    private PrefixMap access(String graphName)
    {
        graphName = canonical(graphName) ;
        PrefixMap pmap = prefixes.get(graphName) ;
        if ( pmap == null )
            return emptyPrefixMap ;
        return pmap ;
    }


    private static String canonical(String graphName)
    {
        if ( graphName == null && dftGraph2.equals(graphName) )
            return dftGraph ;
        return graphName ;
    }

}
