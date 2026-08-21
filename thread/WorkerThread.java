package thread;

public class WorkerThread extends Thread
{
    private String threadName;
    private int steps;

    public WorkerThread (String threadName, int steps)
    {
        this.threadName = threadName;
        this.steps = steps;
    }

    public void run()
    {
        for (int i = 1; i <= this.steps; i++)
        {
            System.out.println (this.threadName + ": step " + i);

        try
        {
            Thread.sleep(200);
        }
        catch (InterruptedException e)
        {
            System.out.println (e.getMessage());
        }
        }


    }
}