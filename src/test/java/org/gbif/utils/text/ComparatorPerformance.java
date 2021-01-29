/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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

import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.junit.jupiter.api.Test;

/**
 * @author markus
 */
public class ComparatorPerformance {

  private final String ENCODING = "UTF-8";

  /**
   * Comparing performance for various file soring methods.
   * <p/>
   * Executed on a MacPro with 9GB, 8-core 3GHz and 1TB disk
   * sorting a 207MB large text file made from concatenation of irmng.tail:
   * <p/>
   * Sorting with unix sort took 5817 ms
   * Sorting with org.gbif.utils.text.StringComparator and 10k lines in memory (200 parts) took 48968 ms
   * Sorting with org.gbif.utils.text.CCollationComparator and 10k lines in memory (200 parts) took 49858 ms
   * Sorting with org.gbif.utils.text.StringComparator and 100k lines in memory (20 parts) took 17962 ms
   * Sorting with org.gbif.utils.text.CCollationComparator and 100k lines in memory (20 parts) took 14046 ms
   * Sorting with org.gbif.utils.text.StringComparator and 1000k lines in memory (2 parts) took 15492 ms
   * Sorting with org.gbif.utils.text.CCollationComparator and 1000k lines in memory (2 parts) took 14317 ms
   */

  @Test
  public void testFileSorting() throws IOException, InstantiationException, IllegalAccessException {
    // 10MB text file, big file used in results above was concatenated from this one
    File source = FileUtils.getClasspathFile("sorting/irmng.tail");
    File sorted = File.createTempFile("gbif-common-file-sort2", "sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();

    // test unix sort
    long start = System.currentTimeMillis();
    futils.sort(source, sorted, ENCODING, 0, "\t", null, "\n", 0);
    long end = System.currentTimeMillis();
    System.out.println(String.format("Sorting with unix sort took %s ms", (end - start)));

    // sort with comparator to test
    List<Comparator<String>> comparators = availableComparators();

    for (Integer linesInMen : Arrays.asList(100000)) {
      futils.setLinesPerMemorySort(linesInMen);
      for (Comparator<String> comp : comparators) {

        start = System.currentTimeMillis();
        futils.sortInJava(source, sorted, ENCODING, comp, 0);
        end = System.currentTimeMillis();

        System.out.println(
          String.format("Sorting with %s and %s lines in memory took %s ms", comp.getClass().getName(), linesInMen, (end - start)));
      }
    }
  }

  private List<Comparator<String>> availableComparators() {
    List<Comparator<String>> comparators = Lists.newArrayList();
    comparators.add(new StringComparator());
    comparators.add(Ordering.<String>natural().nullsFirst());
    return comparators;
  }

  @Test
  public void testVariousComparators() throws IOException, InstantiationException, IllegalAccessException {
    // sort with comparator to test
    List<Comparator<String>> comparators = availableComparators();
    for (Comparator<String> comp : comparators) {
      LinkedList<String> source = FileUtils.streamToList(FileUtils.classpathStream("sorting/irmng.tail"));
      long start = System.currentTimeMillis();
      Collections.sort(source, comp);
      long end = System.currentTimeMillis();
      System.out.println("Sorting with " + comp.getClass().getName() + " took " + (end - start) + " ms");
    }
  }
}
