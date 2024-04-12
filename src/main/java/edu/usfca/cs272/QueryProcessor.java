package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** Process query for each line. */
public class QueryProcessor {

  /** Map of the query and its score */
  private final TreeMap<String, ArrayList<InvertedIndex.Score>> searches;

  /** partial tag for the search. */
  public final boolean partialSearch;

  /** Inverted Index to search through. */
  private final InvertedIndex index;

  Function<Set<String>, ArrayList<InvertedIndex.Score>> searchMethod;

  /** Stemmer for the processor. */
  private static final SnowballStemmer stemmer =
      new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

  /**
   * Constructor for Searcher
   *
   * @param invertedIndex The index to be searched.
   * @param partial true if partial search is to be performed.
   */
  public QueryProcessor(InvertedIndex invertedIndex, boolean partial) {
    this.searches = new TreeMap<>();
    this.partialSearch = partial;
    this.index = invertedIndex;
    searchMethod = partial ? index::partialSearch : index::exactSearch;
  }

  /**
   * Constructor for QueryProcessor which always runs an exact search.
   *
   * @param invertedIndex index to be searched
   */
  public QueryProcessor(InvertedIndex invertedIndex) {
    this.searches = new TreeMap<>();
    this.index = invertedIndex;
    this.partialSearch = false;
    searchMethod = index::exactSearch;
  }

  /**
   * Read queries from the path.
   *
   * @param query path of the file which contains the queries.
   * @throws IOException if the path is null or doesn't exist.
   */
  public void parseQuery(Path query) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(query, UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        parseQuery(line);
      }
    }
  }

  /**
   * Logic for populating scores for every line
   *
   * @param query query.
   */
  public void parseQuery(String query) {
    var stems = FileStemmer.uniqueStems(query, stemmer);
    query = String.join(" ", stems);

    if (query.isBlank() || searches.containsKey(query)) {
      return;
    }
    ArrayList<InvertedIndex.Score> scores = searchMethod.apply(stems);
    searches.put(query, scores);
  }

  /**
   * Writes the search results map to path in pretty json
   *
   * @param path Path of results output file
   * @throws IOException if the file doesn't exist or path is null.
   */
  public void toJson(Path path) throws IOException {
    JsonWriter.writeSearch(searches, path);
  }

  /**
   * to String method for Searcher
   *
   * @return toString
   */
  @Override
  public String toString() {
    return JsonWriter.writeSearch(searches);
  }

  /**
   * Returns the number of queries in the searches
   *
   * @return the number of queries in the searches
   */
  public int numOfResults() {
    return searches.size();
  }

  /**
   * Returns the number of scores for a query
   *
   * @param query the query for which the scores need to be found.
   * @return the number of scores for a query.
   */
  public int numOfScores(String query) {
    return getScores(query).size();
  }

  /**
   * Returns the scores for a query
   *
   * @param query the query for which the scores are returned
   * @return the scores for a query
   */
  public List<InvertedIndex.Score> getScores(String query) {
    // TODO Stem and rejoin the query line before the get
    /* TODO var blah = searches.get(query);
    if null...
    */
    return searches.containsKey(query)
        ? Collections.unmodifiableList(searches.get(query))
        : Collections.emptyList();
  }

  /**
   * Returns a set of the queries in the searches map.
   *
   * @return a set of the queries in the searches map.
   */
  public Set<String> getQueries() {
    return Collections.unmodifiableSet(searches.keySet());
  }

  /**
   * Returns true if a query is in the searches map.
   *
   * @param query the query to find.
   * @return true if a query is in the searches map.
   */
  public boolean hasQuery(String query) {
    // TODO return getScores(query).isEmpty();
    return searches.containsKey(query);
  }

  /**
   * Returns if there is a score for a query in a specific location.
   *
   * @param query the query to be looked up.
   * @param location the file path
   * @return true if there is a score for a query in a specific location.
   */
  public boolean hasScoreLocation(String query, String location) { // TODO remove
    if (searches.containsKey(query)) {
      var scores = searches.get(query);
      if (scores != null) {
        for (var score : scores) {
          if (score.getLocation().equals(location)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
