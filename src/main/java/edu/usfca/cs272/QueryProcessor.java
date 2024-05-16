package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** Single threaded query processor.*/
public class QueryProcessor implements Processor {

  /** Map of the query and its score */
  private final TreeMap<String, ArrayList<InvertedIndex.Score>> searches;

  /** The search method that will be used on the index. */
  private final Function<Set<String>, ArrayList<InvertedIndex.Score>> searchMethod;

  /** Stemmer for the processor. */
  private static final SnowballStemmer stemmer =
      new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

  /**
   * Creates a new query processor.
   *
   * @param invertedIndex The index to be searched.
   * @param partial true if partial search is to be performed.
   */
  public QueryProcessor(InvertedIndex invertedIndex, boolean partial) {
    this.searches = new TreeMap<>();
    this.searchMethod = partial ? invertedIndex::partialSearch : invertedIndex::exactSearch;
  }

  /**
   * Creates a new Query Processor that always executes an exact search.
   *
   * @param invertedIndex index to be searched
   */
  public QueryProcessor(InvertedIndex invertedIndex) {
    this.searches = new TreeMap<>();
    this.searchMethod = invertedIndex::exactSearch;
  }

  @Override
  public void parseQuery(String query) {
    var stems = FileStemmer.uniqueStems(query, stemmer);
    query = String.join(" ", stems);

    if (query.isBlank() || searches.containsKey(query)) {
      return;
    }
    ArrayList<InvertedIndex.Score> scores = searchMethod.apply(stems);
    searches.put(query, scores);
  }

  @Override
  public void toJson(Path path) throws IOException {
    JsonWriter.writeSearch(searches, path);
  }

  @Override
  public String toString() {
    return JsonWriter.writeSearch(searches);
  }


@Override
  public List<InvertedIndex.Score> getScores(String query) {
    var stems = FileStemmer.uniqueStems(query, stemmer);
    query = String.join(" ", stems);
    ArrayList<InvertedIndex.Score> scores = searches.get(query);
    if (scores == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(scores);
    }
  }

@Override
  public Set<String> getQueries() {
    return Collections.unmodifiableSet(searches.keySet());
  }
}
