package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Pattern;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * Utility class for parsing, cleaning, and stemming text and text files into collections of
 * processed words.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class FileStemmer {
  /** Regular expression that matches any whitespace. * */
  public static final Pattern SPLIT_REGEX = Pattern.compile("(?U)\\s+");

  /** Regular expression that matches non-alphabetic characters. * */
  public static final Pattern CLEAN_REGEX = Pattern.compile("(?U)[^\\p{Alpha}\\s]+");

  /**
   * Cleans the text by removing any non-alphabetic characters (e.g. non-letters like digits,
   * punctuation, symbols, and diacritical marks like the umlaut) and converting the remaining
   * characters to lowercase.
   *
   * @param text the text to clean
   * @return cleaned text
   */
  public static String clean(String text) {
    String cleaned = Normalizer.normalize(text, Normalizer.Form.NFD);
    cleaned = CLEAN_REGEX.matcher(cleaned).replaceAll("");
    return cleaned.toLowerCase();
  }

  /**
   * Splits the supplied text by whitespaces.
   *
   * @param text the text to split
   * @return an array of {@link String} objects
   */
  public static String[] split(String text) {
    return text.isBlank() ? new String[0] : SPLIT_REGEX.split(text.strip());
  }

  /**
   * Parses the text into an array of clean words.
   *
   * @param text the text to clean and split
   * @return an array of {@link String} objects
   * @see #clean(String)
   * @see #parse(String)
   */
  public static String[] parse(String text) {
    return split(clean(text));
  }

  /**
   * Parses the line into cleaned and stemmed words and adds them to the provided collection.
   *
   * @param line the line of words to clean, split, and stem
   * @param stemmer the stemmer to use
   * @param stems the collection to add stems
   * @see #parse(String)
   * @see Stemmer#stem(CharSequence)
   * @see Collection#add(Object)
   */
  public static void addStems(String line, Stemmer stemmer, Collection<? super String> stems) {
    String[] words = parse(line);
    for (String word : words) {
      stems.add(stemmer.stem(word).toString());
    }
  }

  /**
   * Parses the line into a list of cleaned and stemmed words.
   *
   * @param line the line of words to clean, split, and stem
   * @param stemmer the stemmer to use
   * @return a list of cleaned and stemmed words in parsed order
   * @see #parse(String)
   * @see Stemmer#stem(CharSequence)
   * @see #addStems(String, Stemmer, Collection)
   */
  public static ArrayList<String> listStems(String line, Stemmer stemmer) {
    ArrayList<String> stems = new ArrayList<>();
    addStems(line, stemmer, stems);
    return stems;
  }

  /**
   * Parses the line into a list of cleaned and stemmed words using the default stemmer for English.
   *
   * @param line the line of words to parse and stem
   * @return a list of cleaned and stemmed words in parsed order
   * @see SnowballStemmer#SnowballStemmer(ALGORITHM)
   * @see ALGORITHM#ENGLISH
   * @see #listStems(String, Stemmer)
   */
  public static ArrayList<String> listStems(String line) {
    Stemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
    return listStems(line, stemmer);
  }

  /**
   * Reads a file line by line, parses each line into cleaned and stemmed words using the default
   * stemmer for English.
   *
   * @param input the input file to parse and stem
   * @return a list of stems from file in parsed order
   * @throws IOException if unable to read or parse file
   * @see SnowballStemmer
   * @see ALGORITHM#ENGLISH
   * @see StandardCharsets#UTF_8
   * @see #listStems(String, Stemmer)
   */
  public static ArrayList<String> listStems(Path input) throws IOException {
    ArrayList<String> list = new ArrayList<>();
    Stemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
    try (BufferedReader br = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
      String line;
      while ((line = br.readLine()) != null) {
        addStems(line, stemmer, list);
      }
    }
    return list;
  }

  /**
   * Parses the line into a set of unique, sorted, cleaned, and stemmed words.
   *
   * @param line the line of words to parse and stem
   * @param stemmer the stemmer to use
   * @return a sorted set of unique cleaned and stemmed words
   * @see #parse(String)
   * @see Stemmer#stem(CharSequence)
   * @see #addStems(String, Stemmer, Collection)
   */
  public static TreeSet<String> uniqueStems(String line, Stemmer stemmer) {
    TreeSet<String> set = new TreeSet<>();
    addStems(line, stemmer, set);
    return set;
  }

  /**
   * Parses the line into a set of unique, sorted, cleaned, and stemmed words using the default
   * stemmer for English.
   *
   * @param line the line of words to parse and stem
   * @return a sorted set of unique cleaned and stemmed words
   * @see SnowballStemmer#SnowballStemmer(ALGORITHM)
   * @see ALGORITHM#ENGLISH
   * @see #uniqueStems(String, Stemmer)
   */
  public static TreeSet<String> uniqueStems(String line) {
    Stemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
    return uniqueStems(line, stemmer);
  }

  /**
   * Reads a file line by line, parses each line into a set of unique, sorted, cleaned, and stemmed
   * words using the default stemmer for English.
   *
   * @param input the input file to parse and stem
   * @return a sorted set of unique cleaned and stemmed words from file
   * @throws IOException if unable to read or parse file
   * @see SnowballStemmer
   * @see ALGORITHM#ENGLISH
   * @see StandardCharsets#UTF_8
   * @see #uniqueStems(String, Stemmer)
   */
  public static TreeSet<String> uniqueStems(Path input) throws IOException {
    TreeSet<String> set = new TreeSet<>();
    Stemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
    try (BufferedReader br = Files.newBufferedReader(input)) {
      String line;
      while ((line = br.readLine()) != null) {
        addStems(line, stemmer, set);
      }
    }
    return set;
  }

  /**
   * Reads a file line by line, parses each line into unique, sorted, cleaned, and stemmed words
   * using the default stemmer for English, and adds the set of unique sorted stems to a list per
   * line in the file.
   *
   * @param input the input file to parse and stem
   * @return a list where each item is the sets of unique sorted stems parsed from a single line of
   *     the input file
   * @see SnowballStemmer
   * @see ALGORITHM#ENGLISH
   * @see StandardCharsets#UTF_8
   * @see #uniqueStems(String, Stemmer)
   */
  public static ArrayList<TreeSet<String>> listUniqueStems(Path input) { // TODO Go back to throwing the exception here
    ArrayList<TreeSet<String>> list = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(input)) {
      String line;
      while ((line = br.readLine()) != null) {
        list.add(uniqueStems(line)); // TODO Reuse a stemmer object here
      }
    } catch (IOException e) { // TODO Remove catch block, do not suppress the exception!
      System.out.println("IO Issue");
    }
    return list;
  }

  /**
   * Demonstrates this class.
   *
   * @param args unused
   * @throws IOException if an I/O error occurs
   */
  public static void main(String[] args) throws IOException { // TODO Remove
    // demonstrates how to use split, clean, and parse
    System.out.println("____PARSING DEMO____");
    System.out.println();

    String sally = """
				Sally Sue...\t sells 76 sea-shells
				at THE sEa_shorE soirée!""";

    System.out.println("Original:");
    System.out.println(sally);
    System.out.println();

    System.out.println("Cleaned:");
    System.out.println(clean(sally));
    System.out.println();

    System.out.println(" Split: " + Arrays.toString(split(sally)));
    System.out.println("Parsed: " + Arrays.toString(parse(sally)));
    System.out.println();

    // demonstrates how to use stemmer
    System.out.println("____STEMMER DEMO____");
    System.out.println();

    Stemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
    String demo = "practicing";
    String stem = stemmer.stem(demo).toString();

    System.out.println("Word: " + demo);
    System.out.println("Stem: " + stem);
    System.out.println();

    // demonstrates how to use list/uniqueStems methods
    System.out.println("____STEMMING TEXT____");
    System.out.println();

    String practice =
        """
				practic practical practice practiced practicer practices
				practicing practis practisants practise practised practiser
				practisers practises practising practitioner practitioners
				""";

    System.out.println("Original: \n" + practice);
    System.out.println("  List: " + listStems(practice));
    System.out.println("Unique: " + uniqueStems(practice));
    System.out.println();

    // demonstrates stemming files
    System.out.println("____STEMMING FILE____");
    System.out.println();

    Path base = Path.of("src", "test", "resources", "stemmer");
    Path file = base.resolve("cleaner.txt");
    String input = Files.readString(file, StandardCharsets.UTF_8);

    System.out.println("Original:\n" + input);

    System.out.println("       List: " + listStems(file));
    System.out.println("     Unique: " + uniqueStems(file));
    System.out.println("List Unique: " + listUniqueStems(file));
  }

  /** Prevent instantiating this class of static methods. */
  private FileStemmer() {}
}
