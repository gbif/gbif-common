package org.gbif.utils.rs;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class knowing the url layout of rs.gbif.org to access authority and dictionary files.
 */
public class RsGbifOrg {
  protected static final Logger log = LoggerFactory.getLogger(RsGbifOrg.class);
  public static final String DICTIONARY_URL = "http://rs.gbif.org/dictionaries/";
  public static final String FILENAME_BLACKLIST = "blacklisted.txt";
  public static final String FILENAME_SUPRAGENERIC = "suprageneric.txt";
  public static final String FILENAME_EPITHETA = "epitheta.txt";
  public static final String FILENAME_EPITHETA_AMIGOUS = "epitheta_ambigous.txt";
  public static final String FILENAME_AUTHORS = "authors.txt";
  public static final String FILENAME_GENERA = "genera.txt";
  public static final String FILENAME_GENERA_AMIGOUS = "genera_ambigous.txt";

  /**
   * @param filename of authority dictionary file requested
   * @return url to file inside to authority folder of rs.gbif.org
   */
  public static URL authorityUrl(String filename) {
    return dictionaryUrl("authority/" + filename);
  }

  /**
   * @param filename of dictionary file requested
   * @return url to file inside to dictionary folder of rs.gbif.org
   */
  public static URL dictionaryUrl(String filename) {
    try {
      return new URL(DICTIONARY_URL + "/" + filename);
    } catch (MalformedURLException e) {
      log.error("Cannot create dictionary url for file " + filename, e);
    }
    return null;
  }

  /**
   * @param filename of synonyms file requested
   * @return url to file inside to synonyms dictionary folder of rs.gbif.org
   */
  public static URL synonymUrl(String filename) {
    return dictionaryUrl("synonyms/" + filename);
  }
}
