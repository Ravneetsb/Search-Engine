package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** Query Processor for multi-threaded search */
public class ThreadSafeQueryProcessor extends QueryProcessor {

  /** The invertedIndex to search through */
  private final InvertedIndex index;

  /** the work queue for tasks. */
  private final WorkQueue queue;

  /** search method to be used. */
  private static final Function<Set<String>, ArrayList<InvertedIndex.Score>> searchMethod;

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
    searchMethod = partial ? invertedIndex::partialSearch : invertedIndex::exactSearch;
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
    try (BufferedReader br = Files.newBufferedReader(query, UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        queue.execute(new Task(line, searches));
      }
    }
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

  /** Task for the QueryProcessor */
  public static class Task implements Runnable {

    /** the Query String */
    private final String query;

    /** Stores the search results */
    private final TreeMap<String, ArrayList<InvertedIndex.Score>> searches;

    /**
     * Creates a new task for the queryFile
     *
     * @param query the search query
     * @param searches stores the results of the search.
     */
    public Task(String query, TreeMap<String, ArrayList<InvertedIndex.Score>> searches) {
      this.searches = searches;
      this.query = query;
    }

    @Override
    public void run() {
      SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
      var stems = FileStemmer.uniqueStems(query, stemmer);
      String queryString = String.join(" ", stems);
      synchronized (searches) {
      if (queryString.isBlank() || searches.containsKey(query)) {
        return;
      }
      ArrayList<InvertedIndex.Score> scores = searchMethod.apply(stems);
      searches.put(query, scores);
    }}
  }
}
