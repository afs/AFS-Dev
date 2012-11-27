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

package lib ;

import org.junit.Test ;
import org.apache.jena.atlas.junit.BaseTest ;

public class TestByteArray extends BaseTest
{
    @Test public void bytearray_01()
    {
        ByteArray b = new ByteArray() ;
        compare(b, new byte[]{}) ;
    }
    
    @Test public void bytearray_02()
    {
        ByteArray b = new ByteArray() ;
        b.add((byte)1) ;
        compare(b, new byte[]{1}) ;
    }
    
    
    private static void compare(ByteArray bytes, byte[] contents)
    {
        assertEquals(bytes.length(), contents.length) ;
        for ( int i = 0 ; i < contents.length ; i++ )
            assertEquals(contents[i], bytes.get(i)) ;
        
    }
}
