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

package projects.blocks ; 
// From TDB.

import java.io.ByteArrayOutputStream ;
import java.io.PrintStream ;
import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.ByteBufferLib ;

public final class Block
{
    private final Long id ;          // Keep as object to use as a cache key.
    //private BlockRef blockRef ;

    // Information carrying - these are not enforced. 
    private boolean readOnly = false ;
    private boolean modified = false ;
    
    private final ByteBuffer byteBuffer ;
    // If the byteBuffer is, say, a slice of another one,
    // this can be used to carry a ref to the real ByteBuffer.  
    private ByteBuffer underlyingByteBuffer ;

    public Block(long id, ByteBuffer byteBuffer) {
        this(Long.valueOf(id), byteBuffer) ;
    }

    public Block(Long id, ByteBuffer byteBuffer) {
        // ByteBuffer is whole disk space from byte 0 for this disk unit. 
        this.id = id ; 
        this.byteBuffer = byteBuffer ;
        //this.blockRef = null ;
        
        this.readOnly = false ;
        this.modified = false ;
        this.underlyingByteBuffer = null ;
    }
    
    public <T> T convert(BlockConverter<T> converter)
    {
        // converter.checkType(type) ; 
        return converter.fromBlock(this) ;
    }
    
    public final Long getId()   { return id ; }
    
    public final ByteBuffer getByteBuffer()
    {
        return byteBuffer ;
    }
 
    public boolean isReadOnly()     { return readOnly ; }
    public void setReadOnly(boolean readonly)
    {
        if ( readonly && modified )
            throw new BlockException("Attempt to mark a modified block as read-only") ;
        this.readOnly = readonly ;
    }
    
    public boolean isModified()     { return modified ; }

    public void setModified(boolean modified)
    {
        if ( readOnly && modified )
            throw new BlockException("Attempt to mark a readonly block as modified") ;
        this.modified = modified ;
    }

//    public BlockRef getBlockRef()   { return blockRef ; }

    public ByteBuffer getUnderlyingByteBuffer()
    { return underlyingByteBuffer ; }


    public void setUnderlyingByteBuffer(ByteBuffer underlyingByteBuffer)
    { this.underlyingByteBuffer = underlyingByteBuffer ; }
    
    @Override
    public String toString()
    {
        ByteBuffer bb = getByteBuffer() ;
        if ( true )
            // Short form.
            return String.format("Block: %d (posn=%d, limit=%d, cap=%d)", id, bb.position(), bb.limit(), bb.capacity()) ;
        // Long form - with some bytes from the ByteBuffer.
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        PrintStream x = new PrintStream(out) ;
        ByteBufferLib.print(x, bb) ;
        x.flush() ;
        String str = out.toString() ;
        return String.format("Block: %d %s", id, str) ;
    }
    
    /** Deep copy, including ByteBuffer contents into a HeapByteBuffer. */
    public Block replicate()
    {
        ByteBuffer dstBuffer = ByteBuffer.allocate(getByteBuffer().capacity());
        return replicate(dstBuffer);
    }
    
    /**
     * Deep copy, including ByteBuffer contents, using the supplied ByteBuffer to hold the contents and
     * to be used when constructing the new Block.  The capacity of the supplied ByteBuffer must be equal
     * to or greater than this block's capacity.
     */
    public Block replicate(ByteBuffer dstBuffer)
    {
        replicateByteBuffer(getByteBuffer(), dstBuffer) ;
        Block b = new Block(getId(), dstBuffer) ;
        b.modified = modified ;
        b.readOnly = readOnly ;
//        b.blockRef = null ;
        return b ;
    }

    public static void replicate(Block srcBlock, Block dstBlock)
    {
        if ( ! srcBlock.getId().equals(dstBlock.getId()) )
            throw new BlockException("FileAccessMem: Attempt to copy across blocks: "+srcBlock.getId()+" => "+dstBlock.getId()) ;
        replicate(srcBlock.getByteBuffer(), dstBlock.getByteBuffer()) ;
    }  

    private static ByteBuffer replicateByteBuffer(ByteBuffer srcBlk, ByteBuffer dstBlk)
    {
        int x = srcBlk.position() ;
        int y = srcBlk.limit() ;
        srcBlk.clear() ;
        
        if ( srcBlk.hasArray() && dstBlk.hasArray() )
            System.arraycopy(srcBlk.array(), 0, dstBlk.array(), 0, srcBlk.capacity()) ;
        else
            dstBlk.put(srcBlk) ;
        
        srcBlk.position(x);
        dstBlk.position(x);
        srcBlk.limit(y);
        dstBlk.limit(y);
        return dstBlk ; 
    }  

    private static void replicate(ByteBuffer srcBlk, ByteBuffer dstBlk)
    {
        srcBlk.position(0) ;
        dstBlk.position(0) ;
        dstBlk.put(srcBlk) ;
    }  
}
