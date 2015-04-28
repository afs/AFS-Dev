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

package projects.search;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.QueryExecException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.pfunction.PropFuncArgType ;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval ;

public abstract class BaseSearchPF extends PropertyFunctionEval
{
    public BaseSearchPF()
    {
        super(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_SINGLE) ;
    }

    protected abstract Search searchEngine() ;
    
    @Override
    public QueryIterator execEvaluated(final Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
                                       ExecutionContext execCxt)
    {
        // check subject is a variable.
        if ( ! argSubject.getArg().isVariable() )
            throw new QueryExecException("Subject not a variable") ;
        
        final Var var = Var.alloc(argSubject.getArg()) ;
        
        if ( ! argObject.getArg().isLiteral() )
            throw new QueryExecException("Subject not a literal") ;

        String searchTerm = argObject.getArg().getLiteralLexicalForm() ;
        Search search = searchEngine() ;
        Iterator<String> x = search.search(searchTerm) ;
        Iter<String> iter = Iter.iter(x) ;
        
        Transform<String , Binding> converter = new Transform<String , Binding>(){

            @Override
            public Binding convert(String item)
            {
                return BindingFactory.binding(binding, var, NodeFactory.createURI(item)) ;
            }} ;
        QueryIterator qIter = new QueryIterPlainWrapper(iter.map(converter)) ;
        return qIter ;
    }

}
