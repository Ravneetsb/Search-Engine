package edu.usfca.cs272;

import static edu.usfca.cs272.Driver.log;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Thread Safe implementation of InvertedIndexBuilder */
public class ThreadSafeInvertedIndexBuilder extends InvertedIndexBuilder {

  /** Work queue. */
  private final WorkQueue queue;

  /** Thread Safe inverted index. */
  private final ThreadSafeInvertedIndex index;

  /**
   * Constructor
   *
   * @param invertedIndex the index.
   * @param queue The workqueue
   */
  public ThreadSafeInvertedIndexBuilder(ThreadSafeInvertedIndex invertedIndex, WorkQueue queue) {
    super(invertedIndex);
    this.index = invertedIndex;
    this.queue = queue;
  }

  @Override
  public void build(Path input) throws IOException {
    if (Files.isDirectory(input)) {
      readDirectory(input);
    } else {
      queue.execute(new Task(input));
    }
    queue.finish();
  }

  @Override
  public void readFile(Path file) throws IOException {
    super.readFile(file);
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
      /* TODO This is the "easy" way to get the tests passing... but not THAT fast
      var stems = FileStemmer.listStems(path);
      index.addAll(path.toString(), stems);

      (Don't change anything, just an FYI.)
      */

      InvertedIndex localIndex = new InvertedIndex();
      try {
        InvertedIndexBuilder.readFile(path, index);
      } catch (IOException e) {
        log.error("Unable to read file from {}", path);
        throw new UncheckedIOException(e);
      }
      index.addIndex(localIndex);
    }
  }
}
