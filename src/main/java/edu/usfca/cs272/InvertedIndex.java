package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.*;

/**
* InvertedIndex Data Structure for the Search Engine Project.
 * @author Ravneet Singh Bhatia, CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
*/
public class InvertedIndex {
  private final Map<String, Map<String, Collection<Integer>>> map;
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
  public Map<String, Collection<? extends Number>> getPositions(String word) {
    return Collections.unmodifiableMap(this.map.get(word));
  }

/**
* Helper function to populate index.
 * @param path String value of file path
 * @param stem word stem
 * @param location index in stemList
*/
  private void add(String path, String stem, int location) {
    var stemMap = this.map.get(stem);
    stemMap.putIfAbsent(path, new TreeSet<>());
    var locationSet = stemMap.get(path);
    locationSet.add(location);
  }

/**
* add stem to index.
 * @param path file path
 * @param stem word stem
 * @param location index in stemList
 * @return true if added successfully.
*/
  public boolean add(Path path, String stem, int location) {
    this.map.putIfAbsent(stem, new TreeMap<>());
    try {
      this.add(String.valueOf(path), stem, location + 1);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

/**
* Returns true if index is empty.
 * @return true if index is empty.
*/
  public boolean isEmpty() {
    return this.map.isEmpty();
  }

/**
* Returns the number of stems in the index.
 * @return the number of stems in the index.
*/
  public int size() {
    return this.map.size();
  }

/**
* Returns entrySet for index.
 * @return entrySet for index.
*/
  public Set<Map.Entry<String, Map<String, Collection<Integer>>>> entrySet() {
    return this.map.entrySet();
  }

/**
* add file path and the number of stems in that file to countsMap.
 * @param file path of file.
 * @param size number of stems.
*/
  public void addCounts(Path file, int size) {
    this.countsMap.put(String.valueOf(file), size);
  }
}
