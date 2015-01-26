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

package projects.prefixes;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.ActionKeyValue ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.iri.IRI ;

import com.hp.hpl.jena.shared.PrefixMapping ;

/** Converted from PrefixMap (a used by the parsers) to Jena's inferface PrefixMapping
 * (which is a bit XML-centric for historical reasons).
 */

public class PrefixMappingOverPrefixMapI implements PrefixMapping
{
    private PrefixMapI pmap ;

    PrefixMappingOverPrefixMapI(PrefixMapI pmap) { this.pmap = pmap ; }

    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri)
    {
        pmap.add(prefix, uri) ;
        return this ;
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix)
    {
        pmap.delete(prefix) ;
        return this ;
    }

    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping other)
    {
        return setNsPrefixes(other.getNsPrefixMap()) ;
    }

    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> map)
    {
        for ( Map.Entry<String, String> e : map.entrySet() )
        {
            String prefix = e.getKey() ;
            String iriStr = e.getValue() ;
            pmap.add(prefix, iriStr) ;
        }
        return this ;
    }

    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping map)
    {
        Map<String, String> emap = map.getNsPrefixMap() ;
        for  ( Map.Entry<String, String> e : emap.entrySet() )
        {
            String prefix = e.getKey() ;
            String iriStr = e.getValue() ;
            if ( ! pmap.contains(prefix) )
                pmap.add(prefix, iriStr) ;
        }
        return this ;
    }

    @Override
    public String getNsPrefixURI(String prefix)
    {
        return pmap.getMapping().get(prefix).toString() ;
    }

    @Override
    public String getNsURIPrefix(String uri)
    {
        Pair<String , String> abbrev = pmap.abbrev(uri) ;
        if ( abbrev == null )
            return null ;
        return abbrev.getLeft() ;
    }

    @Override
    public Map<String, String> getNsPrefixMap()
    {
        Map<String, IRI> map = pmap.getMapping() ;
        final Map<String, String> smap = new HashMap<String, String>() ;
        ActionKeyValue<String, IRI> action = new ActionKeyValue<String, IRI>(){
            @Override
            public void apply(String key, IRI value)
            {
                String str = value.toString() ;
                smap.put(key, str) ;
            }
        } ;
        Iter.apply(map, action) ;
        return smap  ;
    }

    @Override
    public String expandPrefix(String prefixed)
    {
        String str = pmap.expand(prefixed) ;
        if ( str == null )
            return prefixed ;
        return str ;
    }

    @Override
    public String shortForm(String uri)
    {
        String s = pmap.abbreviate(uri) ;
        if ( s == null )
            return uri ;
        return s ;
    }

    @Override
    public String qnameFor(String uri)
    {
        return pmap.abbreviate(uri) ;
    }

    @Override
    public PrefixMapping lock()
    {
        return this ;
    }

    @Override
    public boolean samePrefixMappingAs(PrefixMapping other)
    {
        return this.getNsPrefixMap().equals( other.getNsPrefixMap() ) ;
    }
    
}

