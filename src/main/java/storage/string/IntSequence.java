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

package storage.string;

/** Simple interface to a finite and known sequence of int-like units (bytes, chars)  */
public interface IntSequence {
    // There needs to be an "end" marker.
    // Exceptions are a bit costly for wrapping simple things up.
    
    /** Return the current byte value, or -1 if at the end of the stream. */
    public int current() ;
    
    /** Move forward, and return the next byte. */  
    public int forward() ;
    
    /** Move backwards. Return -1 if at the start of the stream. */
    public int backward() ;
    
    /*
     * Get the position. This is between 0 and length, where "length" means it's
     * at the end of the content
     */
    public int position() ;
    
    /**
     * Set the position. Must be between 0 and length, where "length" mans it's
     * pointing to just after the contents
     */
    public void position(int newPosn) ;
    public int length() ;
}
