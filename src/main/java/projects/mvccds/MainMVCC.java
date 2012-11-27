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

import org.apache.jena.atlas.logging.Log ;

public class MainMVCC
{
    static { Log.setLog4j() ; }
    
    public static GenTree<String> add(GenTree<String> tree, String ...elts)
    {
        GenTree<String> tree2 = tree.beginUpdate() ;
        for(String s : elts )
            tree2.add(s) ;
        tree2.commitUpdate() ;
        return tree2 ;
    }
    
    public static void main(String ... args)
    {
        TNodeAllocator<String> allocator = new TNodeAllocator<>() ;
        
        Log.enable(GenTree.class) ;
        
        String[] x1 = { "BBB", "DDD", } ;
        String[] x2 = { "A", "AAA", "ZZZ", "CCC", "XXX" } ;
        String[] x3 = { "5", "0", "2", "9"} ;
        //String[] x4 = { "ABC", "DEF", "ZZZZ", "111" } ;
        String[] x4 = { "ABC" } ;
        GenTree<String> tree = GenTree.create(allocator) ;

        tree = add(tree, x1) ;
        System.out.println("** Input") ;
        //tree.dump() ;
        tree.records() ;
        
        //GenTree.DEBUG = true ;
        
        GenTree<String> tree2 = tree.beginUpdate() ;
        for(String s : x2 )
        {
            // if 
            //System.out.println(">> Insert: "+s) ;
            //dump(tree2) ;
            tree2.add(s) ;
            //System.out.println("<< Insert: "+s) ;
            //dump(tree2) ;
            //System.out.println() ;
        }
        tree2 = tree2.commitUpdate() ;
        //tree2.dump() ;
        System.out.println("** Update 1") ;
        tree2.records() ;
        
        tree2 = tree2.beginUpdate() ;
        for ( String s : x3 )
            tree2.add(s) ;
        tree2 = tree2.abortUpdate() ;
        System.out.println("** Abort") ;
        tree2.records() ;
        
        //GenTree.DEBUG = true ;
        GenTree<String> tree2a = GenTree.duplicate(tree2) ;
        GenTree<String> tree3 = tree2.beginUpdate() ;
        for ( String s : x4 )
            tree3.add(s) ;
        tree3 = tree3.commitUpdate() ;
        System.out.println("** Update 2") ;
        tree3.records() ;
        System.out.println("** Pre-update 2") ;
        tree2a.records() ;
        System.out.println() ;
        tree3.dump() ;
      }

    private static void dump(GenTree<String> tree)
    {
        tree.records() ;
        tree.dump() ;
    }
    
}

