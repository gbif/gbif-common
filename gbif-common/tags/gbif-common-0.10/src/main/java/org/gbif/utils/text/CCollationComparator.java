package org.gbif.utils.text;

import java.util.Comparator;

/**
 * A comparator producing an ANSI C / POSIX collation.
 * This sort order is often used in various unix tools, databases and the one used in the FileUtils.sortInUnix shell
 * environment.
 *
 * @author markus
 */
public class CCollationComparator implements Comparator<String> {

  public int compare(String arg0, String arg1) {
    if (arg0 == null && arg1 == null) {
      return 0;
    }
    if (arg0 == null) {
      return 1;
    }
    if (arg1 == null) {
      return -1;
    }
    // no nulls anymore
    // get minimum string length
    int minLength = Math.min(arg0.length(), arg1.length());

    for (int i = 0; i < minLength; i++) {
      int x0 = arg0.charAt(i);
      int x1 = arg1.charAt(i);
      if (x0 == x1) {
        continue;
      }
      return x0 < x1 ? -1 : 1;
    }
    // the smaller string comes first
    if (arg0.length() == minLength) {
      return -1;
    }
    if (arg1.length() == minLength) {
      return 1;
    }
    // exactly same strings
    return 0;
  }

}
