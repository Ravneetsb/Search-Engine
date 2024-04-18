package edu.usfca.cs272;

import java.util.LinkedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorkQueue {

  /** Workers that wait until work is available */
  private final Worker[] workers;

  /** Queue of pending tasks. */
  private final LinkedList<Runnable> tasks;

  /** Used to signla that the workers can terminate. */
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
    pending--;
  }
}
