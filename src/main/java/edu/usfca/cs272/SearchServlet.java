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
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.StringJoiner;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.crypto.Data;

/** Servlet for the Home Page. */
class SearchServlet extends HttpServlet {

  /** Class version for serialization, in [YEAR][TERM] format (unused). */
  @Serial private static final long serialVersionUID = 202401;

  /** The thread-safe query processor to be used. */
  private final transient Processor processor;

  /** The logger for this class. */
  private final transient Logger log = LogManager.getLogger();

  /** The html template to serve the client. */
  private final String htmlTemplate;

  private final DatabaseConnector db;

  /**
   * The servlet for index page.
   *
   * @param processor the thread-safe query processor to use.
   * @throws IOException if the template cannot be read.
   */
  public SearchServlet(Processor processor, DatabaseConnector db) throws IOException {
    this.db = db;
    this.processor = processor;
    htmlTemplate =
        Files.readString(SearchServer.base.resolve("index.html"), StandardCharsets.UTF_8);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
//    DatabaseConnector db = new DatabaseConnector(Path.of("src/main/resources/database.properties"));
    String query = request.getParameter("query");
    query = StringEscapeUtils.escapeHtml4(query);
    StringJoiner sb = new StringJoiner("\n");
    StringJoiner stats = new StringJoiner("\n");
    if (query != null) {
      if (query.split(" ").length > 1) {
        try {
          Connection connection = db.getConnection();
          db.insertSearch(connection, query.replaceAll("\\s+", " "));
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }

      Instant start = Instant.now();
      processor.parseQuery(query);
      long elapsed = Duration.between(start, Instant.now()).toMillis();
      double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
      var scores = processor.getScores(query);
      sb.add("<div class='hero has-text-centered'>");
      sb.add(
          "<br /> <p class='sub-title is-5'> "
              + scores.size()
              + " results in "
              + seconds
              + " seconds."
              + "</p>");
      sb.add("</div>");
      for (var score : scores) {
        sb.add("<pre>");
        sb.add("<div class='container is-block'>");
        sb.add(
            String.join(
                "",
                "<a href='",
                score.getLocation(),
                "' target='_blank'>",
                score.getLocation(),
                "</a>"));
        sb.add(
            "<p class='sub-title is-6'> Score: "
                + score.getScore()
                + "\tMatches:"
                + score.getCount());
        sb.add("</p> </div> </pre>");
        sb.add("\n");
      }
    }

    try {
      Connection connection = db.getConnection();
      var topFive = db.getTopFiveSearches(connection);
      for (var stat: topFive) {
        stats.add("<pre>");
        stats.add(stat);
        stats.add("</pre>");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);

    // output generated html
    PrintWriter out = response.getWriter();
    out.printf(htmlTemplate, SearchServer.theme, sb, stats);
    out.flush();
  }
}
