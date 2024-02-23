package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

// TODO Format code!

public class InvertedIndex {

    private final Map<String, Map<String, Collection<Integer>>> map;
    // TODO private final Map<String, Integer> map;

    public InvertedIndex() {
        this.map = new TreeMap<>();
    }

    public Set<String> getWords() {
        return Collections.unmodifiableSet(this.map.keySet());
    }

    public Map<String, Collection<? extends Number>> getPositions(String word) {
        return Collections.unmodifiableMap(this.map.get(word));
    }

    /*
     * TODO Move all of the stemming into a new class
     * Inside of a builder class
     */

    public int index(Path path, String text, int arrSize) {
        ArrayList<String> stems = FileStemmer.listStems(text);
        int nextVal = 0;
        for (int i = 0; i < stems.size(); i++) {
            String stem = stems.get(i);
            this.map.putIfAbsent(stem, new TreeMap<>());
            this.compute(String.valueOf(path), stem, i+1 + arrSize);
            nextVal = i+1+arrSize;
        }
        return nextVal==0?arrSize:nextVal;
    }
    public void index(Path path, String text) {
    	// TODO public void index(Path path, InvertedIndex index) {
        ArrayList<String> stems = FileStemmer.listStems(text); // TODO listStems(path)
        for (int i = 0; i < stems.size(); i++) {
            String stem = stems.get(i);
            	// TODO index.add(stem, path.toString(), i + 1)
            this.map.putIfAbsent(stem, new TreeMap<>()); // TODO Move into compute
            this.compute(String.valueOf(path), stem, i+1);
        }

        // TODO index.addCount(path.toString(), stems.size());
    }

    private void compute(String path, String stem, int location) { // TODO add method, make public
        var stemMap = this.map.get(stem);
        stemMap.putIfAbsent(path, new TreeSet<>());
        var locationSet = stemMap.get(path);
        locationSet.add(location);
    }

    public void printMap() {
        System.out.println(this.map);
    }

    public void write(Path path) throws IOException {
        JsonWriter.writeIndex(this.map, path);
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }


  private final Map<String, Map<String, Collection<Integer>>> map;

  public InvertedIndex() {
    this.map = new TreeMap<>();
  }

  public Set<String> getWords() {
    return Collections.unmodifiableSet(this.map.keySet());
  }

  public Map<String, Collection<? extends Number>> getPositions(String word) {
    return Collections.unmodifiableMap(this.map.get(word));
  }

  public int index(Path path, String text, int arrSize) {
    ArrayList<String> stems = FileStemmer.listStems(text);
    int nextVal = 0;
    for (int i = 0; i < stems.size(); i++) {
      String stem = stems.get(i);
      this.map.putIfAbsent(stem, new TreeMap<>());
      this.compute(String.valueOf(path), stem, i + 1 + arrSize);
      nextVal = i + 1 + arrSize;
    }
    return (nextVal == 0) ? arrSize : nextVal;
  }

  public void index(Path path, String text) {
    ArrayList<String> stems = FileStemmer.listStems(text);
    for (int i = 0; i < stems.size(); i++) {
      String stem = stems.get(i);
      this.map.putIfAbsent(stem, new TreeMap<>());
      this.compute(String.valueOf(path), stem, i + 1);
    }
  }

  private void compute(String path, String stem, int location) {
    var stemMap = this.map.get(stem);
    stemMap.putIfAbsent(path, new TreeSet<>());
    var locationSet = stemMap.get(path);
    locationSet.add(location);
  }

  public void printMap() {
    System.out.println(this.map);
  }

  public void write(Path path) throws IOException {
    JsonWriter.writeIndex(this.map, path);
  }

  public boolean isEmpty() {
    return this.map.isEmpty();
  }

}
