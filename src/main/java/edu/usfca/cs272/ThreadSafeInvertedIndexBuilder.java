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

  /** Task for the ThreadSafeInvertedIndexBuilder */
  public static class Task implements Runnable {
    /** Path of the file from which to build index. */
    private final Path path;

    /** The index to be built. */
    private final ThreadSafeInvertedIndex index;

    /**
     * Constructor for the Builder task.
     *
     * @param path Path of the file from which to build index.
     * @param index The index to be built.
     */
    public Task(Path path, ThreadSafeInvertedIndex index) {
      this.path = path;
      this.index = index;
    }

    @Override
    public void run() {
      InvertedIndex localIndex = new InvertedIndex();
      InvertedIndexBuilder localBuilder = new InvertedIndexBuilder(localIndex);
      try {
        localBuilder.build(path);
      } catch (IOException e) {
        System.err.printf("ERROR! at %s", e);
      }
      synchronized (index) {
        index.addIndex(localIndex);
      }
    }
  }
}
