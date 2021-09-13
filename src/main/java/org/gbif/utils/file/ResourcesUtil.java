/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.utils.file;

import org.gbif.utils.PreconditionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils class dealing with classpath resources.
 */
public final class ResourcesUtil {

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
      URL url;
      try {
        url = getResource(res);
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
      FileUtils.createParentDirs(f);

      try {
        Files.copy(Paths.get(url.toURI()), f.toPath());
      } catch (URISyntaxException e) {
        throw new IOException(e);
      }
    }
  }

  /**
   * Returns a {@code URL} pointing to {@code resourceName} if the resource is found using the
   * {@linkplain Thread#getContextClassLoader() context class loader}. In simple environments, the
   * context class loader will find resources from the class path. In environments where different
   * threads can have different class loaders, for example app servers, the context class loader
   * will typically have been set to an appropriate loader for the current thread.
   *
   * <p>In the unusual case where the context class loader is null, the class loader that loaded
   * this class will be used instead.
   *
   * <p>From Guava.
   *
   * @throws IllegalArgumentException if the resource is not found
   */
  public static URL getResource(String resourceName) {
    ClassLoader loader =
        ObjectUtils.firstNonNull(Thread.currentThread().getContextClassLoader(), ResourcesUtil.class.getClassLoader());
    URL url = loader.getResource(resourceName);
    PreconditionUtils.checkArgument(url != null, "resource " + resourceName + " not found.");
    return url;
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
        if (!path.endsWith("/")) {
            path = path + "/";
        }
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
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, FileUtils.UTF8));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    if (!StringUtils.isBlank(entry)) {
                        int checkSubdir = entry.indexOf("/");
                        if (checkSubdir >= 0) {
                            // if it is a subdirectory, we just return the directory name
                            entry = entry.substring(0, checkSubdir);
                        }
                        result.add(entry);
                    }
                }
            }
            return result.toArray(new String[result.size()]);
        }

        return new String[]{};
    }

}
