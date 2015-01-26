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
import java.util.Map.Entry ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;

public class PrefixMapBase implements PrefixMapI {

    static final IRIFactory factory = IRIFactory.iriImplementation() ;
    private final PrefixMapStorage prefixes ;
    
    PrefixMapBase(PrefixMapStorage storage)
    { this.prefixes = storage ; }
    
    // remove
    @Override
    public Map<String, IRI> getMapping()
    {
        return getMappingCopy() ;
    }

    // remove
    @Override
    public Map<String, IRI> getMappingCopy()
    {
        Map<String, IRI> map = new HashMap<String, IRI>() ;
        for ( Pair<String, String> p : prefixes )
            map.put(p.getLeft(), factory.create(p.getRight())) ;
        return map ;
    }

    @Override
    public Map<String, String> getMappingCopyStr()
    {
        Map<String, String> map = new HashMap<String, String>() ;
        for ( Pair<String, String> p : prefixes )
            map.put(p.getLeft(), p.getRight()) ;
        return map ;
    }

    @Override
    public PrefixMapStorage getPrefixMapStorage()
    {
        return prefixes ;
    }

    @Override
    public void add(String prefix, String iriString)
    {
        prefix = canonicalPrefix(prefix) ;
        prefixes.put(prefix, iriString) ;
    }

    @Override
    public void add(String prefix, IRI iri)
    {
        prefix = canonicalPrefix(prefix) ;
        prefixes.put(prefix, iri.toString()) ;
    }

    @Override
    public void putAll(PrefixMapI pmap)
    {
        Map<String, IRI> map = pmap.getMapping() ;
        for ( Entry<String, IRI> e : map.entrySet() )
            add(e.getKey(), e.getValue()) ;
    }

    @Override
    public void delete(String prefix)
    {
        prefix = canonicalPrefix(prefix) ;
        prefixes.remove(prefix) ;
    }

    @Override
    public String get(String prefix)
    {
        prefix = canonicalPrefix(prefix) ;
        return prefixes.get(prefix) ;
    }

    @Override
    public boolean contains(String prefix)
    {
        prefix = canonicalPrefix(prefix) ;
        return prefixes.containsKey(prefix) ;
    }

    @Override
    public String abbreviate(String uriStr)
    {
        prefixes.iterator() ;
        
        for ( Pair<String, String> e : prefixes)
        {
            String prefix = e.getRight().toString() ;
            
            if ( uriStr.startsWith(prefix) )
            {
                String ln = uriStr.substring(prefix.length()) ;
                if ( strSafeFor(ln, '/') && strSafeFor(ln, '#') && strSafeFor(ln, ':') )
                    return e.getLeft()+":"+ln ;
            }
        }
        return null ;
    }

    private static boolean strSafeFor(String str, char ch) { return str.indexOf(ch) == -1 ; }
    
    @Override
    public Pair<String, String> abbrev(String uriStr)
    {
        for ( Pair<String, String> e : prefixes )
        {
            String uriForPrefix = e.getRight().toString() ;
            
            if ( uriStr.startsWith(uriForPrefix) )
                return Pair.create(e.getLeft(), uriStr.substring(uriForPrefix.length())) ;
        }
        return null ;
    }

    @Override
    public String expand(String prefixedName)
    {
        int i = prefixedName.indexOf(':') ;
        if ( i < 0 ) return null ;
        return expand(prefixedName.substring(0,i),
                      prefixedName.substring(i+1)) ;
    }

    @Override
    public String expand(String prefix, String localName)
    {
        prefix = canonicalPrefix(prefix) ;
        String x = prefixes.get(prefix) ;
        if ( x == null )
            return null ;
        return x+localName ;
    }
    
    protected static String canonicalPrefix(String prefix)
    {
        if ( prefix.endsWith(":") )
            return prefix.substring(0, prefix.length()-1) ;
        return prefix ;
    }
    
    @Override public String toString() 
    {
        StringBuilder sb = new StringBuilder() ;
        sb.append("{ ") ;
        boolean first = true ;

        for ( Pair<String, String> e : prefixes )
        {
            String prefix = e.getLeft() ;
            String iri = e.getRight() ;
            if ( first )
                first = false ;
            else
                sb.append(" ,") ;
            sb.append(prefix) ;
            sb.append(":=") ;
            sb.append(iri) ;
        }
        sb.append(" }") ;
        return sb.toString() ; 
    }
}