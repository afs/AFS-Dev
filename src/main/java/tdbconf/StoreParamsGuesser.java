/*
 *  Copyright 2013, 2014 Andy Seaborne
 *
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
 */

package tdbconf;

import java.io.File ;
import java.io.FileFilter ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.Arrays ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Action ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.MultiMap ;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.StoreParams ;
import com.hp.hpl.jena.tdb.sys.Names ;

/** Build a TDB system parameters object based on a location */  
public class StoreParamsGuesser {
    
    private static final String ConfigFileName = "tdb.conf" ;
    
    private static List<String> indexesExts = Arrays.asList(Names.bptExtTree, Names.bptExtRecords) ;
    private static List<String> directExts = Arrays.asList(Names.bptExtRecords) ;
    
    public static StoreParams configure(Location loc) {
        StoreParams params = null ;
        if ( loc.isMem() ) {
            // Can't
            throw new IllegalArgumentException("Can only build parameters from a disk location") ; 
        }
        
        String dir = loc.getDirectoryPath() ;
        Path path = Paths.get(dir) ;
        
        if ( loc.exists(ConfigFileName) ) {
            System.out.println(ConfigFileName+"2 found") ;
        }
        
        // All the non-hidden files, with a dot in their name.
        FileFilter fnf = new FileFilter() {
            @Override
            public boolean accept(File file) {
                String fn = file.getName() ;
                if ( fn.startsWith(".") )
                    return false ;
                if ( ! fn.contains(".") )
                    return false ;
                if ( file.isDirectory() )
                    return false ;
                return true ;
            }
        };

        // Break up file names.
        List<File> files = Arrays.asList(path.toFile().listFiles(fnf)) ;
        
        // Name to extensions found.
        final MultiMap<String, String> fileParts = MultiMap.createMapList() ;
        Action<File> splitter = new Action<File>() {
            @Override
            public void apply(File item) {
                String name = item.getName() ;
                String base = FileOps.basename(name) ;
                String ext = FileOps.extension(name) ;
                fileParts.put(base, ext) ;
            }
        } ;
        
        // Map base name to extensions found 
        Iter.apply(files, splitter) ;
        
        List<String> indexesTriples = DS.list() ;
        List<String> indexesQuads = DS.list() ;
        
        // Index seeking.
        for ( String base : fileParts.keys() ) {
            List<String> exts = (List<String>)fileParts.get(base) ;
            
            if ( base.length() == 3 && tripleIndex(base, exts) ) {
                indexesTriples.add(base) ;
                continue ;
            }
            if ( base.length() == 4 && quadIndex(base, exts) ) {
                indexesQuads.add(base) ;
                continue ;
            }
        }

        String[] dummy = {} ;
        String tripleIndexes[] = indexesTriples.toArray(dummy) ;
        String quadIndexes[] = indexesQuads.toArray(dummy) ;
            
        return params ;
    }


    private static boolean tripleIndex(String base, List<String> extensions) {
        if ( extensions == null ) return false ; 
        //if ( base.length() != 3 ) return false ;
        if ( ! base.matches("^[SPO]{3}$") ) return false ;
        return checkParts(base, extensions, indexesExts) ;
    }
    
    private static boolean quadIndex(String base, List<String> extensions) {
        if ( extensions == null ) return false ; 
        //if ( base.length() != 4 ) return false ;
        if ( ! base.matches("^[GSPO]{4}$") ) return false ;
        return checkParts(base, extensions, indexesExts) ;
    }

    private static boolean checkParts(String base, List<String> extensions, List<String> expected) {
        if ( extensions.size() != expected.size()) {
            System.out.printf("Warning: bad index: %s extensions=%s expected=%s\n", base, extensions, expected) ;
            return false;
        }
        
        // Assumes extensions are not repeated.
        for ( String ext : extensions ) {
            if ( expected.contains(ext) ) continue ;
            System.out.printf("Warning: bad index: %s extensions=%s expected=%s\n", base, extensions, expected) ;
            return false ;
        }
        return true ;
    }

    
}