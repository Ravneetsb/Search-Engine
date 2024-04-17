package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Thread Safe Inverted Index. */
public class ThreadSafeInvertedIndex extends InvertedIndex {
  private final MultiReaderLock lock;

  /** Constructor for ThreadSafe Inverted Index. */
  public ThreadSafeInvertedIndex() {
    lock = new MultiReaderLock();
  }

  @Override
  public Set<String> getWords() {
    lock.readLock().lock();
    try {
      return super.getWords();
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Set<Integer> getPositions(String word, String location) {
    lock.readLock().lock();
    try {
      return super.getPositions(word, location);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Set<String> getLocations(String word) {
    lock.readLock().lock();
    try {
      return super.getLocations(word);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean hasCount(String location) {
    lock.readLock().lock();
    try {
      return super.hasCount(location);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean hasLocation(String word, String location) {
    lock.readLock().lock();
    try {
      return super.hasLocation(word, location);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean hasPosition(String word, String location, int position) {
    lock.readLock().lock();
    try {
      return super.hasPosition(word, location, position);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Map<String, Integer> getCounts() {
    return super.getCounts();
  }

  @Override
  public boolean add(String stem, String path, int location) {
    return super.add(stem, path, location);
  }

  @Override
  public boolean addAll(String path, List<String> stems) {
    return super.addAll(path, stems);
  }

  @Override
  public boolean isEmpty() {
    return super.isEmpty();
  }

  @Override
  public int size() {
    return super.size();
  }

  @Override
  public int sizeOfCounts() {
    return super.sizeOfCounts();
  }

  @Override
  public int numOfLocations(String word) {
    return super.numOfLocations(word);
  }

  @Override
  public int numOfPositions(String word, String location) {
    return super.numOfPositions(word, location);
  }

  @Override
  public boolean hasWord(String word) {
    return super.hasWord(word);
  }

  @Override
  public ArrayList<Score> search(Set<String> queries, boolean partial) {
    return super.search(queries, partial);
  }

  @Override
  public ArrayList<Score> exactSearch(Set<String> queries) {
    return super.exactSearch(queries);
  }

  @Override
  public ArrayList<Score> partialSearch(Set<String> queries) {
    return super.partialSearch(queries);
  }

  @Override
  public void toJson(Path output) throws IOException {
    super.toJson(output);
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
