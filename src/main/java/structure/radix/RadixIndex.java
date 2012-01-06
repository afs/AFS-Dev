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

package structure.radix;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;

public class RadixIndex implements RangeIndex
{
    // DEBUG
    public RadixTree radix = new RadixTree() ;
    private RecordFactory recordFactory ;
    
    public RadixIndex(RecordFactory recordFactory)
    {
        this.recordFactory = recordFactory ;
        if ( recordFactory.hasValue() )
            throw new UnsupportedOperationException("Records with values") ; 
    }
    
    @Override
    public Record find(Record record)
    {
        RadixNode radixNode = radix.find(record.getKey()) ;
        if ( radixNode == null )
            return null ;
        return record ; 
    }

    @Override
    public boolean contains(Record record)
    {
        return find(record) != null ;
    }

    @Override
    public boolean add(Record record)
    {
        return radix.insert(record.getKey()) ;
    }

    @Override
    public boolean delete(Record record)
    {
        return radix.delete(record.getKey()) ;
    }

    Transform<ByteBuffer, Record> t = new Transform<ByteBuffer, Record>() {
        @Override
        public Record convert(ByteBuffer item)
        {
            return recordFactory.create(item.array()) ;
        }} ;
    
    @Override
    public Iterator<Record> iterator()
    {
        return Iter.map(radix.iterator(), t) ;
    }

    @Override
    public RecordFactory getRecordFactory()
    {
        return recordFactory ;
    }

    @Override
    public void close()
    {}

    @Override
    public boolean isEmpty()
    {
        return false ;
    }

    @Override
    public void clear()
    {}

    @Override
    public void check()
    {}

    @Override
    public long size()
    {
        return radix.size() ;
    }

    @Override
    public void sync()
    {}

    @Override
    public Iterator<Record> iterator(Record recordMin, Record recordMax)
    {
        return Iter.map(radix.iterator(recordMin.getKey(), recordMax.getKey()), t) ;
    }

    @Override
    public Record minKey()
    {
        // Better???
        Iterator<ByteBuffer> iter = radix.iterator() ;
        if ( !iter.hasNext() ) return null ;
        return t.convert(radix.iterator().next()) ;
    }

    @Override
    public Record maxKey()
    {
        return null ;
    }
    
}
