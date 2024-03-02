package edu.usfca.cs272;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * InvertedIndexBuilder Class for the Search Engine Project.
 *
 * @author Ravneet Singh Bhatia, CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class InvertedIndexBuilder {
  private final Path input;
  private final InvertedIndex index;

  /**
   * Constructor for InvertedIndexBuilder Class.
   *
   * @param input file/directory path
   * @param index InvertedIndex
   */
  public InvertedIndexBuilder(Path input, InvertedIndex index) {
    this.input = input;
    this.index = index;
  }

  public void build() throws IOException {
    if (Files.isDirectory(input)) {
      readDirectory(input);
    } else readFile(input);
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
          }
        }
      }
    } catch (Exception e) {
      throw new IOException("Unable to parse directory at: " + directory);
    }
  }

  // CITE: Talked to Frank about not having multi-line reading.
  /**
   * reads text file to populate InvertedIndex.
   *
   * @param file path of text file.
   */
  public void readFile(Path file) throws IOException {
    int iter = 0;
    try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      String line;
      Stemmer stemmer = new SnowballStemmer(ENGLISH);
      while ((line = br.readLine()) != null) {
        String[] words = FileStemmer.parse(line);
        for (String word : words) {
          index.add(file.toString(), stemmer.stem(word).toString(), iter++);
        }
      }
      if (iter != 0) index.addCounts(file.toString(), iter);
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
}
