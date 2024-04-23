package edu.usfca.cs272;

import java.util.LinkedList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** A simple work queue. */
public class WorkQueue {

  /** Workers that wait until work is available */
  private final Worker[] workers;

  /** Queue of pending tasks. */
  private final LinkedList<Runnable> tasks;

  /** Used to signal that the workers can terminate. */
  private volatile boolean shutdown;

  /** Default number of worker threads to use. */
  public static final int DEFAULT = 5;

  /** Logger used for this class. */
  private static final Logger log = LogManager.getLogger();

  /** Count to keep track of pending tasks. */
  private int pending;

  /** Starts a work queue with default number of threads. */
  public WorkQueue() {
    this(DEFAULT);
  }

  /**
   * Starts a work queue with given number of threads.
   *
   * @param threads number of worker threads to use.
   */
  public WorkQueue(int threads) {
    this.tasks = new LinkedList<>();
    this.workers = new Worker[threads];
    this.shutdown = false;
    this.pending = 0;

    for (int i = 0; i < threads; i++) {
      workers[i] = new Worker();
      workers[i].start();
    }
  }

  /** Incrment count of pending work. */
  private synchronized void incrementPending() {
    pending++;
  }

  /** Decrement count of pending work. */
  private synchronized void decrementPending() {
    assert pending > 0;
    pending--;
    if (pending == 0) {
      this.notifyAll();
    }
  }

  /**
   * Adds a task request to the queue.
   *
   * @param task task request.
   */
  public void execute(Runnable task) {
    incrementPending();
    synchronized (tasks) {
      tasks.addLast(task);
      tasks.notifyAll();
    }
  }

  /** Waits for all the pending work to be finished. Does no terminate the worker threads. */
  public synchronized void finish() {
    try {
      while (pending > 0) {
        this.wait();
      }
    } catch (InterruptedException e) {
      log.catching(e);
    }
  }

  /** Asks the work queue to shutdown. Any tasks left in the queue will not be completed. */
  public void shutdown() {
    shutdown = true;
    synchronized (tasks) {
      tasks.notifyAll();
    }
  }

  /**
   * Waits for all the work to be finished and the worker threads to terminate. The work queue
   * cannot be reused after this call completes.
   */
  public void join() {
    try {
      finish();
      shutdown();

      for (Worker worker : workers) {
        worker.join();
      }
    } catch (InterruptedException e) {
      System.err.println("Warning: Work queue interrupted while joining.");
      log.catching(Level.WARN, e);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Return the number of worker threads being used by the work queue.
   *
   * @return the number of worker threads.
   */
  public int size() {
    return workers.length;
  }

  /**
   * Waits until task is available in the work queue. When work is found, it will remove it from the
   * queue and run it.
   */
  private class Worker extends Thread {
    /** initializes a worker thread with a custom name. */
    public Worker() {
      setName("Worker" + getName());
    }

    @Override
    public void run() {
      Runnable task = null;

      try {
        while (true) {
          synchronized (tasks) {
            while (tasks.isEmpty() && !shutdown) {
              tasks.wait();
            }

            if (shutdown) {
              break;
            }

            task = tasks.removeFirst();
          }
          try {
            task.run();
          } catch (RuntimeException e) {
            System.err.println(e);
          }
          decrementPending();
        }
      } catch (InterruptedException e) {
        log.catching(Level.WARN, e);
        Thread.currentThread().interrupt();
      }
    }
  }
}
