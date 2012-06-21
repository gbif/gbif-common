package org.gbif.utils.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Resources;

/**
 * Utils class dealing with classpath resources in addition to guavas {@link Resources}.
 */
public class ResourcesUtil {

  /**
   * Copies classpath resources to real files.
   *
   * @param folder             to copy resource files into
   * @param classpathPrefix    common prefix added to all classpath resources which is not used for the result file
   *                           path.
   * @param classpathResources list of classpath resources to be copied into folder
   */
  public static void copy(File folder, String classpathPrefix, String... classpathResources) throws IOException {
    for (String classpathResource : classpathResources) {
      String res = classpathPrefix + classpathResource;
      URL url = Resources.getResource(res);
      if (url == null) {
        throw new IOException("Classpath resource " + res + " not existing");
      }
      File f = new File(folder, classpathResource);
      Files.createParentDirs(f);
      OutputStream out = new FileOutputStream(f);
      Resources.copy(url, out);
      Closeables.closeQuietly(out);
    }
  }

}
