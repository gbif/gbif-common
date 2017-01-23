package org.gbif.utils;

import java.lang.reflect.Field;

/**
 * Utility method to work with annotations.
 */
public class AnnotationUtils {

  /**
   * Check if a field is annotated with @Deprecated in the provided class.
   * Mostly used on elements of an Enum but will also work on class {@link Field} (private and public).
   *
   * @param _class
   * @param fieldName
   * @return true if the specified field is annotated with @Deprecated on the provided class. False is all
   * other cases (including if the field doesn't exist)
   */
  public static boolean isFieldDeprecated(Class<?> _class, String fieldName) {
    try {
      Field field = _class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.isAnnotationPresent(Deprecated.class);
    } catch (NoSuchFieldException ignore) {}
    return false;
  }
}
