package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Builder Class for the Search Engine Project.
 * @author Ravneet Singh Bhatia, CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class Builder { // TODO InvertedIndexBuilder
  private final Path input;
  private final Path indexOutput;
  private final Path countsOutput;

  private final InvertedIndex index; // TODO Only keep this one

  /**
   * Constructor for Builder Class.
   *
   * @param input file/directory path
   * @param countsOutput output path for counts
   * @param indexOutput output path for index
   * @param index InvertedIndex
   */
  public Builder(Path input, Path countsOutput, Path indexOutput, InvertedIndex index) {
    this.input = input;
    this.countsOutput = countsOutput;
    this.indexOutput = indexOutput;
    this.index = index;
  }

  /**
   * Reads text files in a directory or nested directories.
   *
   * @param directory directory path
   */
  public void readDirectory(Path directory) throws IOException {
    try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
      for (Path path : listing) {
        if (Files.isDirectory(path)) {
          readDirectory(path);
        } else {
          if (fileIsTXT(path)) {
            readFile(path);
            writeOutput();
          }
        }
      }
    } catch (Exception e) {
      throw new IOException("Unable to parse directory at: " + directory);
    }
  }

  /** Read text files from a directory / nested directories. */
  public void readDirectory() throws Exception { // TODO Remove
    this.readDirectory(input);
  }

  // CITE: Talked to Frank about not having multi-line reading.
  /**
   * reads text file to populate InvertedIndex.
   *
   * @param file path of text file.
   */
  public void readFile(Path file) throws Exception {
  	/*
  	 * TODO Make this more efficient by copy/pasting some logic from the file stemmer
  	 * into here to add directly into an inverted index, never to a list of stems
  	 */
    ArrayList<String> stems;
    stems = FileStemmer.listStems(file);
    for (int i = 0; i < stems.size(); i++) {
      this.index.add(file, stems.get(i), i);
    }
    if (!stems.isEmpty()) {
			this.index.addCounts(file, stems.size());
		}
    writeOutput();
  }

  /**
   * Validates file extension
   *
   * @param path of file
   * @return true if file has a valid text file extension.
   */
  private boolean fileIsTXT(Path path) { // TODO public static
  	// TODO Make a bit more efficient---don't toString and toLower twice
    return path.toString().toLowerCase().endsWith(".txt")
        || path.toString().toLowerCase().endsWith(".text");
  }

  /**
   * Writes the index and counts to indexOutput and countsOutput respectively. Ensures that the
   * paths are not null.
   */
  public void writeOutput() { // TODO Remove, logic goes into Driver
    if (countsOutput != null) {
      try {
        JsonWriter.writeObject(this.index.getCounts(), countsOutput);
      } catch (IOException e) {
        System.out.printf("Unable to build counts with path: %s\n", countsOutput);
      }
    }
    if (indexOutput != null) {
      try {
        JsonWriter.writeIndex(this.index, indexOutput);
      } catch (IOException e) {
        System.out.printf("Unable to build index with path: %s\n", indexOutput);
      }
    }
  }
}
