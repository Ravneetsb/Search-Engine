package edu.usfca.cs272;

import java.io.IOException;
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
  private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

  /** Map for counts. */
  private final Map<String, Integer> counts;

  /** Constructor for InvertedIndex */
  public InvertedIndex() {
    this.index = new TreeMap<>();
    this.counts = new TreeMap<>();
  }

  /**
   * Returns unmodifiable set of keys in the index.
   *
   * @return unmodifiable set of keys in the index.
   */
  public Set<String> getWords() {
    return Collections.unmodifiableSet(this.index.keySet());
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
    var locations = this.index.get(word);
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
    var locations = this.index.get(word);
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
    return this.index
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
    return this.index.isEmpty();
  }

  /**
   * Returns the number of stems in the index.
   *
   * @return the number of stems in the index.
   */
  public int size() {
    return this.index.size();
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
    var locations = this.index.get(word);
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
    var locations = this.index.get(word);
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
    return this.index.containsKey(word);
  }

  /**
   * checks if the index has a location for the word.
   *
   * @param word stem in the index.
   * @param location location of the stem.
   * @return true if the stem has that location. false if the word or location is not in the index.
   */
  public boolean hasLocation(String word, String location) {
    var locations = this.index.get(word);
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
    var locations = this.index.get(word);
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
   * Method to determine if partial or exact search needs to be performed.
   *
   * @param queries set of stems in the query.
   * @param partial partial flag.
   * @return ArrayList of scores
   */
  public ArrayList<Score> search(Set<String> queries, boolean partial) {
    if (partial) {
      return partialSearch(queries);
    } else {
      return exactSearch(queries);
    }
  }

  /**
   * Performs exact search on a set of queries
   *
   * @param queries set of queries.
   * @return ArrayList of Scores.
   */
  public ArrayList<Score> exactSearch(Set<String> queries) {
    ArrayList<Score> scores = new ArrayList<>();
    for (String query : queries) {
      var locations = index.get(query);
      if (locations != null) {
        for (String location : locations.keySet()) {
          Score score = null;
          for (var existingScore : scores) {
            if (existingScore.getLocation().equals(location)) {
              score = existingScore;
            }
          }
          if (score == null) {
            score = new Score(0, 0, location);
          }
          int stemTotal = counts.get(location);
          int count = index.get(query).get(location).size();
          int totalCount = score.getCount();
          score.setCount(count + totalCount);
          score.setScore(((double) count + totalCount) / stemTotal);
          if (!scores.contains(score)) {
            scores.add(score);
          }
        }
      }
    }
    Collections.sort(scores);
    return scores;
  }

  /**
   * Performs partial search on index
   *
   * @param queries set of queries
   * @return ArrayList of Scores
   */
  public ArrayList<Score> partialSearch(Set<String> queries) {
    ArrayList<Score> scores = new ArrayList<>();
    for (String query : queries) {
      var set = index.navigableKeySet().tailSet(query, true);
      for (String stem : set) {
        if (stem.startsWith(query)) {
          var locations = index.get(stem);
          if (locations != null) {
            for (String location : locations.keySet()) {
              Score score = null;
              for (var existingScore : scores) {
                if (existingScore.getLocation().equals(location)) {
                  score = existingScore;
                  break;
                }
              }
              if (score == null) {
                score = new Score(0, 0, location);
              }
              int stemTotal = counts.get(location);
              int count = index.get(stem).get(location).size();
              int totalCount = score.getCount();
              score.setCount(count + totalCount);
              score.setScore(((double) count + totalCount) / stemTotal);
              if (!scores.contains(score)) {
                scores.add(score);
              }
            }
          }
        } else {
          break;
        }
      }
    }
    Collections.sort(scores);
    return scores;
  }

  /**
   * writes index in pretty Json to output file.
   *
   * @param output Path of output file.
   * @throws IOException if path is invalid.
   */
  public void toJson(Path output) throws IOException {
    JsonWriter.writeIndex(this.index, output);
  }

  /**
   * Returns Inverted Index in pretty Json.
   *
   * @return Inverted Index in pretty Json.
   */
  @Override
  public String toString() {
    return JsonWriter.writeIndex(index);
  }

  /** ScoreMap class to store the result */
  public class Score implements Comparable<Score> {
    /** Number of times query is present in file. */
    private Integer count;

    /** Score for the query. Given by count / totalNumOfStems */
    private Double score;

    /** File path where query word is located. */
    private String location;

    /**
     * Constructor for the ScoreMap
     *
     * @param count number of times a query word was present in the file.
     * @param score score of the query in the file.
     * @param stemLocation the file where the query was searched.
     */
    public Score(int count, double score, String stemLocation) {
      this.count = count;
      this.score = score;
      this.location = stemLocation;
    }

    /** Constructor for ScoreMap. Defaults Numbers to 0 and location to null.` */
    public Score() {
      this.count = 0;
      this.score = 0.0;
      this.location = "";
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
    public String getLocation() {
      return location;
    }

    /**
     * setter for where
     *
     * @param location file path
     */
    public void setLocation(String location) {
      this.location = location;
    }

    @Override
    public int compareTo(Score other) {
      int scoreCompare = other.getScore().compareTo(this.getScore());
      if (scoreCompare == 0) {
        int countCompare = other.getCount().compareTo(this.getCount());
        if (countCompare == 0) {
          return Path.of(this.getLocation()).compareTo(Path.of(other.getLocation()));
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
          + ", location='"
          + location
          + '\''
          + '}';
    }
  }
}
