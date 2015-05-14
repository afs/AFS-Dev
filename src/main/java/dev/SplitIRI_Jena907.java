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

package dev;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.riot.system.RiotChars ;

//@RunWith(Parameterized.class)
//public class SplitIRI_Jena907
//{
//    @Parameters(name="{0} -> {1} : {2}")
//    public static Collection<Object[]> data() {
//        return Arrays.asList(new Object[][]{
//              { "http://example/foo", "http://example/", "foo" }
//            , { "http://example/foo#bar", "http://example/foo#", "bar"}
//            , { "http://example/foo#", "http://example/foo#", "" }
//            }) ;
//    }
    
public class SplitIRI_Jena907
{
    static { LogCtl.setCmdLogging(); }
//    public static void main(String ...argv) {
//    }
    


    public static String namespace(String string) {
        int i = splitpoint(string) ;
        if ( i < 0 )
            return string ;
        return string.substring(0, i) ;
    }
    
    
    /** Calculate a localname - do not escape PN_LOCAL_ESC */
    public static String localname(String string) {
        int i = splitpoint(string) ;
        if ( i < 0 )
            return "" ;
        return string.substring(i) ;
    }
    
    /** Calculate a localname - escape PN_LOCAL_ESC */
    public static String localnameEsc(String string) {
        String x = localname(string) ;
        return escape_PN_LOCAL_ESC(x) ;
    }
    
    //TODO  Turtle additional check: %XX and \ u \ U
/*
            // %  - just need to check that it is followed by two hex. 
            if ( ch == '%' ) {
                if ( i+2 >= uri.length() ) {
                    // Too short
                    return -1 ;
                }
                if ( ! checkhex(uri, i+1) || ! checkhex(uri, i+2) )
                    return -1 ;
                
                // special case.
            }
     
 */
    

    private static String escape_PN_LOCAL_ESC(String x) {
        // Assume that escapes are rare so scan once to make sure there
        // is work to do then scan again doing the work.
        //'\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
        
        int N = x.length() ;
        boolean escchar = false ;
        for ( int i = 0 ; i < N ; i++ ) {
            char ch = x.charAt(i) ;
            if ( isPN_LOCAL_ESC(ch) ) {
                escchar = true ;
                break ;
            }
        }
        if ( ! escchar )
            return x ;
        StringBuilder sb = new StringBuilder(N+10) ;
        for ( int i = 0 ; i < N ; i++ ) {
            char ch = x.charAt(i) ;
            if ( isPN_LOCAL_ESC(ch) )
                sb.append('\\') ;
            sb.append(ch) ;
        }
        return sb.toString() ; 
    }
    
    public static boolean /*RiotChars.*/isPN_LOCAL_ESC(char ch) {
        switch (ch) {
            case '\\': case '_':  case '~': case '.': case '-': case '!': case '$':
            case '&':  case '\'': case '(': case ')': case '*': case '+': case ',':
            case ';':  case '=':  case '/': case '?': case '#': case '@': case '%':
                return true ;
            default:
                return false ;
        }
    }
    
    
    /*
[136s]  PrefixedName    ::=     PNAME_LN | PNAME_NS
Productions for terminals


[163s]  PN_CHARS_BASE   ::=     [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
[164s]  PN_CHARS_U  ::=     PN_CHARS_BASE | '_'
[166s]  PN_CHARS    ::=     PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
[167s]  PN_PREFIX   ::=     PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?

[168s]  PN_LOCAL    ::=     (PN_CHARS_U | ':' | [0-9] | PLX) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX))?
[169s]  PLX     ::=     PERCENT | PN_LOCAL_ESC
[170s]  PERCENT     ::=     '%' HEX HEX
[171s]  HEX     ::=     [0-9] | [A-F] | [a-f]
[172s]  PN_LOCAL_ESC    ::=     '\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
*/
    
    // Special cases: : 
    // a:b:c is legal.
    // PLX : %
    
    
    // Try # and /, then work harder.
    // Some light URI parsing?
    
    // The local name rules are quite complicated:
    // (PN_CHARS_U | ':' | [0-9] | PLX)
    // ((PN_CHARS | '.' | ':' | PLX)*  Includes "-" and 0-9
    // (PN_CHARS | ':' | PLX))?
    
    /** Find the URI split point, return the index into the string that is the
     * first character of a legal Turtle local name.   
     * <p>
     * This is a pragmatic choice, not just finding the maximal point.
     * For example, with escaping '/' can be included but that means 
     * {@code http://example/path/abc} could split to give {@code http://example/}
     * and {@code path/abc} .  
     *   
     * @param uri URI string
     * @return The split point, or -1 for "not found".
     */
    
    static int splitpoint(String uri) {
        boolean isURN = uri.startsWith("urn:") ;
        // Fast track.  Still need to check validity of the prefix part.
        int idx1 = uri.lastIndexOf('#') ;
//        if ( idx1 >= 0 ) {
//            // If legal URI.
//            return idx1+1 ;
//        }
//        
        // Not so simple - \/ in local names 
        int idx2 = 
            isURN ? uri.lastIndexOf(':') : uri.lastIndexOf('/') ;
//        if ( idx2 >= 0 ) {
//            if ( idx1 < 0 || idx2 > idx1 ) 
//                // If legal URI.
//                return idx2+1 ;
//        }

        // If absolute.
        int idx3 = uri.indexOf(':') ; 

            
        // Test the discovered local part.
        // Limit is exclusive.
        int limit = Math.max(idx1, idx2) ;
        limit = Math.max(limit, idx3) ;
        limit = Math.max(-1, limit) ;
        
        // Work harder.
        int splitPoint = -1 ;
        for ( int i = uri.length()-1 ; i > limit ; i-- ) {
            char ch = uri.charAt(i) ;
            
            if ( RiotChars.isPNChars_U_N(ch) || isPN_LOCAL_ESC(ch) || ch == ':' || ch == '-' || ch == '.' ) 
                continue ;
            splitPoint = i+1 ;
            break ;
        }
        // limit was at the end.  No split point (we could escape the limit point)
        if ( splitPoint == -1 )
            splitPoint = limit+1 ;
        // No split point.
        if ( splitPoint >= uri.length() )
            return -1 ;
        
        // Check the first character of the local name.
        // All character are legal localname name characters but may not satisfy the additional
        // first character rule.  Move forward to first legal first character.    
        int ch = uri.charAt(splitPoint) ;
        while ( ch == '.' || ch == '-' ) {
            splitPoint++ ;
            if ( splitPoint >= uri.length() )
                return -1 ;
            ch = uri.charAt(splitPoint) ;
        }
        
        // Check the last.  Not a dot.
        // This could be done earlier.
        ch = uri.charAt(uri.length()-1) ;
        if ( ch == '.' )
            return -1 ;
        
        return splitPoint ;
    }
    
    private static boolean checkhex(String uri, int i) {
        return RiotChars.isHexChar(uri.charAt(i)) ;
    }
    
    
    
    
/*
[141s]  BLANK_NODE_LABEL    ::=     '_:' (PN_CHARS_U | [0-9]) ((PN_CHARS | '.')* PN_CHARS)?
[144s]  LANGTAG     ::=     '@' [a-zA-Z]+ ('-' [a-zA-Z0-9]+)*
[19]    INTEGER     ::=     [+-]? [0-9]+
[20]    DECIMAL     ::=     [+-]? [0-9]* '.' [0-9]+
[21]    DOUBLE  ::=     [+-]? ([0-9]+ '.' [0-9]* EXPONENT | '.' [0-9]+ EXPONENT | [0-9]+ EXPONENT)
[154s]  EXPONENT    ::=     [eE] [+-]? [0-9]+
[22]    STRING_LITERAL_QUOTE    ::=     '"' ([^#x22#x5C#xA#xD] | ECHAR | UCHAR)* '"'
[23]    STRING_LITERAL_SINGLE_QUOTE     ::=     "'" ([^#x27#x5C#xA#xD] | ECHAR | UCHAR)* "'"
[24]    STRING_LITERAL_LONG_SINGLE_QUOTE    ::=     "'''" (("'" | "''")? ([^'\] | ECHAR | UCHAR))* "'''"
[25]    STRING_LITERAL_LONG_QUOTE   ::=     '"""' (('"' | '""')? ([^"\] | ECHAR | UCHAR))* '"""'
[26]    UCHAR   ::=     '\ u' HEX HEX HEX HEX | '\U' HEX HEX HEX HEX HEX HEX HEX HEX
[159s]  ECHAR   ::=     '\' [tbnrf"'\]
[161s]  WS  ::=     #x20 | #x9 | #xD | #xA
[162s]  ANON    ::=     '[' WS* ']'
 
 */
    
    
}

