package edu.usfca.cs272;

import com.sun.source.tree.Tree;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class InvertedIndex {
  private final Map<String, Map<String, Collection<Integer>>> map;
  private final Map<String, Integer> countsMap;

/**
* Constructor for InvertedIndex
*/
  public InvertedIndex() {
    this.map = new HashMap<>();
    this.countsMap = new HashMap<>();
  }

/**
* Returns unmodifiable set of keys in the index.
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
 * @param word key in the index.
 * @return unmodifiable map where key is file path value is location of word.
*/
  public Map<String, Collection<? extends Number>> getPositions(String word) {
    return Collections.unmodifiableMap(this.map.get(word));
  }

/**
*
 * @param path Path of file
 * @param text line
 * @param arrSize 
 * @return
*/
  public int index(Path path, String text, int arrSize) {
    ArrayList<String> stems = FileStemmer.listStems(text);
    int nextVal = 0;
    for (int i = 0; i < stems.size(); i++) {
      String stem = stems.get(i);
      this.map.putIfAbsent(stem, new HashMap<>());
      this.compute(String.valueOf(path), stem, i + 1 + arrSize);
      nextVal = i + 1 + arrSize;
    }
    return (nextVal == 0) ? arrSize : nextVal;
  }

  public void index(Path path, String text) {
    ArrayList<String> stems = FileStemmer.listStems(text);
    for (int i = 0; i < stems.size(); i++) {
      String stem = stems.get(i);
      this.map.putIfAbsent(stem, new HashMap<>());
      this.compute(String.valueOf(path), stem, i + 1);
    }
  }

  private void compute(String path, String stem, int location) {
    var stemMap = this.map.get(stem);
    stemMap.putIfAbsent(path, new HashSet<>());
    var locationSet = stemMap.get(path);
    locationSet.add(location);
  }

  public boolean add(Path path, String stem, int location) {
      this.map.putIfAbsent(stem, new HashMap<>());
      this.compute(String.valueOf(path), stem, location + 1);
    return true;
  }

  public void printMap() {
    System.out.println(this.map);
  }

  public boolean isEmpty() {
    return this.map.isEmpty();
  }
}
