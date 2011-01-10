package org.gbif.utils.text;

import java.util.Comparator;

/**
 * A comparator that produces the same sort order as the unix sort command.
 * As unix sort varies on different OSes we use the simple string comparator for now
 * 
 * @author markus
 * 
 */
public class UnixSortComparator implements Comparator<String> {
  private Comparator<String> comp = String.CASE_INSENSITIVE_ORDER;

  public int compare(String arg0, String arg1) {
    return comp.compare(arg0, arg1);
  }

  @Override
  public boolean equals(Object arg0) {
    return comp.equals(arg0);
  }

}
