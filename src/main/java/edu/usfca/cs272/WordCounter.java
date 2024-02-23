package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class WordCounter {
  private final Map<String, Integer> map;

  public WordCounter() {
    this.map = new TreeMap<>();
  }

  public Map<String, Integer> getMap() {
    return Collections.unmodifiableMap(this.map);
  }

  public void write(Path output) throws IOException {
    JsonWriter.writeObject(this.map, output);
  }

  public void compute(Path path, String data) {
    this.map.compute(
        String.valueOf(path),
        (key, value) ->
            (value == null)
                ? FileStemmer.listStems(data).size()
                : FileStemmer.listStems(data).size() + value);
  }
}
