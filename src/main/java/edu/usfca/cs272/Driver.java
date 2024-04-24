package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class responsible for running this project based on the provided command-line arguments. See the
 * README for details.
 *
 * @author Ravneet Singh Bhatia
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class Driver {

  /** Default path for output file for index. */
  public static final Path DEFAULT_INDEX = Path.of("index.json");

  /** Default path for output file for counts. */
  public static final Path DEFAULT_COUNTS = Path.of("counts.json");

  /** Default path for output file for results. */
  public static final Path DEFAULT_RESULTS = Path.of("results.json");

  /** Default number of threads to be used when multithreading. */
  public static final int DEFAULT_THREADS = 5;

  /** Log */
  public static final Logger log = LogManager.getLogger();

  /**
   * Initializes the classes necessary based on the provided command-line arguments. This includes
   * (but is not limited to) how to build or search an inverted index.
   *
   * @param args flag/value pairs used to start this program
   */
  public static void main(String[] args) {
    // store initial start time
    Instant start = Instant.now();

    ArgumentParser argParser = new ArgumentParser(args);

    InvertedIndex index;
    InvertedIndexBuilder invertedIndexBuilder;
    QueryProcessor processor;

    ThreadSafeInvertedIndex threadSafeInvertedIndex;
    ThreadSafeInvertedIndexBuilder threadSafeInvertedIndexBuilder;
    ThreadSafeQueryProcessor threadSafeProcessor;
    boolean singleThread = !argParser.hasFlag("-threads");

    if (singleThread) {
      index = new InvertedIndex();
      if (argParser.hasValue("-text")) {
        Path path = argParser.getPath("-text");
        log.info("Using {} for source.", path);

        try {
          invertedIndexBuilder = new InvertedIndexBuilder(index);
          invertedIndexBuilder.build(path);
          log.info("Build complete.");
        } catch (IOException e) {
          log.error("Unable to build Index from path: %s", path);
        }
      }

      if (argParser.hasFlag("-counts")) {
        Path countOutput = argParser.getPath("-counts", DEFAULT_COUNTS);
        try {
          JsonWriter.writeObject(index.getCounts(), countOutput);
          log.info("Counts written to {}", countOutput);
        } catch (IOException e) {
          System.err.printf("Unable to write Counts Map to path: %s", countOutput);
        }
      }

      if (argParser.hasFlag("-index")) {
        Path indexOutput = argParser.getPath("-index", DEFAULT_INDEX);
        try {
          index.toJson(indexOutput);
          log.info("Index written to {}", indexOutput);
        } catch (IOException e) {
          System.err.printf("Unable to write Inverted Index to path: %s", indexOutput);
        }
      }
      processor = new QueryProcessor(index, argParser.hasFlag("-partial"));
      if (argParser.hasValue("-query")) {
        Path query = argParser.getPath("-query");
        try {
          processor.parseQuery(query);
        } catch (IOException e) {
          System.err.printf("Can't read from file %s", query);
        }
      }

      if (argParser.hasFlag("-results")) {
        Path results = argParser.getPath("-results", DEFAULT_RESULTS);
        try {
          processor.toJson(results);
        } catch (IOException e) {
          System.err.printf("Unable to write to file %s.", results);
        }
      }
    } else {
      threadSafeInvertedIndex = new ThreadSafeInvertedIndex();
      int threads = argParser.getInteger("-threads", DEFAULT_THREADS);
      if (threads <= 0) {
        threads = DEFAULT_THREADS;
      }
      WorkQueue queue = new WorkQueue(threads);

      if (argParser.hasValue("-text")) {
        Path path = argParser.getPath("-text");
        log.info("Using {} for source.", path);

        try {
          threadSafeInvertedIndexBuilder =
              new ThreadSafeInvertedIndexBuilder(threadSafeInvertedIndex, queue);
          threadSafeInvertedIndexBuilder.build(path);
          log.info("Build complete.");
        } catch (IOException e) {
          log.error("Unable to build Index from path: %s", path);
        }
      }

      if (argParser.hasFlag("-counts")) {
        Path countOutput = argParser.getPath("-counts", DEFAULT_COUNTS);
        try {
          JsonWriter.writeObject(threadSafeInvertedIndex.getCounts(), countOutput);
          log.info("Counts written to {}", countOutput);
        } catch (IOException e) {
          System.err.printf("Unable to write Counts Map to path: %s", countOutput);
        }
      }

      if (argParser.hasFlag("-index")) {
        Path indexOutput = argParser.getPath("-index", DEFAULT_INDEX);
        try {
          threadSafeInvertedIndex.toJson(indexOutput);
          log.info("Index written to {}", indexOutput);
        } catch (IOException e) {
          System.err.printf("Unable to write Inverted Index to path: %s", indexOutput);
        }
      }
      threadSafeProcessor =
          new ThreadSafeQueryProcessor(
              threadSafeInvertedIndex, queue, argParser.hasFlag("-partial"));
      if (argParser.hasValue("-query")) {
        Path query = argParser.getPath("-query");
        try {
          threadSafeProcessor.parseQuery(query);
        } catch (IOException e) {
          System.err.printf("Can't read from file %s", query);
        }
      }

      queue.join();

      if (argParser.hasFlag("-results")) {
        Path results = argParser.getPath("-results", DEFAULT_RESULTS);
        try {
          threadSafeProcessor.toJson(results);
        } catch (IOException e) {
          System.err.printf("Unable to write to file %s.", results);
        }
      }
    }

    // calculate time elapsed and output
    long elapsed = Duration.between(start, Instant.now()).toMillis();
    double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
    System.out.printf("Elapsed: %f seconds%n", seconds);
  }
}
