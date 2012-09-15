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

package projects.mvccds;

public class MainMVCC
{
    public static void main(String ... args)
    {
        String[] x1 = { "BBB", "CCC", } ;
        String[] x2 = { "AAA", "DDD" } ;
        GenTree<String> tree = new GenTree<>() ;
        // Create updatable?
        tree = tree.beginUpdate() ;
        for(String s : x1 )
            tree.add(s) ;
        tree.commitUpdate() ;
        
        System.out.println("Old") ;
        dump(tree) ;
        System.out.println("--------") ;
        GenTree<String> tree2 = tree.beginUpdate() ;
        for(String s : x2 )
        {
            System.out.println(">> Insert: "+s) ;
            dump(tree2) ;
            tree2.add(s) ;
            System.out.println("<< Insert: "+s) ;
            dump(tree2) ;
            System.out.println() ;
        }
        tree2.commitUpdate() ;

        System.out.println("new") ;
        dump(tree2) ;
    }

    private static void dump(GenTree<String> tree)
    {
        tree.dump() ;
        tree.dumpFull() ;
    }
    
}

