package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/** Server for the search engine. */
public class SearchServer {

  /** The server. */
  private final Server server;

  /** The processor to use. */
  private final Processor processor;

  /** Base path for the resource templates. */
  public static final Path base = Path.of("src", "main", "resources", "template");

  /**
   * Host the server on localhost.
   *
   * @param port the port on which to host the server.
   * @param index the index to perform search on
   * @param processor the processor to perform search on index.
   */
  public SearchServer(int port, ThreadSafeInvertedIndex index, Processor processor)
      throws IOException {
    this.server = new Server(port);
    this.processor = processor;
    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(new ServletHolder(new SearchServlet(this.processor)), "/");
    handler.addServletWithMapping(new ServletHolder(new ShutdownServlet()), "/shutdown");

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
      this.server.stop();
      this.server.join();
    } catch (Exception e) {
      System.err.println("Something went wrong shutting down the server.");
    }
  }

  @Override
  public String toString() {
    return "Search Server for the Search Engine";
  }
}
