package edu.usfca.cs272;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.StringJoiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.crypto.Data;

/** Servlet for the Home Page. */
class ThemeServlet extends HttpServlet {

  /** Class version for serialization, in [YEAR][TERM] format (unused). */
  @Serial private static final long serialVersionUID = 202401;

  /** The logger for this class. */
  private final transient Logger log = LogManager.getLogger();

  /** The html template to serve the client. */
  private final String htmlTemplate;

  private final DatabaseConnector db;

  /**
   * The servlet for settings page.
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

    SearchServer.changeTheme();

    String referer = request.getHeader("referer");

    if (referer != null && !referer.isEmpty()) {
      response.sendRedirect(referer);
    } else {
      response.sendRedirect("/");
    }
  }
}
