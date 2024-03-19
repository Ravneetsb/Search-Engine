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
  private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> map;

  /** Map for counts. */
  private final Map<String, Integer> counts;

  /** Constructor for InvertedIndex */
  public InvertedIndex() {
    this.map = new TreeMap<>();
    this.counts = new TreeMap<>();
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
    return Collections.unmodifiableMap(this.counts);
  }

  /**
   * Returns unmodifiable map where key is file path value is location of word.
   *
   * @param word key in the index.
   * @param location the file in which word is located.
   * @return unmodifiable map where key is file path value is location of word.
   */
  public Set<Integer> getPositions(String word, String location) {
    var locations = this.map.get(word);
    if (locations != null) {
      var positions = locations.get(location);
      if (positions != null) {
        return Collections.unmodifiableSet(positions);
      }
    }
    return Collections.emptySet();
  }

  /**
   * returns the locations in which a word occurs
   *
   * @param word stem to be searched
   * @return locations.
   */
  public Set<String> getLocations(String word) {
    var locations = this.map.get(word);
    if (locations != null) {
      return Collections.unmodifiableSet(locations.keySet());
    }
    return Collections.emptySet();
  }

  /**
   * add stem to index.
   *
   * @param stem word stem
   * @param path file path
   * @param location index in stemList
   * @return true if added successfully.
   */
  public boolean add(String stem, String path, int location) {
    return this.map
        .computeIfAbsent(stem, s -> new TreeMap<>())
        .computeIfAbsent(path, p -> new TreeSet<>())
        .add(location + 1);
  }

  /**
   * Add a collection of stems to Index.
   *
   * @param path Path of file.
   * @param stems Collection of stems
   * @return true if successful.
   */
  public boolean addAll(String path, List<String> stems) {
    int location = 0;
    for (String stem : stems) {
      this.add(stem, path, location);
      location++;
    }
    return true;
  }

  /**
   * add file path and the number of stems in that file to countsMap.
   *
   * @param file path of file.
   * @param size number of stems.
   * @return true if size was valid.
   */
  public boolean addCounts(String file, int size) {
    if (size < 1) {
      return false;
    }
    this.counts.put(file, size);
    return true;
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
   * Returns the number of locations for which there is a stem count.
   *
   * @return the number of locations for which there is a stem count.
   */
  public int sizeOfCounts() {
    return this.counts.size();
  }

  /**
   * Returns the number of locations where a word occurs.
   *
   * @param word stem to be searched.
   * @return the number of locations where a word occurs.
   */
  public int numOfLocations(String word) {
    var locations = this.map.get(word);
    if (locations != null) {
      return locations.size();
    }
    return 0;
  }

  /**
   * Returns the number of positions where a word occurs in a location.
   *
   * @param word stem to be searched.
   * @param location file path.
   * @return the number of positions where the word occurs in location.
   */
  public int numOfPositions(String word, String location) {
    var locations = this.map.get(word);
    if (locations != null) {
      var positions = locations.get(location);
      return positions.size();
    }
    return 0;
  }

  /**
   * checks if word is in the index or not.
   *
   * @param word stem to be looked up
   * @return true if word is in the index.
   */
  public boolean hasWord(String word) {
    return this.map.containsKey(word);
  }

  /**
   * checks if the index has a location for the word.
   *
   * @param word stem in the index.
   * @param location location of the stem.
   * @return true if the stem has that location. false if the word or location is not in the index.
   */
  public boolean hasLocation(String word, String location) {
    var locations = this.map.get(word);
    if (locations != null) {
      return locations.containsKey(location);
    }
    return false;
  }

  /**
   * Checks to see if a word is present in location at specified position
   *
   * @param word stem to be checked
   * @param location file path
   * @param position position in file to be checked.
   * @return true if the word is found in the location at specified location.
   */
  public boolean hasPosition(String word, String location, int position) {
    var locations = this.map.get(word);
    if (locations != null) {
      var positions = locations.get(location);
      if (positions != null) {
        return positions.contains(position);
      }
    }
    return false;
  }

  /**
   * checks if stem counts for a location is present.
   *
   * @param location path of file.
   * @return true if there is a stem count for the location.
   */
  public boolean hasCount(String location) {
    return this.counts.containsKey(location);
  }

  /**
   * writes index in pretty Json to output file.
   *
   * @param output Path of output file.
   * @throws IOException if path is invalid.
   */
  public void toJson(Path output) throws IOException {
    JsonWriter.writeIndex(this.map, output);
  }

  /**
   * Returns Inverted Index in pretty Json.
   *
   * @return Inverted Index in pretty Json.
   */
  @Override
  public String toString() {
    return JsonWriter.writeIndex(map);
  }

  /**
   * create new instance of the inner Searcher class.
   *
   * @param query path of query
   * @param partial true if partial search is to be performed.
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
    public final boolean partial;

    /**
     * Constructor for Searcher
     *
     * @param query path of the queries file.
     * @param partial true if partial search is to be performed.
     * @throws IOException if the file doesn't exist or path is null.
     */
    public Searcher(Path query, boolean partial) throws IOException {
      this.queries = parseQuery(query);
      this.searchMap = new TreeMap<>();
      this.partial = partial;
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
      if (partial) {
        partialSearch();
      } else {
        exactSearch();
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
                int stemTotal = counts.get(file);
                int count = map.get(possibility).get(file).size();
                Integer totalCount = scoreMap.getCount();
                scoreMap.setCount(count + totalCount);
                double finalScore = (double) (count + totalCount) / stemTotal;
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
              int stemTotal = counts.get(file);
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
