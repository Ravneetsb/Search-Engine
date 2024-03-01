package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
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
  public static final Path DEFAULT_INDEX = Path.of("index.json");
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

    Path indexOutput = null; // TODO Declare these where they are defined
    Path countOutput = null;

    if (argParser.hasFlag("-index")) { // TODO Remove
      indexOutput = argParser.getPath("-index", DEFAULT_INDEX);
      try {
        Files.createFile(indexOutput);
      } catch (IOException e) {
        System.out.printf("Unable to create index file at: %s", indexOutput);
      }
    }

    if (argParser.hasFlag("-counts")) { // TODO Remove
      countOutput = argParser.getPath("-counts", DEFAULT_COUNTS);
      try {
        Files.createFile(countOutput);
      } catch (IOException e) {
        System.out.printf("Unable to create counts file at: %s", countOutput);
      }
    }

    if (argParser.hasValue("-text")) {
      Path path = argParser.getPath("-text");
      InvertedIndexBuilder invertedIndexBuilder = new InvertedIndexBuilder(path, index); // TODO InvertedIndexBuilder shouldn't need the output paths

      if (Files.isDirectory(path)) { // TODO Make a build(...) method that chooses whether to call readDir or file.
        try {
          invertedIndexBuilder.readDirectory();
        } catch (Exception e) {
          System.out.printf("Could not parse path: %s\n", path);
        }
      } else {
        try {
          invertedIndexBuilder.readFile(path);
        } catch (Exception e) {
          System.out.printf("Could not read file from: %s\n", path);
        }
      }
    }

    if (countOutput != null) {
      try {
        JsonWriter.writeObject(index.getCounts(), countOutput);
      } catch (IOException e) {
          System.err.printf("Unable to write Counts Map to path: %s", countOutput);
      }
    }

    if (indexOutput != null) {
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

/*
 * TODO
Description	Resource	Path	Location	Type
Javadoc: Missing comment for default declaration	Driver.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 18	Java Problem
Javadoc: Missing comment for default declaration	Driver.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 19	Java Problem
Javadoc: Missing comment for private declaration	InvertedIndexBuilder.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 15	Java Problem
Javadoc: Missing comment for private declaration	InvertedIndexBuilder.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 16	Java Problem
Javadoc: Missing comment for private declaration	InvertedIndexBuilder.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 17	Java Problem
Javadoc: Missing comment for private declaration	InvertedIndexBuilder.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 19	Java Problem
Javadoc: Missing comment for private declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 12	Java Problem
Javadoc: Missing comment for private declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 13	Java Problem
Javadoc: Missing tag for declared exception Exception	InvertedIndexBuilder.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 59	Java Problem
Javadoc: Missing tag for declared exception Exception	InvertedIndexBuilder.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 69	Java Problem
Javadoc: Missing tag for declared exception IOException	InvertedIndexBuilder.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 41	Java Problem
*/
