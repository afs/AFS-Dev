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

import java.util.Iterator ;

import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.RiotLoader ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.setup.* ;
import com.hp.hpl.jena.tdb.store.* ;

public class RunAFS
{
    public static void main(String ...argv)
    {
        Log.setLog4j() ;
        DatasetBuilder builder = new DatasetBuilderBasic(new IndexBuilderRadix(), new RangeIndexBuilderRadix() ) ;
        DatasetGraphTDB dsg = builder.build(Location.mem(), null) ;
        
        //Log.enable(NodeTable.class) ;
        //dsg.add(SSE.parseQuad("(_ <s> <p> <o>)")) ;
        //if ( false )
        Timer t = new Timer() ;
        t.startTimer() ;
        RiotLoader.read("/home/afs/Datasets/BSBM/bsbm-1m.nt.gz", dsg) ;
        long x = t.endTimer() ;
        System.out.println("load = "+Timer.timeStr(x)+"s") ;
        
        if ( false )
            SSE.write(dsg) ;
    }
    
    static class NodeTableRadix implements NodeTable
    {

        @Override
        public void sync()
        {}

        @Override
        public void close()
        {}

        @Override
        public NodeId getNodeIdForNode(Node node)
        {
            return null ;
        }

        @Override
        public Node getNodeForNodeId(NodeId id)
        {
            return null ;
        }

        @Override
        public NodeId getAllocateNodeId(Node node)
        {
            return null ;
        }

        @Override
        public Iterator<Pair<NodeId, Node>> all()
        {
            return null ;
        }

        @Override
        public NodeId allocOffset()
        {
            return null ;
        }

        @Override
        public boolean isEmpty()
        {
            return false ;
        }
        
    }
}

