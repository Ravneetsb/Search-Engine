package edu.usfca.cs272;

import java.io.IOException;
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

  @Override
  public void parseQuery(Path query) throws IOException {
    Processor.super.parseQuery(query);
    queue.finish();
  }

  @Override
  public void toJson(Path path) throws IOException {
    synchronized (searches) {
      JsonWriter.writeSearch(searches, path);
    }
  }

  @Override
  public int numOfResults() {
    synchronized (searches) {
      return searches.size();
    }
  }

  @Override
  public List<InvertedIndex.Score> getScores(String query) {
    SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
    var stems = FileStemmer.uniqueStems(query, stemmer);
    query = String.join(" ", stems);
    ArrayList<InvertedIndex.Score> scores;
    synchronized (searches) {
      scores = searches.get(query);
    }
    if (scores == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(scores);
    }
  }

  @Override
  public Set<String> getQueries() {
    synchronized (searches) {
      return Collections.unmodifiableSet(searches.keySet());
    }
  }

  @Override
  public void parseQuery(String query) {
    queue.execute(new Task(query));
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
      if (stems.isEmpty()) {
        return;
      }
      String queryKey = String.join(" ", stems);

      synchronized (searches) {
        if (searches.containsKey(query)) {
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
    return JsonWriter.writeSearch(searches);
  }
}
