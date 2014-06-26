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

package element ;

import java.util.ArrayDeque ;
import java.util.Deque ;
import java.util.List ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.ExprTransform ;
import com.hp.hpl.jena.sparql.expr.ExprTransformer ;
import com.hp.hpl.jena.sparql.syntax.* ;

/** A bottom-up application of a transformation of SPARQL syntax Elements. 
 * {@linkplain QueryTransformOps#transform} provides the mechanism
 * to apply to a {@linkplain Query}.
 * @see UpdateTransformOps#transform
 */
public class ElementTransformer {
    private static ElementTransformer singleton = new ElementTransformer() ;

    /** Get the current transformer */
    public static ElementTransformer get() {
        return singleton ;
    }

    /** Set the current transformer - use with care */
    public static void set(ElementTransformer value) {
        ElementTransformer.singleton = value ;
    }

    /** Transform an algebra expression */
    public static Element transform(Element element, ElementTransform transform) {
        return transform(element, transform, null, null, null) ;
    }

    /** Transformation with specific ElementTransform and ExprTransform */
    public static Element transform(Element element, ElementTransform transform, ExprTransform exprTransform) {
        return get().transformation(element, transform, exprTransform, null, null) ;
    }

    public static Element transform(Element element, ElementTransform transform, ExprTransform exprTransform,
                                    ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        return get().transformation(element, transform, exprTransform, beforeVisitor, afterVisitor) ;
    }

    // To allow subclassing this class, we use a singleton pattern
    // and these protected methods.
    protected Element transformation(Element element, ElementTransform transform, ExprTransform exprTransform,
                                     ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        ApplyTransformVisitor v = new ApplyTransformVisitor(transform, exprTransform) ;
        return transformation(v, element, beforeVisitor, afterVisitor) ;
    }

    protected Element transformation(ApplyTransformVisitor transformApply, Element element,
                                     ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        if ( element == null ) {
            Log.warn(this, "Attempt to transform a null element - ignored") ;
            return element ;
        }
        return applyTransformation(transformApply, element, beforeVisitor, afterVisitor) ;
    }

    /** The primitive operation to apply a transformation to an Op */
    protected Element applyTransformation(ApplyTransformVisitor transformApply, Element element,
                                          ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        ElementWalker.walk(element, transformApply) ; // , beforeVisitor,
                                                      // afterVisitor) ;
        Element r = transformApply.result() ;
        return r ;
    }

    protected ElementTransformer() {}

    static class ApplyTransformVisitor implements ElementVisitor {
        protected final ElementTransform transform ;
        private final ExprTransform      exprTransform ;

        private final Deque<Element>     stack = new ArrayDeque<Element>() ;

        protected final Element pop() {
            return stack.pop() ;
        }

        protected final void push(Element elt) {
            stack.push(elt) ;
        }

        protected final void pushChanged(Element elt, Element elt2) {
            if ( elt == elt2 )
                stack.push(elt) ;
            else
                stack.push(elt2) ;
        }

        public ApplyTransformVisitor(ElementTransform transform, ExprTransform exprTransform) {
            this.transform = transform ;
            this.exprTransform = exprTransform ;
        }

        final Element result() {
            if ( stack.size() != 1 )
                Log.warn(this, "Stack is not aligned") ;
            return pop() ;
        }

        @Override
        public void visit(ElementTriplesBlock el) {
            Element el2 = transform.transform(el) ;
            pushChanged(el, el2) ;
        }

        @Override
        public void visit(ElementPathBlock el) {
            Element el2 = transform.transform(el) ;
            pushChanged(el, el2) ;
        }

        @Override
        public void visit(ElementFilter el) {
            Expr expr = el.getExpr() ;
            Expr expr2 = transform(expr, exprTransform) ;
            Element el2 = transform.transform(el, expr2) ;
            pushChanged(el, el2) ;
        }

        @Override
        public void visit(ElementAssign el) {
            Var v = el.getVar() ;
            Var v1 = TransEltLib.applyVar(v, exprTransform) ;
            Expr expr = el.getExpr() ;
            Expr expr1 = ExprTransformer.transform(exprTransform, expr) ;
            if ( v == v1 && expr == expr1 ) {
                push(el) ;
                return ;
            }
            push(new ElementAssign(v1, expr1)) ;
        }

        @Override
        public void visit(ElementBind el) {
            Var v = el.getVar() ;
            Var v1 = TransEltLib.applyVar(v, exprTransform) ;
            Expr expr = el.getExpr() ;
            Expr expr1 = ExprTransformer.transform(exprTransform, expr) ;
            if ( v == v1 && expr == expr1 ) {
                push(el) ;
                return ;
            }
            push(new ElementBind(v1, expr1)) ;
        }

        @Override
        public void visit(ElementData el) {
            push(el) ;
        }

        @Override
        public void visit(ElementOptional el) {
            Element elSub = el.getOptionalElement() ;
            Element elSub2 = pop() ;
            Element el2 = (elSub == elSub2) ? el : new ElementOptional(elSub2) ;
            pushChanged(el, el2) ;
        }

        @Override
        public void visit(ElementGroup el) {
            ElementGroup newElt = new ElementGroup() ;
            boolean b = transformFromTo(el.getElements(), newElt.getElements()) ;
            if ( b )
                push(newElt) ;
            else
                push(el) ;
        }

        @Override
        public void visit(ElementUnion el) {
            ElementUnion newElt = new ElementUnion() ;
            boolean b = transformFromTo(el.getElements(), newElt.getElements()) ;
            if ( b )
                push(newElt) ;
            else
                push(el) ;
        }

        private boolean transformFromTo(List<Element> elts, List<Element> elts2) {
            boolean changed = false ;
            for (Element elt : elts) {
                Element elt2 = pop() ;
                changed = (changed || (elt != elt2)) ;
                // Add reversed.
                elts2.add(0, elt2) ;
            }
            return changed ;
        }

        @Override
        public void visit(ElementDataset el) {
            push(el) ;
            // TODO
            // el.getPatternElement() ;
        }

        @Override
        public void visit(ElementNamedGraph el) {
            Node n = el.getGraphNameNode() ;
            Node n1 = TransEltLib.apply(n, exprTransform) ;
            Element elt = el.getElement() ;
            Element elt1 = pop() ;
            if ( n == n1 && elt == elt1 )
                push(el) ;
            else
                push(new ElementNamedGraph(n1, elt1)) ;
        }

        @Override
        public void visit(ElementExists el) {
            Element elt = el.getElement() ;
            Element elt1 = subElement(elt) ;
            if ( elt == elt1 )
                push(el) ;
            else
                push(new ElementExists(elt1)) ;
        }

        @Override
        public void visit(ElementNotExists el) {
            Element elt = el.getElement() ;
            Element elt1 = subElement(elt) ;
            if ( elt == elt1 )
                push(el) ;
            else
                push(new ElementNotExists(elt1)) ;
        }

        // When you need to force the walking of the tree ... EXISTS / NOT
        // EXISTS
        private Element subElement(Element elt) {
            ElementWalker.walk(elt, this) ;
            Element elt1 = pop() ;
            return elt1 ;
        }

        @Override
        public void visit(ElementMinus el) {
            Element elt = el.getMinusElement() ;
            Element elt1 = pop() ;
            if ( elt == elt1 )
                push(el) ;
            else
                push(new ElementMinus(elt1)) ;
        }

        @Override
        public void visit(ElementService el) {
            boolean b = el.getSilent() ;
            Node n = el.getServiceNode() ;
            Node n1 = TransEltLib.apply(n, exprTransform) ;
            Element elt = el.getElement() ;
            Element elt1 = pop() ;
            if ( n == n1 && elt == elt1 )
                push(el) ;
            else
                push(new ElementService(n1, elt1, b)) ;
        }

        @Override
        public void visit(ElementSubQuery el) {
            Query newQuery = QueryTransformOps.transform(el.getQuery(), transform, exprTransform) ;
            push(new ElementSubQuery(newQuery)) ;
        }

        // private Element transform(Element el)
        // {
        // el.visit(this) ;
        // return pop() ;
        // }

        private ExprList transform(ExprList exprList, ExprTransform exprTransform) {
            if ( exprList == null || exprTransform == null )
                return exprList ;
            return ExprTransformer.transform(exprTransform, exprList) ;
        }

        private Expr transform(Expr expr, ExprTransform exprTransform) {
            if ( expr == null || exprTransform == null )
                return expr ;
            return ExprTransformer.transform(exprTransform, expr) ;
        }
    }
}
