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

package projects.prefixes ;

import java.util.* ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Pair ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Quad ;

/** In-memory dataset prefixes */

public class DatasetPrefixesMem implements DatasetPrefixes // No -- extends
                                                           // DatasetPrefixStorage
{
    // Effectively this a map of maps
    private Map<Node, PrefixMapI> map = new HashMap<Node, PrefixMapI>() ;

    DatasetPrefixesMem() {}

    @Override
    public String get(Node graphNode, String prefix) {
        PrefixMapI pmap = map.get(canonical(graphNode)) ;
        if ( pmap == null )
            return null ;
        return pmap.get(prefix) ;
    }

    @Override
    public Iterator<Pair<String, String>> get(Node graphNode) {
        PrefixMapI pmap = map.get(canonical(graphNode)) ;
        if ( pmap == null )
            return Iter.nullIterator() ;
        PrefixMapStorage storage = pmap.getPrefixMapStorage() ;
        if ( storage != null )
            return storage.iterator() ;

        // Do it the hard way
        List<Pair<String, String>> x = new ArrayList<Pair<String, String>>() ;
        for ( Map.Entry<String, String> e : pmap.getMappingCopyStr().entrySet() )
            x.add(Pair.create(e.getKey(), e.getValue())) ;
        return x.iterator() ;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return map.keySet().iterator() ;
    }

    /** Add a prefix, overwites any existing association */
    @Override
    public void add(Node graphNode, String prefix, String iriStr) {
        accessForUpdate(graphNode).add(prefix, iriStr) ;
    }

    /** Delete a prefix mapping */
    @Override
    public void delete(Node graphNode, String prefix) {
        access(graphNode).delete(prefix) ;
    }

    @Override
    public void deleteAll(Node graphNode) {
        access(graphNode).clear() ;
    }

    /** Abbreviate an IRI or return null */
    @Override
    public String abbreviate(Node graphNode, String iriStr) {
        return access(graphNode).abbreviate(iriStr) ;
    }

    @Override
    public Pair<String, String> abbrev(Node graphNode, String iriStr) {
        return access(graphNode).abbrev(iriStr) ;
    }

    /** Expand a prefix named, return null if it can't be expanded */
    @Override
    public String expand(Node graphNode, String prefixedName) {
        return access(graphNode).expand(prefixedName) ;
    }

    /** Expand a prefix, return null if it can't be expanded */
    @Override
    public String expand(Node graphNode, String prefix, String localName) {
        return access(graphNode).expand(prefix, localName) ;
    }

    // Access or return the empty, dummy mapping.
    private PrefixMapI accessForUpdate(Node graphName) {
        graphName = canonical(graphName) ;
        PrefixMapI pmap = map.get(graphName) ;
        if ( pmap == null ) {
            pmap = PrefixesFactory.createMem() ;
            map.put(graphName, pmap) ;
        }
        return pmap ;
    }

    // Access or return the empty, dummy mapping.
    private PrefixMapI access(Node graphName) {
        graphName = canonical(graphName) ;
        PrefixMapI pmap = map.get(graphName) ;
        if ( pmap == null )
            return PrefixesFactory.empty() ;
        return pmap ;
    }

    // The default graph : preferred name is the explicitly used name.
    private static final Node dftGraph  = Quad.defaultGraphIRI ;
    // Also seen as:
    private static final Node dftGraph2 = Quad.defaultGraphNodeGenerated ;

    private static Node canonical(Node graphName) {
        if ( graphName == null )
            return dftGraph ;
        if ( dftGraph2.equals(graphName) )
            return dftGraph ;
        return graphName ;
    }
}
