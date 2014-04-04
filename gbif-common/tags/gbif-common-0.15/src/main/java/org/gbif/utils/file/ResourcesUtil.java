package org.gbif.utils.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils class dealing with classpath resources in addition to guavas {@link Resources}.
 */
public class ResourcesUtil {

  private static final Logger LOG = LoggerFactory.getLogger(ResourcesUtil.class);

  /**
   * Static utils class.
   */
  private ResourcesUtil() {

  }

  /**
   * Copies classpath resources to real files.
   *
   * @param folder                 to copy resource files into
   * @param ignoreMissingResources if true ignores missing resources, throws IOException otherwise
   * @param classpathPrefix        common prefix added to all classpath resources which is not used for the result file
   *                               path.
   * @param classpathResources     list of classpath resources to be copied into folder
   */
  public static void copy(File folder, String classpathPrefix, boolean ignoreMissingResources,
    String... classpathResources) throws IOException {
    for (String classpathResource : classpathResources) {
      String res = classpathPrefix + classpathResource;
      URL url = null;
      try {
        url = Resources.getResource(res);
        if (url == null) {
          throw new IllegalArgumentException("Classpath resource " + res + " not existing");
        }
      } catch (IllegalArgumentException e) {
        if (ignoreMissingResources) {
          LOG.debug("Resource {} not found", res);
          continue;
        }
        throw new IOException(e);
      }
      File f = new File(folder, classpathResource);
      Files.createParentDirs(f);
      OutputStream out = new FileOutputStream(f);
      Resources.copy(url, out);
      Closeables.closeQuietly(out);
    }
  }

}
