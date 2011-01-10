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
import org.gbif.utils.file.FileUtilsTest;

import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author markus
 * 
 */
public class CCollationComparatorTest {

  /**
   * make sure the sorting corresponds with the C-locale unix sort command
   * 
   * @throws IOException
   */
  @Test
  public void testSorting() throws IOException {
    LinkedList<String> unsorted = FileUtils.streamToList(FileUtils.classpathStream("sorting/LF_unix.txt"));
    // sort with comparator to test
    Collections.sort(unsorted, new CCollationComparator());
    for (String r : unsorted) {
      System.out.println(r);
    }
    // assert new order is fine
    FileUtilsTest.assertUnixSortOrder(unsorted.iterator());
  }
}
