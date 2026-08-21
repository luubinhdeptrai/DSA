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

        WorkerThread threadf = new WorkerThread("Thread 1", 10);
        WorkerThread threads = new WorkerThread("Thread 2", 10);

        threadf.setPriority(Thread.MIN_PRIORITY);
        threads.setPriority(Thread.MAX_PRIORITY);

        threadf.start();
        threads.start();


    }
}
