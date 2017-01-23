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

package langtag;

import java.util.IllformedLocaleException;
import java.util.Locale;

import org.apache.jena.atlas.lib.Chars;
import org.apache.jena.riot.system.RiotChars;

public class LangTag {
    
    public static void main(String...a) {
        try { 
            Locale.Builder locBuild = new Locale.Builder();
            //locBuild.setLanguageTag("de-CH-x-phonebk");
            locBuild.setLanguageTag("zh-CN-a-myext-x-private");
            Locale lc = locBuild.build();
            System.out.println("L:"+lc.getLanguage());
            System.out.println("S:"+lc.getScript());
            System.out.println("C:"+lc.getCountry());
            System.out.println("V:"+lc.getVariant());
            System.out.println(lc.toLanguageTag());
        } catch (IllformedLocaleException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Basic syntax check for a language tags: 
     *   [a-zA-Z]+ ('-' [a-zA-Z0-9]+)*
     */
    public static boolean checkSyntax(String languageTag) {
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
    
    // Java9 : Locale.IsoCountryCode
    
    private final String lang;
    private final String script;
    private final String region;
    private final String variant;
    private final String extension;

    public LangTag(String lang, String script, String region, String variant, String extension) {
        this.lang       = nullToEmpty(lang,      "'lang' is null");
        this.script     = nullToEmpty(script,    "'script' is null");
        this.region     = nullToEmpty(region,    "'region' is null");
        this.variant    = nullToEmpty(variant,   "'variant' is null");
        this.extension  = nullToEmpty(extension, "'extension' is null");
    }

    private static String nullToEmpty(String x, String msg) {
        // Choice.
//        if ( x == null )
//            throw new NullPointerException(msg);
        return x==null ? "" : x ; 
    }
    
    @Override
    public String toString() { return asString() ; }
    
    public String asString() {
        if ( lang == null || lang.isEmpty() ) {
            // Grandfathered
            return extension;
        }

        StringBuilder sb = new StringBuilder() ;
        sb.append(lang) ;
        appendIf(sb, script);
        appendIf(sb, region);
        appendIf(sb, variant);
        appendIf(sb, extension);
        return sb.toString() ;
    }

    private void appendIf(StringBuilder sb, String str) {
        if ( str == null || str.isEmpty() )
            return ;
        sb.append("-");
        sb.append(str);
    }

    public String getLang()         { return lang; }
    public String getScript()       { return script; }
    public String getRegion()       { return region; }
    public String getVariant()      { return variant; }
    public String getExtension()    { return extension; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((extension == null) ? 0 : extension.hashCode());
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + ((region == null) ? 0 : region.hashCode());
        result = prime * result + ((script == null) ? 0 : script.hashCode());
        result = prime * result + ((variant == null) ? 0 : variant.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        LangTag other = (LangTag)obj;
        if ( extension == null ) {
            if ( other.extension != null )
                return false;
        } else if ( !extension.equals(other.extension) )
            return false;
        if ( lang == null ) {
            if ( other.lang != null )
                return false;
        } else if ( !lang.equals(other.lang) )
            return false;
        if ( region == null ) {
            if ( other.region != null )
                return false;
        } else if ( !region.equals(other.region) )
            return false;
        if ( script == null ) {
            if ( other.script != null )
                return false;
        } else if ( !script.equals(other.script) )
            return false;
        if ( variant == null ) {
            if ( other.variant != null )
                return false;
        } else if ( !variant.equals(other.variant) )
            return false;
        return true;
    }
}
