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

package langtag;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Locale ;

import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.riot.system.RiotChars ;

/** replace LangTag -- uses avoids using regexs */
public class LangTag2 {
    // TODO
    // Exception vs "return null"
    
    static class LangTagException extends RuntimeException {
        LangTagException(String str) { super(str) ; }
    }
    
    /** Index of the language part */
    public static final int idxLanguage     = 0 ;
    /** Index of the script part */ 
    public static final int idxScript       = 1 ;
    /** Index of the region part */
    public static final int idxRegion       = 2 ;
    /** Index of the variant part */
    public static final int idxVariant      = 3 ;
    /** Index of all extensions */
    public static final int idxExtension    = 4 ;
    
    private static final int partsLength    = 5 ;

    /*
     *     <li>ABNF definition: <a href="http://www.ietf.org/rfc/rfc4234.txt">RFC 4234</a></li>

   Language-Tag  = langtag
                 / privateuse             ; private use tag
                 / grandfathered          ; grandfathered registrations

   langtag       = (language
                    ["-" script]
                    ["-" region]
                    *("-" variant)
                    *("-" extension)
                    ["-" privateuse])

   language      = (2*3ALPHA [ extlang ]) ; shortest ISO 639 code
                 / 4ALPHA                 ; reserved for future use
                 / 5*8ALPHA               ; registered language subtag

   extlang       = *3("-" 3ALPHA)         ; reserved for future use

   script        = 4ALPHA                 ; ISO 15924 code

   region        = 2ALPHA                 ; ISO 3166 code
                 / 3DIGIT                 ; UN M.49 code

   variant       = 5*8alphanum            ; registered variants
                 / (DIGIT 3alphanum)

   extension     = singleton 1*("-" (2*8alphanum))

   singleton     = %x41-57 / %x59-5A / %x61-77 / %x79-7A / DIGIT
                 ; "a"-"w" / "y"-"z" / "A"-"W" / "Y"-"Z" / "0"-"9"
                 ; Single letters: x/X is reserved for private use

   privateuse    = ("x"/"X") 1*("-" (1*8alphanum))

   grandfathered = 1*3ALPHA 1*2("-" (2*8alphanum))
                   ; grandfathered registration
                   ; Note: i is the only singleton
                   ; that starts a grandfathered tag

   alphanum      = (ALPHA / DIGIT)       ; letters and numbers
     */

    static String[] template = new String[] {} ; 
    
    public static String[] parse(String x) { 
        List<String> strings = parse1(x) ;
        if ( strings == null )
            return null ;
        String[] parts = parse2(strings) ;
        if ( parts == null )
            return null ;
        parts[idxLanguage] = lowercase(parts[idxLanguage]) ;
        parts[idxScript] = strcase(parts[idxScript]) ;
        parts[idxRegion] = strcase(parts[idxRegion]) ;
        parts[idxVariant] = strcase(parts[idxVariant]) ;
        // Leave extensions alone.
        // parts[idxExtension] = strcase(parts[idxExtension]) ; 
        return parts ;
    }

    private static List<String> parse1(String x) {
        List<String> strings = new ArrayList<>() ;
        // Split efficiently(?) based on [a-z][A-Z][0-9] units separated by "-"s
        StringBuilder sb = new StringBuilder() ;
        
        boolean start = true ;
        for ( int idx = 0 ; idx < x.length() ; idx++ ) {
            char ch = x.charAt(idx) ;
            if ( RiotChars.isA2ZN(ch) ) {
                sb.append(ch) ;
                continue ;
            }
            if ( ch == '-' ) {
                String str = sb.toString() ;
                strings.add(str) ;
                sb.setLength(0) ;
                continue ;
            }
            return null ;
            //throw new LangTagException(String.format("Bad character: (0x%02X) '%c' index %d", (int)ch, Character.valueOf(ch), idx)) ;
        }
        String strLast = sb.toString() ;
        if ( strLast.isEmpty() ) {
            return null ;
            //throw new LangTagException("Empty part: "+x) ;
        }
        strings.add(strLast) ;
        return strings ;
    }
    
    /*
     * Having broken the string up, use the size rules
     * to place them into the right places.
     */
    private static String[] parse2(List<String> strings) {    
        if ( strings.size() == 0 ) {
            return null ;
            //throw new LangTagException("No parts found") ;
        }
        
        String [] langTag = new String[partsLength] ;
        int idx = 0 ;
        // language - mandatory.
        //language = (2*3ALPHA [ extlang ]) or 4ALPHA or 5*8ALPHA
        
        String str = strings.get(idx) ;
        if ( str.length() == 1 ) {
            // privateuse
            langTag[idxLanguage] = str ;
            langTag[idxExtension] = theRest(strings, 1) ;
            return langTag ;
        }
        
        if ( str.length() < 2 || str.length() > 8 ) {
            return null ;
            //throw new LangTagException("language part must be between 3 and 8 characters. Got: "+str) ;
        }
        langTag[idxLanguage] = str ;
        if ( ! checkAlpha(str) ) {
            return null ;
            //throw new LangTagException("Language part must contain only A-Z or a-z. Got: "+str) ;
        }
        // extlang - reserved for future use
        
        idx++ ;
        if ( idx >= strings.size() )
            return langTag ;
        str = strings.get(idx) ;
        
        // script = 4ALPHA  
        if ( str.length() == 4 ) {
            if ( ! checkAlpha(str) ) {
                return null ;
                //throw new LangTagException("Script part must contain only A-Z or a-z. Got: "+str) ;
            }
            langTag[idxScript] = str ;
            idx++ ;
            if ( idx >= strings.size() )
                return langTag ;
            str = strings.get(idx) ;
        }

        // region  = 2ALPHA or 3DIGIT   
        if ( str.length() == 2 || str.length() == 3 ) {
            langTag[idxRegion] = str ;
            idx++ ;
            if ( idx >= strings.size() )
                return langTag ;
            str = strings.get(idx) ;
        }
        
        // variant  = 5*8alphanum  or (DIGIT 3alphanum)  
        if ( str.length() >= 4 && str.length() <= 8 ) {
            langTag[idxVariant] = str ;
            idx++ ;
            if ( idx >= strings.size() )
                return langTag ;
            str = strings.get(idx) ;
        }
        
        // Extensions - just rebuild with less checking.
        langTag[idxExtension] = theRest(strings, idx) ;
        return langTag ;
    }

    private static String theRest(List<String> strings, int idx) {
        boolean first = true ;
        StringBuilder sb = new StringBuilder() ;
        for ( ; idx < strings.size() ; idx++ ) {
            if ( ! first )
                sb.append('-') ;
            else
                first = false ;
            sb.append(strings.get(idx)) ;
        }
        return sb.toString() ;
    }
    
    // Check only alphabetic.
    private static boolean checkAlpha(String str) {
        for ( int i = 0 ; i < str.length() ; i++ ) {
            char ch = str.charAt(i) ;
            if ( ! RiotChars.isA2Z(ch) )
                return false ;
        }
        return true ;
    }

    public static String canonical(String str) {
        if ( str == null )
            return null ;
        String[] parts = parse(str) ;
        String x = canonical(parts) ;
        if ( x == null ) {
            // Could try to apply the rule case-seeting rules
            // even through it's not a conforming langtag.
            return str ;
        }
        return x ;
    }
    
    /**
     * Canonicalize with the rules of RFC 4646 "In this format, all non-initial
     * two-letter subtags are uppercase, all non-initial four-letter subtags are
     * titlecase, and all other subtags are lowercase." In addition, leave
     * extensions unchanged.
     */
    public static String canonical(String[] parts) {
        // We canonicalised parts on parsing.
        // RFC 5646 is slightly different.
        if ( parts == null )
            return null ;

        if ( parts[0] == null ) {
            // Grandfathered
            return parts[idxExtension] ;
        }

        StringBuilder sb = new StringBuilder() ;
        sb.append(parts[0]) ;
        for (int i = 1; i < parts.length; i++) {
            if ( parts[i] != null ) {
                sb.append("-") ;
                sb.append(parts[i]) ;
            }
        }
        return sb.toString() ;
    }
    
    
    /**
     * Validate - basic syntax check for a language tags: [a-zA-Z]+ ('-'
     * [a-zA-Z0-9]+)*
     */
    public static boolean check(String languageTag) {
        int len = languageTag.length() ;
        int idx = 0 ;
        boolean first = true ;
        while (idx < languageTag.length()) {
            int idx2 = checkPart(languageTag, idx, first) ;
            first = false ;
            if ( idx2 == idx )
                // zero length part.
                return false ;
            idx = idx2 ;
            if ( idx == len )
                return true ;
            if ( languageTag.charAt(idx) != Chars.CH_DASH )
                return false ;
            idx++ ;
            if ( idx == len )
                // trailing DASH
                return false ;
        }
        return true ;
    }

    private static int checkPart(String languageTag, int idx, boolean alphaOnly) {
        for (; idx < languageTag.length(); idx++) {
            int ch = languageTag.charAt(idx) ;
            if ( alphaOnly ) {
                if ( RiotChars.isA2Z(ch) )
                    continue ;
            } else {
                if ( RiotChars.isA2ZN(ch) )
                    continue ;
            }
            // Not acceptable.
            return idx ;
        }
        // Off end.
        return idx ;
    }
    
    private static String strcase(String string) {
        if ( string == null )
            return null ;
        if ( string.length() == 2 )
            return uppercase(string) ;
        if ( string.length() == 4 )
            return titlecase(string) ;
        return lowercase(string) ;
    }

    private static String lowercase(String string) {
        if ( string == null )
            return null ;
        return string.toLowerCase(Locale.ROOT) ;
    }

    private static String uppercase(String string) {
        if ( string == null )
            return null ;
        return string.toUpperCase(Locale.ROOT) ;
    }

    private static String titlecase(String string) {
        if ( string == null )
            return null ;
        char ch1 = string.charAt(0) ;
        ch1 = Character.toUpperCase(ch1) ;
        string = lowercase(string.substring(1)) ;
        return ch1 + string ;
    }
}
