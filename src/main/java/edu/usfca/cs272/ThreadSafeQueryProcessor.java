package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

/** Query Processor for multi-threaded search */
public class ThreadSafeQueryProcessor extends QueryProcessor {

  /** The invertedIndex to search through */
  private final InvertedIndex index;

  /** the work queue for tasks. */
  private final WorkQueue queue;

  /** search method to be used. */
  private final Function<Set<String>, ArrayList<InvertedIndex.Score>> searchMethod;

  /** The results of the search. */
  private final TreeMap<String, ArrayList<InvertedIndex.Score>> searches;

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
    this.searches = new TreeMap<>();
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
    this.searches = new TreeMap<>();
  }

  @Override
  public void parseQuery(Path query) throws IOException {
    super.parseQuery(query);
  }

  @Override
  public void parseQuery(String query) {
    super.parseQuery(query);
  }

  @Override
  public void toJson(Path path) throws IOException {
    super.toJson(path);
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public int numOfResults() {
    return super.numOfResults();
  }

  @Override
  public int numOfScores(String query) {
    return super.numOfScores(query);
  }

  @Override
  public List<InvertedIndex.Score> getScores(String query) {
    return super.getScores(query);
  }

  @Override
  public Set<String> getQueries() {
    return super.getQueries();
  }

  @Override
  public boolean hasQuery(String query) {
    return super.hasQuery(query);
  }
}
