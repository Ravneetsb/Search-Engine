package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;

/** Thread Safe implementation of InvertedIndexBuilder */
public class ThreadSafeInvertedIndexBuilder extends InvertedIndexBuilder {

  /**
   * Constructor
   *
   * @param invertedIndex the index.
   */
  public ThreadSafeInvertedIndexBuilder(ThreadSafeInvertedIndex invertedIndex) {
    super(invertedIndex);
  }

  @Override
  public void build(Path input) throws IOException {
    super.build(input);
  }

  @Override
  public void readDirectory(Path directory) throws IOException {
    super.readDirectory(directory);
  }

  @Override
  public void readFile(Path file) throws IOException {
    super.readFile(file);
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
