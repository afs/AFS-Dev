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

import java.lang.reflect.Field ;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.logging.LogCtl ;
import sun.misc.Unsafe ;

@SuppressWarnings("restriction")
public class RunAFS
{
    static { LogCtl.setCmdLogging(); }
    
    private static Unsafe getUnsafe() {
        try {

            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            return (Unsafe) singleoneInstanceField.get(null);

        } catch (IllegalArgumentException e) {
            throw createExceptionForObtainingUnsafe(e);
        } catch (SecurityException e) {
            throw createExceptionForObtainingUnsafe(e);
        } catch (NoSuchFieldException e) {
            throw createExceptionForObtainingUnsafe(e);
        } catch (IllegalAccessException e) {
            throw createExceptionForObtainingUnsafe(e);
        }
    }
    
    
    private static RuntimeException createExceptionForObtainingUnsafe(Exception e) {
        return new RuntimeException(e) ;
    }


    public static void main(String ...argv) {
        Unsafe x = getUnsafe() ;
        final long offset = Unsafe.ARRAY_BYTE_BASE_OFFSET ; /* Typically, 16, on 64 bit JVM */ //x.arrayBaseOffset(byte[].class) ;
        final long scale = Unsafe.ARRAY_BYTE_INDEX_SCALE ; /* Typically, 1 */                 //x.arrayIndexScale(byte[].class) ;
        System.out.printf("%d / %d\n", offset, scale) ;
        byte[] b = new byte[8*10] ;
        
        int i = 6 ;
        
        // Network order != x68 order.
        x.putLong(b, offset+i*scale, Long.reverseBytes(153L)) ; // Order? little endian.
        
        
        long z = Bytes.getLong(b, 6) ;
        
        System.out.printf("==> %d 0x%08X\n", z,z) ;
        z = x.getLong(b, offset+i*scale) ;
        
        z = Long.reverseBytes(z) ;
        System.out.printf("==> %d 0x%08X\n", z,z) ;
        
        System.out.println("DONE") ;
        
    }
}

