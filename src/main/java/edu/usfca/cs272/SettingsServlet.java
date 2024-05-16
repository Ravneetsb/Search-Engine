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
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servlet for the settings page. Responsible for validating the admin password for triggering a
 * metadata reset.
 *
 * @author Ravneet Singh Bhatia
 * @version Spring 2024
 */
class SettingsServlet extends HttpServlet {

  /** Class version for serialization, in [YEAR][TERM] format (unused). */
  @Serial private static final long serialVersionUID = 202401;

  /** The logger for this class. */
  private final transient Logger log = LogManager.getLogger();

  /** The html template to serve the client. */
  private final String htmlTemplate;

  /** The database connector used to connect to the on-campus database. */
  private final DatabaseConnector db;

  /** The admin password to trigger a reset of the metadata. */
  private final String PASSWORD = "p@$$w0rd";

  /**
   * Creates a new Settings servlet.
   *
   * @throws IOException if the template cannot be read.
   */
  public SettingsServlet(DatabaseConnector db) throws IOException {
    this.db = db;
    htmlTemplate =
        Files.readString(SearchServer.base.resolve("settings.html"), StandardCharsets.UTF_8);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String pass = request.getParameter("password");
    if (pass != null && pass.equals(PASSWORD)) { // authenticate the password.
      try {
        db.resetMetaData(db.getConnection());
        response.sendRedirect("/"); // send user back to home page if the reset is successful.
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);

    // output generated html
    PrintWriter out = response.getWriter();
    out.printf(htmlTemplate, SearchServer.theme);
    out.flush();
  }
}
