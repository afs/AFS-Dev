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

/** Use Google's search site feature. */
public class SearchSite extends SearchGoogle 
{
    String siteName ="http;//jena.sf.net/" ;
    
    public SearchSite(String site)
    {
        this.siteName  = site ; 
    }

    //"site:SITE"
    @Override
    protected String url(String searchExpr)
    {
        return  serviceURL+searchExpr+"%20site:"+siteName  ;
    }
}
