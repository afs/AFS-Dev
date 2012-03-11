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

import java.nio.ByteBuffer ;

import org.openjena.atlas.lib.StrUtils ;

public class StrUTF8Main
{
    public static void main(String ... args)
    {
        String[] x = { "", "a", "abcdefghjiklmnopqrstuvwxyz" } ;
        
        for ( String s : x )
        {
            StringUTF8 z = StringUTF8.alloc(s) ;
            System.out.println("'"+z+"'") ;
        }
        
        for ( String s : x )
        {
            byte bytes[] = StrUtils.asUTF8bytes(s) ;
            ByteBuffer bb = ByteBuffer.wrap(bytes) ;
            StringUTF8 z = StringUTF8.alloc(bb) ;
            System.out.println("'"+z+"'") ;
        }
    }
}

