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
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Server for the Search Engine.
 *
 * @author Ravneet Singh Bhatia
 * @version Spring 2024
 */
public class SearchServer {

  /** The server. */
  private final Server server;

  /** The work queue that executes the search tasks. */
  private final WorkQueue queue;

  /** The theme for the web pages. The default theme is light. */
  public static String theme = "<html lang=\"en\" data-theme=\"light\">";

  /** The html line for the light theme. */
  private static final String LIGHT_THEME = "<html lang=\"en\" data-theme=\"light\">";

  /** The html line for the dark theme. */
  private static final String DARK_THEME = "<html lang=\"en\" data-theme=\"dark\">";

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
    DatabaseConnector db = new DatabaseConnector(Path.of("src/main/resources/database.properties"));
    this.server = new Server(port); // instantiate the Server.
    this.queue = queue;
    ServletHandler handler = new ServletHandler();
    // Map the servlets.
    handler.addServletWithMapping(new ServletHolder(new SearchServlet(processor, db)), "/");
    handler.addServletWithMapping(new ServletHolder(new SettingsServlet(db)), "/settings");
    handler.addServletWithMapping(new ServletHolder(new ShutdownServlet()), "/shutdown");
    handler.addServletWithMapping(new ServletHolder(new ThemeServlet(db)), "/theme-change");
    handler.addServletWithMapping(new ServletHolder(new ResultTrackerServlet(db)), "/result");

    server.setHandler(handler);
  }

  /** Changes the theme of the webpages. */
  public static void changeTheme() {
    if (theme.equals(LIGHT_THEME)) {
      theme = DARK_THEME;
    } else {
      theme = LIGHT_THEME;
    }
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
   * Shutdown servlet for the server. This allows the user to trigger a graceful shutdown by
   * entering a password.
   */
  public class ShutdownServlet extends HttpServlet {
    /** Class version for serialization, in [YEAR][TERM] format (unused). */
    @Serial private static final long serialVersionUID = 202401;

    /** The html template to serve. */
    private final String htmlTemplate;

    /** The password to trigger the shutdown. */
    private static final String PASSWORD = "p@$$w0rd";

    /**
     * Creates a new Shutdown servlet.
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

      password = StringEscapeUtils.escapeHtml4(password);

      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);

      // output generated html
      PrintWriter out = response.getWriter();
      out.printf(htmlTemplate, SearchServer.theme);
      out.flush();

      if (password != null) { // there is an input.
        if (password.equals(PASSWORD)) { // authenticate.
          try {
            // Graceful shutdown 🧘‍♂️
            server.stop();
            server.join();
            queue.join();
          } catch (Exception e) {
            System.err.println("Unable to shutdown server");
          }
        } else {
          // Add pop-up if there is time.
        }
      }
    }
  }
}
