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

package opexec ;

import java.io.StringReader ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpFilter ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.main.OpExecutor ;
import org.apache.jena.sparql.engine.main.OpExecutorFactory ;
import org.apache.jena.sparql.engine.main.QC ;

/**
 * Example skeleton for a query engine. To just extend ARQ by custom basic graph
 * pattern matching (a very common case) see the arq.examples.bgpmatching
 * package
 */

public class OpExecutorExample // extends QueryEngineMain
{
    // UNFINISHED
    // Check where OpExecutorFactory.create happens.

    /*
     * To install a custom OpExecutor, the application needs
     * 
     * 
     * The example MyQueryEngine shows how to take over the execution of a
     * SPARQL algebra expression. This allows customization of optimizations
     * running before query execution starts.
     * 
     * An OpExecutor controls the running of an algebra expression. An executor
     * needs to cope with the fact a dataset might be composed of a mixture of
     * graphs, and that it might be be being called for any kind of storage
     * unit, not just one it is designed for.
     * 
     * Thsi is done by having a chain (via subclassing) of OpExecutors, with the
     * base class being hthe general purpose one for ARQ that can operate on any
     * data storage layer.
     */

    static void init() {
        // Wire the new factory into the system.
        ARQ.init() ;
        // *** Where is the factory choosen?
        OpExecutorFactory current = QC.getFactory(ARQ.getContext()) ;
        // maybe null
        QC.setFactory(ARQ.getContext(), new MyOpExecutorFactory(current)) ;
    }

    public static void main(String... argv) {
        LogCtl.setLog4j2() ;
        init() ;
        Model m = data() ;

        String s = "SELECT DISTINCT ?s { ?s ?p ?o FILTER (?o=12) } " ;
        Query query = QueryFactory.create(s) ;
        try(QueryExecution qExec = QueryExecutionFactory.create(query, m)) {
            ResultSetFormatter.out(qExec.execSelect()) ;
        }
    }

    private static Model data() {
        String s = StrUtils.strjoinNL("<s> <p> 12 .", "<s> <p> 15 .") ;
        Model m = ModelFactory.createDefaultModel() ;
        m.read(new StringReader(s), null, "TTL") ;
        return m ;
    }

    // This is a simple example.
    // For execution logging, see:
    // http://openjena.org/wiki/ARQ/Explain
    // which printout more information.
    static class MyOpExecutor extends OpExecutor
    {
        protected MyOpExecutor(ExecutionContext execCxt)
        {
            super(execCxt) ;
        }

        @Override
        protected QueryIterator execute(OpBGP opBGP, QueryIterator input) {
            System.out.print("Execute: " + opBGP) ;
            // This is an illustration - it's a copy of the default
            // implementation
            BasicPattern pattern = opBGP.getPattern() ;
            return stageGenerator.execute(pattern, input, execCxt) ;
        }

        @Override
        protected QueryIterator execute(OpFilter opFilter, QueryIterator input) {
            System.out.print("Execute: " + opFilter) ;
            return super.execute(opFilter, input) ;
        }
    }

    /** A factory to make OpExecutors */
    static class MyOpExecutorFactory implements OpExecutorFactory
    {
        private final OpExecutorFactory other ;

        public MyOpExecutorFactory(OpExecutorFactory other)
        {
            this.other = other ;
        }

        @Override
        public OpExecutor create(ExecutionContext execCxt) {
            return new MyOpExecutor(execCxt) ;
        }
    }
}
