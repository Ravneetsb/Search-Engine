package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines are used to
 * separate elements and nested elements are indented using spaces.
 *
 * <p>Warning: This class is not thread-safe. If multiple threads access this class concurrently,
 * access must be synchronized externally.
 *
 * @author Ravneet Singh Bhatia, CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class JsonWriter {
  /**
   * Indents the writer by the specified number of times. Does nothing if the indentation level is 0
   * or less.
   *
   * @param writer the writer to use
   * @param indent the number of times to indent
   * @throws IOException if an IO error occurs
   */
  public static void writeIndent(Writer writer, int indent) throws IOException {
    while (indent-- > 0) {
      writer.write("  ");
    }
  }

  /**
   * Indents and then writes the String element.
   *
   * @param element the element to write
   * @param writer the writer to use
   * @param indent the number of times to indent
   * @throws IOException if an IO error occurs
   */
  public static void writeIndent(String element, Writer writer, int indent) throws IOException {
    writeIndent(writer, indent);
    writer.write(element);
  }

  /**
   * Indents and then writes the text element surrounded by {@code " "} quotation marks.
   *
   * @param element the element to write
   * @param writer the writer to use
   * @param indent the number of times to indent
   * @throws IOException if an IO error occurs
   */
  public static void writeQuote(String element, Writer writer, int indent) throws IOException {
    writeIndent(writer, indent);
    writer.write('"');
    writer.write(element);
    writer.write('"');
  }

  /**
   * Writes the elements as a pretty JSON array.
   *
   * @param elements the elements to write
   * @param writer the writer to use
   * @param indent the initial indent level; the first bracket is not indented, inner elements are
   *     indented by one, and the last bracket is indented at the initial indentation level
   * @throws IOException if an IO error occurs
   * @see Writer#write(String)
   * @see #writeIndent(Writer, int)
   * @see #writeIndent(String, Writer, int)
   */
  public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent)
      throws IOException {
    writer.write("[");
    var iterator = elements.iterator();
    String data;
    if (iterator.hasNext()) {
      writer.write("\n");
      data = iterator.next().toString();
      writeIndent(data, writer, indent + 1);
    }
    while (iterator.hasNext()) {
      data = iterator.next().toString();
      writer.write(",\n");
      writeIndent(data, writer, indent + 1);
    }
    writer.write("\n");
    writeIndent("]", writer, indent);
  }

  /**
   * Writes the elements as a pretty JSON array to file.
   *
   * @param elements the elements to write
   * @param path the file path to use
   * @throws IOException if an IO error occurs
   * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
   * @see StandardCharsets#UTF_8
   * @see #writeArray(Collection, Writer, int)
   */
  public static void writeArray(Collection<? extends Number> elements, Path path)
      throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
      writeArray(elements, writer, 0);
    }
  }

  /**
   * Returns the elements as a pretty JSON array.
   *
   * @param elements the elements to use
   * @return a {@link String} containing the elements in pretty JSON format
   * @see StringWriter
   * @see #writeArray(Collection, Writer, int)
   */
  public static String writeArray(Collection<? extends Number> elements) {
    try {
      StringWriter writer = new StringWriter();
      writeArray(elements, writer, 0);
      return writer.toString();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Writes the elements as a pretty JSON object.
   *
   * @param elements the elements to write
   * @param writer the writer to use
   * @param indent the initial indent level; the first bracket is not indented, inner elements are
   *     indented by one, and the last bracket is indented at the initial indentation level
   * @throws IOException if an IO error occurs
   * @see Writer#write(String)
   * @see #writeIndent(Writer, int)
   * @see #writeIndent(String, Writer, int)
   */
  public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent)
      throws IOException {
    writeIndent("{", writer, 0);
    var iterator = elements.entrySet().iterator();
    if (iterator.hasNext()) {
      var element = iterator.next();
      writer.write("\n");
      writeQuote(element.getKey(), writer, indent + 1);
      writer.write(String.format(": %s", element.getValue()));
    }
    while (iterator.hasNext()) {
      var element = iterator.next();
      writer.write(",\n");
      writeQuote(element.getKey(), writer, indent + 1);
      writer.write(String.format(": %s", element.getValue()));
    }
    writer.write("\n");
    writeIndent("}", writer, indent);
  }

  /**
   * Writes the elements as a pretty JSON object to file.
   *
   * @param elements the elements to write
   * @param path the file path to use
   * @throws IOException if an IO error occurs
   * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
   * @see StandardCharsets#UTF_8
   * @see #writeObject(Map, Writer, int)
   */
  public static void writeObject(Map<String, ? extends Number> elements, Path path)
      throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
      writeObject(elements, writer, 0);
    }
  }

  /**
   * Returns the elements as a pretty JSON object.
   *
   * @param elements the elements to use
   * @return a {@link String} containing the elements in pretty JSON format
   * @see StringWriter
   * @see #writeObject(Map, Writer, int)
   */
  public static String writeObject(Map<String, ? extends Number> elements) {
    try {
      StringWriter writer = new StringWriter();
      writeObject(elements, writer, 0);
      return writer.toString();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Writes the elements as a pretty JSON object with nested arrays. The generic notation used
   * allows this method to be used for any type of map with any type of nested collection of number
   * objects.
   *
   * @param elements the elements to write
   * @param writer the writer to use
   * @param indent the initial indent level; the first bracket is not indented, inner elements are
   *     indented by one, and the last bracket is indented at the initial indentation level
   * @throws IOException if an IO error occurs
   * @see Writer#write(String)
   * @see #writeIndent(Writer, int)
   * @see #writeIndent(String, Writer, int)
   * @see #writeArray(Collection)
   */
  public static void writeObjectArrays(
      Map<String, ? extends Collection<? extends Number>> elements, Writer writer, int indent)
      throws IOException {
    writer.write("{");
    var iterator = elements.entrySet().iterator();
    if (iterator.hasNext()) {
      var element = iterator.next();
      writer.write("\n");
      writeQuote(element.getKey(), writer, indent + 1);
      writer.write(": ");
      writeArray(element.getValue(), writer, indent + 1);
    }
    while (iterator.hasNext()) {
      var element = iterator.next();
      writer.write(",\n");
      writeQuote(element.getKey(), writer, indent + 1);
      writer.write(": ");
      writeArray(element.getValue(), writer, indent + 1);
    }
    writer.write("\n");
    writeIndent("}", writer, indent);
  }

  /**
   * Writes the elements as a pretty JSON object with nested arrays to file.
   *
   * @param elements the elements to write
   * @param path the file path to use
   * @throws IOException if an IO error occurs
   * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
   * @see StandardCharsets#UTF_8
   * @see #writeObjectArrays(Map, Writer, int)
   */
  public static void writeObjectArrays(
      Map<String, ? extends Collection<? extends Number>> elements, Path path) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
      writeObjectArrays(elements, writer, 0);
    }
  }

  /**
   * Returns the elements as a pretty JSON object with nested arrays.
   *
   * @param elements the elements to use
   * @return a {@link String} containing the elements in pretty JSON format
   * @see StringWriter
   * @see #writeObjectArrays(Map, Writer, int)
   */
  public static String writeObjectArrays(
      Map<String, ? extends Collection<? extends Number>> elements) {
    try {
      StringWriter writer = new StringWriter();
      writeObjectArrays(elements, writer, 0);
      return writer.toString();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Writes the elements as a pretty JSON array with nested objects. The generic notation used
   * allows this method to be used for any type of collection with any type of nested map of String
   * keys to number objects.
   *
   * @param elements the elements to write
   * @param writer the writer to use
   * @param indent the initial indent level; the first bracket is not indented, inner elements are
   *     indented by one, and the last bracket is indented at the initial indentation level
   * @throws IOException if an IO error occurs
   * @see Writer#write(String)
   * @see #writeIndent(Writer, int)
   * @see #writeIndent(String, Writer, int)
   * @see #writeObject(Map)
   */
  public static void writeArrayObjects(
      Collection<? extends Map<String, ? extends Number>> elements, Writer writer, int indent)
      throws IOException {
    writer.write("[");
    var iterator = elements.iterator();
    if (iterator.hasNext()) {
      var element = iterator.next();
      writer.write("\n");
      writeIndent("", writer, indent + 1);
      writeObject(element, writer, indent + 1);
    }
    while (iterator.hasNext()) {
      var element = iterator.next();
      writer.write(",\n");
      writeIndent("", writer, indent + 1);
      writeObject(element, writer, indent + 1);
    }
    writer.write("\n");
    writeIndent("]", writer, indent);
  }

  /**
   * Writes the elements as a pretty JSON array with nested objects to file.
   *
   * @param elements the elements to write
   * @param path the file path to use
   * @throws IOException if an IO error occurs
   * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
   * @see StandardCharsets#UTF_8
   * @see #writeArrayObjects(Collection)
   */
  public static void writeArrayObjects(
      Collection<? extends Map<String, ? extends Number>> elements, Path path) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
      writeArrayObjects(elements, writer, 0);
    }
  }

  /**
   * Returns the elements as a pretty JSON array with nested objects.
   *
   * @param elements the elements to use
   * @return a {@link String} containing the elements in pretty JSON format
   * @see StringWriter
   * @see #writeArrayObjects(Collection)
   */
  public static String writeArrayObjects(
      Collection<? extends Map<String, ? extends Number>> elements) {
    try {
      StringWriter writer = new StringWriter();
      writeArrayObjects(elements, writer, 0);
      return writer.toString();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Writes InvertedIndex as a pretty JSON.
   *
   * @param index InvertedIndex
   * @return null if exception.
   */
  public static String writeIndex(
      Map<String, ? extends Map<String, ? extends Set<? extends Number>>> index) {
    try {
      StringWriter writer = new StringWriter();
      writeIndex(index, writer, 0);
      return writer.toString();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Writes InvertedIndex as a pretty JSON.
   *
   * @param index InvertedIndex
   * @param path path of output file.
   * @throws IOException if BufferedWriter error.
   */
  public static void writeIndex(
      Map<String, ? extends Map<String, ? extends Set<? extends Number>>> index, Path path)
      throws IOException {
    try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, UTF_8)) {
      writeIndex(index, bufferedWriter, 0);
    }
  }

  /**
   * Writes InvertedIndex as a pretty JSON
   *
   * @param index InvertedIndex
   * @param writer Writer
   * @param indent indent value
   * @throws IOException if writer error.
   */
  public static void writeIndex(
      Map<String, ? extends Map<String, ? extends Set<? extends Number>>> index,
      Writer writer,
      int indent)
      throws IOException {
    writer.write("{");
    var iterator = index.entrySet().iterator();
    if (iterator.hasNext()) {
      var element = iterator.next();
      writer.write("\n");
      writeQuote(element.getKey(), writer, indent + 1);
      writer.write(": ");
      writeObjectArrays(element.getValue(), writer, indent + 1);
    }
    while (iterator.hasNext()) {
      var element = iterator.next();
      writer.write(",\n");
      writeQuote(element.getKey(), writer, indent + 1);
      writer.write(": ");
      writeObjectArrays(element.getValue(), writer, indent + 1);
    }
    writer.write("\n");
    writeIndent("}", writer, indent);
  }
  
  // TODO Update writeSerch to work with any map, and any type of collection of search results

  /**
   * Writes search results in pretty Json
   *
   * @param searchMap search results map
   * @return pretty Json or null if IOException is thrown.
   */
  public static String writeSearch(TreeMap<String, ArrayList<InvertedIndex.Score>> searchMap) {
    try {
      StringWriter writer = new StringWriter();
      writeSearch(searchMap, writer, 0);
      return writer.toString();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Writes search results in pretty Json
   *
   * @param searchMap search results map
   * @param path output file.
   * @throws IOException if unable to write to file.
   */
  public static void writeSearch(
      TreeMap<String, ArrayList<InvertedIndex.Score>> searchMap, Path path) throws IOException {
    if (path == null) { // TODO Remove this null check, let the exception happen
      return;
    }
    try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, UTF_8)) {
      writeSearch(searchMap, bufferedWriter, 0);
    }
  }
  
  /*
   * TODO Create 1 more method to output a collection of score objects, then call
   * that in the code below. You don't have to create convenience methods for that one
   * or the writeScore method. 
   */

  /**
   * Writes Search Results in pretty Json
   *
   * @param searchMap Search results
   * @param writer writer
   * @param indent indent value
   * @throws IOException if unable to write to path.
   */
  public static void writeSearch(
      TreeMap<String, ArrayList<InvertedIndex.Score>> searchMap, Writer writer, int indent)
      throws IOException {
    var entryIterator = searchMap.entrySet().iterator();
    writer.write("{");
    if (entryIterator.hasNext()) {
      var entry = entryIterator.next();
      String stem = entry.getKey();
      writer.write("\n");
      writeQuote(stem, writer, indent + 1);
      writer.write(": [");
      var iterator = entry.getValue().iterator();
      if (iterator.hasNext()) {
        var map = iterator.next();
        writer.write("\n");
        writeScore(writer, indent, map);
      }
      while (iterator.hasNext()) {
        writer.write(",\n");
        var map = iterator.next();
        writeScore(writer, indent, map);
      }
      writer.write("\n");
      writeIndent("]", writer, indent + 1);
    }
    while (entryIterator.hasNext()) {
      writer.write(",");
      var entry = entryIterator.next();
      String stem = entry.getKey();
      writer.write("\n");
      writeQuote(stem, writer, indent + 1);
      writer.write(": [");
      var iterator = entry.getValue().iterator();
      if (iterator.hasNext()) {
        var map = iterator.next();
        writer.write("\n");
        writeScore(writer, indent, map);
      }
      while (iterator.hasNext()) {
        var map = iterator.next();
        writer.write(",\n");
        writeScore(writer, indent, map);
      }
      writer.write("\n");
      writeIndent("]", writer, indent + 1);
    }
    writer.write("\n}");
  }

  /**
   * Writes the score map to writer.
   *
   * @param writer writer
   * @param indent indent value
   * @param map scoreMap
   * @throws IOException if the writing fails.
   */
  private static void writeScore(Writer writer, int indent, InvertedIndex.Score map)
      throws IOException { // TODO public
    DecimalFormat format = new DecimalFormat("0.00000000");
    writeIndent("{", writer, indent + 2);
    writer.write("\n");
    writeQuote("count", writer, indent + 3);
    writer.write(": ");
    writer.write(String.valueOf(map.getCount()));
    writer.write(",\n");
    writeQuote("score", writer, indent + 3);
    writer.write(": ");
    writer.write(format.format(map.getScore()));
    writer.write(",\n");
    writeQuote("where", writer, indent + 3);
    writer.write(": ");
    writeQuote(map.getLocation(), writer, indent);
    writer.write("\n");
    writeIndent("}", writer, indent + 2);
  }

  /** Prevent instantiating this class of static methods. */
  private JsonWriter() {}
}
