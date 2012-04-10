package org.gbif.utils.file.properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.google.common.io.Closeables;
import com.google.common.io.Resources;

/**
 * Utility class for handling properties files.
 */
public class PropertiesUtil {


  /**
   * Loads a properties file.
   * The file should be available in the classpath, the default {@link ClassLoader} is used to load the file.
   */
  public static Properties loadProperties(String propertiesFile) throws IOException {
    InputStream inputStream = null;
    Properties tempProperties;
    try {
      URL configFileURL = Resources.getResource(propertiesFile);
      inputStream = Resources.newInputStreamSupplier(configFileURL).getInput();
      tempProperties = new Properties();
      tempProperties.load(inputStream);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
    return tempProperties;
  }
  
  /**
   * Private default constructor.
   */
  private PropertiesUtil(){
    //empty block
  }
}
