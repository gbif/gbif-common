package org.gbif.utils.file;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.LineIterator;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author markus
 * 
 */
public class FileUtilsTest {
  public static void assertUnixSortOrder(File sorted) throws IOException {
    // read file
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sorted), "UTF-8"));
    LineIterator liter = new LineIterator(br);
    assertUnixSortOrder(liter);
  }

  public static void assertUnixSortOrder(Iterator<String> it) throws IOException {
    LinkedList<String> sorted = FileUtils.streamToList(FileUtils.classpathStream("sorting/LF_sorted.txt"));
    while (it.hasNext()) {
      assertEquals(sorted.poll(), it.next());
    }
  }

  @Test
  public void testSortingHeaderlessFile() throws IOException {
    final int IDCOLUMN = 0;
    File source = FileUtils.getClasspathFile("sorting/irmng.tail");
    File sorted = File.createTempFile("gbif-common-file-sort", "sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, IDCOLUMN, "\t", null, "\n", 0);

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
   * 
   * @throws IOException
   */
  @Test
  public void testSortingMac() throws IOException {
    final int IDCOLUMN = 0;
    File source = FileUtils.getClasspathFile("sorting/LF_mac.txt");
    File sorted = File.createTempFile("sort-test", "mac.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, IDCOLUMN, "\t", '"', "\r", 1);

    assertUnixSortOrder(sorted);
  }

  /**
   * tests sorting unix line endings \n which work with unix sort
   * 
   * @throws IOException
   */
  @Test
  public void testSortingUnix() throws IOException {
    final int IDCOLUMN = 0;
    File source = FileUtils.getClasspathFile("sorting/LF_unix.txt");
    File sorted = File.createTempFile("sort-test", "unix.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, IDCOLUMN, "\t", '"', "\n", 1);

    assertUnixSortOrder(sorted);

  }

  /**
   * tests sorting windows line endings \r\n which work with unix sort
   * 
   * @throws IOException
   */
  @Test
  public void testSortingWindows() throws IOException {
    final int IDCOLUMN = 0;
    File source = FileUtils.getClasspathFile("sorting/LF_win.txt");
    File sorted = File.createTempFile("sort-test", "windows.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, IDCOLUMN, "\t", '"', "\r\n", 1);

    assertUnixSortOrder(sorted);
  }

  @Test
  public void testSortingWithHeaders() throws IOException {
    final int IDCOLUMN = 0;
    File source = FileUtils.getClasspathFile("sorting/csv_always_quoted.csv");
    File sorted = File.createTempFile("gbif-common-file-sort", "sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, IDCOLUMN, ",", '"', "\n", 1);

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
        assertTrue(row.startsWith("\"8728372\",\"18728372\",\"Event\",\"18728372\",\"Muscardinus avellanarius\",\"52.31635664254722\""));
      }
    }
  }

  @Test
  public void testSortingWithNonFirstIdColumn() throws IOException {
    final int IDCOLUMN = 3;
    File source = FileUtils.getClasspathFile("sorting/TDB_104.csv");
    File sorted = File.createTempFile("gbif-common-file-sort", "sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, IDCOLUMN, ";", null, "\n", 1);

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
        assertEquals("taxonRank;scientificName;scientificNameAuthorship;taxonID;parentNameUsageID;vernacularName;taxonomicStatus", row);
      } else if (line == 2) {
        assertEquals(
            "tribe;Exenterini;;urn:lsid:luomus.fi:taxonconcept:0071b855-3d23-4fdc-b2e0-8464c22d752a:1;urn:lsid:luomus.fi:taxonconcept:028d487f-c989-4fd0-bdae-447c470b94ce:1;;valid",
            row);
      } else if (line == 100) {
        assertEquals(
            "species;Ctenochira angulata;(Thomson, 1883) ;urn:lsid:luomus.fi:taxonconcept:4adcf436-a0d2-4940-9155-220ffc6f5859:1;urn:lsid:luomus.fi:taxonconcept:817994ea-b58b-4deb-973f-9fa99c537f8a:1;;valid",
            row);
      }
    }
  }

}
