/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package projects.riot_sse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.sse.Item ;
import org.apache.jena.vocabulary.XSD ;
import org.junit.Test ;

public class TestSSE_Forms
{
    static PrefixMapping pmap = new PrefixMappingImpl() ;
    static { 
        pmap.setNsPrefix("xsd", XSD.getURI()) ;
        pmap.setNsPrefix("ex", "http://example/") ;
    }

    // ---- Assume ParseHandlerResolver from here on
    
    @Test public void testBase_01()
    { 
        Item r = Item.createNode(NodeFactory.createURI("http://example/x")) ; 
        testItem("(base <http://example/> <x>)", r) ;
    }
    
    @Test public void testBase_02()
    { 
        Item r = Item.createNode(NodeFactory.createURI("http://example/x")) ; 
        testItem("(base <http://HOST/> (base <http://example/xyz> <x>))", r) ;
    }

    @Test public void testBase_03()
    { 
        Item r = parse("(1 <http://example/xyz>)") ;
        testItem("(base <http://example/> (1 <xyz>))", r) ;
    }
    
    @Test public void testBase_04()
    { 
        Item r = parse("(1 <http://example/xyz>)") ;
        testItem("(1 (base <http://example/> <xyz>))", r) ;
    }
    
    @Test public void testBase_05()
    { 
        Item r = parse("(<http://example/xyz> <http://EXAMPLE/other#foo>)") ;
        testItem("((base <http://example/> <xyz>) (base <http://EXAMPLE/other> <#foo>))", r) ;
    }
    
    @Test public void testBase_06()
    { 
        Item r = parse("(<http://example/xyz> <http://EXAMPLE/other#foo>)") ;
        testItem("(base <http://example/> (<xyz> (base <http://EXAMPLE/other> <#foo>)))", r) ;
    }

    @Test public void testBase_07()
    { 
        Item r = parse("(<http://example/xyz> <http://EXAMPLE/other#foo>)") ;
        testItem("(base <http://EXAMPLE/other#> ((base <http://example/> <xyz>) <#foo>))", r) ;
    }
    
    // ----
    
    @Test public void testPrefix_01()
    { 
        Item r = Item.createNode(NodeFactory.createURI("http://example/abc")) ;
        testItem("(prefix ((ex: <http://example/>)) ex:abc)", r);
    }

    @Test public void testPrefix_02()
    { 
        Item r = Item.createNode(NodeFactory.createURI("http://EXAMPLE/abc")) ;
        testItem("(prefix ((ex: <http://example/>)) (prefix ((ex: <http://EXAMPLE/>)) ex:abc))", r);
    }
    
    @Test public void testPrefix_03()
    { 
        Item r = parse("(<http://example/abc>)") ;
        testItem("(prefix ((ex: <http://example/>)) (ex:abc))", r);
    }
    
    @Test public void testPrefix_04()
    { 
        Item r = parse("(<http://EXAMPLE/abc>)") ;
        testItem("(prefix ((ex: <http://example/>)) ( (prefix ((ex: <http://EXAMPLE/>)) ex:abc) ))", r);
    }
    
    @Test public void testPrefix_05()
    { 
        Item r = parse("(<http://example/abc>)") ;
        testItem("(prefix ((ex: <http://example/>)) ( (prefix ((x: <http://EXAMPLE/>)) ex:abc) ))", r);
    }
    
    // ---- Form
    
    @Test public void testForm_01()
    { 
        // Special form of nothing.
        Item item = parse("(prefix ((ex: <http://example/>)))") ;
        assertNull(item) ;
    }
    
    @Test public void testForm_02()
    { 
        // Special form of nothing.
        Item item = parse("(base <http://example/>)") ;
        assertNull(item) ;
    }
    // ----
    
    // URI resolving.
    
    @Test public void testTypedLit_r1()
    { 
        Node node = NodeFactory.createLiteral("3", XSDDatatype.XSDinteger) ; 
        testItem("'3'^^xsd:integer", Item.createNode(node)) ;
    }

    @Test public void testBasePrefix_01()
    { 
        Item r = parse("<http://example/abc>") ;
        testItem("(base <http://example/> (prefix ((x: <>)) x:abc) )", r);
    }
    
    private void testItem(String str, Item result)
    {
        Item item = parse(str) ;
        assertEquals(result, item) ;
    }
    
    private Item parse(String str)
    {
        Item item = ParserSSE.parse(str) ;
        return item ;
    }
}
