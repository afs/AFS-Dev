/*
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

package riot.io;

import java.io.Writer ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.riot.tokens.Token ;

import com.hp.hpl.jena.graph.Node ;

public interface TokenOutputStream extends Closeable, Sync
{
    public void sendToken(Token token) ;
    public void sendNode(Node node) ;
    public void sendString(String string) ;
    public void sendWord(String word) ;
    public void sendControl(char character) ;
    public void sendNumber(long number) ;

    public void startTuple() ;
    public void endTuple() ;
    
    public void startSection() ;
    public void endSection() ;
    
    public Writer getWriter() ;
    
    public void flush() ;
    // Remove this when TDB jar gets updated.
    @Override
    public void sync() ;

}
