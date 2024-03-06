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
   * @param location the file in which word is located.
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
      done = this.add(path, stem, location);
      location++;
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
   * writes index in pretty Json to output file.
   *
   * @param output Path of output file.
   * @throws IOException if path is invalid.
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
    return JsonWriter.writeIndex(this.map);
  }
}
