package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

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

    private final Map<String, Collection<TreeMap<String, String>>> searchMap;

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
      System.out.println(map);
      System.out.println(queries);
      for (var query : queries) {
        if (query.isEmpty()) continue;
        searchMap.putIfAbsent(query, new ArrayList<>());
        var qList = searchMap.get(query);
        for (String q : query.split(" ")) {
          var locationData = map.get(q);
          if (locationData != null) {
            for (var entry : locationData.entrySet()) {
              TreeMap<String, String> scoreMap = new TreeMap<>();
              String file = entry.getKey();
              scoreMap.put("where", file);
              int total = countsMap.get(file);
              double count = map.get(q).get(file).size();
              scoreMap.put("count", String.valueOf(count));
              scoreMap.put("score", String.valueOf(formatter.format(count / total)));
              qList.add(scoreMap);
            }
          }
        }
      }
      System.out.println(searchMap);
    }

    public void toJson(Path path) throws IOException {
      JsonWriter.writeSearch(searchMap, path);
    }
  }
}
