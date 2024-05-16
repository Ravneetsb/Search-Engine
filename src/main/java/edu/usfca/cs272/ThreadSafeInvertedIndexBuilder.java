package edu.usfca.cs272;

import static edu.usfca.cs272.Driver.log;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/** Thread-safe version of the Inverted index
 * @author Ravneet Singh Bhatia
 * @version Spring 2024*/
public class ThreadSafeInvertedIndexBuilder extends InvertedIndexBuilder {

  /** Work queue. */
  private final WorkQueue queue;

  /** Thread Safe inverted index. */
  private final ThreadSafeInvertedIndex index;

  /**
   * Creates a new ThreadSafeInvertedIndexBuilder
   *
   * @param invertedIndex the index.
   * @param queue workqueue
   */
  public ThreadSafeInvertedIndexBuilder(ThreadSafeInvertedIndex invertedIndex, WorkQueue queue) {
    super(invertedIndex);
    this.index = invertedIndex;
    this.queue = queue;
  }

  @Override
  public void build(Path input) throws IOException {
    super.build(input);
    queue.finish();
  }

  @Override
  public void readFile(Path file) {
    queue.execute(new Task(file));
  }

  @Override
  public String toString() {
    return super.toString();
  }

  /** Task for the ThreadSafeInvertedIndexBuilder */
  private class Task implements Runnable {
    /** Path of the file from which to build index. */
    private final Path path;

    /**
     * Constructor for the Builder task.
     *
     * @param path Path of the file from which to build index.
     */
    private Task(Path path) {
      this.path = path;
    }

    @Override
    public void run() {
      InvertedIndex localIndex = new InvertedIndex();   // creating a local index minimizes blocking.
      try {
        InvertedIndexBuilder.readFile(path, localIndex);      // populate the local index.
      } catch (IOException e) {
        log.error("Unable to read file from {}", path);
        throw new UncheckedIOException(e);
      }
      index.addIndex(localIndex);   // add the local index to the main thread-safe index.
    }

    @Override
    public String toString() {
      return "Task{" + "path=" + path + '}';
    }
  }
}
