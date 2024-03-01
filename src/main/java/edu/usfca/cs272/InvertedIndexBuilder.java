package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * InvertedIndexBuilder Class for the Search Engine Project.
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
  public void readFile(Path file) throws Exception {
  	/*
  	 * TODO Make this more efficient by copy/pasting some logic from the file stemmer
  	 * into here to add directly into an inverted index, never to a list of stems
  	 */
    ArrayList<String> stems;
    stems = FileStemmer.listStems(file);
    String fileName = file.toString();
    for (int i = 0; i < stems.size(); i++) {
      this.index.add(fileName, stems.get(i), i);
    }
    if (!stems.isEmpty()) {
			this.index.addCounts(fileName, stems.size());
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
    return clean.endsWith(".txt")
        || clean.endsWith(".text");
  }

}
