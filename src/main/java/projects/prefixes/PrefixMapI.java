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

import java.util.Map ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.iri.IRI ;

public interface PrefixMapI
{
    /** return the underlying mapping - do not modify */
    public abstract Map<String, IRI> getMapping() ;

    /** return a copy of the underlying mapping */
    public abstract Map<String, IRI> getMappingCopy() ;

    public abstract Map<String, String> getMappingCopyStr() ;

    /* Return the underlying PrefixMapStorage - optional operation, may return null */ 
    public abstract PrefixMapStorage getPrefixMapStorage() ;
    
    /** Add a prefix, overwites any existing association */
    public abstract void add(String prefix, String iriString) ;

    /** Add a prefix, overwites any existing association */
    public abstract void add(String prefix, IRI iri) ;

    /** Add a prefix, overwites any existing association */
    public abstract void putAll(PrefixMapI pmap) ;

    /** Delete a prefix */
    public abstract void delete(String prefix) ;

    public abstract String get(String prefix) ;
    
    public abstract boolean contains(String prefix) ;

    /** Abbreviate an IRI or return null */
    public abstract String abbreviate(String uriStr) ;

    /** Abbreviate an IRI or return null */
    public abstract Pair<String, String> abbrev(String uriStr) ;

    /** Expand a prefix named, return null if it can't be expanded */
    public abstract String expand(String prefixedName) ;

    /** Expand a prefix, return null if it can't be expanded */
    public abstract String expand(String prefix, String localName) ;
}
