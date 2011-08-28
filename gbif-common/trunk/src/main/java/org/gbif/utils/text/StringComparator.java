package org.gbif.utils.text;

import java.util.Comparator;

/**
 * A comparator based around Javas String.compareTo()
 *
 * @author markus
 */
public class StringComparator implements Comparator<String> {

  public int compare(String arg0, String arg1) {
    if (arg0 == null && arg1 == null) {
      return 0;
    } else if (arg0 == null) {
      return 1;
    } else if (arg1 == null) {
      return -1;
    }
    return arg0.compareTo(arg1);
  }

}
