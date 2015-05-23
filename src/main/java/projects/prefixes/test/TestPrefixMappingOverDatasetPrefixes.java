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

package projects.prefixes.test;

import projects.prefixes.* ;

import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.graph.AbstractTestPrefixMapping2 ;

public class TestPrefixMappingOverDatasetPrefixes extends AbstractTestPrefixMapping2
{
    DatasetPrefixes dsgprefixes ;
    
    
    @Override
    protected PrefixMapping create()
    {
        dsgprefixes = PrefixesFactory.newDatasetPrefixesMem() ;
        return view() ;
    }

    @Override
    protected PrefixMapping view()
    {
        PrefixMapStorage view = PrefixMapStorageView.viewDefaultGraph(dsgprefixes) ;
        PrefixMapI pmap = PrefixesFactory.newPrefixMap(view) ;
        return PrefixesFactory.newPrefixMappingOverPrefixMapI(pmap) ;
    }
}

