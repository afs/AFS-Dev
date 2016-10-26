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

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.Sync ;
import projects.prefixes.atlas.SimpleMap ;

public interface PrefixMapStorage extends SimpleMap<String, String>, Sync, Closeable {
    @Override
    public void put(String prefix, String uriStr) ;
    @Override
    public String get(String prefix) ;
    @Override
    public void remove(String prefix) ;
    @Override
    public void clear() ;
    @Override
    public boolean isEmpty() ;
    @Override
    public int size() ;
    
    @Override
    public Iterator<Pair<String, String>> iterator() ;
    @Override
    public Iterator<String> keys() ;
    
    @Override
    public void sync() ;
    @Override
    public void close() ;
}
