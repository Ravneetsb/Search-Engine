package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** Processor interface. */
public interface Processor {
  /**
   * Reads a file containing queries to parse the queries within.
   *
   * @param query the file containing the queries.
   * @throws IOException if the file is not found.
   */
  public void parseQuery(Path query) throws IOException;

  /**
   * Parses a query.
   *
   * @param query the query to be searched.
   */
  public void parseQuery(String query);

  /**
   * Writes the search results in pretty Json to file path.
   *
   * @param path the path of the file.
   * @throws IOException if the file is not found.
   */
  public void toJson(Path path) throws IOException;

  /**
   * Returns the number of queries for which there is a result.
   *
   * @return the number of queries for which there is a result.
   */
  public int numOfResults();

  /**
   * Returns the number of results for a query.
   *
   * @param query the query for which the number of scores is returned.
   * @return the number of results for a query.
   */
  public default int numOfScores(String query) {
    return getScores(query).size();
  }

  /**
   * Returns the scores for a query.
   *
   * @param query the query for which to return scores.
   * @return the scores for a query.
   */
  public List<InvertedIndex.Score> getScores(String query);

  /**
   * Returns the set of queries.
   *
   * @return the set of queries.
   */
  public Set<String> getQueries();

  /**
   * Returns true if a query is in the results.
   *
   * @param query the query to lookup
   * @return true if a query is found in the results.
   */
  public default boolean hasQuery(String query) {
    return getScores(query).isEmpty();
  }
}