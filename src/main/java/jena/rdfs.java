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

package jena;

import java.util.Iterator;

import arq.cmdline.CmdMain;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.FileManager;

/** Apply RDFS reasoning to data and schema */

public class rdfs extends CmdMain
{
    public static void main(String[] argv)
    {
        new rdfs(argv).mainRun() ;
    }
    
    private rdfs(String[] argv)
    {
        super(argv) ;
        super.add("simple", false) ;
        super.add("schema", true) ;
    }

    @Override
    protected void exec()
    {
        Model modelSchema = ModelFactory.createDefaultModel() ;
        Model modelData = ModelFactory.createDefaultModel() ;

        for ( Iterator<?> iter = super.getValues("schema").iterator(); iter.hasNext() ; )
        {
            String filename = (String)iter.next();
            FileManager.get().readModel(modelSchema, filename) ;
        }

        for ( Iterator<?> iter = super.getPositional().iterator(); iter.hasNext() ; )
        {
            String filename = (String)iter.next();
            FileManager.get().readModel(modelData, filename) ;
        }

        Reasoner reasoner = null ;
        if ( super.contains("simple") )
        {
            System.out.println("# Simple") ;
            reasoner = ReasonerRegistry.getRDFSSimpleReasoner() ;
        }
        else
            reasoner = ReasonerRegistry.getRDFSReasoner() ;
        
        
        
        // Fetch the rule set and create the reasoner
//        BuiltinRegistry.theRegistry.register(new Deduce());
//        Map prefixes = new HashMap();
//        List rules = loadRules((String)cl.getItem(0), prefixes);
//        Reasoner reasoner = new GenericRuleReasoner(rules);
        
        System.out.println("# Schema") ;
        modelSchema.write(System.out, "TTL") ;
        System.out.println() ;
        
        System.out.println("# Data") ;
        modelData.write(System.out, "TTL") ;
        System.out.println() ;
        
        InfModel infModel = ModelFactory.createInfModel(reasoner, modelSchema, modelData) ;
        infModel.prepare();
        
        System.out.println("# Find") ;
        StmtIterator sIter = infModel.listStatements(infModel.createResource("http://example/x"), null, (RDFNode)null) ;
        while(sIter.hasNext())
            System.out.println(sIter.nextStatement()) ;
        System.out.println() ;
        
        System.out.println("# Deductions") ;
        Model deductions = infModel.getDeductionsModel() ;
        deductions.write(System.out, "TTL") ;
        System.out.println() ;

        System.out.println("# All") ;
        infModel.write(System.out, "TTL") ;
        System.out.println() ;
    }


    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
    } 
}
