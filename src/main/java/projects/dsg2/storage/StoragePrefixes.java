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

package projects.dsg2.storage;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

public interface StoragePrefixes {
    
    /** Entry in a prefix map. */ 
    public static class PrefixEntry {
        // Custom interface to get more appropriate names.
        
        public static PrefixEntry create(String prefix, String uri) {
            return new PrefixEntry(prefix, uri);
        }
        private final String prefix;
        private final String uri ;
        
        private PrefixEntry(String prefix, String uri) {
            this.prefix = prefix ;
            this.uri = uri ;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getUri() {
            return uri;
        }
    }
    
    /* Get the prefix mapping of a prefix */
    public String get(Node graphNode, String prefix) ;
    
    /* Access to the storage - access by graph name */
    public Stream<PrefixEntry> get(Node graphNode) ;

    /** Add a prefix, overwites any existing association */
    public void add(Node graphNode, String prefix, String iriStr) ;
    
    /** Delete a prefix mapping */
    void delete(Node graphNode, String prefix) ;
    
    /** Delete prefix mappings for a specific graph name. */
    void deleteAll(Node graphNode) ;
    
    /** Return whether there are any prefix mappings or not (any graph). */
    public boolean isEmpty() ;
    
    /** Return the number of mappings. */
    public int numPrefixes();
    
    /** Return the number of mappings. */
    public int numPrefixes(Node graphNode);

}
