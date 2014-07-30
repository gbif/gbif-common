package org.gbif.conf.properties;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Utility class that exposes operations for handling PropertyKey annotations.
 */
public class PropertyKeyUtils {

  private PropertyKeyUtils() {
    //
  }


  /**
   * Extracts the PropertyKey annotations from declared fields.
   */
  public static Properties toProperties(Object object) {
    Properties properties = new Properties();
    for (Field field : object.getClass().getDeclaredFields()) {
      PropertiesKey propertiesKey = field.getAnnotation(PropertiesKey.class);
      if (propertiesKey != null) {
        try {
          final Object value = field.get(object);
          properties.put(propertiesKey.value(), value != null ? value.toString() : "");
        } catch (IllegalArgumentException e) {
          // do nothing
        } catch (IllegalAccessException e) {
          // do nothing
        }
      }
    }

    return properties;
  }
}
