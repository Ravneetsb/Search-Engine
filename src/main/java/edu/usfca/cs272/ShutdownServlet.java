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

/**
 * Shutdown servlet for the server. This allows the user to trigger a graceful shutdown by entering
 * a password.
 */
public class ShutdownServlet extends HttpServlet {
  /** Class version for serialization, in [YEAR][TERM] format (unused). */
  @Serial private static final long serialVersionUID = 202401;

  /** The html template to serve. */
  private final String htmlTemplate;

/**
* The password to trigger the shutdown.
*/
  private static final String PASSWORD = "a";

  /**
   * The servlet for the shutdown page.
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
//    StringJoiner joiner = new StringJoiner("\n");

    response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);

    // output generated html
    PrintWriter out = response.getWriter();
    out.printf(htmlTemplate);
    out.flush();

    if (password != null) {
      System.out.println(password);
      if (password.equals(PASSWORD)) {
        SearchServer server = (SearchServer) getServletContext().getAttribute("searchServer");
        System.out.println(server);
        server.shutdown();
        System.exit(-1);
      }
    }
    System.out.println("Shutdown servlet.");


  }
}
