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

package jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.atlas.logging.LogCtl ;

import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.LockMRSW;

public class TestMRSW
{
    static { LogCtl.setLog4j() ; }
    
    static Lock lock = new LockMRSW() ;
    static AtomicInteger readerCount = new AtomicInteger(0) ;
    static AtomicInteger writerCount = new AtomicInteger(0) ;
    
    public static void main(String... args) throws Exception
    {
        //Log.enable(LockMRSW.class) ;
        
        int wThreads = 3 ;
        int rThreads = 5 ;
        
        List<Thread> threads = new ArrayList<Thread>();
        
        for ( int i = 0 ; i < rThreads ; i++ )
            threads.add(new Thread(new ReaderRunner())) ;

        for ( int i = 0 ; i < wThreads ; i++ )
            threads.add(new Thread(new WriterRunner())) ;
        

        for (Thread thread: threads)
            thread.start();
        
        for (Thread thread: threads)
            thread.join();
    }
    
    static void check()
    {
        // Not perfect when called from outside the critical region. 
        // Can see r = 1, then [threads run, r -> 0, w -> 1] read w = 1 => check fails.  
        int r = readerCount.intValue() ;
        int w = writerCount.intValue() ;
        
        if ( r > 0 && w > 0 )
            System.err.printf("r=%d w=%d", r, w) ;

        if ( r == 0 && w > 1 )
            System.err.printf("r=%d w=%d", r, w) ;

        if ( r < 0 || w < 0 )
            System.err.printf("r=%d w=%d", r, w) ;
    }
    
    
    static class ReaderRunner implements Runnable
    {

        @Override
        public void run()
        {
            while(true)
            {
                //Checks here may see inconsistency.
                randomWait(5) ;
                try {
                    lock.enterCriticalSection(Lock.READ) ;
                    readerCount.incrementAndGet() ;
                    check() ;
                    randomWait(10) ;
                    check() ;
                } finally
                { 
                    readerCount.decrementAndGet() ;
                    lock.leaveCriticalSection() ; 
                } 
            }
        }
    }

    static class WriterRunner implements Runnable
    {

        @Override
        public void run()
        {
            while(true)
            {
                //Bad - check() ;
                randomWait(5) ;
                try {
                    lock.enterCriticalSection(Lock.WRITE) ;
                    writerCount.incrementAndGet() ;
                    check() ;
                    randomWait(10) ;
                    check() ;
                } finally
                { 
                    writerCount.decrementAndGet() ;
                    lock.leaveCriticalSection() ; 
                }
            }
        }
    }
    
    
    static Random random = new Random() ; 
    static int randomWait(int milliseconds)
    {
        return random.nextInt(milliseconds) ;
    }
}
