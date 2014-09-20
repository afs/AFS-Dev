/*
 *  Copyright 2014 Andy Seaborne
 *
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
 */

package tdbconf;

import java.io.File ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.typesafe.config.Config ;
import com.typesafe.config.ConfigFactory ;

public class TDBConfReader {
    public static void main(String... argv) throws Exception {
        Config conf = ConfigFactory.parseFile(new File("tdb.conf")) ;
        System.out.println(conf) ;
        
        long z = conf.getBytes("tdb.store.blocksize") ; 
        System.out.println(z) ;
        String x = conf.getString("tdb.segment_size") ;
        Expr expr = ExprUtils.parse(x, null) ;
        FunctionEnv env = new ExecutionContext(ARQ.getContext(), null, null, null) ; 
        NodeValue r = expr.eval(null, env) ;
        long val = r.getInteger().longValue() ;
        System.out.println(val) ;
//        Node n = r.asNode() ;
//        String s = NodeFmtLib.displayStr(n) ;
//        System.out.println(s) ;
    }
}