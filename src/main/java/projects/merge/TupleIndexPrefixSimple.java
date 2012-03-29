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

package projects.merge;

import java.util.Arrays ;
import java.util.Iterator ;

import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class TupleIndexPrefixSimple  extends TupleIndexWrapper implements TupleIndexPrefix
{

    public TupleIndexPrefixSimple(TupleIndex index)
    {
        super(index) ;
    }

    @Override
    public Iterator<Tuple<NodeId>> findPrefix(Tuple<NodeId> pattern)
    {
        // Does not exploit the features of the index.
        // Extend the Tuple to table width.
        int patternLen = getTupleLength() ;
        int indexLen = getTupleLength() ;
        NodeId[] x = new NodeId[indexLen] ;
        System.arraycopy(pattern.tuple(), 0, x, 0, patternLen) ;
        Arrays.fill(x, patternLen, indexLen, NodeId.NodeIdAny) ;
        return find(Tuple.create(x)) ;
    }

}

