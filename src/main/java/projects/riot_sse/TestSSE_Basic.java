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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.sse.Item ;
import org.apache.jena.sparql.sse.ItemException ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.junit.Test ;

public class TestSSE_Basic extends BaseTest
{
    // Tests not requiring URI resolution or prefix name handling.
    
    static Node int1 = org.apache.jena.graph.NodeFactory.createLiteral("1", XSDDatatype.XSDinteger) ;
    static Node int2 = org.apache.jena.graph.NodeFactory.createLiteral("2", XSDDatatype.XSDinteger) ;
    static Node int3 = org.apache.jena.graph.NodeFactory.createLiteral("3", XSDDatatype.XSDinteger) ;
    static Node strLangEN = org.apache.jena.graph.NodeFactory.createLiteral("xyz", "en") ;

    static Node typeLit1 = NodeFactoryExtra.createLiteralNode("123", null, "http://example/type") ;
    
    static Item int1i = Item.createNode(int1) ;
    static Item int2i = Item.createNode(int2) ;
    static Item int3i = Item.createNode(int3) ;
    
    // ---- Parsing : not check for the correct outcome
    @Test public void testParseTerm_01() { parse("'xyz'") ; }
    @Test public void testParseTerm_02() { parse("'xyz'@en") ; }
    
    @Test(expected=RiotException.class)
    public void testParseTerm_03() { parse("'xyz' @en") ; }

    @Test public void testParseSymbol_01() { parse("a") ; }
    
    @Test(expected=RiotException.class)
    public void testParseSymbol_02() { parse("'a") ; }
    // TODO Parser needs fixing
    @Test public void testParseSymbol_03() { parse("@a") ; }
    @Test(expected=RiotException.class)
    public void testParseSymbol_04() { parse("a@") ; }
    
    
    @Test public void testParseList_01() { parse("()") ; }
    @Test public void testParseList_02() { parse("(a)") ; }
    @Test public void testParseList_03() { parse(" (a)") ; }
    @Test public void testParseList_04() { parse("( a)") ; }
    @Test public void testParseList_05() { parse("(a )") ; }
    @Test public void testParseList_06() { parse("(a) ") ; }
    @Test public void testParseList_07() { parse("('a') ") ; }
    @Test public void testParseList_08() { parse("(<a>) ") ; }
    
    @Test(expected=RiotException.class) public void testParse_10() { parse("'foo' @en") ; }

    // ---- Terms 
    @Test public void testLit_01() { testNode("'foo'") ; } 
    @Test public void testLit_02() { testNode("\"foo\"") ; } 
    @Test public void testLit_03() { testNode("''") ; }
    @Test public void testLit_04() { testNode("\"\"") ; }
    @Test public void testLit_05() { testNode("'foo'@en") ; } 
    @Test(expected=RiotException.class) public void testLit_06() { parse("'foo' @en") ; } 
    @Test(expected=RiotException.class) public void testLit_07() { parse("'") ; }
    @Test(expected=RiotException.class) public void testLit_08() { parse("'\"") ; }
    @Test(expected=RiotException.class) public void testLit_09() { parse("'''") ; } 
    @Test(expected=RiotException.class) public void testLit_10() { parse("''@") ; }
    
    @Test public void testLit_11() { testNode("'''abc\\ndef'''", org.apache.jena.graph.NodeFactory.createLiteral("abc\ndef")) ; }
    
    @Test public void testLit_12()
    { 
        Node n = org.apache.jena.graph.NodeFactory.createLiteral("A\tB") ;
        testNode("'''A\\tB'''", n) ;
    }
    
    @Test public void testLit_13() { testNode("'abc\\ndef'") ; }
    
    @Test public void testNum_1() { testNode("1") ; }
    @Test public void testNum_2() { testNode("1.1") ; }
    @Test public void testNum_3() { testNode("1.0e6") ; }

    @Test public void testNum_4() { testNode("1 ", NodeConst.nodeOne) ; }
    
    @Test(expected=RiotException.class) public void testNum_5() { parseNode("1 1") ; }
 
    @Test public void testURI_1() { testNode("<http://example/base>") ; }
    @Test(expected=RiotException.class) public void testURI_2() { parseNode("http://example/baseNoDelimiters") ; }
    
    @Test public void testURI_3() { parseNode("<http://example/ space>") ; }
    
    // Four \ is two \ in the lexical URI string (java escape) is one in the URI (SSE esscape).  
    @Test public void testURI_4() { testNode("<http://example/base\\\\name>", org.apache.jena.graph.NodeFactory.createURI("http://example/base\\name")) ; }
    
    @Test public void testVar_01() { testVar("?x") ; }
    @Test public void testVar_02() { testVar("?") ; }
    @Test public void testVar_03() { testVar("?0") ; }
    // See ARQConstants.anonVarMarker
    @Test public void testVar_04() { testVar("??x") ; }
    @Test public void testVar_05() { testVar("??") ; }
    @Test public void testVar_06() { testVar("??0") ; }
    
    // See ARQConstants.allocVarMarker
    @Test public void testVar_07() { testVar("?"+ARQConstants.allocVarMarker+"0") ; }
    @Test public void testVar_08() { testVar("?"+ARQConstants.allocVarMarker) ; }

    // Default allocations
    @Test public void testVar_09()
    { 
        Node v = parseNode("?") ;
        assertTrue( v instanceof Var ) ;
        String vn = ((Var)v).getVarName() ;
        assertFalse(vn.equals("")) ;
    }
    
    @Test public void testVar_10()
    { 
        Node v = parseNode("?"+ARQConstants.allocVarAnonMarker) ;
        assertTrue( v instanceof Var ) ;
        String vn = ((Var)v).getVarName() ;
        assertFalse(vn.equals(ARQConstants.allocVarAnonMarker)) ;
    }
    
    @Test public void testWS_1() { parseNode("?x ") ; }
    @Test public void testWS_2() { parseNode(" ?x") ; }
    
    // ---- Nodes
    
    @Test public void testNode_1()    { testNode("3", int3) ; }
    @Test public void testNode_2()    { testNode("<http://example/node1>", org.apache.jena.graph.NodeFactory.createURI("http://example/node1")) ; } 
    @Test public void testTypedLit_1() { testNode("\"123\"^^<http://example/type>", typeLit1) ; }
    @Test public void testTypedLit_2() { testNode("'123'^^<http://example/type>", typeLit1) ; }
    @Test public void testTypedLit_3() { testNode("'3'^^<"+XSDDatatype.XSDinteger.getURI()+">", int3) ; }

    // --- Symbols
    
    @Test public void testSymbol_1()    { testSymbol("word") ; }
    @Test public void testSymbol_2()    { testSymbol("+") ; }
    
    @Test public void testSymbol_3()    { testSymbol("-") ; }
    
    // Tokenizer does not support these.
//    @Test public void testSymbol_5()    { testSymbol("^") ; }
//    @Test public void testSymbol_6()    { testSymbol("^^") ; }
//    @Test public void testSymbol_7()    { testSymbol("^^<foo>") ; }
    @Test public void testSymbol_8()    { testSymbol("@") ; }
    @Test public void testSymbol_9()    { testSymbol("@en") ; }
    
    // --- nil
    
    @Test public void testNil_1()    { testItem("nil", Item.nil) ; }
    @Test public void testNil_2()    { testNotItem("null", Item.nil) ; }
    @Test public void testNil_3()    { testNotItem("()", Item.nil) ; }
    @Test public void testNil_4()
    { 
        Item x = Item.createList() ;
        x.getList().add(Item.nil) ;
        testItem("(nil)", x) ;
    }

    // ---- Lists
    
    @Test public void testList_1()
    { 
        Item item = parse("()") ;
        assertTrue(item.isList()) ;
        assertEquals(item.getList().size(), 0 ) ;
    }

    @Test public void testList_2()    { testList("(1)", int1i) ; }
    @Test public void testList_3()    { testList("(1 2)", int1i, int2i) ; }
    @Test public void testList_4()    { testList("(1 a)", int1i, Item.createSymbol("a")) ; }
    
    @Test public void testList_5()
    { 
        Item list = Item.createList() ;
        list.getList().add(int1i) ;
        testList("((1) a)", list, Item.createSymbol("a")) ;
    }
    
    @Test public void testList_6()
    { testList("(+ 1)", Item.createSymbol("+"), int1i) ; }

    @Test public void testList_7()
    { testList("[+ 1]", Item.createSymbol("+"), int1i) ; }
    
    
    @Test public void testNum_01()
    { 
        Item item = parse("1") ;
        assertEquals(1, item.getInt()) ;
    }
        
    @Test public void testNum_02()
    { 
        Item item = parse("3") ;
        assertEquals(3d, item.getDouble(), 0) ;
    }

    @Test public void testNum_03()
    { 
        Item item = parse("2.5") ;      // Exact double
        assertEquals(2.5d, item.getDouble(), 0) ;
    }
    
    @Test public void testNum_04()
    { 
        Item item = parse("abc") ;
        try {
            item.getInt() ;
            fail("Succeeded where exception expected") ;
        } catch (ItemException ex) {}
    }

    @Test public void testNum_05()
    { 
        Item item = parse("<x>") ;
        try {
            item.getInt() ;
            fail("Succeeded where exception expected") ;
        } catch (ItemException ex) {}
    }
    
    
    @Test public void testMisc_01()    { testEquals("()") ; }
    @Test public void testMisc_02()    { testEquals("(a)") ; }
    @Test public void testMisc_10()    { testNotEquals("(a)", "a") ; }
    @Test public void testMisc_11()    { testNotEquals("(a)", "()") ; }
    @Test public void testMisc_12()    { testNotEquals("(a)", "(<a>)") ; }
    
    @Test public void testTaggedList_1()
    {
        Item x = Item.createTagged("TAG") ;
        assertTrue(x.isTagged()) ;
        assertTrue(x.isTagged("TAG")) ;
    }
    
    @Test public void testTaggedList_2()
    {
        Item x = Item.createTagged("TAG") ;
        Item.addPair(x.getList(), "KEY", "VALUE") ;
        
        Item y = Item.find(x.getList(), "KEY") ;
        assertNotNull(y) ;
        
        Item z = Item.find(x.getList(), "KEYKEY") ;
        assertNull(z) ;
    }
     
    // ---- Workers ----
    
    private void testEquals(String x)
    {
        Item item1 = parse(x) ;
        Item item2 = parse(x) ;
        assertTrue(item1.equals(item2)) ;
        assertTrue(item2.equals(item1)) ;
    }
    
    private void testNotEquals(String x1, String x2)
    {
        Item item1 = parse(x1) ;
        Item item2 = parse(x2) ;
        assertFalse(item1.equals(item2)) ;
        assertFalse(item2.equals(item1)) ;
    }
    
    private Item parse(String str)
    {
        Item item = ParserSSE.parse(str) ;
        return item ;
    }
    
    private Node parseNode(String str)
    {
        Item item = ParserSSE.parse(str) ;
        return item.getNode() ;
    }
    
    private void testSymbol(String str)
    {
        Item item = parse(str) ;
        assertTrue(item.isSymbol()) ;
        assertEquals(item.getSymbol(), str) ;
    }
    
    private void testList(String str, Item item1)
    {
        Item item = parse(str) ;
        assertTrue(item.isList()) ;
        
        Item i = item.getList().get(0) ;
        
        assertEquals(1, item.getList().size()) ;
        assertEquals(item.getList().get(0), item1) ;
    }

    private void testList(String str, Item item1, Item item2)
    {
        Item item = parse(str) ;
        assertTrue(item.isList()) ;
        assertEquals(2, item.getList().size()) ;
        assertEquals(item.getList().get(0), item1) ;
        assertEquals(item.getList().get(1), item2) ;
    }

    private void testList(String str, Item item1, Item item2, Item item3)
    {
        Item item = parse(str) ;
        assertTrue(item.isList()) ;
        assertEquals(3, item.getList().size()) ;
        assertEquals(item.getList().get(0), item1) ;
        assertEquals(item.getList().get(1), item2) ;
        assertEquals(item.getList().get(2), item3) ;
    }
    
    private void testItem(String str, Item result)
    {
        Item item = parse(str) ;
        assertEquals(result, item) ;
    }
    
    private void testNotItem(String str, Item result)
    {
        Item item = parse(str) ;
        assertFalse(result.equals(item)) ;
    }

    private void testNode(String str)
    {
        Node node = parseNode(str) ;
    }
    
    private void testVar(String str)
    {
        Node node = parseNode(str) ;
        assertTrue( node instanceof Var ) ;
    }
    
    private void testNode(String str, Node result)
    {
        Node node = parseNode(str) ;
        assertEquals(result, node) ;
    }

    
//    private void parse(String str)
//    {
//        try {
//            Item item = parse(str) ;
//            //System.out.println(str+" => "+item) ;
//            fail("Did not get a parse failure") ;
//        } 
//        catch (RiotParseException ex) {}
//        catch (ARQException ex) {}
//    }
//    
//    private void parseNode(String str)
//    {
//        try {
//            Node node = parseNode(str) ;
//            //System.out.println(str+" => "+item) ;
//            fail("Did not get a parse failure") ;
//        } 
//        catch (RiotParseException ex) {}
//        catch (ARQException ex) {}
//    }

}
