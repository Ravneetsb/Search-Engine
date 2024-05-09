package edu.usfca.cs272;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** WebCrawler for the search engine. */
public class WebCrawler {

  /** The number of redirects to follow. */
  public static final int REDIRECTS = 3;

  /** The invertedIndex to populate. */
  public final ThreadSafeInvertedIndex index;

  /** The Workqueue to use. */
  public final WorkQueue queue;

  /** The seed uri for the web crawler. */
  public final URI seed;

  /** Keep track of links that have been already processed. */
  public final HashSet<URI> seen = new HashSet<>();

  /** The tracker for number of links to process. */
  private final AtomicInteger crawlLimit;

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
    this.crawlLimit = new AtomicInteger(0);
    log.info("Using {} as crawlLimit", max);
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
      synchronized (seen) {
        seen.add(link);
        log.info("link: {} || seen by: {}", link, Thread.currentThread());
        log.info("Links seen: {}", seen.size());
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
      ArrayList<URI> internalLinks = LinkFinder.listUris(seed, html);

      synchronized (seen) {
        for (var internalLink : internalLinks) {
          if (seen.size() >= max) {
            break;
          }
          if (!seen.contains(internalLink)) {
            seen.add(internalLink);
            queue.execute(new Task(internalLink));
          } else {
            log.warn("{} was already processed", link);
          }
        }
      }

      // Step 3: Finish cleaning the html.
      String clean = HtmlCleaner.stripHtml(html);

      // Step 4: Add the stems to the index.
      SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
      var stems = FileStemmer.listStems(clean, stemmer);

      InvertedIndex local = new InvertedIndex();

      local.addAll(LinkFinder.toAbsolute(seed, link.toString()).toString(), stems);
      index.addIndex(local);
      log.info("{} was added to index.", link);
    }
  }
}
