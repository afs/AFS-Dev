/**
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

package dev ;

import java.nio.ByteBuffer ;

public class PerfMem {
    // Faster if the JVM knows the buffer type.
    
    public static void main(String ...a) {
        for ( int i = 0 ; i < 5 ; i++ ) {
            heap() ;
            direct() ;
        }
    }
 
    public static void heap() {
        ByteBuffer buf = ByteBuffer.allocate(2048);
        exec("heap", buf) ;
    }
 
    public static void direct() {
        ByteBuffer buf = ByteBuffer.allocateDirect(2048);
        exec("direct", buf) ;
    }
    
    public static void exec(String label, ByteBuffer buf) {
        
        long startTime = System.currentTimeMillis();
        for (int i = 1048576; i > 0; i --) {
            buf.clear();
            for ( int j = 0 ; j < buf.capacity()/4 ; j++ ) {
                buf.putInt((byte) 0);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("%-8s %dms\n", label, (endTime - startTime));
        
    }
}
