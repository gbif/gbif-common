/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.utils.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResourcesUtilTest {

  private InputStreamUtils isu = new InputStreamUtils();

  @Test
  public void testCopyResources() throws Exception {
    File tmp = FileUtils.createTempDir();
    tmp.deleteOnExit();
    ResourcesUtil.copy(tmp, "", true, "testNOT_EXISTING.txt", "test1/test.txt", "test1/test2/test.txt");

    // test
    File t1 = new File(tmp, "test1/test.txt");
    File t2 = new File(tmp, "test1/test2/test.txt");
    org.apache.commons.io.FileUtils.contentEquals(t1, t2);
    assertTestFile(t1);
    assertTestFile(t2);
  }

  @Test
  public void testCopyResourcesThrowing() throws Exception {
    File tmp = FileUtils.createTempDir();
    tmp.deleteOnExit();
    assertThrows(IOException.class,
        () -> ResourcesUtil.copy(tmp, "", false,
            "testNOT_EXISTING.txt", "test1/test.txt", "test1/test2/test.txt"));

    // test
    File t1 = new File(tmp, "test1/test.txt");
    File t2 = new File(tmp, "test1/test2/test.txt");
    org.apache.commons.io.FileUtils.contentEquals(t1, t2);
    assertThrows(IOException.class, () -> assertTestFile(t1));
    assertThrows(IOException.class, () -> assertTestFile(t2));
  }

  @Test
  public void testList() throws Exception {
    assertEquals(newHashSet("test.txt", "test2"), newHashSet(ResourcesUtil.list(ResourcesUtil.class, "test1")));
    assertEquals(newHashSet("test.txt"), newHashSet(ResourcesUtil.list(ResourcesUtil.class, "test1/test2")));
    assertEquals(newHashSet("utf16be.xml", "utf16le.xml", "utf8.xml", "utf8bom.xml"), newHashSet(ResourcesUtil.list(ResourcesUtil.class, "sax")));
    assertEquals(newHashSet(), newHashSet(ResourcesUtil.list(ResourcesUtil.class, "abba")));
  }

  private Set<String> newHashSet(String... elements) {
    HashSet<String> set = new HashSet<>(elements.length);
    Collections.addAll(set, elements);
    return set;
  }

  private void assertTestFile(File tf) throws FileNotFoundException {
    assertEquals("hallo", isu.readEntireStream(FileUtils.getInputStream(tf)));
  }
}
