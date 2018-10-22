/***************************************************************************
 * Copyright 2014 Global Biodiversity Information Facility Secretariat
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.utils.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Ordering;
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

  // Direct copy from From IPT codebase
  // see https://code.google.com/p/gbif-providertoolkit/source/browse/trunk/gbif-ipt/src/main/java/org/gbif/ipt/task/GenerateDwca.java#93
  private static final Comparator<String> IGNORE_CASE_COMPARATOR = Ordering.from(new Comparator<String>() {

    public int compare(String o1, String o2) {
      return o1.compareToIgnoreCase(o2);
    }
  }).nullsFirst();

  /**
   * Test for respecting equals and compareTo to ensure comparator respects java contracts.
   * http://dev.gbif.org/issues/browse/POR-2730
   */
  @Test
  public void testEqualsCompareToContract() throws IOException {

    LineComparator comp = new LineComparator(0, ",", null, IGNORE_CASE_COMPARATOR);

    String s1 = ",2,3"; // null in first instance
    String s2 = ",2,3"; // null in first instance

    // http://docs.oracle.com/javase/7/docs/api/java/util/Comparator.html#compare(T,%20T)
    int sign1 = (int) Math.signum(comp.compare(s1, s2));
    int sign2 = (int) Math.signum(comp.compare(s2, s1));

    assertEquals(sign1 * -1, sign2);

  }
}
