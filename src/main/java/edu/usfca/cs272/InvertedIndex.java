package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * InvertedIndex Data Structure for the Search Engine Project.
 *
 * @author Ravneet Singh Bhatia, CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class InvertedIndex {
  /** Map for Index. */
  private final Map<String, Map<String, Collection<Integer>>> map;

  /** Map for counts. */
  private final Map<String, Integer> countsMap;

  /** Constructor for InvertedIndex */
  public InvertedIndex() {
    this.map = new TreeMap<>();
    this.countsMap = new TreeMap<>();
  }

  /**
   * Returns unmodifiable set of keys in the index.
   *
   * @return unmodifiable set of keys in the index.
   */
  public Set<String> getWords() {
    return Collections.unmodifiableSet(this.map.keySet());
  }

  /**
   * Returns unmodifiable map of files and their stem counts.
   *
   * @return unmodifiable map of files and their stem counts.
   */
  public Map<String, Integer> getCounts() {
    return Collections.unmodifiableMap(this.countsMap);
  }

  /**
   * Returns unmodifiable map where key is file path value is location of word.
   *
   * @param word key in the index.
   * @param location file name.
   * @return unmodifiable map where key is file path value is location of word.
   */
  public Collection<? extends Number> getPositions(String word, String location) {
    return Collections.unmodifiableCollection(this.map.get(word).get(location));
  }

  /**
   * add stem to index.
   *
   * @param path file path
   * @param stem word stem
   * @param location index in stemList
   * @return true if added successfully.
   */
  public boolean add(String path, String stem, int location) {
    this.map.putIfAbsent(stem, new TreeMap<>());
    try {
      var stemMap = this.map.get(stem);
      stemMap.putIfAbsent(path, new TreeSet<>());
      var locationSet = stemMap.get(path);
      locationSet.add(location + 1);
    } catch (RuntimeException e) {
      return false;
    }
    return true;
  }

  /**
   * Add a collection of stems to Index.
   *
   * @param path Path of file.
   * @param stems Collection of stems
   * @return true if successful.
   */
  public boolean addAll(String path, Collection<String> stems) {
    boolean done = false;
    int location = 0;
    for (String stem : stems) {
      done = this.add(path, stem, location++);
    }
    return done;
  }

  /**
   * Returns true if index is empty.
   *
   * @return true if index is empty.
   */
  public boolean isEmpty() {
    return this.map.isEmpty();
  }

  /**
   * Returns the number of stems in the index.
   *
   * @return the number of stems in the index.
   */
  public int size() {
    return this.map.size();
  }

  /**
   * writes the index to output in pretty json
   *
   * @param output file name
   * @throws IOException if error in writing to file.
   */
  public void toJson(Path output) throws IOException {
    JsonWriter.writeIndex(this.map, output);
  }

  /**
   * add file path and the number of stems in that file to countsMap.
   *
   * @param file path of file.
   * @param size number of stems.
   */
  public void addCounts(String file, int size) {
    this.countsMap.put(String.valueOf(file), size);
  }

  @Override
  public String toString() {
    return JsonWriter.writeIndex(map);
  }

  /**
   * create new instance of the inner Searcher class.
   *
   * @param query path of query
   * @return new Searcher object.
   */
  public Searcher newSearcher(Path query, boolean partial) {
    try {
      return new Searcher(query, partial);
    } catch (IOException e) {
      System.out.println("Query not found");
    }
    return null;
  }

  /** Searcher class for the inverted index. */
  public class Searcher {

    /** ScoreMap class to store the result */
    public class ScoreMap implements Comparable<ScoreMap> {
      private Integer count;
      private Double score;
      private String where;

      /**
       * Constructor for the ScoreMap
       *
       * @param count number of times a query word was present in the file.
       * @param score score of the query in the file.
       * @param where the file where the query was searched.
       */
      public ScoreMap(int count, double score, String where) {
        this.count = count;
        this.score = score;
        this.where = where;
      }

      /** Constructor for ScoreMap. Defaults Numbers to 0 and where to null.` */
      public ScoreMap() {
        this.count = 0;
        this.score = 0.0;
        this.where = null;
      }

      /**
       * getter for count
       *
       * @return count
       */
      public Integer getCount() {
        return count;
      }

      /**
       * setter for count
       *
       * @param count number of times query stem is present in the file.
       */
      public void setCount(int count) {
        this.count = count;
      }

      /**
       * getter for score.
       *
       * @return the score of the query in the file.
       */
      public Double getScore() {
        return score;
      }

      /**
       * sets the score for the query
       *
       * @param score score of the query. given by counts/total stems in file.
       */
      public void setScore(double score) {
        this.score = score;
      }

      /**
       * getter for where
       *
       * @return the file path
       */
      public String getWhere() {
        return where;
      }

      /**
       * stter for where
       *
       * @param where file path
       */
      public void setWhere(String where) {
        this.where = where;
      }

      @Override
      public int compareTo(ScoreMap other) {
        int scoreCompare = other.getScore().compareTo(this.getScore());
        if (scoreCompare == 0) {
          int countCompare = other.getCount().compareTo(this.getCount());
          if (countCompare == 0) {
            return Path.of(this.getWhere()).compareTo(Path.of(other.getWhere()));
          } else return countCompare;
        } else return scoreCompare;
      }

      @Override
      public String toString() {
        return "ScoreMap{"
            + "count="
            + count
            + ", score="
            + score
            + ", where='"
            + where
            + '\''
            + '}';
      }
    }

    /** Queries TreeSet. */
    private final TreeSet<String> queries;

    /** Map of the query and its score */
    private final TreeMap<String, List<ScoreMap>> searchMap;

    /** partial tag for the search. */
    public final boolean PARTIAL;

    /**
     * Constructor for Searcher
     *
     * @param query path of the queries file.
     * @throws IOException if the file doesn't exist or path is null.
     */
    public Searcher(Path query, boolean partial) throws IOException {
      this.queries = parseQuery(query);
      this.searchMap = new TreeMap<>();
      this.PARTIAL = partial;
    }

    /**
     * gives the cleaned set of queries.
     *
     * @param query file path of queries file.
     * @return Set of queries.
     * @throws IOException if the file path is invalid.
     */
    private TreeSet<String> parseQuery(Path query) throws IOException {
      TreeSet<String> treeSet = new TreeSet<>();
      try (BufferedReader br = Files.newBufferedReader(query, UTF_8)) {
        String line;
        while ((line = br.readLine()) != null) {
          var stems = FileStemmer.uniqueStems(line);
          StringJoiner sb = new StringJoiner(" ");
          for (var q : stems) {
            if (!q.isEmpty()) {
              sb.add(q);
            }
          }
          treeSet.add(sb.toString());
        }
      }
      return treeSet;
    }

    /** calls search based on partial flag. */
    public void search() {
      if (!PARTIAL) {
        exactSearch();
      } else {
        partialSearch();
      }
    }

    /**
     * uses the queries to search through the inverted index and create the search results map based
     * on partial query results.
     */
    public void partialSearch() {
      for (var query : queries) {
        if (query.isEmpty()) {
          continue;
        }
        searchMap.putIfAbsent(query, new ArrayList<>());
        var qList = searchMap.get(query);
        for (String q : query.split(" ")) {
          ArrayList<String> possibleQueries = new ArrayList<>();
          for (String indexStem : map.keySet()) { // get possible queries
            if (indexStem.startsWith(q)) {
              possibleQueries.add(indexStem);
            }
          }
          //          System.out.println(possibleQueries);
          for (var possibility : possibleQueries) {
            var locationData = map.get(possibility);
            if (locationData != null) {
              var set = locationData.entrySet();
              for (var entry : set) {
                ScoreMap scoreMap = null;
                String file = entry.getKey();
                for (var whereCheck : qList) {
                  if (whereCheck.getWhere().equals(file)) {
                    scoreMap = whereCheck;
                    break;
                  }
                }
                if (scoreMap == null) {
                  scoreMap = new ScoreMap(0, 0, file);
                }
                int stemTotal = countsMap.get(file);
                int count = map.get(possibility).get(file).size();
                Integer totalCount = scoreMap.getCount();
                Double existingStemTotal = scoreMap.getScore();
                scoreMap.setCount(count + totalCount);
                var finalScore = Double.sum((double) count / stemTotal, existingStemTotal);
                scoreMap.setScore(finalScore);
                if (!qList.contains(scoreMap)) {
                  qList.add(scoreMap);
                }
              }
            }
          }
        }
      }
      for (var list : searchMap.values()) {
        Collections.sort(list);
      }
      searchMap.get("b").forEach(System.out::println);
    }

    /** uses the queries to search through the inverted index and create the search results map. */
    public void exactSearch() {
      for (var query : queries) {
        if (query.isEmpty()) {
          continue;
        }
        searchMap.putIfAbsent(query, new ArrayList<>());
        var qList = searchMap.get(query);
        for (String q : query.split(" ")) {
          var locationData = map.get(q);
          if (locationData != null) {
            var set = locationData.entrySet();
            for (var entry : set) {
              ScoreMap scoreMap = null;
              String file = entry.getKey();
              for (var whereCheck : qList) {
                if (whereCheck.getWhere().equals(file)) {
                  scoreMap = whereCheck;
                  break;
                }
              }
              if (scoreMap == null) {
                scoreMap = new ScoreMap(0, 0, file);
              }
              int stemTotal = countsMap.get(file);
              int count = map.get(q).get(file).size();
              Integer totalCount = scoreMap.getCount();
              Double existingStemTotal = scoreMap.getScore();
              scoreMap.setCount(count + totalCount);
              scoreMap.setScore(Double.sum((double) count / stemTotal, existingStemTotal));
              if (!qList.contains(scoreMap)) {
                qList.add(scoreMap);
              }
            }
          }
        }
      }
      for (var lists : searchMap.values()) {
        Collections.sort(lists);
      }
    }

    /**
     * Writes the search results map to path in pretty json
     *
     * @param path Path of results output file
     * @throws IOException if the file doesn't exist or path is null.
     */
    public void toJson(Path path) throws IOException {
      JsonWriter.writeSearch(searchMap, path);
    }

    /**
     * to String method for Searcher
     *
     * @return toString
     */
    @Override
    public String toString() {
      return JsonWriter.writeSearch(searchMap);
    }
  }
}
