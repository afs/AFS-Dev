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

package projects.blkmgr;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.StrUtils ;

public class ControlBlock
{
    public static void main(String...argv)
    {
        ControlParams params = new ControlParams() ;
        params.freeChain = 10 ;
        params.sequence = 20 ;
        params.roots.add(Pair.create("foo", -1L)) ;
        params.roots.add(Pair.create("",    200L)) ;
        params.roots.add(Pair.create("bar", 300L)) ;
        
        ByteBuffer bb = ByteBuffer.allocate(1000) ;
        
        write(bb, params) ;
        bb.flip() ;
        ControlParams params2 = parse(bb) ;
        if ( !equals(params,params2))
            System.out.println("**** Different") ;
        print(params) ;
        print(params2) ;
    }
    
    
    static final int SizeofInt =    Integer.SIZE/Byte.SIZE ;
    static final int SizeofLong =   Long.SIZE/Byte.SIZE ;
    
    /* Format:
     *   Free chain start (long)
     *   Allocation sequnce number (long)
     *   Roots 
     *      (strlen, bytes, id)
     *      (int, bytes, long)
     *   -1
     */
    
    static class ControlParams
    {
        long freeChain = -1 ;
        long sequence = -1 ;
        List<Pair<String, Long>> roots = new ArrayList<>() ;
    }
    
    static boolean equals(ControlParams cp1, ControlParams cp2)
    {
        if (cp1.freeChain != cp2.freeChain) return false ;
        if (!cp1.roots.equals(cp2.roots)) return false ;
        if (cp1.sequence != cp2.sequence) return false ;
        return true ;
    }
    
    static ControlParams parse(ByteBuffer bb)
    {
        ControlParams params = new ControlParams() ;
        bb.position(0) ;
        
        params.freeChain = bb.getLong() ;
        params.sequence = bb.getLong() ;
        
        while (true)
        {
            int len = bb.getInt() ;
            if ( len < 0 ) break ;
            byte[] b = new byte[len] ;
            bb.get(b) ;
            String key = StrUtils.fromUTF8bytes(b) ;
            Long rootId = bb.getLong() ;
            params.roots.add(Pair.create(key, rootId)) ;
        }
        return params ;
    }
    
    static void print(ControlParams params)
    {
        System.out.println("free chain: " + params.freeChain) ;
        System.out.println("sequence:   " + params.sequence) ;
        for ( Pair<String, Long> r : params.roots )
            System.out.printf("  %d : %s\n", r.getRight(), r.getLeft()) ;
        System.out.println("roots = "+params.roots.size()) ;
    }
    
    static void write(ByteBuffer bb, ControlParams params)
    {
        bb.position(0) ;
        bb.putLong(params.freeChain) ;
        bb.putLong(params.sequence) ;
        for ( Pair<String, Long> r : params.roots )
        {
            byte[] b = StrUtils.asUTF8bytes(r.getLeft()) ;
            bb.putInt(b.length) ;
            bb.put(b) ;
            bb.putLong(r.getRight()) ;
        }
        bb.putInt(-1) ;
    }
}

