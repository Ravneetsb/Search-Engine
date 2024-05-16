package edu.usfca.cs272;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Serial;
import java.sql.SQLException;

/**
 * The servlet responsible for keeping track of the result links when they are clicked and updating
 * the visit count of those results.
 *
 * @author Ravneet Singh Bhatia
 * @version Spring 2024
 */
public class ResultTrackerServlet extends HttpServlet {
  /** Class version for serialization, in [YEAR][SEMESTER] format. */
  @Serial private static final long serialVersionUID = 202401;

  /** Connects the servlet to the on-campus database. */
  private final transient DatabaseConnector db;

  /**
   * Creates a new ResultTrackerServlet.
   *
   * @param db the database connector used to connect to the on-campus database.
   */
  public ResultTrackerServlet(DatabaseConnector db) {
    this.db = db;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String link = request.getParameter("link");
    String referer = request.getHeader("referer");
    if (referer == null || referer.isEmpty()) {
      response.sendRedirect("/");
    }
      try {
          db.insertResults(db.getConnection(), link);
      } catch (SQLException e) {
          throw new RuntimeException(e);
      }
      response.sendRedirect(link);
  }
}
