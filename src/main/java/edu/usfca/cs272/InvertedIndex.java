package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.*;

public class InvertedIndex {
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
            this.compute(String.valueOf(path), stem, i+1 + arrSize);
            nextVal = i+1+arrSize;
        }
        return nextVal;
    }
    public void index(Path path, String text) {
        ArrayList<String> stems = FileStemmer.listStems(text);
        for (int i = 0; i < stems.size(); i++) {
            String stem = stems.get(i);
            this.map.putIfAbsent(stem, new TreeMap<>());
            this.compute(String.valueOf(path), stem, i+1);
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


}
