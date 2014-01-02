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

package projects.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A crude page scraper-based google search API.
 *  Portable.
 */ 

public class SearchGoogle extends SearchBase
{
    String serviceURL = "http://www.google.com/search?q=" ;
  
    //  http://www.google.com/search?num=100&start=0&q=
    //  num= removes results from same site
    //  100 is the maximum value for num that you can use.
    
    // Non-greedy .*
    private static Pattern pattern = Pattern.compile(".*?<h2 class=r><a href=\"([^\"]*)\"") ;

    
    @Override
    protected Iterator<String> parse(InputStream in)
    {
        // Get bored - read it all in.
        String content = readAll(in, "ISO-8859-1") ;
        return parse(content) ;
    }
     
    private Iterator<String> parse(String content)
    {    
        if ( ! content.contains("<h2 class=r>") )
            System.err.println("NOTHING") ;
        
        Matcher matcher = pattern.matcher(content); 
        ArrayList<String> links = new ArrayList<String>(20) ;
        while(matcher.find())
        {
            String link = matcher.group(1) ;
            links.add(link) ;
            if ( links.size() > 9 )
                break ;
        }
        return links.iterator() ;
    }

    protected String url(String searchExpr)
    {
        return  serviceURL+searchExpr ;
    }
    
    @Override
    protected InputStream execGet(String str)
    {
        URL target = null ;
        try {
                String qs = url(str) ;
                target = new URL(qs) ;
        }
        catch (MalformedURLException malEx)
        { throw new SearchException("Malformed URL: "+malEx) ; }
        
        try
        {
            HttpURLConnection httpConnection = (HttpURLConnection) target.openConnection();
            // By default, following 3xx redirects is true
            //conn.setFollowRedirects(true) ;
            httpConnection.setRequestProperty("Accept", "text/html") ;
            httpConnection.setRequestProperty("Accept-Charset", "utf-8") ;
            httpConnection.setRequestProperty("User-Agent", "Mozilla 9 // ARQ SPARQL Query Engine") ;
            httpConnection.setDoInput(true);
            httpConnection.connect();
            return execCommon(httpConnection);
        }
        catch (java.net.ConnectException connEx)
        { throw new SearchException("Failed to connect to remote server"); }
        catch (IOException ioEx)
        { throw new SearchException(ioEx); }
    }
   
}
