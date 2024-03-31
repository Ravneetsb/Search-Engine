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

  //  /** Queries TreeSet. */
  //  private final TreeSet<String> queries;

  /** Map of the query and its score */
  private final TreeMap<String, ArrayList<Score>> searches;

  /** partial tag for the search. */
  public final boolean partialSearch;

  /** Inverted Index to search through. */
  private final InvertedIndex index;

  /** Counts map from the invertedIndex. */
  private final Map<String, Integer> counts;

  /**
   * Constructor for Searcher
   *
   * @param invertedIndex The index to be searched.
   * @param partial true if partial search is to be performed.
   * @throws IOException if the file doesn't exist or path is null.
   */
  public QueryProcessor(InvertedIndex invertedIndex, boolean partial) throws IOException {
    //    this.queries = parseQuery(query);
    this.searches = new TreeMap<>();
    this.partialSearch = partial;
    this.index = invertedIndex;
    this.counts = index.getCounts();
  }

  /**
   * Constructor for QueryProcessor which always runs an exact search.
   *
   * @param invertedIndex index to be searched
   * @throws IOException if the file doesn't exist or the path is null.
   */
  public QueryProcessor(InvertedIndex invertedIndex) throws IOException {
    //    this.queries = parseQuery(query);
    this.searches = new TreeMap<>();
    this.index = invertedIndex;
    this.partialSearch = false;
    this.counts = index.getCounts();
  }

  /* TODO
  public void parseQuery(Path query) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(query, UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
      		parseQuery(line);
      }
    }
  }

  public void parseQuery(String line) {
  		get the stems
  		join the line
  		get the search results from the index
  		and store them
  }
  */

  /**
   * Read queries from the path.
   *
   * @param query path of the file which contains the queries.
   * @throws IOException if the path is null or doesn't exist.
   */
  public void parseQuery(Path query) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(query, UTF_8)) {
      SnowballStemmer stemmer =
          new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH); // re-using the stemmer.
      String line;
      while ((line = br.readLine()) != null) {
        var stems = FileStemmer.uniqueStems(line, stemmer);
        String queryLine = String.join(" ", stems);
        parseQuery(queryLine);
      }
    }
  }

  /**
   * Logic for populating scores for every line
   *
   * @param query query.
   */
  public void parseQuery(String query) {
    if (partialSearch) {
      partialSearch(query);
    } else {
      exactSearch(query);
    }
  }

  /**
   * Perform a search for the exact query given.
   *
   * @param queryLine query to the index.
   */
  private void exactSearch(String queryLine) {
    if (queryLine.isBlank() || queryLine.isEmpty()) {
      return;
    }
    if (searches.containsKey(queryLine)) {
      return;
    }
    searches.putIfAbsent(queryLine, new ArrayList<>());
    ArrayList<Score> scores = searches.get(queryLine);
    for (String query : queryLine.split(" ")) {
      Set<String> locations = index.getLocations(query);
      for (String location : locations) {
        Score score =
            scores.stream()
                .filter(score1 -> score1.getWhere().equals(location))
                .findFirst()
                .orElse(new Score(0, 0, location));

        int stemTotal = counts.get(location);
        int count = index.numOfPositions(query, location);
        Integer totalCount = score.getCount();
        Double existingStemTotal = score.getScore();
        score.setCount(count + totalCount);
        score.setScore(Double.sum((double) count / stemTotal, existingStemTotal));
        if (!scores.contains(score)) {
          scores.add(score);
        }
      }
    }
    Collections.sort(scores);
  }

  /**
   * Performs partial search on the index.
   *
   * @param queryLine query
   */
  private void partialSearch(String queryLine) {
    if (queryLine.isBlank() || queryLine.isEmpty()) {
      return;
    }
    if (searches.containsKey(queryLine)) {
      return;
    }
    searches.putIfAbsent(queryLine, new ArrayList<>());
    ArrayList<Score> scores = searches.get(queryLine);

    for (String rootQuery: queryLine.split(" ")) {
      ArrayList<String> queries = getPartialQueries(rootQuery);
      for (String query: queries) {
        Set<String> locations = index.getLocations(query);
        for (String location : locations) {
          Score score = scores.stream()
                  .filter(score1 -> score1.getWhere().equals(location))
                  .findFirst()
                  .orElse(new Score(0, 0, location));

          int stemTotal = counts.get(location);
          int count = index.numOfPositions(query, location);
          Integer totalCount = score.getCount();
          Double existingStemTotal = score.getScore();
          score.setCount(count + totalCount);
          score.setScore(Double.sum((double) count / stemTotal, existingStemTotal));
          if (!scores.contains(score)) {
            scores.add(score);
          }
        }
      }
    }
    Collections.sort(scores);
  }

  private ArrayList<String> getPartialQueries(String query) {
    ArrayList<String> queries = new ArrayList<>();
    for (String stem: index.getWords()) {
      if (stem.startsWith(query)) {
        queries.add(stem);
      }
    }
    return queries;
  }

  /*
   * uses the queries to search through the inverted index and create the search results map based
   * on partial query results.
   */
  /*
  public void partialSearch() {
    for (var query : queries) {
      if (query.isEmpty()) {
        continue;
      }
      searches.putIfAbsent(query, new ArrayList<>());
      var qList = searches.get(query);
      for (String q : query.split(" ")) {
        ArrayList<String> possibleQueries = new ArrayList<>();
        getPossibleQueries(q, possibleQueries);
        for (var possibility : possibleQueries) {
          var locationData = index.getLocations(possibility);
          if (locationData != null) {
            for (var entry : locationData) {
              Score score = null;
              String file = entry;
              for (var whereCheck : qList) {
                if (whereCheck.getWhere().equals(file)) {
                  score = whereCheck;
                  break;
                }
              }
              if (score == null) {
                score = new Score(0, 0, file);
              }
              int stemTotal = counts.get(file);
              int count = index.numOfPositions(possibility, file);
              Integer totalCount = score.getCount();
              score.setCount(count + totalCount);
              double finalScore = (double) (count + totalCount) / stemTotal;
              score.setScore(finalScore);
              if (!qList.contains(score)) {
                qList.add(score);
              }
            }
          }
        }
      }
    }
    for (var list : searches.values()) {
      Collections.sort(list);
    }
  }

  private void getPossibleQueries(String q, ArrayList<String> possibleQueries) {
    for (String indexStem : index.getWords()) { // get possible queries
      if (indexStem.startsWith(q)) {
        possibleQueries.add(indexStem);
      }
    }
  }
  */
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
}
