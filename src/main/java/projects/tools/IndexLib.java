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

package projects.tools;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.base.record.Record ;
import org.apache.jena.tdb.index.RangeIndex ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.tupletable.TupleIndex ;
import org.apache.jena.tdb.sys.SetupTDB ;

public class IndexLib
{
//  public static void dumpRangeIndex(RangeIndex rIndex)
//  {
//      System.out.println("Index: "+srcIndex) ;
//      String fn = location.getPath(srcIndex+"."+Names.bptExtRecords) ; //Names.bptExtTree
//      if ( ! new File(fn).exists() )
//          throw new CmdException("No such index: "+srcIndex) ;
//
//      int indexRecordLen = srcIndex.length()*NodeId.SIZE ;
//      RangeIndex rIndex = SetupTDB.makeRangeIndex(location, srcIndex, indexRecordLen, 0, 100*1000, 100*1000) ;
//      Iterator<Record> rIter = rIndex.iterator() ;
//      for ( ; rIter.hasNext() ; )
//      {
//          Record r = rIter.next() ;
//          System.out.println(r) ;
//      }
//  }
//

  public static void dumpRangeIndex(RangeIndex rIndex)
  {
      Iterator<Record> rIter = rIndex.iterator() ;
      for ( ; rIter.hasNext() ; )
      {
          Record r = rIter.next() ;
          System.out.println(r) ;
      }
  }

    public static void dumpTupleIndex(TupleIndex index)
    {
        System.out.println("Index: "+index.getName()) ;
        Iterator<Tuple<NodeId>> iter = index.all() ;
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> tuple = iter.next() ;
            System.out.println(tuple) ;
        }
    }

    public static TupleIndex connect(Location location, String primaryIndexName, String indexName)
    {
        int recordLength = NodeId.SIZE * primaryIndexName.length() ;
        return SetupTDB.makeTupleIndex(location, primaryIndexName, indexName, indexName, recordLength) ;
    }

    // LoadMonitor to become supclass of a general "Monitor"
    
    public static void copyIndex(TupleIndex srcIndex, TupleIndex destIndex)
    {
        Iterator<Tuple<NodeId>> srcIter = srcIndex.all() ;
        for ( ; srcIter.hasNext() ; )
        {
            Tuple<NodeId> tuple = srcIter.next() ;
            destIndex.add(tuple) ;
        }
        
    }
    
//    public static void copyIndexBulk(DatasetGraphTDB dsg, String srcIndex, String destIndex)
//    {
//        LoadMonitor loadMonitor = BulkLoader.createLoadMonitor(dsg, "TDB", true) ;
//        
//        BuilderSecondaryIndexes builder = new BuilderSecondaryIndexesSequential(loadMonitor) ;
//        
//        if ( srcIndex.length() != 3 && srcIndex.length() != 4 )
//            throw new CmdException("Source index '"+srcIndex+"' must be of length 3 or 4") ;
//        
//        if ( destIndex.length() != 3 && destIndex.length() != 4 )
//            throw new CmdException("Destination index '"+destIndex+"' must be of length 3 or 4") ;
//        
//        if ( srcIndex .length() != destIndex.length() )
//            throw new CmdException("Source and destination indexes must be the same tuple length") ;
//
//        NodeTupleTable ntt =
//            ( srcIndex .length() == 3 ) ? dsg.getTripleTable().getNodeTupleTable() : dsg.getQuadTable().getNodeTupleTable() ;
//
//        TupleIndex[] indexes = ntt.getTupleTable().getIndexes() ;
//        
//        TupleIndex srcIdx = find(indexes, srcIndex) ;
//        TupleIndex dstIdx = find(indexes, destIndex) ;
//        
//        if ( srcIdx == null )
//            throw new CmdException("No such index: "+srcIndex) ;
//        if ( dstIdx != null )
//            throw new CmdException("Index already exists and is in-use: "+destIndex) ;
//        
//        //if ( true) throw new RuntimeException("BANG") ;
//        
//        ColumnMap colMap = new ColumnMap(srcIndex, destIndex) ;
//        System.out.println(colMap.getLabel()) ;
//        int indexRecordLen = srcIndex.length()*NodeId.SIZE ;
//
//        RangeIndex rIndex = SetupTDB.makeRangeIndex(dsg.getLocation(), destIndex, indexRecordLen, 0, 100*1000, 100*1000) ;
//        String indexName = destIndex ;
//        dstIdx = new TupleIndexRecord(destIndex.length(), colMap, indexName, rIndex.getRecordFactory(), rIndex) ;
//        
//        // NOT PRIMARY INDEX
//        
//        loadMonitor.startLoad() ;
//        // What about a builder that knows how to copy from one index to another while exploiting semi-locality?   
//        builder.createSecondaryIndexes(srcIdx, new TupleIndex[] {dstIdx}) ;
//        loadMonitor.finishLoad() ;
//    }
    
    private static TupleIndex find(TupleIndex[] indexes, String srcIndex)
    {
        for ( TupleIndex idx : indexes )
        {
            // Index named by simple "POS"
            if ( idx.getName().equals(srcIndex) )
                return idx ;
            
            // Index named by column mapping "SPO->POS"
            // This is silly.
            int i = idx.getColumnMap().getLabel().indexOf('>') ;
            String name = idx.getMapping().substring(i+1) ;
            if ( name.equals(srcIndex) )
                return idx ;
        }
        return null ;
    }
    
}

