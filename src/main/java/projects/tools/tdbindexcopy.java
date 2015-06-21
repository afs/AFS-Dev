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

package projects.tools;

import java.util.List ;

import jena.cmd.CmdException ;

import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.store.tupletable.TupleIndex ;
import org.apache.jena.tdb.sys.Names ;

import tdb.cmdline.CmdTDB ;

/** Create a TDB index.
 *  Defaults to assuming the "primary" index (SPO or GSPO) already exists.
 */
public class tdbindexcopy extends CmdTDB
{
    static public void main(String... argv)
    { 
        TDB.setOptimizerWarningFlag(false) ;
        new tdbindexcopy(argv).mainRun() ;
    }

    protected tdbindexcopy(String[] argv)
    {
        super(argv) ;
    }        
    
    @Override
    protected String getSummary()
    {
        return getCommandName()+"-loc DIR [SrcIndex] DestIndex" ;
    }

    @Override
    protected void exec()
    {
        List<String> args = super.getPositional() ;

        if ( args.size() == 0 || args.size() > 2 )
            throw new CmdException("Wrong number of arguments (expected 1 or 2; got "+args.size()+")") ; 

        String srcIndexName ;
        String destIndexName ;

        if ( args.size () == 2 )
        {
            srcIndexName = args.get(0) ;
            destIndexName = args.get(1) ;
        }
        else
        {
            srcIndexName = null ;
            destIndexName = args.get(0) ;
        }

        
        String primaryIndexName ;
        
        if ( destIndexName.length() == 3 )
            primaryIndexName = Names.primaryIndexTriples ;
        else
            primaryIndexName = Names.primaryIndexQuads ;

        
        if ( srcIndexName.length() != 3 && srcIndexName.length() != 4 )
            throw new CmdException("Wrong length of index name: "+srcIndexName) ;
        if ( srcIndexName.length() != destIndexName.length() )
            throw new CmdException("Index names do not match: "+srcIndexName+" and "+destIndexName) ;
        
        srcIndexName = srcIndexName.toUpperCase() ;
        destIndexName = destIndexName.toUpperCase() ;
        Location location = super.getLocation() ; 
        
        TupleIndex srcIndex = IndexLib.connect(location, primaryIndexName, srcIndexName) ;
        TupleIndex destIndex = IndexLib.connect(location, primaryIndexName, destIndexName) ;
       
        IndexLib.copyIndex(srcIndex, destIndex) ;
    }
}

