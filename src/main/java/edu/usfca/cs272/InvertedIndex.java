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
    var set = this.map.get(word).get(location);
    if (set != null) {
      return Collections.unmodifiableSet(set);
    } else {
      return new TreeSet<>();
    }
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
    this.map
        .computeIfAbsent(stem, s -> new TreeMap<>())
        .computeIfAbsent(path, p -> new TreeSet<>())
        .add(location + 1);
    return true;

    /*
     * TODO Time to refactor this add method. It can be either more compact, more
     * efficient, or both:
     *
     * 1. Focus on making the most compact code possible with a 3 line solution and
     * putIfAbsent, but extra get calls. 2. Focus on making the most efficient code
     * possible by reducing the number of times the underlying data is accessed
     * (without using putIfAbsent or containsKey methods). 3. Focus on making the
     * most compact and efficient code by using lambda expressions and the
     * computeIfAbsent method.
     *
     * Choose one option, then make the same design choice in all your other
     * methods!
     */
  }

  /**
   * Add a collection of stems to Index.
   *
   * @param path Path of file.
   * @param stems Collection of stems
   * @return true if successful.
   */
  public boolean addAll(String path, Collection<String> stems) {
    int location = 0;
    for (String stem : stems) {
      this.add(stem, path, location);
      location++;
    }
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

  /*
   * TODO This class is still missing some has/contains methods and num/size
   * methods, and one get/view method (choose a naming scheme and stick to it).
   * For example:
   *
   * hasWord(String word) → does the inverted index have this word?
   *
   * hasLocation(String word, String location) → does the inverted index have this
   * location for this word?
   *
   * hasPosition(String word, String location, Integer position) → does the
   * inverted index have this position for the given location and word?
   *
   * hasCount(String location) → does the word counts map have a count for this
   * location?
   *
   * There are usually the same number of get, has, and num methods.
   */

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
  public boolean addCounts(String file, int size) {
    if (size < 1) {
      return false;
    }
    this.counts.put(file, size);
    return true;
  }

  @Override
  public String toString() {
    return JsonWriter.writeIndex(this.map);
  }

  /*
   * TODO Use the outline view to reorder and group your methods so
   * the add methods are grouped together
   */
}
