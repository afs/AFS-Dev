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

package element ;

import org.apache.jena.atlas.lib.InternalErrorException ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprTransform ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

public class TransEltLib {
    public static Var applyVar(Var v, ExprTransform exprTransform) {
        ExprVar expr = new ExprVar(v) ;
        Expr e = exprTransform.transform(expr) ;
        if ( e instanceof ExprVar )
            return ((ExprVar)e).asVar() ;
        throw new InternalErrorException("Managed to turn a variable " + v + " into " + e) ;
    }

    public static Node apply(Node n, ExprTransform exprTransform) {
        Expr e = null ;
        if ( Var.isVar(n) ) {
            Var v = Var.alloc(n) ;
            ExprVar expr = new ExprVar(v) ;
            e = exprTransform.transform(expr) ;
        } else {
            NodeValue nv = NodeValue.makeNode(n) ;
            e = exprTransform.transform(nv) ;
        }

        if ( e instanceof ExprVar )
            return ((ExprVar)e).asVar() ;
        if ( e instanceof NodeValue )
            return ((NodeValue)e).asNode() ;
        throw new InternalErrorException("Managed to turn a node " + n + " into " + e) ;
    }

}
