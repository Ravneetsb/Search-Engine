package edu.usfca.cs272;

import static edu.usfca.cs272.Driver.log;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
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
   * @param threads the number of threads to use.
   */
  // TODO Create a work queue in the Driver and pass in to here
  public ThreadSafeInvertedIndexBuilder(ThreadSafeInvertedIndex invertedIndex, int threads) {
    super(invertedIndex);
    this.index = invertedIndex;
    this.queue = new WorkQueue(threads);
  }

  @Override
  public void build(Path input) throws IOException {
    if (Files.isDirectory(input)) {
      readDirectory(input);
    } else {
      queue.execute(new Task(input, index));
    }
    queue.finish();
  }

  @Override
  public void readDirectory(Path directory)
      throws IOException { // TODO Remove this, inherit original
    try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
      for (Path path : listing) {
        if (Files.isDirectory(path)) {
          readDirectory(path);
        } else {
          if (fileIsTXT(path)) {
            queue.execute(new Task(path, index));
          }
        }
      }
    }
  }

  @Override
  public void readFile(Path file) throws IOException {
    super.readFile(file);
    // TODO queue.execute(new Task(file, index));
  }

  @Override
  public String toString() {
    return super.toString();
  }

  /** Task for the ThreadSafeInvertedIndexBuilder */
  public static class Task implements Runnable { // TODO private non-static
    /** Path of the file from which to build index. */
    private final Path path;

    /** The index to be built. */
    private final ThreadSafeInvertedIndex index; // TODO Remove

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
      /* TODO This is the "easy" way to get the tests passing... but not THAT fast
      var stems = FileStemmer.listStems(path);
      index.addAll(path.toString(), stems);

      (Don't change anything, just an FYI.)
      */

      InvertedIndex localIndex = new InvertedIndex();
      InvertedIndexBuilder localBuilder =
          new InvertedIndexBuilder(localIndex); // TODO Use the static method
      try {
        localBuilder.readFile(path);
      } catch (IOException e) {
        log.error("Unable to read file from {}", path);
        // TODO throw new UncheckedIOException(e);
      }
      synchronized (index) { // TODO Consider whether you should need this
        index.addIndex(localIndex);
      }
    }
  }
}
