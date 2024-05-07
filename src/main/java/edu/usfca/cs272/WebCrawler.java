package edu.usfca.cs272;

import java.net.URI;
import java.util.HashSet;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

  /** Keep track of links that have been already processed. */
  public final HashSet<URI> seen = new HashSet<>();

  /** Logger */
  public static final Logger log = LogManager.getLogger();

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
    queue.execute(new Task(seed));
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
      synchronized (seen) {
        if (seen.contains(link)) {
          log.warn("Already seen: {}", link);
          return;
        } else {
          seen.add(link);
        }
      }

      String html = HtmlFetcher.fetch(link, REDIRECTS);

      if (html == null) {
        return;
      }

      log.info("Task for: {}", link);

      SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
      String clean = HtmlCleaner.stripHtml(html);
      var stems = FileStemmer.listStems(clean, stemmer);

      //      HashSet<URI> links = new HashSet<>();
      //      LinkFinder.findLinks(seed, html, links);
      //
      //      for (var embeddedLink : links) {
      //        queue.execute(new Task(embeddedLink));
      //      }

      InvertedIndex local = new InvertedIndex();

      local.addAll(String.valueOf(LinkFinder.toAbsolute(seed, seed.toString())), stems);
      index.addIndex(local);
    }
  }
}
