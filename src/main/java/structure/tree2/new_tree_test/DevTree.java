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

package structure.tree2.new_tree_test;

import structure.OrderedMap ;
import structure.tree2.Tree ;

public class DevTree {

    public static void main(String[] args) {
        int[] r = {1, 16, 3, 14, 5, 2, 37, 11, 6, 23, 4, 25, 12, 7, 40} ;
        OrderedMap<Integer, Integer> index = create(r) ;

        System.out.println(index) ;
        boolean b = index.remove(1) ;
        System.out.println(index) ;
        System.out.println(b) ;
//        for ( int i : r )
//            assertTrue("remove i=" + i, index.remove(i)) ;
    }

    private static OrderedMap<Integer, Integer> create(int[] r) {
        OrderedMap<Integer, Integer> index = new Tree<>() ;
        for ( int i : r )
            index.insert(i,i) ;
        return index ;
    }
}

