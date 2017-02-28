package org.gbif.utils;

import java.text.MessageFormat;

import org.apache.commons.lang3.*;

/**
 * The main purpose of this utility class is to more or less offer precondition checks similar to Guava Preconditions
 * without using Guava.
 *
 * This utility class also uses the same placeholder ({}) for message formatting as SLF4J org.slf4j.helpers.MessageFormatter
 * without using it to avoid introducing a dependency to slf4j only to format a String. Java {@link MessageFormat}
 * uses placeholder like {1}, {2} which can be useful but most of the type makes the syntax heavier. String.format
 * forces you to define a type for the arguments which we could avoid in the context of a message of an exception.
 *
 * Guava and SLF4J are currently already included in the dependencies of this project but we want to keep the liberty
 * to remove it without affecting basic functionality like precondition checks.
 *
 */
public class Preconditions {

  private static final String PLACEHOLDER = "{}";

  /**
   * Utility method to validate arguments of a function that are expecting to meet an expression.
   *
   * @param expression expression to decides if the current argument sh {@link IllegalArgumentException} shall be thrown or not
   * @param message message or message template to use for the {@link IllegalArgumentException}. Template are in the
   *                form of: checkArgument(list.size() > 1, "This method was expecting size to be > {}", 1);
   * @param arguments arguments to use with the template
   */
  public static void checkArgument(boolean expression, String message, Object ... arguments) {

    if (!expression) {
      String formattedMessage = org.apache.commons.lang3.ObjectUtils.defaultIfNull(message, "No message specified");
      if(arguments != null && message != null) {
        for (int i = 0; i < arguments.length; i++) {
          formattedMessage = StringUtils.replaceOnce(formattedMessage, PLACEHOLDER, arguments[i].toString());
        }
      }
      throw new IllegalArgumentException(formattedMessage);
    }
  }
}
