package org.gbif.utils.file.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;

import com.google.common.base.Strings;
import com.google.common.io.Closer;
import com.google.common.io.Resources;

/**
 * Utility class for handling properties files.
 */
public class PropertiesUtil {

  /**
   * When we encode strings, we always specify UTF8 encoding
   */
  public static final String UTF8_ENCODING = "UTF-8";

  /**
   * Private default constructor.
   */
  private PropertiesUtil() {
    // empty block
  }

  /**
   * Loads a properties file.
   * The file should be available in the classpath, the default {@link ClassLoader} is used to load the file.
   *
   * @throws IOException              Should there be an issue in loading the file
   * @throws IllegalArgumentException If the file does not exist
   */
  public static Properties loadProperties(String propertiesFile) throws IOException, IllegalArgumentException {
    Properties tempProperties = new Properties();
    Closer closer = Closer.create();
    try {
      URL configFileURL = Resources.getResource(propertiesFile);
      InputStream inputStream = closer.register(Resources.newInputStreamSupplier(configFileURL).getInput());
      tempProperties.load(inputStream);
    } finally {
      closer.close();
    }
    return tempProperties;
  }

  /**
   * Reads a property file from an absolute filepath.
   */
  public static Properties readFromFile(String filepath) throws IOException, IllegalArgumentException {
    if (Strings.isNullOrEmpty(filepath)) {
      throw new IllegalArgumentException("No properties file given");
    }
    File pf = new File(filepath);
    if (!pf.exists()) {
      throw new IllegalArgumentException("Cannot find properties file " + filepath);
    }
    Properties properties = new Properties();

    Closer closer = Closer.create();
    try {
      FileReader reader = closer.register(new FileReader(pf));
      properties.load(reader);
    } finally {
      closer.close();
    }
    return properties;
  }

  /**
   * Reads and casts the named property as an Double.
   *
   * @param p                The properties file to read from.
   * @param key              To read the value of.
   * @param exceptionForNull If true, and the property is not found an IAE is thrown, otherwise defaultValue is
   *                         returned
   * @param defaultValue     If the property is not found, and exceptionForNull is false, this is returned for missing
   *                         properties.
   *
   * @return The property at the key as an Double
   *
   * @throws IllegalArgumentException if the property is invalid (can't be cast to a double) or not found and we are
   *                                  instructed to throw it.
   */
  public static Double propertyAsDouble(Properties p, String key, boolean exceptionForNull, Double defaultValue)
    throws IllegalArgumentException {
    String v = p.getProperty(key);
    if (v != null) {
      try {
        return Double.parseDouble(v);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid value[" + v + "] supplied for " + key);
      }
    } else {
      if (exceptionForNull) {
        throw new IllegalArgumentException("Missing property for " + key);
      } else {
        return defaultValue;
      }
    }
  }

  /**
   * Reads and casts the named property as an Float.
   *
   * @param p                The properties file to read from.
   * @param key              To read the value of.
   * @param exceptionForNull If true, and the property is not found an IAE is thrown, otherwise defaultValue is
   *                         returned
   * @param defaultValue     If the property is not found, and exceptionForNull is false, this is returned for missing
   *                         properties.
   *
   * @return The property at the key as an Float
   *
   * @throws IllegalArgumentException if the property is invalid (can't be cast to a float) or not found and we are
   *                                  instructed to throw it.
   */
  public static Float propertyAsFloat(Properties p, String key, boolean exceptionForNull, Float defaultValue)
    throws IllegalArgumentException {
    String v = p.getProperty(key);
    if (v != null) {
      try {
        return Float.parseFloat(v);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid value[" + v + "] supplied for " + key);
      }
    } else {
      if (exceptionForNull) {
        throw new IllegalArgumentException("Missing property for " + key);
      } else {
        return defaultValue;
      }
    }
  }

  /**
   * Reads and casts the named property as an Integer.
   *
   * @param p                The properties file to read from.
   * @param key              To read the value of.
   * @param exceptionForNull If true, and the property is not found an IAE is thrown, otherwise defaultValue is
   *                         returned
   * @param defaultValue     If the property is not found, and exceptionForNull is false, this is returned for missing
   *                         properties.
   *
   * @return The property at the key as an int
   *
   * @throws IllegalArgumentException if the property is invalid (can't be cast to an int) or not found and we are
   *                                  instructed to throw it.
   */
  public static Integer propertyAsInt(Properties p, String key, boolean exceptionForNull, Integer defaultValue)
    throws IllegalArgumentException {
    String v = p.getProperty(key);
    if (v != null) {
      try {
        return Integer.parseInt(v);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid value[" + v + "] supplied for " + key);
      }
    } else {
      if (exceptionForNull) {
        throw new IllegalArgumentException("Missing property for " + key);
      } else {
        return defaultValue;
      }
    }
  }

  /**
   * Reads and converts the named property as UTF8 bytes.
   *
   * @param p                The properties file to read from.
   * @param key              To read the value of.
   * @param exceptionForNull If true, and the property is not found an IAE is thrown, otherwise defaultValue is
   *                         returned
   * @param defaultValue     If the property is not found, and exceptionForNull is false, this is returned for missing
   *                         properties.
   *
   * @return The property at the key as byte[]t
   *
   * @throws IllegalArgumentException if the property is not found and we are instructed to throw it.
   */
  public static byte[] propertyAsUTF8Bytes(Properties p, String key, boolean exceptionForNull, byte[] defaultValue)
    throws IllegalArgumentException {
    String v = p.getProperty(key);
    if (v != null) {
      try {
        return v.getBytes(UTF8_ENCODING);
      } catch (UnsupportedEncodingException e) {
        // never one would hope
        throw new RuntimeException("System does not support " + UTF8_ENCODING + " encoding");
      }
    } else {
      if (exceptionForNull) {
        throw new IllegalArgumentException("Missing property for " + key);
      } else {
        return defaultValue;
      }
    }
  }
}
