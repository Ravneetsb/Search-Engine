package edu.usfca.cs272;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/** Server for the search engine. */
public class SearchServer {

  /** The server. */
  private final Server server;

  /** The processor to use. */
  private final Processor processor;

  private final WorkQueue queue;

  /** Base path for the resource templates. */
  public static final Path base = Path.of("src", "main", "resources", "template");

  /**
   * Host the server on localhost.
   *
   * @param port the port on which to host the server.
   * @param index the index to perform search on
   * @param processor the processor to perform search on index.
   */
  public SearchServer(int port, ThreadSafeInvertedIndex index, Processor processor, WorkQueue queue)
      throws IOException {
    this.server = new Server(port);
    this.processor = processor;
    this.queue = queue;
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

  @Override
  public String toString() {
    return "Search Server for the Search Engine";
  }

  /**
   * Shutdown servlet for the server. This allows the user to trigger a graceful shutdown by entering
   * a password.
   */
  public class ShutdownServlet extends HttpServlet {
    /** Class version for serialization, in [YEAR][TERM] format (unused). */
    @Serial
    private static final long serialVersionUID = 202401;

    /** The html template to serve. */
    private final String htmlTemplate;

    /** The password to trigger the shutdown. */
    private static final String PASSWORD = "a";

    /**
     * The servlet for the shutdown page.
     *
     * @throws IOException if the template cannot be read.
     */
    public ShutdownServlet() throws IOException {
      this.htmlTemplate =
              Files.readString(SearchServer.base.resolve("shutdown.html"), StandardCharsets.UTF_8);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

      String password = request.getParameter("password");
      //    StringJoiner joiner = new StringJoiner("\n");

      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);

      if (password != null) {
        System.out.println(password);
        if (password.equals(PASSWORD)) {
          System.out.println(server);
          try {
            server.stop();
            server.join();
            queue.join();
          } catch (Exception e) {
            System.err.println("Unable to shutdown server");
          }
        } else {

        }
      }

      // output generated html
      PrintWriter out = response.getWriter();
      out.printf(htmlTemplate, "data-theme='light'");
      out.flush();
    }
  }
}
