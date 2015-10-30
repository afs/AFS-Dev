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

package storage.string;

class ByteStreamStringUTF8 implements IntSequence {
    private final StringUTF8 bytes;
    private final int length;
    private int index ;

    ByteStreamStringUTF8(StringUTF8 strBytes) {
        this.bytes = strBytes ;
        this.length = bytes.byteLength() ;
        this.index = 0 ;  
    }
    
    @Override
    public int current() {
        if ( index < 0 || index >= length )
            return -1 ;
        return bytes.get(index) ;
    }

    @Override
    public int forward() {
        if ( index >= length )
            return -1 ;
        index++ ;
        return bytes.get(index) ;
    }

    @Override
    public int backward() {
        if ( index <= 0 ) 
            return -1 ;
        index-- ;
        return bytes.get(index) ;
    }

    @Override
    public int position() {
        return index ;
    }

    @Override
    public int length() {
        return length ;
    }

    @Override
    public void position(int newPosn) {
        if ( newPosn < 0 || newPosn > length )
            throw new IndexOutOfBoundsException("Position "+newPosn+" not in [0, "+(length-1)+"]") ;
        index = newPosn ;
    }
    
    @Override
    public String toString() {
        return String.format("[ByteStreamStringUTF8 : index=%d : length=%d]", index, length) ; 
    }
}