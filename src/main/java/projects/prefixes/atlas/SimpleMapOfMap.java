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
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Pair ;

/** Adapter */ 
public class SimpleMapOfMap<K,V> implements SimpleMap<K, V>
{
    private Map<K, V> map ;

    public SimpleMapOfMap(Map<K,V> map) { this.map = map ; }

    @Override
    public V get(K key)                 { return map.get(key) ; }

    @Override
    public boolean containsKey(K key)   { return map.containsKey(key) ; } 

    @Override
    public void put(K key, V value)     { map.put(key, value) ; }

    @Override
    public void remove(K key)           { map.remove(key) ; }

    @Override
    public void clear()                 { map.clear() ; }

    @Override
    public boolean isEmpty()            { return map.isEmpty() ; }

    private Transform<Entry<K,V>, Pair<K,V>> transform = new Transform<Entry<K,V>, Pair<K,V>>() {
        @Override
        public Pair<K, V> convert(Entry<K, V> item)
        {
            return Pair.create(item.getKey(), item.getValue()) ;
        } } ; 
    
    @Override
    public Iterator<Pair<K, V>> iterator()
    {
        return Iter.map(map.entrySet().iterator(), transform) ;
    }

    @Override
    public Iterator<K> keys()
    {
        return map.keySet().iterator() ;
    }
}
