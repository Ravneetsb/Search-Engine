package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.*;

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

  private void compute(String path, String stem, int location) {
    var stemMap = this.map.get(stem);
    stemMap.putIfAbsent(path, new TreeSet<>());
    var locationSet = stemMap.get(path);
    locationSet.add(location);
  }

  public boolean add(Path path, String stem, int location) {
    this.map.putIfAbsent(stem, new TreeMap<>());
    this.compute(String.valueOf(path), stem, location + 1);
    return true;
  }

  public boolean isEmpty() {
    return this.map.isEmpty();
  }

  public int size() {
    return this.map.size();
  }

  public Set<Map.Entry<String, Map<String, Collection<Integer>>>> entrySet() {
    return this.map.entrySet();
  }

  public void addCounts(Path file, int size) {
    this.countsMap.put(String.valueOf(file), size);
  }
}
