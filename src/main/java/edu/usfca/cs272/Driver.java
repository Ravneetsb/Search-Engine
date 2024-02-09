package edu.usfca.cs272;

import org.checkerframework.checker.units.qual.A;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
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
	public static void main(String[] args) throws IOException {
		// store initial start time
		Instant start = Instant.now();

		// TODO Fill in and modify as needed
		ArgumentParser argParser = new ArgumentParser(args);
		Path path = argParser.getPath("-text");
		Path output = argParser.getPath("-count", Path.of("counts.json"));
		System.out.println("Working Directory: " + Path.of(".").toAbsolutePath().normalize().getFileName());
		System.out.println("Arguments: " + Arrays.toString(args));

		if (Files.isDirectory(path)) {
			readDirectory(path, output);
		} else {
			readFile(path, output);
		}

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

	private static void readDirectory(Path path, Path output) throws IOException {

		try (Stream<Path> files = Files.walk(path, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)) {
			files.forEach((filePath) -> {
                try {
                    readFile(filePath, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
		} catch (Exception e) {
			throw new IOException();
		}
	}

	private static void readFile(Path path, Path output) throws IOException {
		Map<String, ? extends Number> map = new TreeMap<>();
		try (BufferedReader br = Files.newBufferedReader(path)) {
			String text;
			while ((text = br.readLine()) != null) {
				// Add stemming.
			}
		}
	}

}
