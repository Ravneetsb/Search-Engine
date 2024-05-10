package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and returns HTML content if
 * possible.
 *
 * @see HttpsFetcher
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2024
 */
public class HtmlFetcher {
  /**
   * Returns {@code true} if and only if there is a "content-type" header (assume lowercase) and the
   * first value of that header starts with the value "text/html" (case-insensitive).
   *
   * @param headers the HTTP/1.1 headers to parse
   * @return {@code true} if the headers indicate the content type is HTML
   * @see HttpsFetcher#processHttpHeaders(BufferedReader)
   */
  public static boolean isHtml(Map<String, List<String>> headers) {
    var contentType = headers.get("content-type");
    if (contentType != null) {
    	// TODO NoSuchElementException - if this collection is empty
      return contentType.getFirst().toLowerCase().startsWith("text/html");
    }
    return false;
  }
  
  // TODO Any of the getFirst need to be careful about using

  /**
   * Parses the HTTP status code from the provided HTTP headers, assuming the status line is stored
   * under the {@code null} key.
   *
   * @param headers the HTTP/1.1 headers to parse
   * @return the HTTP status code or -1 if unable to parse for any reasons
   * @see HttpsFetcher#processHttpHeaders(BufferedReader)
   */
  public static int getStatusCode(Map<String, List<String>> headers) {
    var status = headers.get(null).getFirst();
    Pattern pattern = Pattern.compile("\\s(\\d{3})\\s"); // TODO Your regexes could usually be static
    Matcher matcher = pattern.matcher(status);
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    }
    return -1;
  }

  /**
   * If the HTTP status code is between 300 and 399 (inclusive) indicating a redirect, returns the
   * first redirect location if it is provided. Otherwise returns {@code null}.
   *
   * @param headers the HTTP/1.1 headers to parse
   * @return the first redirected location if the headers indicate a redirect
   * @see HttpsFetcher#processHttpHeaders(BufferedReader)
   */
  public static String getRedirect(Map<String, List<String>> headers) {
    if (isRedirect(headers)) {
      var redirects = headers.get("location");
      return redirects.getFirst();
    }
    return null;
  }

  /**
   * Returns true if the status code indicates that there is a redirect.
   *
   * @param headers html headers.
   * @return true if the status code indicates that there is a redirect.
   */
  public static boolean isRedirect(Map<String, List<String>> headers) {
    int statusCode = getStatusCode(headers);
    return (statusCode <= 399 && statusCode >= 300);
  }

  /**
   * Efficiently fetches HTML using HTTP/1.1 and sockets.
   *
   * <p>The HTTP body will only be fetched and processed if the status code is 200 and the
   * content-type is HTML. In that case, the HTML will be returned as a single joined String using
   * the .
   *
   * <p>Otherwise, the HTTP body will not be fetched. However, if the status code is a redirect,
   * then the location of the redirect will be recursively followed up to the specified number of
   * times. Once the number of redirects falls to 0 or lower, then redirects will no longer be
   * followed.
   *
   * <p>If valid HTML cannot be fetched within the specified number of redirects, then {@code null}
   * is returned.
   *
   * @param uri the URI to fetch
   * @param redirects the number of times to follow redirects
   * @return the HTML or {@code null} if unable to fetch valid HTML
   * @see HttpsFetcher#openConnection(URI)
   * @see HttpsFetcher#printGetRequest(PrintWriter, URI)
   * @see HttpsFetcher#processHttpHeaders(BufferedReader)
   * @see String#join(CharSequence, CharSequence...)
   * @see System#lineSeparator()
   * @see #isHtml(Map)
   * @see #getRedirect(Map)
   */
  public static String fetch(URI uri, int redirects) {
    String html = null;

    try (Socket socket = HttpsFetcher.openConnection(uri);
        PrintWriter request = new PrintWriter(socket.getOutputStream());
        InputStreamReader input = new InputStreamReader(socket.getInputStream(), UTF_8);
        BufferedReader response = new BufferedReader(input)) {
      HttpsFetcher.printGetRequest(request, uri);
      var headers = HttpsFetcher.processHttpHeaders(response);
      if (isHtml(headers)) {
        if (isRedirect(headers) && redirects > 0) {
          html = fetch(getRedirect(headers), redirects - 1);
        } else if (getStatusCode(headers) == 200) {
          html = String.join("\n", response.lines().toList());
        }
      }
    } catch (IOException e) {
      html = null;
      System.out.println("Not a html Page."); // TODO Remove 
    }

    return html;
  }

  /**
   * Converts the {@link String} into a {@link URI} object and then calls {@link #fetch(URI, int)}.
   *
   * @param uri the URI to fetch
   * @param redirects the number of times to follow redirects
   * @return the HTML or {@code null} if unable to fetch valid HTML
   * @see #fetch(URI, int)
   */
  public static String fetch(String uri, int redirects) {
    try {
      return fetch(new URI(uri), redirects);
    } catch (NullPointerException | URISyntaxException e) {
      return null;
    }
  }

  /**
   * Converts the {@link String} url into a {@link URL} object and then calls {@link #fetch(URI,
   * int)} with 0 redirects.
   *
   * @param uri the URI to fetch
   * @return the HTML or {@code null} if unable to fetch valid HTML
   * @see #fetch(URI, int)
   */
  public static String fetch(String uri) {
    return fetch(uri, 0);
  }

  /**
   * Calls {@link #fetch(URI, int)} with 0 redirects.
   *
   * @param uri the URI to fetch
   * @return the HTML or {@code null} if unable to fetch valid HTML
   */
  public static String fetch(URI uri) {
    return fetch(uri, 0);
  }
}
