package org.gbif.utils.file;

/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

import org.gbif.utils.text.LineComparator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.common.base.Splitter;
import org.apache.commons.io.LineIterator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author markus
 */
public class FileUtilsTest {

  private final String ENCODING = "UTF-8";

  public static void assertUnixSortOrder(File sorted) throws IOException {
    // read file
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sorted), "UTF-8"));
    LineIterator liter = new LineIterator(br);
    assertUnixSortOrder(liter);
  }

  public static void assertUnixSortOrder(Iterator<String> it) throws IOException {
    LinkedList<String> sorted = FileUtils.streamToList(FileUtils.classpathStream("sorting/LF_sorted.txt"));
    while (it.hasNext()) {
      String x = it.next();
      System.out.println(x);
      assertEquals(sorted.poll(), x);
    }
  }

  @Test
  public void humanReadableByteCountTest() {
    assertTrue(FileUtils.humanReadableByteCount(11, true).equals("11 B"));
    assertTrue(FileUtils.humanReadableByteCount(1000, true).equals("1.0 kB"));
    assertTrue(FileUtils.humanReadableByteCount(1000000, true).equals("1.0 MB"));
    assertTrue(FileUtils.humanReadableByteCount(1000000000, true).equals("1.0 GB"));
    assertTrue(FileUtils.humanReadableByteCount(1000000000000L, true).equals("1.0 TB"));

    assertTrue(FileUtils.humanReadableByteCount(11, false).equals("11 B"));
    assertTrue(FileUtils.humanReadableByteCount(1024, false).equals("1.0 KiB"));
    assertTrue(FileUtils.humanReadableByteCount(1024*1024, false).equals("1.0 MiB"));
    assertTrue(FileUtils.humanReadableByteCount(1024*1024*1024, false).equals("1.0 GiB"));
    assertTrue(FileUtils.humanReadableByteCount(1024*1024*1024*1024L, false).equals("1.0 TiB"));
  }

  /**
   * tests deleting directory recursively.
   */
    public
    void testDeleteRecursive() throws IOException {
    File topFile = File.createTempFile("top", ".tmp");
    File middleFile = File.createTempFile("middle", ".tmp", topFile.getParentFile());
    File downFile = File.createTempFile("down", ".tmp", middleFile.getParentFile());

    assertTrue(topFile.getParentFile().exists());
    assertTrue(topFile.exists());
    assertTrue(middleFile.getParentFile().exists());
    assertTrue(middleFile.exists());
    assertTrue(downFile.getParentFile().exists());
    assertTrue(downFile.exists());

    FileUtils.deleteDirectoryRecursively(topFile.getParentFile());

    assertFalse(topFile.getParentFile().exists());
    assertFalse(topFile.exists());
    assertFalse(middleFile.getParentFile().exists());
    assertFalse(middleFile.exists());
    assertFalse(downFile.getParentFile().exists());
    assertFalse(downFile.exists());
  }

  @Test
  public void testSortingHeaderlessFile() throws IOException {
    final int IDCOLUMN = 0;
    File source = FileUtils.getClasspathFile("sorting/irmng.tail");
    File sorted = File.createTempFile("gbif-common-file-sort", "sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, ENCODING, IDCOLUMN, "\t", null, "\n", 0);

    // read file
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sorted), "UTF-8"));
    int line = 0;
    while (true) {
      line++;
      String row = br.readLine();
      if (row == null) {
        break;
      }

      if (line == 1) {
        assertTrue(row.startsWith("aca10000053"));
      } else if (line == 2) {
        assertTrue(row.startsWith("aca1000012"));
      } else if (line == 100) {
        assertTrue(row.startsWith("acr10001387"));
      } else if (line == 100000) {
        assertTrue(row.startsWith("vir10000981"));
      }
    }
  }

  /**
   * tests sorting mac line endings \r which dont work with unix sort
   */
  @Test
  @Ignore
  public void testSortingMac() throws IOException {
    File source = FileUtils.getClasspathFile("sorting/LF_mac.txt");
    File sorted = File.createTempFile("sort-test", "mac.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, ENCODING, 0, "×", null, "\r", 0);

    assertUnixSortOrder(sorted);
  }

  /**
   * tests sorting unix line endings \n which work with unix sort
   */
  @Test
  public void testSortingUnix() throws IOException {
    File source = FileUtils.getClasspathFile("sorting/LF_unix.txt");
    File sorted = File.createTempFile("sort-test", "unix.txt");
    // sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, ENCODING, 0, "×", null, "\n", 0);

    assertUnixSortOrder(sorted);

  }

  /**
   * tests sorting windows line endings \r\n which work with unix sort
   */
  @Test
  public void testSortingWindows() throws IOException {
    File source = FileUtils.getClasspathFile("sorting/LF_win.txt");
    File sorted = File.createTempFile("sort-test", "windows.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, ENCODING, 0, "×", null, "\r\n", 0);

    assertUnixSortOrder(sorted);
  }

  @Test
  public void testSortingWithHeaders() throws IOException {
    final int IDCOLUMN = 0;
    File source = FileUtils.getClasspathFile("sorting/csv_always_quoted.csv");
    File sorted = File.createTempFile("gbif-common-file-sort", "sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, ENCODING, IDCOLUMN, ",", '"', "\n", 1);

    // read file
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sorted), "UTF-8"));
    int line = 0;
    while (true) {
      line++;
      String row = br.readLine();
      if (row == null) {
        break;
      }

      if (line == 1) {
        assertTrue(row.startsWith("\"ID\",\"catalogNumber\""));
      } else if (line == 2) {
        assertTrue(row.startsWith("\"18728553\",\"18728553\",\"Event\",\"18728553\",\"Muscardinus avellanarius\""));
      } else if (line == 3) {
        assertTrue(row.startsWith(
          "\"8728372\",\"18728372\",\"Event\",\"18728372\",\"Muscardinus avellanarius\",\"52.31635664254722\""));
      }
    }
  }

  @Test
  public void testSortingWithNonFirstIdColumn() throws IOException {
    File source = FileUtils.getClasspathFile("sorting/TDB_104.csv");
    File sorted = File.createTempFile("gbif-common-file-sort", "sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, ENCODING, 3, ";", null, "\n", 1);

    // read file
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sorted), "UTF-8"));
    int line = 0;
    while (true) {
      line++;
      String row = br.readLine();
      if (row == null) {
        break;
      }

      if (line == 1) {
        assertEquals(
          "taxonRank;scientificName;scientificNameAuthorship;taxonID;parentNameUsageID;vernacularName;taxonomicStatus",
          row);
      } else if (line == 2) {
        // row 2 and 3 have the same ids - only test if the id is correct (actual order of those 2 records can differ)
        Iterator<String> columns = Splitter.on(";").split(row).iterator();
        columns.next();
        columns.next();
        columns.next();
        assertEquals("urn:lsid:luomus.fi:taxonconcept:0071b855-3d23-4fdc-b2e0-8464c22d752a:1", columns.next());

      } else if (line == 100) {
        assertEquals(
          "species;Ctenochira angulata;(Thomson, 1883) ;urn:lsid:luomus.fi:taxonconcept:4adcf436-a0d2-4940-9155-220ffc6f5859:1;urn:lsid:luomus.fi:taxonconcept:817994ea-b58b-4deb-973f-9fa99c537f8a:1;;valid",
          row);
      }
    }
  }

  /**
   * Test that ensures the chunk file is deleted at the end of sortInJava method. Otherwise, unwanted chunk files
   * will be left over.
   */
  @Test
  public void testSortInJava() throws IOException {
    File source = FileUtils.getClasspathFile("sorting/taxon.txt");
    File sorted = File.createTempFile("gbif-common-file-sort", "taxon_sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    Comparator<String> lineComparator = new LineComparator(0, "\t");
    futils.sortInJava(source, sorted, ENCODING, lineComparator, 1);

    // the chunk file should NOT exist
    File chunkFile = new File(source.getParent(), "taxon_0txt");
    assertFalse(chunkFile.exists());

    // the sorted file should exist
    System.out.println(sorted.getAbsolutePath());
    assertTrue(sorted.exists());

    // read file
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sorted), "UTF-8"));
    int line = 0;
    while (true) {
      line++;
      String row = br.readLine();
      if (row == null) {
        break;
      }
      // first line (smallest ID)
      if (line == 2) {
        assertTrue(row.startsWith("118701359"));
      }
      // last line (largest ID)
      else if (line == 8) {
        assertTrue(row.startsWith("120320038"));
      }
    }
  }
}
