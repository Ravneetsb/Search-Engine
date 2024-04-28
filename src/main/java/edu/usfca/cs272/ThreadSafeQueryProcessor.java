package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** The thread safe query processor */
public class ThreadSafeQueryProcessor implements Processor {

  /** Workqueue for the processor. */
  private final WorkQueue queue;

  /** The map to store the search results. */
  private final TreeMap<String, ArrayList<InvertedIndex.Score>> searches;

  /** The search method to use. */
  private final Function<Set<String>, ArrayList<InvertedIndex.Score>> searchMethod;

  /**
   * Creates a new ThreadSafeQueryProcessor
   *
   * @param index the thread safe invertedindex to search through.
   * @param queue the workqueue to use.
   * @param partialSearch true if partial search needs to be performed.
   */
  public ThreadSafeQueryProcessor(
      ThreadSafeInvertedIndex index, WorkQueue queue, boolean partialSearch) {

    this.searchMethod = partialSearch ? index::partialSearch : index::exactSearch;
    this.queue = queue;
    this.searches = new TreeMap<>();
  }

  /**
   * Parses query line by line from a file.
   *
   * @param query The file that contains the queries.
   * @throws IOException if the file is not found.
   */
  public void parseQuery(Path query) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(query, UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        queue.execute(new Task(line));
      }
      queue.finish();
    }
  }

  /**
   * Writes the search results in pretty json.
   *
   * @param path Path of output file.
   * @throws IOException if the file is unable to be written.
   */
  public void toJson(Path path) throws IOException {
    JsonWriter.writeSearch(searches, path);
  }

  @Override
  public int numOfResults() {
    return searches.size();
  }

  @Override
  public List<InvertedIndex.Score> getScores(String query) {
    SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
    var stems = FileStemmer.uniqueStems(query, stemmer);
    query = String.join(" ", stems);
    ArrayList<InvertedIndex.Score> scores = searches.get(query);
    if (scores == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(scores);
    }
  }

  @Override
  public Set<String> getQueries() {
    return Collections.unmodifiableSet(searches.keySet());
  }

  /**
   * Parse a string as a query and performs search on it.
   *
   * @param line the query line.
   */
  public void parseQuery(String line) {
    SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
    var stems = FileStemmer.uniqueStems(line, stemmer);
    searches.put(String.join(" ", stems), searchMethod.apply(stems));
  }

  /** Task for the Processor */
  private class Task implements Runnable {
    /** The query to perform search for. */
    private final String query;

    /**
     * Creates a new task
     *
     * @param query The query to search
     */
    private Task(String query) {
      this.query = query;
    }

    @Override
    public void run() {
      SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
      var stems = FileStemmer.uniqueStems(query, stemmer);
      String queryKey = String.join(" ", stems);

      synchronized (searches) {
        if (query.isBlank() || searches.containsKey(query)) {
          return;
        }
      }
      ArrayList<InvertedIndex.Score> scores = searchMethod.apply(stems);
      synchronized (searches) {
        searches.put(queryKey, scores);
      }
    }

    @Override
    public String toString() {
      return "Task{" + "query='" + query + '\'' + '}';
    }
  }

  @Override
  public String toString() {
    return "ThreadSafeQueryProcessor{" + "searchMethod=" + searchMethod + '}';
  }
}
