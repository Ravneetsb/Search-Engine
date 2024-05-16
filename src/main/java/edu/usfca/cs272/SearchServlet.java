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
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.StringJoiner;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Search Servlet. This class is responsible for triggering the search on the index based on
 * user input.
 *
 * @author Ravneet Singh Bhatia
 * @version Spring 2024
 */
public class SearchServlet extends HttpServlet {

  /** Class version for serialization, in [YEAR][TERM] format (unused). */
  @Serial private static final long serialVersionUID = 202401;

  /** The thread-safe query processor to be used. */
  private final transient Processor processor;

  /** The logger for this class. */
  private final transient Logger log = LogManager.getLogger();

  /** The html template to serve the client. */
  private final String htmlTemplate;

  /** The database connector to connect to the on-campus SQL database. */
  private final transient DatabaseConnector db;

  /**
   * Creates a new Search servlet.
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
    String query = request.getParameter("query");
    query = StringEscapeUtils.escapeHtml4(query);
    StringJoiner sb = new StringJoiner("\n");
    StringJoiner stats = new StringJoiner("\n");
    if (query != null) {
      if (query.split(" ").length > 1) {
        // Add the multi-word query in the database.
        try {
          Connection connection = db.getConnection();
          db.insertSearch(connection, query.replaceAll("\\s+", " "));
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }

      Instant start = Instant.now(); // the instant the search is going to be called.
      processor.parseQuery(query);
      long elapsed = Duration.between(start, Instant.now()).toMillis();
      double seconds =
          (double) elapsed / Duration.ofSeconds(1).toMillis(); // time taken to execute the search.
      var scores = processor.getScores(query);

      // Let the user know how many results were found in how many seconds.
      sb.add("<div class='hero has-text-centered'>");
      sb.add(
          "<br /> <p class='sub-title is-5'> "
              + scores.size()
              + " results in "
              + seconds
              + " seconds."
              + "</p>");
      sb.add("</div>");

      // Serve the results.
      for (var score : scores) {
        sb.add("<pre>");
        sb.add("<div class='container is-block'>");
        sb.add(
            String.join(
                "",
                "<a href='/result?link=",
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

    // Let the user know the top 5 multi-word queries in the database.
    try {
      Connection connection = db.getConnection();
      var topFive = db.getTopFiveSearches(connection);
      for (var stat : topFive) {
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
