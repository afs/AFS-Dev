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

import java.io.IOException ;
import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.io.Reader ;
import java.net.HttpURLConnection ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.NullIterator ;
import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.sparql.util.Convert ;

public abstract class SearchBase implements Search 
{
    @Override
    public final Iterator<String> search(String... words)
    {
        String str = StrUtils.strjoin(" ", words) ;
        str = Convert.encWWWForm(str) ;
        InputStream in = execGet(str) ;
        if ( in == null )
            return new NullIterator<String>() ;
        
        return parse(in) ;
    }
    
    protected abstract InputStream execGet(String searchString) ;
    protected abstract Iterator<String> parse(InputStream in) ;
    
    // Put somewhere!
    public static String readAll(InputStream in, String charset)
    {
        try(Reader r = new InputStreamReader(in, charset)) { //FileUtils.asUTF8(in)
            StringBuilder sw = new StringBuilder(64*1024);
            char buff[] = new char[8*1024];
            while (true)
            {
                int len = r.read(buff);
                if (len < 0)
                    break;
                sw.append(buff, 0, len);
            }
            return sw.toString();
        } catch (IOException ex)
        {
            throw new SearchException(ex) ;
        }
    }

    
    protected InputStream execCommon(HttpURLConnection httpConnection)
    {
        try {        
            int responseCode = httpConnection.getResponseCode() ;
            String responseMessage = Convert.decWWWForm(httpConnection.getResponseMessage()) ;
            
            // 1xx: Informational 
            // 2xx: Success 
            // 3xx: Redirection 
            // 4xx: Client Error 
            // 5xx: Server Error 
            
            if ( 300 <= responseCode && responseCode < 400 )
                throw new SearchException(responseCode+" "+responseMessage) ;
            
            // Other 400 and 500 - errors 
            if ( responseCode >= 400 )
                throw new SearchException(responseCode+" "+responseMessage) ;
  
            // Request suceeded
            InputStream in = httpConnection.getInputStream() ;
            
//            Map<String, List<String>> x = httpConnection.getHeaderFields() ;
//            for ( String f : x.keySet() )
//                System.out.println(f+" = "+x.get(f)) ;
            
            return in ;
        }
        catch (IOException ioEx)
        {
            throw new SearchException(ioEx) ;
        } 
    }
 
    
}
