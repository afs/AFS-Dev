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

package dev;

import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.index.IndexBuilder ;
import org.apache.jena.tdb.index.RangeIndexBuilder ;
import org.apache.jena.tdb.setup.* ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;

public class RunAFS
{
    static { LogCtl.setCmdLogging(); }
    
    static class  DatasetBuilderRadix extends DatasetBuilderStd {
        DatasetBuilderRadix() { 
            super() ;
            setup() ;
        }
        
        private void setup() {
            StoreParams params = StoreParams.getDftStoreParams() ;
            ObjectFileBuilder objectFileBuilder = new BuilderStdDB.ObjectFileBuilderStd()  ;
            
            IndexBuilder indexBuilder = new IndexBuilderRadix() ;
            RangeIndexBuilder rangeIndexBuilder = new RangeIndexBuilderRadix() ;
            
            NodeTableBuilder nodeTableBuilder = new BuilderStdDB.NodeTableBuilderStd(indexBuilder, objectFileBuilder) ;
            TupleIndexBuilder tupleIndexBuilder = new BuilderStdDB.TupleIndexBuilderStd(rangeIndexBuilder) ;
            super.set(nodeTableBuilder, tupleIndexBuilder) ;
        }
    }
    

    public static void main(String ...argv)
    {
        LogCtl.setLog4j() ;
        DatasetBuilder builder = new DatasetBuilderRadix() ;
        DatasetGraphTDB dsg = builder.build(Location.mem(), StoreParams.getDftStoreParams()) ;
        
        //Log.enable(NodeTable.class) ;
        //dsg.add(SSE.parseQuad("(_ <s> <p> <o>)")) ;
        //if ( false )
        Timer t = new Timer() ;
        t.startTimer() ;
        RDFDataMgr.read(dsg, "/home/afs/Datasets/BSBM/bsbm-50k.nt.gz") ;
        long x = t.endTimer() ;
        System.out.println("load = "+Timer.timeStr(x)+"s") ;
        
        if ( false )
            SSE.write(dsg) ;
    }
}

