package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** Process query for each line. */
public class QueryProcessor {

  /** Map of the query and its score */
  private final TreeMap<String, ArrayList<InvertedIndex.Score>> searches;

  /** partial tag for the search. */
  public final boolean partialSearch;

  /** Inverted Index to search through. */
  private final InvertedIndex index;

  private final SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

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
        var stems = FileStemmer.uniqueStems(line, stemmer); // TODO Remove
        String queryLine = String.join(" ", stems); // TODO Remove
        if (!searches.containsKey(queryLine)) {
          parseQuery(queryLine);
        }
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
    // TODO Move the logic to decide search into a convenience method like:
    // TODO public List<Score> search(Set<String> queries, boolean partial) <-- inside of the
    // inverted index
    ArrayList<InvertedIndex.Score> scores =
        partialSearch
            ? index.partialSearch(FileStemmer.uniqueStems(query))
            : index.exactSearch(FileStemmer.uniqueStems(query));
    searches.put(query, scores);
  }

  /**
   * Performs partial search on the index.
   *
   * @param queryLine query
   */
  // TODO public ArrayList<Score> partialSearch(Set<String> queries) {
  private ArrayList<InvertedIndex.Score> partialSearch(
      String queryLine) { // TODO Move this into InvertedIndex
    ArrayList<InvertedIndex.Score> scores = new ArrayList<>();

    /* TODO
    for (String query : queries) {
    		for (String stem : index.getWords()) { // TODO Use tailMap + break
    			if (stem.startsWith(query)) {
    				Set<String> locations = index.getLocations(query);
    				for (String location : locations) {

    				}
    			}
    		}
    }
    */

    for (String rootQuery : queryLine.split(" ")) {
      ArrayList<String> queries = getPartialQueries(rootQuery);
      for (String query : queries) {
        Set<String> locations = index.getLocations(query); // TODO Directly access the index instead
        for (String location : locations) {
          InvertedIndex.Score score =
              scores.stream()
                  .filter(score1 -> score1.getLocation().equals(location))
                  .findFirst()
                  .orElse(index.newScore(0, 0, location));

          int stemTotal = index.getCounts().get(location);
          int count = index.numOfPositions(query, location);
          Integer totalCount = score.getCount();
          score.setCount(count + totalCount);
          score.setScore((double) (count + totalCount) / stemTotal);
          if (!scores.contains(score)) {
            scores.add(score);
          }
        }
      }
    }
    Collections.sort(scores);
    return scores;
  }

  private ArrayList<String> getPartialQueries(String query) {
    ArrayList<String> queries = new ArrayList<>();
    for (String stem : index.getWords()) {
      if (stem.startsWith(query)) {
        queries.add(stem);
      }
    }
    return queries;
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

  // TODO Think about other generally useful methods
}
