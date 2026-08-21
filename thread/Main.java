package thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main (String[] args)
    {
        // WorkerThread threadf = new WorkerThread("Thread 1",10);
        // Thread threads = new Thread (new WorkerTask("Thread 2", 10));

        // System.out.println(threadf.isAlive());
        // threadf.start();
        // System.out.println(threadf.isAlive());

        // threads.start();

        // try
        // {
        //     threadf.join();
        // }
        // catch (InterruptedException e)
        // {
        //     System.out.println ("Thread 1 exception: " + e.getMessage());
        // }

        // try
        // {
        //     threads.join();
        // }
        // catch (InterruptedException e)
        // {
        //     System.out.println ("Thread 2 exception: " + e.getMessage());
        // }

        // System.out.println(threadf.isAlive());

        // System.out.println("All initial worker completed");

        /* Task 8 + 9 */


        Counter counter = new Counter();

        Thread tf = new Thread ( () -> {
            for (int i=0; i<10000; i++)
            {
                counter.increment();
            }
        });

        Thread ts = new Thread ( () -> {
            for (int i=0; i<10000; i++)
            {
                counter.increment();
            }
        });

        tf.start();
        ts.start();
        
        try
        {
            tf.join();
        }
        catch (InterruptedException e)
        {
            System.out.println( "T1: " + e.getMessage());
        }

        try 
        {
            ts.join();
        }
        catch (InterruptedException e)
        {
            System.out.println ("T2: " + e.getMessage());
        }

        System.out.println (counter.getCount());

        /* Task 10 */
        
        WorkerTask t1 = new WorkerTask("t1", 3);
        WorkerTask t2 = new WorkerTask("t2", 3);
        WorkerTask t3 = new WorkerTask("t3", 3);
        WorkerTask t4 = new WorkerTask("t4", 3);

        ExecutorService executor = Executors.newFixedThreadPool (2);

        executor.submit(t1);
        executor.submit(t2);
        executor.submit(t3);
        executor.submit(t4);

        executor.shutdown();

    }
}
