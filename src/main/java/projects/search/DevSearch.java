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

import java.util.Iterator;

import org.apache.jena.util.FileUtils;


public class DevSearch
{
    public static void search()
    {
        //socksProxyHost=socks-server
        // This happens to work on the public net as well because the socks-server DNS does not exist. 
        if ( System.getProperty("socksProxyHost") == null &&
             System.getProperty("http.socksProxyHost") == null &&
             System.getProperty("httpProxyHost") == null && 
             System.getProperty("http.httpProxyHost") == null )
        {
            System.setProperty("socksProxyHost", "socks-server") ;
        }
        
        String term = "sodium citrate" ;

        String[] a1 = { "--data=D.ttl", "PREFIX s: <java:search.> SELECT * { ?url s:WikipediaPF '"+term+"'}" } ;  
        String[] a2 = { "--data=D.ttl", "PREFIX s: <java:search.> SELECT * { ?url s:GooglePF '"+term+"'}" } ;
        System.out.println("Wikpedia") ;
        arq.sparql.main(a1) ;
        System.out.println("Google") ;
        arq.sparql.main(a2) ;
        System.exit(0) ;
    }

    // Call directly for debugging
    public static void search(String ... words)
    {
        SearchGoogle s = new SearchGoogle() ;
        Iterator<String> links = s.search(words) ;
        while ( links.hasNext() )
        {
            String x = links.next();
            System.out.println("Link: "+x) ;
        }
        System.out.println("--") ;
    }
    
    public static void scrapePage()
    {
        try {
            String x = FileUtils.readWholeFileAsUTF8("sodium search.htm") ;
            Iterator<String> iter = SearchWikipedia.parse(x) ;

            for ( ; iter.hasNext(); )
            {
                String z = iter.next() ;
                System.out.println(z) ;
            }

            System.out.println("------------") ;
        } catch (Exception ex)
        {
            ex.printStackTrace() ;
        }
        System.exit(0) ;
    }
}
