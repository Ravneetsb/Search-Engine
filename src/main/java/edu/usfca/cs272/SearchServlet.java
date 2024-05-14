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

/** Servlet for the Home Page. */
class SearchServlet extends HttpServlet {

  /** Class version for serialization, in [YEAR][TERM] format (unused). */
  @Serial
  private static final long serialVersionUID = 202401;

/**
* The thread-safe query processor to be used.
*/
  private final transient Processor processor;

/**
* The logger for this class.
*/
  private final transient Logger log = LogManager.getLogger();

/**
* The html template to serve the client.
*/
  private final String htmlTemplate;

/**
* The servlet for index page.
 * @param processor the thread-safe query processor to use.
 * @throws IOException if the template cannot be read.
*/
  public SearchServlet(Processor processor) throws IOException {
    this.processor = processor;
    htmlTemplate = Files.readString(SearchServer.base.resolve("index.html"), StandardCharsets.UTF_8);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String query = request.getParameter("query");
    StringJoiner sb = new StringJoiner("\n");
    if (query != null) {
      sb.add("Your Query:" + query);
      log.info("Searching for: {}", query);
      processor.parseQuery(query);
      var scores = processor.getScores(query);
      for (var score : scores) {
        sb.add("<div>");
        sb.add("Score: " + score.getScore());
        sb.add(
            String.join(
                "",
                "<a href='",
                score.getLocation(),
                "' target='_blank'>",
                score.getLocation(),
                "</a>"));
        sb.add("Count: " + score.getCount());
        sb.add("</div>");
        sb.add("\n");
      }

    }

    response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);

    // output generated html
    PrintWriter out = response.getWriter();
    out.printf(htmlTemplate, "Search Engine", sb);
    out.flush();
  }
}
