package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
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
		Path output = argParser.getPath("-counts", Path.of("count.json"));
		System.out.println("Using " + output);
		System.out.println("Working Directory: " + Path.of(".").toAbsolutePath().normalize().getFileName());
		System.out.println("Arguments: " + Arrays.toString(args));
		Map<String, Integer> map = new TreeMap<>();
		if (Files.isDirectory(path)) {
			readDirectory(path, output, map);
		} else {
			 readFile(path, map);
			JsonWriter.writeObject(map, output);
		}

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

	private static void readDirectory(Path input, Path output, Map<String, Integer> map) throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(input)) {
			for (Path path: listing) {
				System.out.println(path.toString());
				if (Files.isDirectory(path)) {
					readDirectory(path, output, map);
				} else {
					if (path.toString().toLowerCase().endsWith(".txt") || path.toString().toLowerCase().endsWith(".text")) {
						readFile(path, map);
						JsonWriter.writeObject(map, output);
					}
				}
			}
		}
	}

	private static void readFile(Path path, Map<String, Integer> map) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path)) {
			String text;
			while ((text = br.readLine()) != null) {
				String data = text;
				map.compute(String.valueOf(path), (key, value) -> (value == null) ? FileStemmer.listStems(data).size() : FileStemmer.listStems(data).size() + value);
			}
		}
	}

	// CITE: Talked to Frank about not having multi-line reading.
}
