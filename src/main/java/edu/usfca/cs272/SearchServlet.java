package edu.usfca.cs272;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.StringJoiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Servlet for the Home Page. */
class SearchServlet extends HttpServlet {

  private ThreadSafeInvertedIndex index;

  private Processor processor;

  private final Logger log = LogManager.getLogger();

  private final String htmlTemplate;

  public SearchServlet(ThreadSafeInvertedIndex index, Processor processor) throws IOException {
    this.index = index;
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
