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

package projects.blkmgr.virtual;

public class DiskBlock
{
    /* public long vIdx; */
    public final long location ;
    public final long length ;

    public DiskBlock(long location, long length)
    {
        super() ;
        this.location = location ;
        this.length = length ;
    }

    // Valu equality.
    @Override
    public int hashCode()
    {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + (int)(length ^ (length >>> 32)) ;
        result = prime * result + (int)(location ^ (location >>> 32)) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true ;
        if (obj == null) return false ;
        if (getClass() != obj.getClass()) return false ;
        DiskBlock other = (DiskBlock)obj ;
        if (length != other.length) return false ;
        if (location != other.location) return false ;
        return true ;
    }
}
