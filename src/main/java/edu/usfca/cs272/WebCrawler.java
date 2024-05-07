package edu.usfca.cs272;

import java.net.URI;
import java.util.HashSet;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** WebCrawler for the search engine. */
public class WebCrawler {

  /** The number of redirects to follow. */
  public static final int REDIRECTS = 3;

  /** The invertedIndex to populate. */
  public final InvertedIndex index;

  /** The Workqueue to use. */
  public final WorkQueue queue;

  /** The seed uri for the web crawler. */
  public final URI seed;

  public final HashSet<URI> seen = new HashSet<>();

  /**
   * Creates a new WebCrawler.
   *
   * @param index the invertedIndex to build.
   * @param queue the workqueue.
   * @param seed the seed uri.
   */
  public WebCrawler(InvertedIndex index, WorkQueue queue, String seed) {
    this.index = index;
    this.queue = queue;
    this.seed = URI.create(seed);
  }

  /**
   * Processes links. Recursively prcesses any links on the webpage.
   *
   * @param seed the base uri
   */
  public void processLink(URI seed) {
    if (seen.contains(seed)) {
      return;
    }
    String html = HtmlFetcher.fetch(seed);
    HashSet<URI> links = new HashSet<>();
    if (html != null) {
      LinkFinder.findLinks(seed, html, links);
      String cleanedHtml = HtmlCleaner.stripHtml(html);
      var stems = FileStemmer.uniqueStems(cleanedHtml);
      index.addAll(seed.toString(), stems);
      seen.add(seed);
      for (URI link : links) {
        queue.execute(new Task(link));
      }
    }
    queue.finish();
  }

  /** Process the seed uri */
  public void processLink() {
    this.processLink(this.seed);
  }

  /** Processing a link in muktithreading. */
  public void process() {}

  /** Task for building the inverted index from a web page. */
  private class Task implements Runnable {

    private final URI link;

    private Task(URI link) {
      this.link = link;
    }

    @Override
    public void run() {
      SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
      InvertedIndex localIndex = new InvertedIndex();
      String html = HtmlFetcher.fetch(link, REDIRECTS);
      String clean = HtmlCleaner.stripHtml(html);
      var stems = FileStemmer.uniqueStems(clean, stemmer);
      localIndex.addAll(link.toString(), stems);
      index.addIndex(localIndex);
    }
  }
}
