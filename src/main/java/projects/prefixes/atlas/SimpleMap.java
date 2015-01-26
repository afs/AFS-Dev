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

package projects.prefixes.atlas;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Pair ;

/** Simpler Map interface
 *  -- typed get()
 *  -- no return from put()
 *  -- no containsValue
 */
public interface SimpleMap<K,V> extends Iterable<Pair<K,V>> {
    public V get(K key) ;
    public boolean containsKey(K key) ;
    public void put(K key, V value) ;
    public void remove(K key) ;
    public void clear() ;
    public boolean isEmpty() ;
    @Override
    public Iterator<Pair<K, V>> iterator() ;
    public Iterator<K> keys() ;
}
