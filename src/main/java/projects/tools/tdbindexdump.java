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

import tdb.cmdline.CmdTDB ;
import arq.cmd.CmdException ;

import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

/** Dump a TDB index.
 */
public class tdbindexdump extends CmdTDB
{
    static public void main(String... argv)
    { 
        TDB.setOptimizerWarningFlag(false) ;
        new tdbindexdump(argv).mainRun() ;
    }
    
    protected tdbindexdump(String[] argv)
    {
        super(argv) ;
    }        
    
    @Override
    protected String getSummary()
    {
        return getCommandName()+"-loc DIR IndexName" ;
    }

    @Override
    protected void exec()
    {
        Location location = super.getLocation() ;
        if ( location == null )
            throw new CmdException("No location") ;

        List<String> args = super.getPositional() ;

        if ( args.size() == 0 || args.size() > 1 )
            throw new CmdException("Wrong number of arguments (expected 1; got "+args.size()+")") ; 

        String index = args.get(0) ;
        String primaryIndex ;

        if ( index.length() != 3  && index.length() != 4 )
            throw new CmdException("Index '"+index+"' must be of length 3 or 4") ;
        if ( index.length() == 3 )
            primaryIndex = Names.primaryIndexTriples ;
        else
            primaryIndex = Names.primaryIndexQuads ;
        int keyLen = NodeId.SIZE*primaryIndex.length() ;

        
        index = index.toUpperCase() ;
        TupleIndex tupleIndex = SetupTDB.makeTupleIndex(location, primaryIndex, index, index, keyLen) ;
        
        TupleIndexRecord tupleIndexRecord = (TupleIndexRecord)tupleIndex ;
        
        IndexLib.dumpTupleIndex(tupleIndex) ;
        System.out.println() ;
        IndexLib.dumpRangeIndex(tupleIndexRecord.getRangeIndex()) ;
    }

    private static TupleIndex find(TupleIndex[] indexes, String srcIndex)
    {
        for ( TupleIndex idx : indexes )
        {
            // Index named by simple "POS"
            if ( idx.getName().equals(srcIndex) )
                return idx ;
            
            // Index named by column mapping "SPO->POS"
            // This is silly.
            int i = idx.getColumnMap().getLabel().indexOf('>') ;
            String name = idx.getMapping().substring(i+1) ;
            if ( name.equals(srcIndex) )
                return idx ;
        }
        return null ;
    }
}

