package edu.usfca.cs272;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/** Servlet for the Home Page. */
class SearchServlet extends HttpServlet {

  private ThreadSafeInvertedIndex index;

  private Processor processor;

  public SearchServlet(ThreadSafeInvertedIndex index, Processor processor) {
    this.index = index;
    this.processor = processor;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // generate html from template
    String html =
        """
					<!DOCTYPE html>
					<html lang="en">

					<head>
					  <meta charset="utf-8">
					  <title>Ravneet</title>
					</head>

					<body>
					<h1>%1$s</h1>

					<form method="get" action="/get_reverse">
					  <p>
					    <input type="text" name="query" size="50"></input>
					  </p>

					  <p>
					    <button>Reverse</button>
					  </p>
					</form>

					<pre>
					%2$s
					</pre>

					</body>
					</html>
					""";

    String query = request.getParameter("query");

    processor.parseQuery(query);

    response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);

    // output generated html
    PrintWriter out = response.getWriter();
    out.printf(html, "Search Engine", processor.getScores(query));
    out.flush();
  }
}
