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

package projects.cache;

import org.apache.jena.atlas.lib.ActionKeyValue ;
import org.apache.jena.atlas.lib.Cache ;


public class MainCache
{
    // Put new -> missCount increments
    // "Put/replace" is a cache hit and calls drop handler.
    // "Put same" is a cache drop.
    // 
    public static void main(String[] args) {
        Cache<String, String> cache = new CacheGuava<>(5) ;
        cache.setDropHandler(new DropHandler()) ;
        String x = "A" ;
        cache.put("1", x) ;
        cache.put("1", x) ;
        cache.put("1", x) ;
//        cache.get("1") ;
//        cache.get("2") ;
//        cache.put("2", "B") ;
//        cache.put("2", "X") ;
//        cache.put("3", "C") ;
//        cache.put("4", "D") ;
        org.apache.jena.ext.com.google.common.cache.Cache<String, String> gcache = ((CacheGuava<String, String>)cache).cache ;
        org.apache.jena.ext.com.google.common.cache.CacheStats _stats = gcache.stats() ;
        
        CacheStats stats = CacheStats$convert(_stats) ;
        
        stats.toString() ;
        System.out.println(stats.toString()) ;
    }
    
    static CacheStats CacheStats$convert(org.apache.jena.ext.com.google.common.cache.CacheStats _stats) {
        return new CacheStatsGuava(_stats) ;
    }

    static class CacheStatsGuava implements CacheStats {
        
        private final org.apache.jena.ext.com.google.common.cache.CacheStats _stats ;
        
        private CacheStatsGuava(org.apache.jena.ext.com.google.common.cache.CacheStats _stats) {
            this._stats = _stats ;
        }

        @Override
        public long getHitCount() {
            return _stats.hitCount() ;
        }

        @Override
        public long getMissCount() {
            return _stats.missCount() ;
        }

        @Override
        public long getEvictionCount() {
            return _stats.evictionCount() ;
        }
        
        @Override
        public String toString() {
            return 
                String.format("CacheStats: hitCount=%d, missCount=%d, evictionCount=%d",
                              getHitCount(), getMissCount(), getEvictionCount()) ;
        }
    }

    static class DropHandler implements ActionKeyValue<String, String> {
        @Override
        public void apply(String key, String value) {
            System.out.printf("Drop {%s, %s}\n", key, value) ;
        }
    }
}
