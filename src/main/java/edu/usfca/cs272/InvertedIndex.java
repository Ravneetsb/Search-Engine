package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
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
    } catch (Exception e) {
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

  public Searcher newSearcher(Path query) {
    try {
      return new Searcher(query);
    } catch (IOException e) {
      System.out.println("Query not found");
    }
    return null;
  }

  public class Searcher {
    private final TreeSet<String> queries;

    private final TreeMap<String, List<TreeMap<String, String>>> searchMap;

    public Searcher(Path query) throws IOException {
      this.queries = parseQuery(query);
      this.searchMap = new TreeMap<>();
    }

    private TreeSet<String> parseQuery(Path query) throws IOException {
      TreeSet<String> queries = new TreeSet<>();
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
          queries.add(sb.toString());
        }
      } catch (IOException e) {
        // Do nothing
      }
      return queries;
    }

    public void search() {
      DecimalFormat formatter = new DecimalFormat("0.00000000");
      for (var query : queries) {
        if (query.isEmpty()) continue;
        searchMap.putIfAbsent(query, new ArrayList<>());
        var qList = searchMap.get(query);
        for (String q : query.split(" ")) {
          var locationData = map.get(q);
          if (locationData != null) {
            var set = locationData.entrySet();
            for (var entry : set) {
              TreeMap<String, String> scoreMap = null;
              String file = entry.getKey();
              for (var whereCheck : qList) {
                if (whereCheck.get("where").equals(file)) {
                  scoreMap = whereCheck;
                  break;
                }
              }
              if (scoreMap == null) {
                scoreMap = new TreeMap<>();
                scoreMap.put("where", file);
              }
              int stemTotal = countsMap.get(file);
              int count = map.get(q).get(file).size();
              int totalCount = Integer.parseInt(scoreMap.getOrDefault("count", "0"));
              double existingStemTotal = Double.parseDouble(scoreMap.getOrDefault("score", "0"));
              scoreMap.put("count", String.valueOf((count + totalCount)));
              scoreMap.put(
                  "score",
                  String.valueOf(
                      formatter.format(Double.sum((double) count / stemTotal, existingStemTotal))));
              if (!qList.contains(scoreMap)) {
                qList.add(scoreMap);
              }
              try {
                sortFiles(qList);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
          }
        }
      }
    }

    /**
     * @param qList List
     * @throws IOException noce
     */
    public void sortFiles(List<TreeMap<String, String>> qList) throws IOException {
      Collections.sort(
          qList,
          (mapOne, mapTwo) -> {
            int scoreCompare =
                Double.compare(
                    Double.parseDouble(mapTwo.get("score")),
                    Double.parseDouble(mapOne.get("score")));
            if (scoreCompare == 0) {
              int countCompare =
                  Integer.compare(
                      Integer.parseInt(mapTwo.get("count")), Integer.parseInt(mapOne.get("count")));
              if (countCompare == 0) {
                return Path.of(mapOne.get("where")).compareTo(Path.of(mapTwo.get("where")));
              } else return countCompare;
            } else return scoreCompare;
          });
    }

    public void toJson(Path path) throws IOException {
      JsonWriter.writeSearch(searchMap, path);
    }
  }
}
