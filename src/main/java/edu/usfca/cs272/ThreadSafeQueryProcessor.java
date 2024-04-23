package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/** Query Processor for multi-threaded search */
public class ThreadSafeQueryProcessor extends QueryProcessor {

  /** the work queue for tasks. */
  private final WorkQueue queue;

  /** The results of the search. */
  private final TreeMap<String, ArrayList<InvertedIndex.Score>> searches;

  /** Search method for search. */
  private final Function<Set<String>, ArrayList<InvertedIndex.Score>> searchMethod;

  /**
   * Creates a new QueryProcessor
   *
   * @param invertedIndex the index to be searched
   * @param partial indicates search type
   * @param queue Workqueue
   */
  public ThreadSafeQueryProcessor(
      ThreadSafeInvertedIndex invertedIndex, boolean partial, WorkQueue queue) {
    super(invertedIndex, partial);
    this.queue = queue;
    this.searches = new TreeMap<>();
    this.searchMethod = partial ? invertedIndex::partialSearch : invertedIndex::exactSearch;
  }

  /**
   * Creates a new QueryProcessor the defaults to exact search.
   *
   * @param invertedIndex the index to be searched
   * @param queue Workqueue
   */
  public ThreadSafeQueryProcessor(ThreadSafeInvertedIndex invertedIndex, WorkQueue queue) {
    super(invertedIndex);
    this.queue = queue;
    this.searches = new TreeMap<>();
    this.searchMethod = invertedIndex::partialSearch;
  }

  @Override
  public void parseQuery(Path query) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(query, UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        parseQuery(line);
      }
      queue.finish();
    }
  }

  @Override
  public void parseQuery(String query) {
    queue.execute(new Task(query));
  }

  @Override
  public void toJson(Path path) throws IOException {
    JsonWriter.writeSearch(searches, path);
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

  /** Task for the QueryProcessor */
  private class Task implements Runnable {

    /** the Query String */
    private final String query;

    /**
     * Creates a new task for the queryFile
     *
     * @param query the search query
     */
    private Task(String query) {
      this.query = query;
    }

    @Override
    public void run() {
      synchronized (searches) {
        QueryProcessor.parseQuery(query, searches, searchMethod);
      }
    }
  }
}
