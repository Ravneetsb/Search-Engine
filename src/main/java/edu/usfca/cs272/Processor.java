package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** Processor Interface.
 * The processor is responsible for executing searches on the index,
 * populating the results to either be sent to the client or output
 * to a file.
 * @author Ravneet Singh Bhatia
 * @version Spring 2024*/
public interface Processor {
  /**
   * Reads a file containing queries to parse the queries within.
   *
   * @param query the file containing the queries.
   * @throws IOException if the file is not found.
   */
  default void parseQuery(Path query) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(query, UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        parseQuery(line);
      }
    }
  }

  /**
   * Parses a query.
   *
   * @param query the query to be searched.
   */
  void parseQuery(String query);

  /**
   * Writes the search results in pretty Json to file path.
   *
   * @param path the path of the file.
   * @throws IOException if the file is not found.
   */
  void toJson(Path path) throws IOException;

  /**
   * Returns the number of queries for which there is a result.
   *
   * @return the number of queries for which there is a result.
   */
  default int numOfResults() {
    return getQueries().size();
  }

  /**
   * Returns the number of results for a query.
   *
   * @param query the query for which the number of scores is returned.
   * @return the number of results for a query.
   */
  default int numOfScores(String query) {
    return getScores(query).size();
  }

  /**
   * Returns the scores for a query.
   *
   * @param query the query for which to return scores.
   * @return the scores for a query.
   */
  List<InvertedIndex.Score> getScores(String query);

  /**
   * Returns the set of queries.
   *
   * @return the set of queries.
   */
  Set<String> getQueries();

  /**
   * Returns true if a query is in the results.
   *
   * @param query the query to lookup
   * @return true if a query is found in the results.
   */
  default boolean hasQuery(String query) {
    return getScores(query).isEmpty();
  }
}
