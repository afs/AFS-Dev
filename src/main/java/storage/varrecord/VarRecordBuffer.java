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

package storage.varrecord;

import java.io.PrintStream ;
import java.nio.ByteBuffer ;
import java.util.Arrays ;

import org.apache.jena.atlas.lib.ByteBufferLib ;
import org.apache.jena.atlas.lib.InternalErrorException ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** Pack variable length items into a fixed size block */ 
public final class VarRecordBuffer
{
    // Alternative: blocked 8K writes.  No write-over boundary (or some kind of continue marker).
    // Less flexible (no delete, no simple extension to replication infrastructure)
    
    // Does this mess up node ids of 64 bits. 
    // id : block + idx in file (16 bits = 64K)
    // 64 = 8(type tag) + 40 + 16
    // Better to steal bits from the 8 byte tag.
    // 2^40 ~= 1e12 or 1 trillion
    
    /*
     * Layout:
     *   Count
     *   (i,j)*             Pairs of shorts indexing key and value byte[]
     *   unused
     *   (varint, bytes)*   Each entry is a varint (length of bytes) and the bytes
     *   
     * The bytes for entries are added from the top, downwards.
     * 
     */
    
    // Layout of variable length things.
    //     (?? Currently - no) Block type
    //     Block lengh (item count) [integer (or short?)]
    //     Ptrs for (K,V) pairs - shorts, byte offsets in the block (max block is 32k) [shorts are signed:-(]
    //       Could do ints as 2 bytes unsigned
    //     Unused area
    //     The keys and values encoded as a VarInt for the length then bytes.
    // The variable length items are added from the top as they arrive
    // They may not in stored in the variable length area in index order
    // because they may have arrived because of an insert in the middle of
    // the indexes at the time.
    
    // Overhead: num elements and the start of the variable length area (it's calculable by scanning?)
    final public static int COUNT           = 0 ;
    // Can find the start of variable area.
//    final public static int LINK            = 8 ;
    final private static int HEADER_LENGTH   = SystemTDB.SizeOfInt ;
    
    final static boolean FILL = true ;
    final static byte FILL_BYTE = (byte)0xAB ;
    final static short FILL_SHORT = (short)0xABAB ;
    
    private static final int SIZE = 100 ;
    
    // Index of byte not-in-use for var size items area.
    // Must initialize
    int numElts ;
    int startVarArea ;
    final ByteBuffer bb ;
    
    public final int LenIdxPtr = Short.SIZE / 8 ;
    public final int LenIdxSlot = 2*LenIdxPtr ;
    
    /**
     * Create a buffer for variable sized records.
     * @param sizeInBytes   Total space for records (total includes internal admin space)
     */
    
    public VarRecordBuffer(int sizeInBytes)
    {
        bb = ByteBuffer.allocate(sizeInBytes) ;
        numElts = 0 ;
        setCount(numElts) ;
        startVarArea = bb.capacity() ;
        if ( FILL )
            ByteBufferLib.fill(bb, FILL_BYTE) ;
    }
    
    private VarRecordBuffer(ByteBuffer bb)
    {
        this.bb = bb ;
        numElts = getCount() ;
        // Calc the var start.
        int varLocMin = bb.capacity() ;
        for ( int i = 0 ; i < numElts ; i++ )
        {
            int x1 = keyStartIdx(i) ; 
            int x2 = valueStartIdx(i) ;
            varLocMin = Math.min(varLocMin, x1) ;
            varLocMin = Math.min(varLocMin, x2) ;
        }
        this.startVarArea = varLocMin ;
    }
    
    public static VarRecordBuffer wrap(ByteBuffer bb)
    {
        return new VarRecordBuffer(bb) ; 
    }
    
    public ByteBuffer getByteBuffer()   { return bb ; }
    
    public int size()                   { return numElts ; }
    public int bytesInUse()              { return (HEADER_LENGTH+LenIdxSlot*numElts) + (bb.limit()-startVarArea) ; }
    
    // ---- Internal abstractions 
    /** Position of the internal index for the i'th key */ 
    private int keyIdx(int i)
    { return HEADER_LENGTH+i*LenIdxSlot ; }
    
    /** Position of the internal index for the i'th value */
    private int valueIdx(int i)
    { return HEADER_LENGTH+i*LenIdxSlot+LenIdxPtr ; }

    /** Position of the start of the bytes for the i'th key */
    private int keyStartIdx(int i)
    { 
        int idx = keyIdx(i) ;
        short start = bb.getShort(idx) ;
        return start ;
    }

    /** Position of the start of the bytes for the i'th value */
    private int valueStartIdx(int i)
    { 
        int idx = valueIdx(i) ;
        short start = bb.getShort(idx) ;
        return start ;
    }

    /** Items */ 
    private int getCount()
    { return bb.getInt(COUNT) ; }

    private void setCount(int count)
    { bb.putInt(COUNT, count) ; }

    public Record get(int idx)
    {
        check(idx) ;
        
        int k = keyStartIdx(idx) ;
        int v = valueStartIdx(idx) ;
        
        byte[] key = getBytes(k) ;
        byte[] value = getBytes(v) ;
        return new Record(key,value) ;
    }

    /** find by key - linear search - does not assume sorted */ 
    public byte[] find(byte[] key)
    {
        for ( int i = 0 ; i < numElts ; i++ )
        {
            int k = keyStartIdx(i) ;
            byte[] keyBytes = getBytes(k) ;
            if ( Arrays.equals(keyBytes, key) )
            {
                int v = valueStartIdx(i) ;
                return getBytes(v) ;
            }
        }
        return null ;
    }
    
    public Record getKey(int idx)
    {
        check(idx) ;
        
        int k = keyStartIdx(idx) ;
        int v = valueStartIdx(idx) ;
        
        System.out.printf("Get: %d (x%02X, x%02X)\n", idx, k, v) ;
        
        byte[] key = getBytes(k) ;
        byte[] value = getBytes(v) ;
        return new Record(key,value) ;
    }
    
    private void check(int idx)
    {
        if ( idx < 0 || idx >= numElts  )
            throw new IllegalArgumentException("VarRecordBuffer.check failure") ;
    }

    public void put(Record r)
    {
        put(numElts, r.getKey(), r.getValue()) ;
    }
    
    public void put(byte[]key, byte[] value)
    {
        put(numElts, key, value) ;
    }
    
    public void put(int idx, Record r)
    {
        put(idx, r.getKey(), r.getValue()) ;
    }
    
    public void put(int idx, byte[]key, byte[] value)
    {
        // Allow put at top end.
        if ( idx < 0 || idx > numElts  )
            throw new IllegalArgumentException("VarRecordBuffer.check failure") ;
        //System.out.printf("Put: %d (%d,%d)\n", idx, key.length, value.length) ;

        VarInteger$ keyLen = VarInteger$.valueOf(key.length) ;
        VarInteger$ valLen = VarInteger$.valueOf(value.length) ;
        
        // Var bytes + change in fixed area
        int lengthNeeded = bytesNeeded(key, value)+LenIdxSlot ;
        
        // Current gap. 
        int currentSpace = startVarArea - (HEADER_LENGTH+LenIdxSlot*numElts) ;
        
//        System.out.printf("put(%d,[%d],[%d]) :: currentSpace=%d , lengthNeeded=%d \n",
//                          idx, key.length, value.length, currentSpace, lengthNeeded) ;
        
        if ( lengthNeeded > currentSpace )
            throw new IllegalArgumentException("Insufficient space") ;
        
        // Put variable parts - bytes first, then length (it goes backwards).
        insertVarByteArray(value) ;
        int vIdxVal = insertVarByteArray(valLen.bytes()) ;
        insertVarByteArray(key) ;
        int vIdxKey = insertVarByteArray(keyLen.bytes()) ;
        
        // Put shorts - shuffle up a bit first.
        // Move up 2 places (if needed)
        
        // Shift up 2 offset slots
//        if ( true )
            shiftUp(bb, keyIdx(idx), LenIdxSlot, /*2*LenIdxPtr,*/ (numElts-idx)*LenIdxSlot) ;
//        else
////        // elt i = elt i-1
//        if ( true )
//        {
//            for ( int i = numElts-1 ; i >= idx ; i-- )
//            {
//                // Better - use a byteblock shuffle.
//                // ith short is
//                // Old key and value internal indexes
//                int x1 = keyStartIdx(i) ;
//                int x2 = valueStartIdx(i) ;
//    
//                // New positions.
//                int j1 = keyIdx(i+1) ;
//                int j2 = valueIdx(i+1) ;
//    
//                System.out.println("j1 = "+j1) ;
//                bb.putShort(j1, (short)x1) ;
//                bb.putShort(j2, (short)x2) ;
//            }
//        }
        // Insert new internal indexes.
        bb.putShort(keyIdx(idx), (short)vIdxKey) ;
        bb.putShort(valueIdx(idx), (short)vIdxVal) ;

        numElts++ ;
        setCount(numElts) ;
    }
    
    private static void shiftUp(ByteBuffer bb, int start, int places, int length)
    {
        //System.out.printf("ShiftUp: start=%d places=%d length=%d\n",start, places, length) ;
        if ( length == 0 )
            return ;
        System.arraycopy(bb.array(), start, bb.array(), start+places, length) ;
        if ( FILL )
            ByteBufferLib.fill(bb, start, start+places, FILL_BYTE) ;
    }
    
    /** Delete at idx */
    public void delete(int idx)
    {
        check(idx) ;
        // Shuffles the space now rather than manages
        // non-contiguos free space.
        // Note that all internal indexes are changed
        // from here to the upper end.

        // ---- Lengths and setup
        // Assumes bytes for keys or values are not shared.
        int k = keyStartIdx(idx) ;
        VarInteger$ kLen = VarInteger$.make(bb,k) ;
        
        int v = valueStartIdx(idx) ;
        VarInteger$ vLen = VarInteger$.make(bb,v) ;
        //System.out.printf("delete: idx=%d (%d,%d)\n", idx, kLen.length()+kLen.value(), vLen.length()+vLen.value());
        
        // So we have from k to v + length of VarInt + length - k bytes

        // v-k is not length of key+varInt
        int delta = (int)kLen.value()+kLen.length()+(int)vLen.value()+vLen.length() ;
        int delta2 = v-k+(int)vLen.value()+vLen.length() ;
        if ( delta != delta2 )
            System.out.printf("*** Deltas: %d/%d\n", delta, delta2) ;
        
        // ---- delta is the size of the byte block, startign at k, to eliminate 
        //   Move startVarArea to k (exc)
        //   remove idx internal index
        //   Reset internal indexes (+delta) idx+1 to numElts
        
        //System.out.printf("delete: idx=%d [%d bytes from %d]\n", idx, delta, k) ;
        
        // ByteBufferLib.blockMove(bb,start,finish,length)
        // Move (startVarArea, k[exc]) up deltas places -- it's an overlapping move.
        for ( int j = k-1 ; j >= startVarArea ; j-- )
        {
            //System.out.printf("Move: %d to %d\n", j, j+tByte) ;
            byte b = bb.get(j) ;
            bb.put(j+delta,b) ;
        }
        startVarArea += delta ;
        
        // Move internal indexes to remove idx
        //System.out.printf("Remove index: %d\n", idx) ;
        for ( int i = idx+1 ; i < numElts ; i++ ) 
        {
            short key = (short)keyStartIdx(i) ;
            short val = (short)valueStartIdx(i) ;
            //System.out.printf("Move index: [%d] <- (%d,%d)(0x%02X,0x%02X)\n", i-1, key, val, key, val) ;
            bb.putShort(keyIdx(i-1), key) ;
            bb.putShort(valueIdx(i-1), val) ;
        }
        // Clear top.
        bb.putShort(keyIdx(numElts-1), FILL_SHORT) ;
        bb.putShort(valueIdx(numElts-1), FILL_SHORT) ;
        numElts -- ;
        setCount(numElts) ;
        
        // Adjust indexes pointing to moved bytes
        for ( int i = 0 ; i < numElts ; i++ )
        {
            int k2 = keyStartIdx(i) ;
            // Don't need look at value
            if ( k2 < k )
            {
                // Fixup.
                int v2 = valueStartIdx(i) ;
                int k3 = k2+delta ;
                int v3 = v2+delta ;

                //System.out.printf("Fixup: %d %d->%d, %d->%d\n", i, k2,k3, v2,v3) ;

                bb.putShort(keyIdx(i), (short)k3) ;
                bb.putShort(valueIdx(i), (short)v3) ;
            }
            else
            {
                int v2 = valueStartIdx(i) ;
                //System.out.printf("No Fixup: %d (%d,%d)\n", i, k2, v2) ;
            }
        }
        
        //throw new NotImplemented("delete") ; 
    }
    
    /** Delete top */
    public void delete()
    {
        delete(numElts-1) ;
    }
    
    private int insertVarByteArray(byte[] bytes)
    {
        int x = startVarArea-bytes.length ;
        for ( int i = 0 ; i < bytes.length ; i++  )
            bb.put(i+x, bytes[i]) ;
        startVarArea -= bytes.length ;
        return startVarArea ;
    }

    private static int bytesNeeded(byte[] key, byte[] value )
    {
        return bytesNeeded(key)+bytesNeeded(value) ;
    }
    
    private static int bytesNeeded(byte[] bytes)
    {
        return VarInteger$.lengthOf(bytes.length) + bytes.length ;
    }
    
    /** Perform consistence checking */
    public void check()
    {
        if ( getCount() != numElts )
            error("Buffer count does not agree with number of elements (%d,%d)", getCount(), numElts) ;
        
        for ( int i = 0 ; i < numElts ; i++ )
        {
            int k = keyStartIdx(i) ;
            int v = valueStartIdx(i) ;
        }

        // Check the unused area is filled with marker bytes.
        
        int lo = valueStartIdx(numElts-1) ;
        int hi = startVarArea ;
        
        if ( lo > hi )
            error("Overlap in internal indexes and variable obects area") ; 

        if ( FILL )
        {
            for ( int j = lo ; j < hi ; j++ )
                if ( bb.get(j) != FILL_BYTE )
                    error("Non-fill byte (0x%02X) unused area", bb.get(j)) ;
        }
        // There are 2*numElts 
        int x = startVarArea ;
        for ( int i = 0 ; i < 2*numElts ; i++ )
        {
            if ( x < startVarArea || x >= bb.limit() )
                error("Variable area is corrupt") ;
            VarInteger$ varInt = VarInteger$.make(bb, x) ;
            int len = varInt.length()+(int)varInt.value() ;
            x = x+len ;
        }
        
    }

    private void error(String string, Object... args)
    {
        String x = String.format(string, args) ;
        throw new InternalErrorException(x) ;
    }

    public void dump()
    {
        print(bb) ;
        StringBuilder sb = new StringBuilder() ;
        sb.append("[size:"+getCount()) ;
        
        for ( int i = 0 ; i < numElts ; i++ )
        {
            sb.append(" ") ;
            
            sb.append("(") ;
            sb.append(keyStartIdx(i)) ;
            sb.append(",") ;
            sb.append(valueStartIdx(i)) ;
            sb.append(")") ;
        }
        sb.append("]") ;
        System.out.println(sb) ;
    }

    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        sb.append("[size:"+getCount()) ;
        
        for ( int i = 0 ; i < numElts ; i++ )
        {
            sb.append(" ") ;
            
            sb.append("(") ;
            sb.append(keyStartIdx(i)) ;
            sb.append(",") ;
            sb.append(valueStartIdx(i)) ;
            sb.append(")") ;
            
            Record r = get(i) ;
            sb.append(r) ;
            
        }
        sb.append("]") ;
        return sb.toString() ;
    }


    private byte[] getBytes(int byteIdx)
    {
        //System.out.println("Get: @"+byteIdx) ;
        VarInteger$ vInt = getVInt(byteIdx) ;                      // Length of variable length object
        int v = (int)vInt.value() ;
        int bytesStart = byteIdx+vInt.length() ;
        byte[] b = new byte[v] ;
        // Better ByteBuffer operation?
        for ( int i = 0 ; i < v ; i++ )
            b[i] = bb.get(bytesStart+i) ;
        return b ;
    }
    
    private VarInteger$ getVInt(int byteIdx)
    {
        return VarInteger$.make(bb, byteIdx) ;
    }
    
    // ---- Development
    public static void print(ByteBuffer byteBuffer)
    {
        print(System.out, byteBuffer) ; 
    }
    
    public static void print(PrintStream out, ByteBuffer byteBuffer)
    {
        out.printf("ByteBuffer[pos=%d lim=%d cap=%d]",byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity()) ;
        out.println() ;
        // Print bytes.
        int i = 0 ;
        int maxBytes = SIZE;
        for ( ; i < maxBytes && i < byteBuffer.limit() ; i++ )
        {
            if ( i%20 == 0 && i != 0 )
                out.println() ;
            out.printf(" 0x%02X", byteBuffer.get(i)) ;
        }
        if ( i < maxBytes && i < byteBuffer.limit() )
            out.print(" ...") ;
        // Print as 4-byte ints
//        int maxSlots = 8 ;
//        int i = 0 ;
//        for ( ; i < maxSlots && 4*i < byteBuffer.limit() ; i++ )
//            out.printf(" 0x%04X", byteBuffer.getInt(4*i)) ;
//        if ( i < maxSlots )
//            out.print(" ...") ;
        out.println();
    }

    // There's some accessor stuff somewhere - combine.
    
    public static void print(PrintStream out, byte[] bytes)
    {
        out.printf("byte[%d]:", bytes.length) ;
        int i = 0 ;
        int maxBytes = SIZE;
        for ( ; i < maxBytes && i < bytes.length  ; i++ )
        {
            if ( i%20 == 0 && i != 0 )
                out.println() ;
            out.printf(" 0x%02X", bytes[i]) ;
        }
        if ( i < maxBytes && i < bytes.length )
            out.print(" ...") ;
        // Print as 4-byte ints
//        int maxSlots = 8 ;
//        int i = 0 ;
//        for ( ; i < maxSlots && 4*i < byteBuffer.limit() ; i++ )
//            out.printf(" 0x%04X", byteBuffer.getInt(4*i)) ;
//        if ( i < maxSlots )
//            out.print(" ...") ;
        out.println();
        
    }


    
}
