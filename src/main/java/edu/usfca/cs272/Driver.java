package edu.usfca.cs272;

import org.eclipse.jetty.util.IO;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Class responsible for running this project based on the provided command-line arguments. See the
 * README for details.
 *
 * @author Ravneet Singh Bhatia
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class Driver {
  static final Path DEFAULT_INDEX = Path.of("index.json");
  static final Path DEFAULT_COUNTS = Path.of("counts.json");
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

    if (argParser.hasFlag("-counts")) {
      try {
        Files.createFile(DEFAULT_COUNTS);
      } catch (IOException e) {
        System.out.printf("Unable to build counts from path: %s\n", DEFAULT_COUNTS);
      }
    }

    if (argParser.hasFlag("-index")) {
      try {
        Files.createFile(Path.of("index.json"));
      } catch (IOException e) {
        System.out.printf("Unable to build counts from path: %s\n", DEFAULT_INDEX);
      }
    }

    if (argParser.hasFlag("-text")) {
      Path path = argParser.getPath("-text");
      Path indexOutput = argParser.getPath("-index", DEFAULT_INDEX);
      Path countOutput = argParser.getPath("-counts", DEFAULT_COUNTS);

      Builder builder = new Builder(path, countOutput, indexOutput, index);

      if (Files.isDirectory(path)) {
        builder.readDirectory();
      } else {
        builder.readFile();
        try {
          JsonWriter.writeObject(index.getCounts(), countOutput);
        } catch (IOException e) {
          System.out.printf("Unable to build counts from path: %s\n", countOutput);
        }
      }
    }
    // calculate time elapsed and output
    long elapsed = Duration.between(start, Instant.now()).toMillis();
    double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
    System.out.printf("Elapsed: %f seconds%n", seconds);
  }
  // CITE: Talked to Frank about not having multi-line reading.

  // Test comment
}
