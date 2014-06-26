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

package dev;

import java.io.ByteArrayInputStream ;
import java.io.IOException ;
import java.io.UnsupportedEncodingException ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.atlas.logging.java.ConsoleHandlerStdout ;
import org.slf4j.LoggerFactory ;

public class LogMain
{
    static { LogCtl.setLog4j() ; }
    public static void main(String ... args) //throws SecurityException, IOException
    {
        mainSLF4j() ;
        mainJUL() ;
    }
    
    public static void logNetwork()
    {
        // org.apache.log4j.net.SimpleSocketServer 6000 log4j.properties
        // org.apache.log4j.net.SocketServer 6000 log4j.properties DIR
        //   log4j.properties logging
        //   DIR is one config file per host

        LogCtl.setLog4j("log4j-net.properties") ;    // Re-initialize log4j.
        LoggerFactory.getLogger("HELLO").info("Message") ;
        System.out.println("Exit") ;
        System.exit(0) ;
    }
    
    private static void mainSLF4j()
    {

        org.slf4j.Logger logger_slf4j = org.slf4j.LoggerFactory.getLogger(LogMain.class) ;
        logger_slf4j.error("org.slf4j") ;
    }        
        
        
        
    private static void mainJUL()
    {
        String s = StrUtils.strjoinNL(
                                      // Handlers - output
                                      // All (comma separated)
                                      //"handlers=java.util.logging.ConsoleHandler,atlas.logging.java.ConsoleHandlerStdout",
                                      
                                      // Atlas.
                                      //"handlers=atlas.logging.java.ConsoleHandlerStdout" ,
                                      
                                      // Provided by the JRE
                                      "handlers=java.util.logging.ConsoleHandler" ,
                                      
                                      // Formatting and levels
                                      //"atlas.logging.java.ConsoleHandlerStdout.level=ALL",
                                      //"atlas.logging.java.ConsoleHandlerStdout.formatter=atlas.logging.java.TextFormatter",
                                      
                                      "java.util.logging.ConsoleHandler.level=INFO"
                                      //, "java.util.logging.ConsoleHandler.formatter=atlas.logging.java.TextFormatter"
                                      ) ;   
        
        try
        {
            java.util.logging.LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(s.getBytes("UTF-8"))) ;
        } catch (SecurityException ex)
        {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

        //System.setProperty("java.util.logging.config.file", "logging.properties") ;
        //LogManager.getLogManager().readConfiguration() ;
        
        java.util.logging.Logger log = java.util.logging.Logger.getLogger(LogMain.class.getName()) ;
        //log.setLevel(Level.WARNING) ;

        // Default output, Atlas/Java logging formatter.
        log.info("Hello World") ;
        
        // Because the parent has the plumbed in ConsoleHandler
        log.setUseParentHandlers(false) ;
        log.addHandler(new ConsoleHandlerStdout()) ;
        log.info("Hello World (part 2)") ;
         
//        // -- Remove any ConsoleHanlder
//        Handler[] handlers = log.getHandlers() ;
//        for ( Handler h : handlers )
//        {
//            if ( h instanceof ConsoleHandler )
//                log.removeHandler(h) ;
//        }
        log.info("Hello World (part 3)") ;
        System.out.println("(End)") ;
        
        
//        // ---- 
//        System.setProperty("log4j.configuration", "file:log4j.properties") ;
//        
//        
//        org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(Run.class);
//        log4j.setLevel(org.apache.log4j.Level.ALL) ;
//        log4j.info("Log4j direct") ;
        
        // Must have the right logger adapter on the classpath: slf4j-log4j... or slf4j-
    }
}
