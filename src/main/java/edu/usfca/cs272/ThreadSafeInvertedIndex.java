package edu.usfca.cs272;

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
  public String toString() {
    return super.toString();
  }
}
