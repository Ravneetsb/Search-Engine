package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

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

		ArgumentParser argParser = new ArgumentParser(args);
		try {
			Path path = null;
			Path output = null;
			if (argParser.hasFlag("-text") && argParser.hasFlag("-counts")) {
				path = argParser.getPath("-text");
				output = argParser.getPath("-counts", Path.of("counts.json"));
			} else if (argParser.hasFlag("-counts")) {
				output = Files.createFile(Path.of("counts.json"));
			} else if (argParser.hasFlag("-text")) {
				path = argParser.getPath("-text");
			}
            System.out.println(output);
			System.out.println("Using " + output);
			System.out.println("Working Directory: " + Path.of(".").toAbsolutePath().normalize().getFileName());
			System.out.println("Arguments: " + Arrays.toString(args));
			WordCounter counter = new WordCounter();

            if (Files.isDirectory(path)) {
				readDirectory(path, output, counter);
			} else {
				readFile(path, counter);
				JsonWriter.writeObject(counter.getMap(), output);
			}
		} catch (Exception e) {
			System.out.println("Error");
		}

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

	private static void readDirectory(Path input, Path output, WordCounter counter) throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(input)) {
			for (Path path: listing) {
				if (Files.isDirectory(path)) {
					readDirectory(path, output, counter);
				} else {
					if (fileIsTXT(path)) {
						readFile(path, counter);
						counter.write(output);
					}
				}
			}
		}
	}


	private static void readFile(Path path, WordCounter counter) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path)) {
			String text;
			while ((text = br.readLine()) != null) {
				counter.compute(path, text);
			}
		}
	}

	private static boolean fileIsTXT(Path path) {
		return path.toString().toLowerCase().endsWith(".txt") || path.toString().toLowerCase().endsWith(".text");
	}

	// CITE: Talked to Frank about not having multi-line reading.
}
