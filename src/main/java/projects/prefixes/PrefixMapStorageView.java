/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package projects.prefixes;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Quad ;

public class PrefixMapStorageView implements PrefixMapStorage
{
    private final DatasetPrefixes dsgPrefixes ;    // change to put/get style -- see DatasetPrefixes
    private final Node graphName ;
    
    public static PrefixMapStorage viewDefaultGraph(DatasetPrefixes dsgPrefixes)
    { return new PrefixMapStorageView(dsgPrefixes, null) ; }
    
    public static PrefixMapStorage viewGraph(DatasetPrefixes dsgPrefixes, Node graphName) 
    { return new PrefixMapStorageView(dsgPrefixes, graphName) ; }
    
    private PrefixMapStorageView(DatasetPrefixes dsgPrefixes, Node graphName)
    {
        this.dsgPrefixes = dsgPrefixes ;
        this.graphName = graphName ;
    }
    
    
    
    @Override
    public void put(String prefix, String uriStr)   { dsgPrefixes.add(graphName, prefix, uriStr) ; }
    
    
    @Override
    public String get(String prefix)                { return dsgPrefixes.get(graphName, prefix) ; } 
    
    @Override
    public boolean containsKey(String prefix)
    {
        return get(prefix) != null ;
    }

    
    @Override
    public void remove(String prefix) { dsgPrefixes.delete(graphName, prefix) ; }
    
    @Override
    public void clear() {
        List<Pair<String, String>> x = Iter.toList(iterator()) ;
        for ( Pair<String, String> e : x )
            remove(e.getLeft()) ;
    }
    
    @Override
    public boolean isEmpty() {  
        return ! dsgPrefixes.listGraphNodes().hasNext() ;
    }
    
    @Override
    public Iterator<Pair<String, String>> iterator()
    {
        return dsgPrefixes.get(graphName) ;
    }
    
    @Override
    public Iterator<String> keys()
    {
        return Iter.map(dsgPrefixes.listGraphNodes(), Node::getURI) ;
    }
    
    @Override
    public void sync() {}
    @Override
    public void close() {}
    
    // The default graph : preferred name is the explicitly used name.
    private static final Node dftGraph =  Quad.defaultGraphIRI ;
    // Also seen as:
    private static final Node dftGraph2 = Quad.defaultGraphNodeGenerated ;

    private static Node canonical(Node graphName)
    {
        if ( graphName == null )
            return dftGraph ;
        if ( dftGraph2.equals(graphName) )
            return dftGraph ;
        return graphName ;
    }

    
}
