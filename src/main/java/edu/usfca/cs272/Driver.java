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

  /** The default number of webpages to crawl. */
  public static final int DEFAUT_CRAWL = 1;

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
    InvertedIndexBuilder builder;
    Processor processor;
    WorkQueue queue = null;
    WebCrawler crawler = null;
    boolean partial = argParser.hasFlag("-partial");

    if (argParser.hasFlag("-threads") || argParser.hasValue("-html")) {
      int threads = argParser.getInteger("-threads", DEFAULT_THREADS);
      if (threads < 1) {
        threads = DEFAULT_THREADS;
      }
      queue = new WorkQueue(threads);
      ThreadSafeInvertedIndex threadedIndex = new ThreadSafeInvertedIndex();
      index = threadedIndex;
      builder = new ThreadSafeInvertedIndexBuilder(threadedIndex, queue);
      processor = new ThreadSafeQueryProcessor(threadedIndex, queue, partial);
      if (argParser.hasValue("-html")) {
        int crawl = argParser.getInteger("-crawl", DEFAUT_CRAWL);
        crawler = new WebCrawler(index, queue, argParser.getString("-html"), crawl);
      }
    } else {
      index = new InvertedIndex();
      builder = new InvertedIndexBuilder(index);
      processor = new QueryProcessor(index, partial);
    }

    if (argParser.hasValue("-text")) {
      Path path = argParser.getPath("-text");
      log.info("Using {} for source.", path);
      try {
        builder.build(path);
        log.info("Build complete.");
      } catch (IOException e) {
        log.error("Unable to build Index from path: {}", path);
      }
    }

    if (crawler != null) {
      crawler.processLink();
    }

    if (argParser.hasValue("-query")) {
      Path queries = argParser.getPath("-query");
      try {
        processor.parseQuery(queries);
      } catch (IOException e) {
        System.err.printf("Unable to read queries from path: %s", queries);
      }
    }

    if (queue != null) {
      queue.shutdown();
    }

    if (argParser.hasFlag("-counts")) {
      Path countOutput = argParser.getPath("-counts", DEFAULT_COUNTS);
      try {
        JsonWriter.writeObject(index.getCounts(), countOutput);
      } catch (IOException e) {
        System.err.printf("Unable to write counts to path: %s", countOutput);
      }
    }

    if (argParser.hasFlag("-index")) {
      Path indexOutput = argParser.getPath("-index", DEFAULT_INDEX);
      try {
        index.toJson(indexOutput);
      } catch (IOException e) {
        System.err.printf("Unable to write index to path: %s", indexOutput);
      }
    }

    if (argParser.hasFlag("-results")) {
      Path results = argParser.getPath("-results", DEFAULT_RESULTS);
      try {
        processor.toJson(results);
      } catch (IOException e) {
        System.err.printf("Unable to write results to path: %s", results);
      }
    }

    if (queue != null) {
      queue.join();
    }

    // calculate time elapsed and output
    long elapsed = Duration.between(start, Instant.now()).toMillis();
    double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
    System.out.printf("Elapsed: %f seconds%n", seconds);
  }
}
