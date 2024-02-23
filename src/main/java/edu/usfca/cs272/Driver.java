package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

/**
 * Class responsible for running this project based on the provided command-line arguments. See the
 * README for details.
 *
 * @author Ravneet Singh Bhatia
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class Driver {

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) throws IOException { // TODO Remove throws IOException
		// store initial start time
		Instant start = Instant.now();

		/* TODO
		ArgumentParser parser = new ArgumentParser(args);
		InvertedIndex index = new InvertedIndex();

		if (parser.hasFlag("-text")) {
			Path path = parser.getPath("-text");

			try {
				1 line of code
			}
			catch ( ) {
				System.out.println("Unable to build the index from path: " + path);
			}
		}

		one per flag/value pair
		 */

		ArgumentParser argParser = new ArgumentParser(args);
		try {
			Path path = null; Path indexOutput = null;
			path = argParser.getPath("-text");
			Path countOutput = argParser.hasFlag("-counts") ? argParser.getPath("-counts", Path.of("counts.json")) : null;
			indexOutput = argParser.hasFlag("-index") ? argParser.getPath("-index", Path.of("index.json")) : null;
			if (path == null) {
				if (countOutput != null) {
					Files.createFile(Path.of("counts.json"));
				}
				if (indexOutput != null) {
					Files.createFile(Path.of("index.json"));
				}
			}
			System.out.println("Using " + indexOutput);
			System.out.println("Working Directory: " + Path.of(".").toAbsolutePath().normalize().getFileName());
			System.out.println("Arguments: " + Arrays.toString(args));
			WordCounter counter = new WordCounter();
			InvertedIndex index = new InvertedIndex();

            if (Files.isDirectory(path)) { // TODO Mix of tabs and spaces
				readDirectory(path, countOutput, counter, indexOutput, index);
			} else {
				readFile(path, counter, indexOutput,  index);
				if (countOutput != null) {
					JsonWriter.writeObject(counter.getMap(), countOutput);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
  
  /**
   * Initializes the classes necessary based on the provided command-line arguments. This includes
   * (but is not limited to) how to build or search an inverted index.
   *
   * @param args flag/value pairs used to start this program
   */
  public static void main(String[] args) throws IOException {
    // store initial start time
    Instant start = Instant.now();

    ArgumentParser argParser = new ArgumentParser(args);
    try {
      Path path = null;
      Path indexOutput = null;
      path = argParser.getPath("-text");
      Path countOutput =
          argParser.hasFlag("-counts")
              ? argParser.getPath("-counts", Path.of("counts.json"))
              : null;
      indexOutput =
          argParser.hasFlag("-index") ? argParser.getPath("-index", Path.of("index.json")) : null;
      if (path == null) {
        if (countOutput != null) Files.createFile(Path.of("counts.json"));
        if (indexOutput != null) Files.createFile(Path.of("index.json"));
      }
      System.out.println("Using " + indexOutput);
      System.out.println(
          "Working Directory: " + Path.of(".").toAbsolutePath().normalize().getFileName());
      System.out.println("Arguments: " + Arrays.toString(args));
      WordCounter counter = new WordCounter();
      InvertedIndex index = new InvertedIndex();

      if (Files.isDirectory(path)) {
        readDirectory(path, countOutput, counter, indexOutput, index);
      } else {
        readFile(path, counter, indexOutput, index);
        if (countOutput != null) {
          JsonWriter.writeObject(counter.getMap(), countOutput);
        }
      }
    } catch (Exception e) {
      System.out.println(e);
    }


    // calculate time elapsed and output
    long elapsed = Duration.between(start, Instant.now()).toMillis();
    double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
    System.out.printf("Elapsed: %f seconds%n", seconds);
  }


	// TODO  MOve into new builder class
	private static void readDirectory(Path input, Path output, WordCounter counter, Path indexOutput, InvertedIndex index) throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(input)) {
			for (Path path: listing) {
				if (Files.isDirectory(path)) {
					readDirectory(path, output, counter, indexOutput, index);
				} else {
					if (fileIsTXT(path)) {
						readFile(path, counter, indexOutput, index);
						if (output != null) {
							counter.write(output);
						}
					}
				}
			}
		}
	}

	// TODO  MOve into new builder class
	private static void readFile(Path path, WordCounter counter, Path indexOutput, InvertedIndex index) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path)) {
			String text;
			int iter = 0;
			while ((text = br.readLine()) != null) {
				if (!text.isEmpty()) {
					counter.compute(path, text);
					iter = index.index(path, text, iter);
				}
//				index.index(path, text);
			}
			index.write(indexOutput);
		}
	}

	private static boolean fileIsTXT(Path path) { // TODO Move into the builder
		return path.toString().toLowerCase().endsWith(".txt") || path.toString().toLowerCase().endsWith(".text");
	}

	// CITE: Talked to Frank about not having multi-line reading.
=======
  private static void readDirectory(
      Path input, Path output, WordCounter counter, Path indexOutput, InvertedIndex index)
      throws IOException {
    try (DirectoryStream<Path> listing = Files.newDirectoryStream(input)) {
      for (Path path : listing) {
        if (Files.isDirectory(path)) {
          readDirectory(path, output, counter, indexOutput, index);
        } else {
          if (fileIsTXT(path)) {
            readFile(path, counter, indexOutput, index);
            if (output != null) counter.write(output);
          }
        }
      }
    }
  }

  private static void readFile(
      Path path, WordCounter counter, Path indexOutput, InvertedIndex index) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(path)) {
      String text;
      int iter = 0;
      while ((text = br.readLine()) != null) {
        if (!text.isEmpty()) {
          counter.compute(path, text);
          iter = index.index(path, text, iter);
        }
      }
      index.write(indexOutput);
    }
  }

  private static boolean fileIsTXT(Path path) {
    return path.toString().toLowerCase().endsWith(".txt")
        || path.toString().toLowerCase().endsWith(".text");
  }

  // CITE: Talked to Frank about not having multi-line reading.

}

/*
TODO
Description	Resource	Path	Location	Type
Javadoc: Missing comment for private declaration	Driver.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 68	Java Problem
Javadoc: Missing comment for private declaration	Driver.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 85	Java Problem
Javadoc: Missing comment for private declaration	Driver.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 100	Java Problem
Javadoc: Missing comment for private declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 8	Java Problem
Javadoc: Missing comment for private declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 42	Java Problem
Javadoc: Missing comment for private declaration	WordCounter.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 10	Java Problem
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 7	Java Problem
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 10	Java Problem
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 14	Java Problem
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 18	Java Problem
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 22	Java Problem
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 33	Java Problem
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 49	Java Problem
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 53	Java Problem
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 57	Java Problem
Javadoc: Missing comment for public declaration	JsonWriter.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 364	Java Problem
Javadoc: Missing comment for public declaration	JsonWriter.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 374	Java Problem
Javadoc: Missing comment for public declaration	JsonWriter.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 383	Java Problem
Javadoc: Missing comment for public declaration	WordCounter.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 9	Java Problem
Javadoc: Missing comment for public declaration	WordCounter.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 12	Java Problem
Javadoc: Missing comment for public declaration	WordCounter.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 16	Java Problem
Javadoc: Missing comment for public declaration	WordCounter.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 20	Java Problem
Javadoc: Missing comment for public declaration	WordCounter.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 24	Java Problem
Javadoc: Missing tag for declared exception IOException	Driver.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 28	Java Problem
The import java.util.concurrent.atomic.AtomicInteger is never used	JsonWriter.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 11	Java Problem
*/