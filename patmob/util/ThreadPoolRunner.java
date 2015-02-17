package patmob.util;    // +JD 7-29-2012

import java.util.*;
import java.util.concurrent.*;
import patmob.convert.OPSBiblio;

/**
 * A thread running the Queue to execute other threads, such as OPSBiblio.
 */
public class ThreadPoolRunner implements Runnable{
    private final ExecutorService threadPool;
    private Queue<Runnable> jobs =  new LinkedList<Runnable>();

    /**
     * Create new thread pool.
     */
    public ThreadPoolRunner() {
        threadPool = Executors.newFixedThreadPool(5);
    }

    /**
     * Executes jobs from the pool, or goes to sleep for 500 ms if none.
     */
    @Override
    public void run() {
        while (true) {
            if (!jobs.isEmpty())
                threadPool.execute(jobs.poll());
            else try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                System.out.println("OPSBiblioRunner: " + ex);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Add job Runnable to the queue for execution.
     */
    public void addJob(Runnable job) {
        jobs.offer(job);
    }
}
