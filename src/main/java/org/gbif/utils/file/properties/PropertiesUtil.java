/*
 * Copyright 2015 Global Biodiversity Information Facility (GBIF)
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
package org.gbif.utils.file.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Closer;
import com.google.common.io.Resources;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.gbif.utils.file.FileUtils;

/**
 * Utility class for handling properties files.
 * TODO this class should probably be in a "properties" package at the same level as "file"
 */
public class PropertiesUtil {

  /**
   * When we encode strings, we always specify UTF8 encoding
   */
  public static final String UTF8_ENCODING = FileUtils.UTF8;

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
   * @throws IOException Should there be an issue in loading the file
   * @throws IllegalArgumentException If the file does not exist
   */
  public static Properties loadProperties(String propertiesFile) throws IOException, IllegalArgumentException {
    Properties tempProperties = new Properties();
    Closer closer = Closer.create();
    try {
      File file = new File(propertiesFile);
      if (file.exists()) {// first tries to load the file as a external file
        tempProperties.load(closer.register(new FileInputStream(file)));
      } else { // tries to load the file as a resource
        URL configFileURL = Resources.getResource(propertiesFile);
        tempProperties.load(closer.register(Resources.asByteSource(configFileURL).openStream()));
      }
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
   * @param p The properties file to read from.
   * @param key To read the value of.
   * @param exceptionForNull If true, and the property is not found an IAE is thrown, otherwise defaultValue is
   *        returned
   * @param defaultValue If the property is not found, and exceptionForNull is false, this is returned for missing
   *        properties.
   * @return The property at the key as an Double
   * @throws IllegalArgumentException if the property is invalid (can't be cast to a double) or not found and we are
   *         instructed to throw it.
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
   * @param p The properties file to read from.
   * @param key To read the value of.
   * @param exceptionForNull If true, and the property is not found an IAE is thrown, otherwise defaultValue is
   *        returned
   * @param defaultValue If the property is not found, and exceptionForNull is false, this is returned for missing
   *        properties.
   * @return The property at the key as an Float
   * @throws IllegalArgumentException if the property is invalid (can't be cast to a float) or not found and we are
   *         instructed to throw it.
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
   * @param p The properties file to read from.
   * @param key To read the value of.
   * @param exceptionForNull If true, and the property is not found an IAE is thrown, otherwise defaultValue is
   *        returned
   * @param defaultValue If the property is not found, and exceptionForNull is false, this is returned for missing
   *        properties.
   * @return The property at the key as an int
   * @throws IllegalArgumentException if the property is invalid (can't be cast to an int) or not found and we are
   *         instructed to throw it.
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
   * Reads and casts the named property as a boolean.
   * Case insensitive values for 'true', 'on', 'yes', 't' and 'y' return true values,
   * 'false', 'off', 'no', 'f' and 'n' return false.
   * Otherwise or in case of a missing property the default will be used.
   *
   * @param p The properties file to read from.
   * @param key To read the value of.
   * @param defaultValue If the property is not found this is returned for missing properties.
   * @return The property at the key as a boolean
   */
  public static boolean propertyAsBool(Properties p, String key, boolean defaultValue) {
    Boolean val = BooleanUtils.toBooleanObject(p.getProperty(key, null));
    return val == null ? defaultValue : val;
  }

  /**
   * Reads and converts the named property as UTF8 bytes.
   *
   * @param p The properties file to read from.
   * @param key To read the value of.
   * @param exceptionForNull If true, and the property is not found an IAE is thrown, otherwise defaultValue is
   *        returned
   * @param defaultValue If the property is not found, and exceptionForNull is false, this is returned for missing
   *        properties.
   * @return The property at the key as byte[]t
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

  /**
   * Filters and translates Properties with a prefix.
   * The resulting Properties will only include the properties that start with the provided prefix with that prefix
   * removed (e.g. myprefix.key1 will be returned as key1 if prefix = "myprefix.")
   *
   * @param properties to filter and translate
   * @param prefix prefix used to filter the properties. (e.g. "myprefix.")
   * @return new Properties object with filtered and translated properties. Never null.
   */
  public static Properties filterProperties(final Properties properties, String prefix) {
    Preconditions.checkNotNull(properties, "Can't filter a null Properties");
    Preconditions.checkState(StringUtils.isNotBlank(prefix), "Can't filter using a blank prefix", properties);

    Properties filtered = new Properties();
    for (String key : properties.stringPropertyNames()) {
      if (key.startsWith(prefix)) {
        filtered.setProperty(key.substring(prefix.length()), properties.getProperty(key));
      }
    }
    return filtered;
  }

  /**
   * Returns a new Properties object that contains only the elements where the key starts by the provided
   * prefix. The same keys will be used in the returned Properties.
   * @param original
   * @param prefix
   * @return
   */
  public static Properties subsetProperties(final Properties original, String prefix) {
    return propertiesByPrefix(original, prefix, false);
  }

  /**
   * Remove properties from the original object and return the removed element(s) as new Properties object.
   * The same keys will be used in the returned Properties.
   * @param original original object in which the element will be removed if key starts with provided prefix.
   * @param prefix
   * @return
   */
  public static Properties removeProperties(final Properties original, String prefix) {
    return propertiesByPrefix(original, prefix, true);
  }

  /**
   * Get a a new Properties object that only contains the elements that start with the prefix.
   * The same keys will be used in the returned Properties.
   * @param original
   * @param prefix
   * @param remove should the element(s) be removed from the original Properties object
   * @return
   */
  private static Properties propertiesByPrefix(final Properties original, String prefix, boolean remove) {
    Preconditions.checkNotNull(original, "Can't filter a null Properties");
    Preconditions.checkState(StringUtils.isNotBlank(prefix), "Can't filter using a blank prefix", original);

    Properties filtered = new Properties();

    if(original.isEmpty()){
      return filtered;
    }

    Iterator<Object> keysIt = original.keySet().iterator();
    String key;
    while (keysIt.hasNext()) {
      key = String.valueOf(keysIt.next());
      if (key.startsWith(prefix)) {
        filtered.setProperty(key, original.getProperty(key));
        if(remove){
          keysIt.remove();
        }
      }
    }
    return filtered;
  }

}
