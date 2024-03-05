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

    Path indexOutput = null;
    Path countOutput = null;

    if (argParser.hasFlag("-index")) {
      indexOutput = argParser.getPath("-index", DEFAULT_INDEX);
    }

    if (argParser.hasFlag("-counts")) {
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
 * TODO Fix the Javadoc warnings in the code.
 * 
 * Other developers will *not* use poorly unprofessionally documented code
 * regardless of whether the code itself is well designed! It is a tedious but
 * critical step to the final steps of refactoring. The "Configuring Eclipse"
 * guide on the course website shows how to setup Eclipse to see the Javadoc
 * warnings. (Open the "View Screenshot" section.) Here is a direct link:
 * 
 * https://usf-cs272-spring2024.notion.site/Configuring-Eclipse-
 * 4f735d746e004dbdbc34af6ad2d988cd#1a1a870909bb45f2a92ef5fc51038635
 * 
 * When conducting asynchronous reviews, I will no longer review code with major
 * formatting issues or warnings in it. Please do a complete pass of your code
 * for these issues before requesting code review.
 * 
 * The warnings found are below for reference:

Description	Resource	Path	Location	Type
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 111	Java Problem
Javadoc: Missing tag for parameter location	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 53	Java Problem

 */
