package edu.usfca.cs272;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class responsible for allowing a user to download the inverted index as a json file.
 *
 * @author Ravneet Singh Bhatia
 * @version Spring 2024
 */
public class DownloadServlet extends HttpServlet {

  /** The inverted index from which the downloadable file will be generated. */
  private final transient ThreadSafeInvertedIndex index;

  /**
   * Creates a new DownloadServlet.
   *
   * @param index the inverted index from which the downloadable file will be generated.
   */
  public DownloadServlet(ThreadSafeInvertedIndex index) {
    this.index = index;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String content = index.toString();
    byte[] bytes = content.getBytes();
    Path temp = Files.createTempFile("index", ".json");
    Files.write(temp, bytes);
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", "attachment; filename=\"index.json\"");
    try {
      Files.copy(temp, response.getOutputStream());
    } catch (IOException e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } finally {
      Files.deleteIfExists(temp);
    }
  }
}
