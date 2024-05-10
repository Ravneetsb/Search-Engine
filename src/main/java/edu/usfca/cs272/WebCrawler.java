package edu.usfca.cs272;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** WebCrawler for the search engine. */
public class WebCrawler {

  /** The number of redirects to follow. */
  private static final int REDIRECTS = 3;

  /** The invertedIndex to populate. */
  private final ThreadSafeInvertedIndex index;

  /** The Workqueue to use. */
  private final WorkQueue queue;

  /** The seed uri for the web crawler. */
  private final URI seed; // TODO Recommend as a parameter

  /** Keep track of links that have been already processed. */
  private final HashSet<URI> seen;

  /** The max number of pages to crawl. */
  private final int max;

  /** Logger */
  public static final Logger log = LogManager.getLogger();

  /**
   * Creates a new WebCrawler.
   *
   * @param index the invertedIndex to build.
   * @param queue the workqueue.
   * @param seed the seed uri.
   * @param max the maximum number of webpages to crawl.
   */
  public WebCrawler(ThreadSafeInvertedIndex index, WorkQueue queue, String seed, int max) {
    this.index = index;
    this.queue = queue;
    this.seed = URI.create(seed);
    this.max = max;
    this.seen = new HashSet<>();
  }

  /**
   * Processes links. Recursively prcesses any links on the webpage.
   *
   * @param seed the base uri
   */
  public void processLink(URI seed) {
    synchronized (seen) {
      if (seen.contains(seed)) {
        return;
      }
    }
    queue.execute(new Task(seed));
    queue.finish();
  }

  /** Process the seed uri */
  public void processLink() {
    this.processLink(this.seed);
  }

  /** Task for building the inverted index from a web page. */
  private class Task implements Runnable {

    /** The link to process */
    private final URI link;

    /**
     * Constructor for the Task
     *
     * @param link the link to process.
     */
    private Task(URI link) {
      this.link = link;
      synchronized (seen) {
        seen.add(link);
      }
    }

    @Override
    public void run() {
      // Step 1: Download the html.
      String html = HtmlFetcher.fetch(link, REDIRECTS);

      if (html == null) {
        log.info("{} was not 200.", link);
        return;
      }

      // Step 2: Process the links.
      html = HtmlCleaner.stripBlockElements(html);
      ArrayList<URI> internalLinks = LinkFinder.listUris(link, html);

      synchronized (seen) {
        for (var internalLink : internalLinks) {
          if (seen.size() >= max) {
            break;
          }
          if (!seen.contains(internalLink)) {
            queue.execute(new Task(internalLink));
          }
        }
      }

      // Step 3: Finish cleaning the html.
      String clean = HtmlCleaner.stripTags(html);
      clean = HtmlCleaner.stripEntities(clean);

      // Step 4: Add the stems to the index.
      SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
      var stems = FileStemmer.listStems(clean, stemmer);

      InvertedIndex local = new InvertedIndex();
      var absoluteLink = LinkFinder.toAbsolute(seed, link.toString());
      if (absoluteLink != null) {
        local.addAll(absoluteLink.toString(), stems);
        index.addIndex(local);
      }
    }

    @Override
    public String toString() {
      return "Task{" + "link=" + link + '}';
    }
  }

  @Override
  public String toString() {
    return "WebCrawler{" + "seed=" + seed + ", max=" + max + '}';
  }
}
