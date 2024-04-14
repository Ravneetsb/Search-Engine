package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * InvertedIndexBuilder Class for the Search Engine Project.
 *
 * @author Ravneet Singh Bhatia, CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class InvertedIndexBuilder {

  /** InvertedIndex data structure to build. */
  private final InvertedIndex index;

  /**
   * Constructor for InvertedIndexBuilder Class.
   *
   * @param invertedIndex InvertedIndex
   */
  public InvertedIndexBuilder(InvertedIndex invertedIndex) {
    this.index = invertedIndex;
  }

  /**
   * Build function for InvertedIndex
   *
   * @param input Path of input file.
   * @throws IOException if file can't be read.
   */
  public void build(Path input) throws IOException {
    if (Files.isDirectory(input)) {
      readDirectory(input);
    } else readFile(input);
  }

  /**
   * Reads text files in a directory or nested directories.
   *
   * @param directory directory path
   * @throws IOException if unable to read directory.
   */
  public void readDirectory(Path directory) throws IOException {
    try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
      for (Path path : listing) {
        if (Files.isDirectory(path)) {
          readDirectory(path);
        } else {
          if (fileIsTXT(path)) {
            readFile(path);
          }
        }
      }
    }
  }

  // CITE: Talked to Frank about not having multi-line reading.
  /**
   * reads text file to populate InvertedIndex.
   *
   * @param file path of text file.
   * @throws IOException if unable to read file.
   */
  public void readFile(Path file) throws IOException {
    int iter = 0;
    try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      String line;
      String location = file.toString();
      Stemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
      while ((line = br.readLine()) != null) {
        String[] words = FileStemmer.parse(line);
        for (String word : words) {
          index.add(String.valueOf(stemmer.stem(word)), location, iter);
          iter++;
        }
      }
      index.addCounts(location, iter); // TODO Remove
    }
  }

  /**
   * Validates file extension
   *
   * @param path of file
   * @return true if file has a valid text file extension.
   */
  public static boolean fileIsTXT(Path path) {
    String clean = path.toString().toLowerCase();
    return clean.endsWith(".txt") || clean.endsWith(".text");
  }

  /**
   * to Stirng for the index builder
   *
   * @return index in pretty json.
   */
  @Override
  public String toString() {
    return index.toString();
  }
}
