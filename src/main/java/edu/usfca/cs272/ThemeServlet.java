package edu.usfca.cs272;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The servlet responsible for toggling between dark and light mode in the webpages.
 *
 * @author Ravneet Singh Bhatia
 * @version Spring 2024
 */
public class ThemeServlet extends HttpServlet {

  /** Class version for serialization, in [YEAR][TERM] format (unused). */
  @Serial private static final long serialVersionUID = 202401;

  /** The logger for this class. */
  private final transient Logger log = LogManager.getLogger();

  /** The html template to serve the client. */
  private final String htmlTemplate;

  /** The database connector to used for connecting to the on-campus database. */
  private final transient DatabaseConnector db;

  /**
   * Creates a new theme servlet.
   *
   * @throws IOException if the template cannot be read.
   */
  public ThemeServlet(DatabaseConnector db) throws IOException {
    this.db = db;
    htmlTemplate =
        Files.readString(SearchServer.base.resolve("settings.html"), StandardCharsets.UTF_8);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    SearchServer.changeTheme(); // change the theme.

    String referer = request.getHeader("referer");

    if (referer.contains("/?query=")) {    // Prevent the search from being triggered again if theme is changed.
      referer = "/";
    }

    if (referer != null && !referer.isEmpty()) {
      // if the change was successful, redirect the user back to the same page they came from.
      response.sendRedirect(referer);
    } else {
      response.sendRedirect("/"); // send back to home page.
    }
  }
}
