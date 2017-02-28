package org.gbif.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/**
 * Unit tests related to {@link Preconditions}.
 */
public class PreconditionsTest {

  @Test
  public void testCheckArgument() {
    try {
      Preconditions.checkArgument(false, "My {} message. {} based on {}", "awesome", true, 2);
      fail("checkArgument shall throw IllegalArgumentException");
    }
    catch (IllegalArgumentException iaEx) {
      assertEquals("My awesome message. true based on 2", iaEx.getMessage());
    }
  }

  @Test
  public void testCheckArgumentMessageArgument() {
    //test less argument than defined in the message. We expect placeholders to be kept.
    try {
      Preconditions.checkArgument(false, "My {} message. {} based on {}", "awesome");
      fail("checkArgument shall throw IllegalArgumentException");
    }
    catch (IllegalArgumentException iaEx) {
      assertEquals("My awesome message. {} based on {}", iaEx.getMessage());
    }

    //test more arguments than defined in the message. We expect additional arguments to be lost.
    try {
      Preconditions.checkArgument(false, "My {} message.", "awesome", true, 2);
      fail("checkArgument shall throw IllegalArgumentException");
    }
    catch (IllegalArgumentException iaEx) {
      assertEquals("My awesome message.", iaEx.getMessage());
    }
  }

  @Test
  public void testCheckArgumentWithNull() {
    try {
      Preconditions.checkArgument(false, null);
      fail("checkArgument shall throw IllegalArgumentException");
    }
    catch (IllegalArgumentException iaEx) {
      assertEquals("No message specified", iaEx.getMessage());
    }
  }
}
