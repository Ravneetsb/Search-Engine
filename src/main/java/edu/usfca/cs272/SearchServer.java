package edu.usfca.cs272;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/** Server for the search engine. */
public class SearchServer {

  /** The server. */
  private final Server server;

  /** Inverted Index */
  private final ThreadSafeInvertedIndex index;

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

  /** Home servlet. */
  public static class HomeServlet extends HttpServlet {

    public HomeServlet(Processor processor) {}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      String html =
          """
                          <!DOCTYPE html>
                          <html lang="en">

                          <head>
                            <meta charset="utf-8">
                            <title>Vibe</title>
                          </head>

                          <body>
                          <h1>Vibe it!</h1>

                          <form method="get" action="/query">
                            <p>
                              <input type="text" name="query" size="50"></input>
                            </p>

                            <p>
                              <button>Search</button>
                            </p>
                          </form>

                          </body>
                          </html>
                          """;
      String query = request.getParameter("query");
      String result;
      if (query != null || query.isBlank()) {
        result = "";
      } else {
        result = null;
      }
    }
  }
}
