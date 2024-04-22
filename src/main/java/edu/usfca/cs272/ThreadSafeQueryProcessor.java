package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** Query Processor for multi-threaded search */
public class ThreadSafeQueryProcessor extends QueryProcessor {

  /** The invertedIndex to search through */
  private final InvertedIndex index;

  /** the work queue for tasks. */
  private final WorkQueue queue;

  /** The results of the search. */
  private final TreeMap<String, ArrayList<InvertedIndex.Score>> searches;

  /** Flag to see if partial search needs to be performed. */
  private final boolean isPartial;

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
    isPartial = partial;
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
    this.searches = new TreeMap<>();
    isPartial = false;
  }

  @Override
  public void parseQuery(Path query) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(query, UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        queue.execute(new Task(line, searches, index, isPartial));
      }
      queue.join();
    }
  }

  @Override
  public void parseQuery(String query) {
    super.parseQuery(query);
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
  public static class Task implements Runnable {

    /** the Query String */
    private final String query;

    /** Stores the search results */
    private final TreeMap<String, ArrayList<InvertedIndex.Score>> searches;

    /** The thread safe index. */
    private final ThreadSafeInvertedIndex index;

    /** Indicator for partial search */
    private final boolean partial;

    /**
     * Creates a new task for the queryFile
     *
     * @param index the Index to search through
     * @param query the search query
     * @param searches stores the results of the search.
     * @param partial indicator for partial search
     */
    public Task(
        String query,
        TreeMap<String, ArrayList<InvertedIndex.Score>> searches,
        InvertedIndex index,
        boolean partial) {
      this.searches = searches;
      this.query = query;
      this.index = (ThreadSafeInvertedIndex) index;
      this.partial = partial;
    }

    @Override
    public void run() {
      SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
      var stems = FileStemmer.uniqueStems(query, stemmer);
      String queryString = String.join(" ", stems);
      synchronized (searches) {
        if (queryString.isBlank() || searches.containsKey(queryString)) {
          return;
        }
        ArrayList<InvertedIndex.Score> scores = index.search(stems, partial);
        searches.putIfAbsent(queryString, new ArrayList<>());
        var list = searches.get(queryString);
        list.addAll(scores);
      }
    }
  }
}
