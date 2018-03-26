package org.gbif.utils.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputStreamUtils {

  private static final Logger LOG = LoggerFactory.getLogger(InputStreamUtils.class);

  public InputStream classpathStream(String path) {
    InputStream in = null;
    // relative path. Use classpath instead
    URL url = getClass().getClassLoader().getResource(path);
    if (url != null) {
      try {
        in = url.openStream();
      } catch (IOException e) {
        LOG.warn("Cant open classpath input stream " + path, e);
      }
    }
    return in;
  }

  /**
   * Converts an entire InputStream to a single String with UTF8 as the character encoding.
   *
   * @param source source input stream to convert
   *
   * @return the string representing the entire input stream
   */
  public String readEntireStream(InputStream source) {
    return readEntireStream(source, FileUtils.UTF8);
  }

  /**
   * Converts an entire InputStream to a single String with explicitly provided character encoding.
   *
   * @param source   source input stream to convert
   * @param encoding the stream's character encoding
   *
   * @return the string representing the entire input stream
   */
  public String readEntireStream(InputStream source, String encoding) {
    if (!Charset.isSupported(encoding)) {
      throw new IllegalArgumentException("Unsupported encoding " + encoding);
    };

    ByteArrayOutputStream result = new ByteArrayOutputStream();

    try {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = source.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }
    } catch (IOException e) {
      LOG.error("Caught exception", e);
    } finally {
      try {
        source.close();
      } catch (IOException e) {
        LOG.error("Caught exception", e);
      }
    }

    // StandardCharsets.UTF_8.name() > JDK 7
    try {
      return result.toString(StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Could not decode stream as " + encoding);
    }
  }
}
