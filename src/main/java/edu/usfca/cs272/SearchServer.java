package edu.usfca.cs272;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/** Server for the search engine. */
public class SearchServer {

  /** The server. */
  private final Server server;

  /** Inverted Index */
  private final ThreadSafeInvertedIndex index;

  private final Logger log = LogManager.getLogger();

  /**
   * Host the server on localhost.
   *
   * @param port the port on which to host the server.
   * @param index the index to perform search on
   * @param processor the processor to perform search on index.
   */
  public SearchServer(int port, ThreadSafeInvertedIndex index, Processor processor) {
    this.server = new Server(port);
    this.index = index;
    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(new ServletHolder(new SearchServlet(index, processor)), "/index");
    //    handler.addServletWithMapping(new HomeServlet(processor), "/index");

    server.setHandler(handler);
  }

  /**
   * Launches the server.
   *
   * @throws Exception if the server cannot be started.
   */
  public void launch() throws Exception {
    server.start();
  }

  /** Shutdowns the server. */
  public void shutdown() {
    try {
      this.server.join();
    } catch (InterruptedException e) {
      System.err.println("Something went wrong shutting down the server.");
    }
  }

  @Override
  public String toString() {
    return "Search Server for the Search Engine";
  }
}
