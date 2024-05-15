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
import java.util.StringJoiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.crypto.Data;

/** Servlet for the Home Page. */
class SettingsServlet extends HttpServlet {

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
  public SettingsServlet(DatabaseConnector db) throws IOException {
    this.db = db;
    htmlTemplate =
        Files.readString(SearchServer.base.resolve("settings.html"), StandardCharsets.UTF_8);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String pass = request.getParameter("password");
    if (pass != null && pass.equals("a")) {
        try {
            db.resetMetaData(db.getConnection());
            response.sendRedirect("/");
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
