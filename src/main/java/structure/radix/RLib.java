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

package structure.radix;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;

public class RLib
{
    public static String str(ByteBuffer bytes)
    {
        StringBuilder sb = new StringBuilder() ;
        char sep = 0 ;
        for ( int i = bytes.position() ; i < bytes.limit() ; i++ )
        {
            byte b = bytes.get(i) ;
            if ( sep != 0 )
                sb.append(sep) ;
            else
                sep = ' ' ;
                
            sb.append(String.format("%02X", b)) ;
        }
        return sb.toString() ;
    }

    public static String str(byte[] bytes)
    { return str(bytes, " ") ; }

    
    public static String str(byte[] bytes, String sep)
    {
        StringBuilder sb = new StringBuilder() ;
        boolean first = true ;
        for ( byte b : bytes )
        {
            if ( ! first )
                sb.append(sep) ;
            first = false ;
            sb.append(String.format("0x%02X", b)) ;
        }
        return sb.toString() ;
    }

    private static String str(RadixTree rt)
    {
        Iterator<String> iter = Iter.map(rt.iterator(), new Transform<ByteBuffer, String>(){
            @Override
            public String convert(ByteBuffer item)
            {
                return "["+str(item)+"]" ;
            }}) ;
        return Iter.asString(iter, ", ") ;
    }

}

