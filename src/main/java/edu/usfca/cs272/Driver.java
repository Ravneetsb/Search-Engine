package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

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
    InvertedIndex index = new InvertedIndex();

    Path indexOutput = null; // TODO Declare these in the scope they are defined and used (see comments below)
    Path countOutput = null;

    if (argParser.hasFlag("-index")) { // TODO Move, the block this is needed is now split from where you do this test
      indexOutput = argParser.getPath("-index", DEFAULT_INDEX);
    }

    if (argParser.hasFlag("-counts")) { // TODO Move, the block this is needed is now split from where you do this test
      countOutput = argParser.getPath("-counts", DEFAULT_COUNTS);
    }

    if (argParser.hasValue("-text")) {
      Path path = argParser.getPath("-text");
      InvertedIndexBuilder invertedIndexBuilder = new InvertedIndexBuilder(path, index);

      try {
        invertedIndexBuilder.build();
      } catch (IOException e) {
        System.err.printf("Unable to build Index from path: %s", path);
      }
    }

    if (countOutput != null) { // TODO check flag
      // TODO get flag value here, where it is used
      try {
        JsonWriter.writeObject(index.getCounts(), countOutput);
      } catch (IOException e) {
        System.err.printf("Unable to write Counts Map to path: %s", countOutput);
      }
    }

    if (indexOutput != null) { // TODO if (argParser.hasFlag("-index")) {
      // TODO Path indexOutput = argParser.getPath("-index", DEFAULT_INDEX);
      
      try {
        index.toJson(indexOutput);
      } catch (IOException e) {
        System.err.printf("Unable to write Inverted Index to path: %s", indexOutput);
      }
    }

    // calculate time elapsed and output
    long elapsed = Duration.between(start, Instant.now()).toMillis();
    double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
    System.out.printf("Elapsed: %f seconds%n", seconds);
  }
}
