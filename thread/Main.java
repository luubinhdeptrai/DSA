package thread;

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

        Counter counter = new Counter();

        Thread t1 = new Thread ( () -> {
            for (int i=0; i<10000; i++)
            {
                counter.increment();
            }
        });

        Thread t2 = new Thread ( () -> {
            for (int i=0; i<10000; i++)
            {
                counter.increment();
            }
        });

        t1.start();
        t2.start();
        
        try
        {
            t1.join();
        }
        catch (InterruptedException e)
        {
            System.out.println( "T1: " + e.getMessage());
        }

        try 
        {
            t2.join();
        }
        catch (InterruptedException e)
        {
            System.out.println ("T2: " + e.getMessage());
        }

        System.out.println (counter.getCount());

    }
}
