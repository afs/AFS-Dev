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

package dev;

import structure.radix.RadixIndex ;

import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.setup.* ;

public class RunAFS
{
    public static void main(String ...argv)
    {
        BlockMgrBuilder blockMgrBuilder = null ;
        NodeTableBuilder nodeTableBuilder = null ;
        
        new DatasetBuilderStd(blockMgrBuilder, nodeTableBuilder) ;
        
    }
    
    static class RangeIndexBuilderR implements RangeIndexBuilder
    {

        @Override
        public RangeIndex buildRangeIndex(FileSet fileSet, RecordFactory recordfactory)
        {
            return new RadixIndex(recordfactory) ;
        }
        
    }

    static class IndexBuilderR implements IndexBuilder
    {

        @Override
        public Index buildIndex(FileSet fileSet, RecordFactory recordfactory)
        {
            return new RadixIndex(recordfactory) ;
        }
    }

}

