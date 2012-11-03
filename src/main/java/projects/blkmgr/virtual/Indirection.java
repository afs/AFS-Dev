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

package projects.blkmgr.virtual;

import java.nio.LongBuffer ;


import com.hp.hpl.jena.tdb.base.StorageException ;

/* Maps long to long */
public class Indirection 
{
    // Need to map long to (long, long)
    
    private Sequence seq ; 
    private LongBuffer buffer = LongBuffer.allocate(1000) ;
    
    public Indirection()
    {
        
    }
    
    public long get(long ref)
    {
        check(ref) ;
        return _get(ref) ;
    }
    
    private long _get(long ref)
    {
        int x = (int)ref ;
        return buffer.get(x) ;
    }

    /** Store the value, return the reference to it */
    public long map(long value)
    {
        int x = (int)seq.next() ;
        if ( x > buffer.limit() )
            buffer = realloc() ;
        buffer.put(x, value) ;
        return x ;
    }
    
    protected LongBuffer realloc()
    {
        LongBuffer buffer2 = LongBuffer.allocate(buffer.capacity()*2) ;
        buffer.position(0) ;
        buffer.limit(buffer.capacity()) ;
        buffer2.put(buffer) ;
        return buffer2 ;
    }
    
    /** Alter an existing mapping.  Return the old value */
    public long remap(long ref, long newValue)
    {
        check(ref) ;
        long oldValue = _get(ref) ;
        int x = (int)seq.next() ;
        _put(x, newValue) ;
        return oldValue ;
    }
    
    private void _put(long ref, long newValue)
    {
        int x = (int)ref ;
        buffer.put(x, newValue) ;
    }
    
    private void check(long ref)
    {
        if ( ref < 0 || ref > seq.readLast() )
            throw new StorageException("Reference out of range: "+ref+" : [0,"+seq.readLast()+"]") ; 
        
    }
    
}
