package edu.usfca.cs272;

/** Query Processor for multi-threaded search */
public class ThreadSafeQueryProcessor extends QueryProcessor {

  /** The invertedIndex to search through */
  private final InvertedIndex index;

  /** the work queue for tasks. */
  private final WorkQueue queue;

  public ThreadSafeQueryProcessor(InvertedIndex invertedIndex, boolean partial, int threads) {
    super(invertedIndex, partial);
    this.index = invertedIndex;
    this.queue = new WorkQueue(threads);
  }

  public ThreadSafeQueryProcessor(InvertedIndex invertedIndex, int threads) {
    super(invertedIndex);
    this.index = invertedIndex;
    this.queue = new WorkQueue(threads);
  }
}
