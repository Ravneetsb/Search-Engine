package edu.usfca.cs272;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;

/** Query Processor for multi-threaded search */
public class ThreadSafeQueryProcessor extends QueryProcessor {

  /** The invertedIndex to search through */
  private final InvertedIndex index;

  /** the work queue for tasks. */
  private final WorkQueue queue;

  /** search method to be used. */
  private final Function<Set<String>, ArrayList<InvertedIndex.Score>> searchMethod;

  /**
   * Creates a new QueryProcessor
   *
   * @param invertedIndex the index to be searched
   * @param partial indicates search type
   * @param threads the number of threads
   */
  public ThreadSafeQueryProcessor(InvertedIndex invertedIndex, boolean partial, int threads) {
    super(invertedIndex, partial);
    this.index = invertedIndex;
    this.queue = new WorkQueue(threads);
    this.searchMethod = partial ? invertedIndex::partialSearch : invertedIndex::exactSearch;
  }

  /**
   * Creates a new QueryProcessor the defaults to exact search.
   *
   * @param invertedIndex the index to be searched
   * @param threads the number of threads.
   */
  public ThreadSafeQueryProcessor(InvertedIndex invertedIndex, int threads) {
    super(invertedIndex);
    this.index = invertedIndex;
    this.queue = new WorkQueue(threads);
    this.searchMethod = invertedIndex::partialSearch;
  }
}
