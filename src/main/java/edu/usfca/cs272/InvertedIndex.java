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
    var set = this.map.get(word).get(location); // TODO Throws a null pointer if get(word) is null! Could have a better name
    if (set != null) {
      return Collections.unmodifiableSet(set);
    } else {
      return new TreeSet<>(); // TODO Collections.emptySet();
    }
    
    /* TODO Have to nest get and null checks, like this:
    var locations = this.map.get(word);
    
    if (locations != null) {
        var positions = locations.get(location);
        
        if (positions != null) {
            return Collections.unmodifiableSet(set);
        }
        
    }
    
    return Collections.emptySet(); 
    */
  }

  // TODO Missing a getLocations(String word) method?
  
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
  }

  /**
   * Add a collection of stems to Index.
   *
   * @param path Path of file.
   * @param stems Collection of stems
   * @return true if successful.
   */
  public boolean addAll(String path, Collection<String> stems) { // TODO List<String> stems... must have order!
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
    return this.map.get(word).size(); // TODO exception if get(word) is null
  }
  
  // TODO Missing numOfPositions(String word, String location)

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
  	// TODO Nice reuse, but not efficient (and since you have the most efficient add, need most efficient implementations in the rest too)
    if (hasWord(word)) {
      return this.map.get(word).containsKey(location);
    } else {
      return false;
    }
  }
  
  // TODO Missing hasPosition

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
    return JsonWriter.writeIndex(this.map);
  }
}
