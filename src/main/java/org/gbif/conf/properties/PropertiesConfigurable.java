package org.gbif.conf.properties;

import java.util.Properties;

/**
 * Interface to expose configuration setting as java Properties.
 */
public interface PropertiesConfigurable {

  /**
   * Generates a java Properties class containing configuration settings.
   */
  Properties toProperties();
}
