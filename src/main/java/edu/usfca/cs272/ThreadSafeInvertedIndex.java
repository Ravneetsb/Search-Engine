package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Thread Safe Inverted Index. */
public class ThreadSafeInvertedIndex extends InvertedIndex {
  /** The multiReaderLock for the index. */
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
    lock.readLock().lock();
    try {
      return super.getCounts();
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean add(String stem, String path, int location) {
    lock.writeLock().lock();
    try {
      return super.add(stem, path, location);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public boolean addAll(String path, List<String> stems) {
    lock.writeLock().lock();
    try {
      return super.addAll(path, stems);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public boolean addIndex(InvertedIndex invertedIndex) {
    lock.writeLock().lock();
    try {
      return super.addIndex(invertedIndex);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public boolean isEmpty() {
    lock.readLock().lock();
    try {
      return super.isEmpty();
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public int size() {
    lock.readLock().lock();
    try {
      return super.size();
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public int sizeOfCounts() {
    lock.readLock().lock();
    try {
      return super.sizeOfCounts();
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public int numOfLocations(String word) {
    lock.readLock().lock();
    try {
      return super.numOfLocations(word);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public int numOfPositions(String word, String location) {
    lock.readLock().lock();
    try {
      return super.numOfPositions(word, location);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean hasWord(String word) {
    lock.readLock().lock();
    try {
      return super.hasWord(word);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public ArrayList<Score> exactSearch(Set<String> queries) {
    lock.readLock().lock();
    try {
      return super.exactSearch(queries);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public ArrayList<Score> partialSearch(Set<String> queries) {
    lock.readLock().lock();
    try {
      return super.partialSearch(queries);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void toJson(Path output) throws IOException {
    lock.readLock().lock();
    try {
      super.toJson(output);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public String toString() {
    lock.readLock().lock();
    try {
      return super.toString();
    } finally {
      lock.readLock().unlock();
    }
  }
}
