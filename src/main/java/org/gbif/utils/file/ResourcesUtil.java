package org.gbif.utils.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.io.Closer;
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

      Closer closer = Closer.create();
      try {
        File f = new File(folder, classpathResource);
        Files.createParentDirs(f);
        OutputStream out = closer.register(new FileOutputStream(f));
        Resources.copy(url, out);
      } catch (Throwable e) {
        // must catch Throwable for closer to work, see https://code.google.com/p/guava-libraries/wiki/ClosingResourcesExplained
        throw closer.rethrow(e);
      } finally {
        closer.close();
      }
    }
  }

    /**
     * List directory contents for a resource folder. Not recursive.
     * Works for regular files and also JARs.
     *
     * Based on code from Greg Briggs, slightly modified.
     *
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths. Empty array in case folder cannot be found
     * @throws IOException
     */
    public static String[] list(Class clazz, String path) throws IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
        /* A file path: easy enough */
            try {
                return new File(dirURL.toURI()).list();
            } catch (URISyntaxException e) {
                throw new IOException("Bad URI. Cannot list files for path " + path + " in class " + clazz, e);
            }
        }

        if (dirURL == null) {
        /*
         * In case of a jar file, we can't actually find a directory.
         * Have to assume the same jar as clazz.
         */
            String me = clazz.getName().replace(".", "/")+".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
        /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            return result.toArray(new String[result.size()]);
        }

        return new String[]{};
    }

}
