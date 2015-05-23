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

package riot.io;

import java.io.UnsupportedEncodingException ;
import java.util.List ;

import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.TokenizerText ;


public class TokenComms
{
    public static final char endSectionMarker = 'Z' ;
    public static final char  endStreamMarker = 'Y' ;
    
    public static byte[] endSectionMarkerBytes ;
    public static  byte[] endStreamMarkerBytes  ;

    
    static
    {
        try
        {
            endSectionMarkerBytes = cntrlAsString(endSectionMarker).getBytes("ASCII") ;
            endStreamMarkerBytes = cntrlAsString(endStreamMarker).getBytes("ASCII") ;
        } catch (UnsupportedEncodingException ex)
        {   // ASCII is required
            throw new Error("ASCII encoding does nto work") ;
        }
    }
    
    //public static final String endMarkerStr = cntrlAsString(endMarker) ;

    public static String cntrlAsString(char cntrl)
    {
        return 
            Character.toString((char)TokenizerText.CTRL_CHAR)+Character.toString(cntrl);
    }
    
//    public static void sendEndMarker(TupleOutputStream output)
//    {
//        output.sendControl(endMarker) ;
////        output.startTuple() ;
////        output.sendString(endMarkerStr) ;
////        output.endTuple() ;
//    }
    
    public static boolean isEndSectionMarker(List<Token> tuple)
    {
        return isControl(tuple, endSectionMarker) ;
    }
    
    public static boolean isEndStreamMarker(List<Token> tuple)
    {
        return isControl(tuple, endStreamMarker) ;
    }
    
    public static boolean isControl(List<Token> tuple, int ctlCode)
    {
        if ( tuple.size() != 1 ) return false ; 
        if ( ! tuple.get(0).hasType(TokenType.CNTRL) )
            return false ;
        int x = tuple.get(0).getCntrlCode() ;
        if ( x == ctlCode )
            return true ; 
        return false ;
    }

}
