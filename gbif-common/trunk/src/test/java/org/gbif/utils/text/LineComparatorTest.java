package org.gbif.utils.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.junit.Test;

public class LineComparatorTest extends TestCase {

  @Test
  public void testLineComparatorCSV() {
    LineComparator comp = new LineComparator(3, ",", '"');
    List<String> lines = new ArrayList<String>();
    String l1 = "121,432423,9099053,\"Frieda karla L.,Ahrens\"";
    String l2 = "adshjhg,fsdfsd,fdsfdsfsd,Forunkel forunculus,Janssen";
    String l3 = ",,,,zzz";
    String l4 = "321,2453,432423,berndef,ahrene32";
    lines.add(l1);
    lines.add(l2);
    lines.add(l3);
    lines.add(l4);

    Collections.sort(lines, comp);

    assertEquals(l2, lines.get(0));
    assertEquals(l1, lines.get(1));
    assertEquals(l4, lines.get(2));
    assertEquals(l3, lines.get(3));
  }

  @Test
  public void testLineComparatorPipe() {
    LineComparator comp = new LineComparator(3, "|");
    List<String> lines = new ArrayList<String>();
    String l1 = "121|432423|9099053|Frieda karla L.|Ahrens";
    String l2 = "adshjhg|fsdfsd|fdsfdsfsd|Forunkel forunculus|Janssen";
    String l3 = "||||zzz";
    String l4 = "321|2453|432423|berndef|ahrene32";
    lines.add(l1);
    lines.add(l2);
    lines.add(l3);
    lines.add(l4);

    Collections.sort(lines, comp);

    assertEquals(l2, lines.get(0));
    assertEquals(l1, lines.get(1));
    assertEquals(l4, lines.get(2));
    assertEquals(l3, lines.get(3));
  }

  @Test
  public void testLineComparatorTab() {
    LineComparator comp = new LineComparator(3, "\t");
    List<String> lines = new ArrayList<String>();
    String l1 = "121\t432423\t9099053\tFrieda karla L.\tAhrens";
    String l2 = "adshjhg\tfsdfsd\tfdsfdsfsd\tForunkel forunculus\tJanssen";
    String l3 = "\t\t\t\tzzz";
    String l4 = "321\t2453\t432423\tberndef\tahrene32";
    lines.add(l1);
    lines.add(l2);
    lines.add(l3);
    lines.add(l4);

    Collections.sort(lines, comp);

    assertEquals(l2, lines.get(0));
    assertEquals(l1, lines.get(1));
    assertEquals(l4, lines.get(2));
    assertEquals(l3, lines.get(3));
  }
}
