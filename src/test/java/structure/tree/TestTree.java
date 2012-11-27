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

package structure.tree;

import java.util.List;

import org.junit.Test;
import org.apache.jena.atlas.junit.BaseTest ;

public class TestTree extends BaseTest
{
    static TreeNode<String> A = new TreeNode<String>("A") ;
    static TreeNode<String> B = new TreeNode<String>("B") ;
    static TreeNode<String> C = new TreeNode<String>("C") ;
    static TreeNode<String> D = new TreeNode<String>("D") ;
    
    @Test public void tree1()
    {
        TreeNode<String> x = new TreeNode<String>("A") ;
        x.insert("B") ;
        List<String> recs = x.records() ;
        assertEquals(2, recs.size()) ;
        assertEquals("A", recs.get(0)) ;
        assertEquals("B", recs.get(1)) ;
    }
    

    @Test public void tree10()
    {
        TreeNode<String> x = new TreeNode<String>("K3", new TreeNode<String>("K1", A, new TreeNode<String>("K2", B, C)), D) ;
        List<String> before = x.records() ;
        x.pivotLeftRight() ;
        List<String> after = x.records() ;
        assertEquals(before, after) ;
    }
    
    @Test public void tree11()
    {
        TreeNode<String> x = new TreeNode<String>("K1", A, new TreeNode<String>("K3", new TreeNode<String>("K2", B, C), D)) ;
        List<String> before = x.records() ;
        x.pivotRightLeft() ;
        List<String> after = x.records() ;
        assertEquals(before, after) ;
    }    
}
