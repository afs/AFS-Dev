/**
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

package projects.cache;

import java.util.Iterator ;

import com.google.common.cache.CacheBuilder ;
import com.google.common.cache.RemovalListener ;
import com.google.common.cache.RemovalNotification ;

import org.openjena.atlas.lib.ActionKeyValue ;
import org.openjena.atlas.lib.Cache ;

final
public class CacheGuava<K,V> implements Cache<K, V>
{
    ActionKeyValue<K, V> dropHandler ;
    com.google.common.cache.Cache<K,V> cache ;
    
    public CacheGuava(int size)
    {
        RemovalListener<K,V> drop = new RemovalListener<K, V>() {
            @Override
            public void onRemoval(RemovalNotification<K, V> notification) {
                if ( dropHandler != null )
                    dropHandler.apply(notification.getKey(),
                                      notification.getValue()) ;
            }
        } ;
        cache = CacheBuilder.newBuilder()
                            .maximumSize(size)
                            //.expireAfterWrite(10, TimeUnit.MINUTES)         // For write caches, set this so it automatcially flushes if left idle.
                            // .expireAfterAccess(30, TimeUnit.MINUTES)
                            .softValues() // ??
                            .removalListener(drop)
                            .build() ;
    }
    
    @Override
    public V get(K key)
    {
        return cache.getIfPresent(key) ;
    }

    @Override
    public V put(K key, V thing)
    {
        V old = get(key) ;
        cache.put(key, thing) ;
        return old ;
    }

    @Override
    public boolean containsKey(K key)
    {
        return cache.getIfPresent(key) != null ;
    }

    @Override
    public boolean remove(K key)
    {
        V old = get(key) ;
        boolean r = ( old != null ) ; 
        cache.invalidate(key) ;
        return r ;
    }

    @Override
    public Iterator<K> keys()
    {
        return null ;
    }

    @Override
    public boolean isEmpty()
    {
        return cache.size() == 0 ;
    }

    @Override
    public void clear()
    {
        cache.invalidateAll() ;
    }

    @Override
    public long size()
    {
        return cache.size() ;
    }

    @Override
    public void setDropHandler(ActionKeyValue<K, V> dropHandler)
    {
        this.dropHandler = dropHandler ;
    }

}

