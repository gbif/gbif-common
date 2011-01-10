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

import org.gbif.utils.file.FileUtilsTest;

import org.apache.commons.io.LineIterator;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author markus
 * 
 */
public class UnixSortComparatorTest {

  private static LinkedList<String> streamToList(InputStream source) throws IOException {
    LinkedList<String> resultList = new LinkedList<String>();
    try {
      LineIterator lines = new LineIterator(new BufferedReader(new InputStreamReader(source, "UTF-8")));
      while (lines.hasNext()) {
        String line = lines.nextLine().trim();
        resultList.add(line);
      }
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Unsupported UTF8 encoding", e);
    }
    return resultList;
  }

  private InputStream classpathStream(String path) throws IOException {
    InputStream in = null;
    // relative path. Use classpath instead
    URL url = getClass().getClassLoader().getResource(path);
    if (url != null) {
      in = url.openStream();
    }
    return in;
  }

  /**
   * make sure the sorting corresponds with the C-locale unix sort command
   * 
   * @throws IOException
   */
  @Test
  public void testSorting() throws IOException {
    LinkedList<String> unsorted = streamToList(classpathStream("sorting/LF_mac.txt"));
    // remove header row
    String h = unsorted.pollFirst();
    System.out.println(h);
    // sort with comparator to test
    Collections.sort(unsorted, new UnixSortComparator());
    // add back in header row
    unsorted.addFirst("HEADER_ROW");
    // assert new order is fine
    FileUtilsTest.assertUnixSortOrder(unsorted.iterator());
  }
}
