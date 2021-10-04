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
package org.gbif.utils.rs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class knowing the url layout of rs.gbif.org to access authority and dictionary files.
 */
public final class RsGbifOrg {

  private static final Logger LOG = LoggerFactory.getLogger(RsGbifOrg.class);

  public static final String DOMAIN = "http://rs.gbif.org/";
  public static final String FILENAME_BLACKLIST = "blacklisted.txt";
  public static final String FILENAME_SUPRAGENERIC = "suprageneric.txt";
  public static final String FILENAME_EPITHETA = "epitheta.txt";
  public static final String FILENAME_EPITHETA_AMIGOUS = "epitheta_ambigous.txt";
  public static final String FILENAME_AUTHORS = "authors.txt";
  public static final String FILENAME_GENERA = "genera.txt";
  public static final String FILENAME_GENERA_AMIGOUS = "genera_ambigous.txt";

  /**
   * @param path given as array of individual names that will be concatenated
   * @return url to file inside rs.gbif.org
   */
  public static URL url(String... path) {
    try {
      if (path == null) {
        return new URL(DOMAIN);
      }

      String fullPath =
          DOMAIN + Arrays.stream(path).filter(Objects::nonNull).collect(Collectors.joining("/"));

      return new URL(fullPath);
    } catch (MalformedURLException e) {
      LOG.error("Cannot create rs.gbif.org url for path " + Arrays.toString(path), e);
    }
    return null;
  }

  /**
   * @param filename of dictionary file requested
   * @return url to file inside to dictionary folder of rs.gbif.org
   */
  public static URL dictionaryUrl(String filename) {
    return url("dictionaries", filename);
  }

  /**
   * @param filename of authority dictionary file requested
   * @return url to file inside to authority folder of rs.gbif.org
   */
  public static URL authorityUrl(String filename) {
    return url("dictionaries", "authority", filename);
  }

  /**
   * @param filename of synonyms file requested
   * @return url to file inside to synonyms dictionary folder of rs.gbif.org
   */
  public static URL synonymUrl(String filename) {
    return url("dictionaries", "synonyms", filename);
  }
}
