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

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author markus
 * 
 */
public class ComparatorPerformance {
  private final String ENCODING = "UTF-8";

  /**
   * Comparing performance for various file soring methods.
   * 
   * Executed on a MacPro with 9GB, 8-core 3GHz and 1TB disk
   * sorting a 207MB large text file made from concatenation of irmng.tail:
   * 
   * Sorting with unix sort took 5817 ms
   * Sorting with org.gbif.utils.text.StringComparator and 10k lines in memory (200 parts) took 48968 ms
   * Sorting with org.gbif.utils.text.CCollationComparator and 10k lines in memory (200 parts) took 49858 ms
   * Sorting with org.gbif.utils.text.StringComparator and 100k lines in memory (20 parts) took 17962 ms
   * Sorting with org.gbif.utils.text.CCollationComparator and 100k lines in memory (20 parts) took 14046 ms
   * Sorting with org.gbif.utils.text.StringComparator and 1000k lines in memory (2 parts) took 15492 ms
   * Sorting with org.gbif.utils.text.CCollationComparator and 1000k lines in memory (2 parts) took 14317 ms
   * 
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
    List<Class> classes = new ArrayList<Class>();
    classes.add(StringComparator.class);
    classes.add(CCollationComparator.class);

    for (Integer linesInMen : Arrays.asList(100000)) {
      futils.setLinesPerMemorySort(linesInMen);
      for (Class cl : classes) {
        Comparator<String> comp = (Comparator<String>) cl.newInstance();

        start = System.currentTimeMillis();
        futils.sortInJava(source, sorted, ENCODING, comp, 0);
        end = System.currentTimeMillis();

        System.out.println(String.format("Sorting with %s and %s lines in memory took %s ms", cl.getName(), linesInMen, (end - start)));
      }
    }
  }

  @Test
  public void testVariousComparators() throws IOException, InstantiationException, IllegalAccessException {
    // sort with comparator to test
    List<Class> classes = new ArrayList<Class>();
    classes.add(StringComparator.class);
    classes.add(CCollationComparator.class);
    for (Class cl : classes) {
      LinkedList<String> source = FileUtils.streamToList(FileUtils.classpathStream("sorting/irmng.tail"));
      Comparator<String> comp = (Comparator<String>) cl.newInstance();
      long start = System.currentTimeMillis();
      Collections.sort(source, comp);
      long end = System.currentTimeMillis();
      System.out.println("Sorting with " + cl.getName() + " took " + (end - start) + " ms");
    }
  }
}
