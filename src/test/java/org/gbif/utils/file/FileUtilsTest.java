/*
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

import org.gbif.utils.text.LineComparator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author markus
 */
public class FileUtilsTest {

  private final String ENCODING = StandardCharsets.UTF_8.displayName();

  public static void assertUnixSortOrder(File sorted) throws IOException {
    // read file
    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(sorted), StandardCharsets.UTF_8));
    LineIterator liter = new LineIterator(br);
    assertUnixSortOrder(liter);
  }

  public static void assertUnixSortOrder(Iterator<String> it) throws IOException {
    LinkedList<String> sorted =
        FileUtils.streamToList(FileUtils.classpathStream("sorting/LF_sorted.txt"));
    while (it.hasNext()) {
      String x = it.next();
      System.out.println(x);
      assertEquals(sorted.poll(), x);
    }
  }

  @Test
  public void humanReadableByteCountTest() {
    assertEquals("11 B", FileUtils.humanReadableByteCount(11, true));
    assertEquals("1.0 kB", FileUtils.humanReadableByteCount(1_000, true));
    assertEquals("1.0 MB", FileUtils.humanReadableByteCount(1_000_000, true));
    assertEquals("1.0 GB", FileUtils.humanReadableByteCount(1_000_000_000, true));
    assertEquals("1.0 TB", FileUtils.humanReadableByteCount(1_000_000_000_000L, true));

    assertEquals("11 B", FileUtils.humanReadableByteCount(11, false));
    assertEquals("1.0 KiB", FileUtils.humanReadableByteCount(1024, false));
    assertEquals("1.0 MiB", FileUtils.humanReadableByteCount(1024 * 1024, false));
    assertEquals("1.0 GiB", FileUtils.humanReadableByteCount(1024 * 1024 * 1024, false));
    assertEquals("1.0 TiB", FileUtils.humanReadableByteCount(1024 * 1024 * 1024 * 1024L, false));
  }

  /**
   * tests deleting directory recursively.
   */
  @Test
  public void testDeleteRecursive() throws IOException {
    File topDirectory = Files.createTempDirectory("top").toFile();
    File middleDirectory = new File(topDirectory, "middle");
    middleDirectory.mkdir();
    File bottomDirectory = new File(middleDirectory, "bottom");
    bottomDirectory.mkdir();
    File bottomFile = new File(bottomDirectory, "bottom");
    FileUtils.touch(bottomFile);

    assertTrue(topDirectory.getParentFile().exists());
    assertTrue(topDirectory.exists());
    assertTrue(middleDirectory.getParentFile().exists());
    assertTrue(middleDirectory.exists());
    assertTrue(bottomDirectory.getParentFile().exists());
    assertTrue(bottomDirectory.exists());
    assertTrue(bottomFile.exists());

    FileUtils.deleteDirectoryRecursively(topDirectory);

    assertTrue(topDirectory.getParentFile().exists());
    assertFalse(topDirectory.exists());
    assertFalse(middleDirectory.getParentFile().exists());
    assertFalse(middleDirectory.exists());
    assertFalse(bottomDirectory.getParentFile().exists());
    assertFalse(bottomDirectory.exists());
    assertFalse(bottomFile.exists());
  }

  @Test
  @Disabled("Run manually to check the performance of merging sorted files.")
  public void testMergeSortedFilesSpeed() throws IOException {
    // This is the chunks produced from the first part of Java-sorting iNaturalist's media file.
    String prefix =
        "/4TB/Matt/unpacked_50c9509d-22c7-4a22-a47d-8c48425ef4a7/media_%dcsv-normalized";
    List<File> sortFiles = new ArrayList<>();
    for (int i = 0; i <= 825; i++) {
      sortFiles.add(new File(String.format(prefix, i)));
    }

    LineComparator lineComparator = new LineComparator(0, ",", '"');

    OutputStreamWriter performanceWriter =
        new OutputStreamWriter(
            new OutputStream() {
              int count = 0;
              StopWatch sw = StopWatch.createStarted();

              @Override
              public void write(int b) throws IOException {
                if (b == '\n') {
                  count++;

                  if (count % 100_000 == 0 && sw.getTime(TimeUnit.SECONDS) > 0) {
                    System.out.println(
                        "Done "
                            + count
                            + " at "
                            + (count / sw.getTime(TimeUnit.SECONDS))
                            + " lines per second");
                  }

                  if (count == 10_000_000) {
                    System.out.println(
                        "Done "
                            + count
                            + " at "
                            + (count / sw.getTime(TimeUnit.SECONDS))
                            + " lines per second");
                    System.out.println("Took " + sw.getTime(TimeUnit.SECONDS));
                    throw new IOException("Did enough");
                  }
                }
              }
            });
    try {
      new FileUtils().mergeSortedFiles(sortFiles, performanceWriter, lineComparator);
    } catch (IOException e) {
    }

    StopWatch sw = StopWatch.createStarted();
    OutputStreamWriter fileWriter =
        new OutputStreamWriter(new FileOutputStream(prefix.replace("%d", "OUTPUT")));
    new FileUtils().mergeSortedFiles(sortFiles, fileWriter, lineComparator);
    System.out.println("Took " + sw.getTime(TimeUnit.SECONDS) + " seconds.");
  }

  @Test
  public void testMergeSortedFiles() throws IOException {
    List<File> sortedSplitFiles = new ArrayList<>();
    for (int i = 0; i <= 4; i++) {
      sortedSplitFiles.add(FileUtils.getClasspathFile("merging/split_" + i + ".txt"));
    }
    // Also add an empty file
    sortedSplitFiles.add(File.createTempFile("gbif-common-file-merge", "empty.txt"));

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(output);

    LineComparator lineComparator = new LineComparator(0, ",", '"');

    new FileUtils().mergeSortedFiles(sortedSplitFiles, writer, lineComparator);

    String[] sorted = output.toString().split("\n");
    for (int i = 1; i < sorted.length; i++) {
      assertTrue(sorted[i - 1].compareTo(sorted[i]) <= 0);
    }
    assertEquals(100, sorted.length);
  }

  @Test
  public void testMergeEmptyFiles() throws IOException {
    List<File> sortedSplitFiles = new ArrayList<>();
    sortedSplitFiles.add(File.createTempFile("gbif-common-file-merge", "empty.txt"));
    sortedSplitFiles.add(File.createTempFile("gbif-common-file-merge", "empty.txt"));

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(output);

    LineComparator lineComparator = new LineComparator(0, ",", '"');

    new FileUtils().mergeSortedFiles(sortedSplitFiles, writer, lineComparator);

    assertEquals("", output.toString());
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
    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(sorted), StandardCharsets.UTF_8));
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
   * Sorting strings containing characters which are surrogate pairs, meaning Unicode characters beyond U+FFFF, will
   * give different results between GNU Sort and a Java String comparator.
   *
   * "ﬂ LATIN SMALL LIGATURE FL" is U+FB02.
   * "ð LINEAR B IDEOGRAM B241 CHARIOT" is U+100CD.
   *
   * GNU sort will use this order, based on the value of the whole character.
   *
   * Java represents ð as a surrogate pair \ud800\udccd in UTF-16, and sorts based on parts of pairs. Therefore, it
   * gives the wrong order.
   */
  @Disabled("Expected to fail")
  @Test
  public void testSortingUnicodeFile() throws IOException {
    FileUtils futils = new FileUtils();
    final int IDCOLUMN = 0;

    File source =
        FileUtils.getClasspathFile("sorting/unicode-supplementary-multilingual-plane.txt");
    File gnuSorted = File.createTempFile("gbif-common-file-sort", "sorted-gnu.txt");
    File javaSorted = File.createTempFile("gbif-common-file-sort", "sorted-java.txt");
    gnuSorted.deleteOnExit();
    javaSorted.deleteOnExit();

    futils.sort(source, gnuSorted, ENCODING, IDCOLUMN, "\t", null, "\n", 0);
    // The columnDelimiter of ' prevents GNU Sort from being used.
    futils.sort(source, javaSorted, ENCODING, IDCOLUMN, "'", null, "\n", 0);

    // read file
    BufferedReader gnuBr =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(gnuSorted), StandardCharsets.UTF_8));
    BufferedReader javaBr =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(javaSorted), StandardCharsets.UTF_8));

    int line = 0;
    String gnuRow, javaRow;
    while ((gnuRow = gnuBr.readLine()) != null) {
      javaRow = javaBr.readLine();

      line++;

      System.out.println(gnuRow + "\t\t\t\t" + javaRow);

      assertEquals("Line " + line, gnuRow, javaRow);
    }
  }

  /**
   * tests sorting mac line endings \r which don't work with unix sort
   */
  @Test
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
    sorted.deleteOnExit();
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

  /**
   * Tests sorting by a column with uneven length strings as the sort column.
   *
   * The order musn't be different depending whether the column is last or not.
   *
   * The "-k×,×" argument to sort is essential here, otherwise the delimiter from the following column is part of the sort order.
   */
  @Test
  public void testSortingUnevenLengths() throws IOException {
    FileUtils futils = new FileUtils();

    File source = FileUtils.getClasspathFile("sorting/uneven_lengths_col1.txt");
    File sorted = File.createTempFile("sort-test", "uneven_lengths_col1.txt");
    sorted.deleteOnExit();
    futils.sort(source, sorted, ENCODING, 0, ";", null, "\n", 0);

    List<String> sortedStrings = FileUtils.streamToList(new FileInputStream(sorted), ENCODING);
    assertEquals("980-sp10;x", sortedStrings.get(0));
    assertEquals("980-sp100;x", sortedStrings.get(1));
    assertEquals("980-sp101;x", sortedStrings.get(2));

    File source2 = FileUtils.getClasspathFile("sorting/uneven_lengths_col2.txt");
    File sorted2 = File.createTempFile("sort-test", "uneven_lengths_col2.txt");
    sorted.deleteOnExit();
    futils.sort(source2, sorted2, ENCODING, 1, ";", null, "\n", 0);

    List<String> sortedStrings2 = FileUtils.streamToList(new FileInputStream(sorted2), ENCODING);
    assertEquals("x;980-sp10", sortedStrings2.get(0));
    assertEquals("x;980-sp100", sortedStrings2.get(1));
    assertEquals("x;980-sp101", sortedStrings2.get(2));
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
    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(sorted), StandardCharsets.UTF_8));
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
        assertTrue(
            row.startsWith(
                "\"18728553\",\"18728553\",\"Event\",\"18728553\",\"Muscardinus avellanarius\""));
      } else if (line == 3) {
        assertTrue(
            row.startsWith(
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
    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(sorted), StandardCharsets.UTF_8));
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
        // row 2 and 3 have the same ids - only test if the id is correct (actual order of those 2
        // records can differ)
        Iterator<String> columns = Arrays.stream(row.split(";", -1)).iterator();

        columns.next();
        columns.next();
        columns.next();
        assertEquals(
            "urn:lsid:luomus.fi:taxonconcept:0071b855-3d23-4fdc-b2e0-8464c22d752a:1",
            columns.next());

      } else if (line == 100) {
        assertEquals(
            "species;Ctenochira angulata;(Thomson, 1883) ;urn:lsid:luomus.fi:taxonconcept:4adcf436-a0d2-4940-9155-220ffc6f5859:1;urn:lsid:luomus.fi:taxonconcept:817994ea-b58b-4deb-973f-9fa99c537f8a:1;;valid",
            row);
      }
    }
  }

  /**
   * If only columns containing delimiters are quoted in CSV, we can't use GNU sort.
   *   X,"Look, now!",1
   *   X,Why should I,2
   */
  @Test
  public void testSortingWithQuotedDelimiters() throws IOException {
    File source = FileUtils.getClasspathFile("sorting/csv_quoted_delimiters.csv");
    File sorted = File.createTempFile("gbif-common-file-sort", "sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, ENCODING, 0, ",", '"', "\n", 1);

    // read file
    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(sorted), StandardCharsets.UTF_8));
    int line = 30950;
    while (true) {
      String row = br.readLine();
      if (row == null) {
        break;
      }

      if (line == 30950) {
        assertEquals("catalogNumber", row.substring(0, 13));
      } else {
        // Catalog number ends in 30951 to 30961.
        assertEquals("ZMA.COL.P." + line, row.replace("\"", "").replace(",", ".").substring(0, 15));
      }
      line++;
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
    futils.sortInJava(source, sorted, ENCODING, lineComparator, 3);

    // the chunk file should NOT exist
    File chunkFile = new File(source.getParent(), "taxon_0txt");
    assertFalse(chunkFile.exists());

    // the sorted file should exist
    System.out.println(sorted.getAbsolutePath());
    assertTrue(sorted.exists());

    // read file
    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(sorted), StandardCharsets.UTF_8));
    int line = 0;
    while (true) {
      line++;
      String row = br.readLine();
      if (row == null) {
        break;
      }
      // first line (smallest ID)
      if (line == 4) {
        assertTrue(row.startsWith("118701359"));
      }
      // last line (largest ID)
      else if (line == 10) {
        assertTrue(row.startsWith("120320038"));
      }
    }
  }

  /**
   * Test using GNU sort (if available on this platform).
   */
  @Test
  public void testSort() throws IOException {
    File source = FileUtils.getClasspathFile("sorting/taxon.txt");
    File sorted = File.createTempFile("gbif-common-file-sort", "taxon_sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(source, sorted, ENCODING, 0, "\t", null, "\n", 3);

    // the sorted file should exist
    System.out.println(sorted.getAbsolutePath());
    assertTrue(sorted.exists());

    // read file
    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(sorted), StandardCharsets.UTF_8));
    int line = 0;
    while (true) {
      line++;
      String row = br.readLine();
      if (row == null) {
        break;
      }
      // first line (smallest ID)
      if (line == 4) {
        assertTrue(row.startsWith("118701359"));
      }
      // last line (largest ID)
      else if (line == 10) {
        assertTrue(row.startsWith("120320038"));
      }
    }
  }

  /**
   * Test sorting multiple fils into a single file. First column, so GNU sort.
   */
  @Test
  public void testMultiFileSort() throws IOException {
    final int IDCOLUMN = 0;
    File source1 = FileUtils.getClasspathFile("sorting/multi/VernacularNames-adai.csv");
    File source2 = FileUtils.getClasspathFile("sorting/multi/VernacularNames-choctaw.csv");
    File source3 = FileUtils.getClasspathFile("sorting/multi/VernacularNames-nahya.csv");
    List<File> sources = Arrays.asList(source1, source2, source3);
    File sorted = File.createTempFile("gbif-common-file-sort", "sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(sources, sorted, ENCODING, IDCOLUMN, ",", '"', "\n", 1);

    // read file
    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(sorted), StandardCharsets.UTF_8));
    int line = 0;
    while (true) {
      line++;
      String row = br.readLine();
      if (row == null) {
        break;
      }

      if (line == 1) {
        assertTrue(row.startsWith("id,vernacularName,language"));
      } else if (line == 2) {
        assertTrue(row.startsWith("122860,xoyamet,und,\"\",\"\",,nahya,,2013-05-16T08:27:53Z"));
      } else if (line == 3) {
        assertTrue(row.startsWith("49662,heohè,und,\"\",\"\",,Adai,Ben,2021-01-26T16:07:11Z"));
      } else if (line == 4) {
        assertTrue(row.startsWith("50897,Umbi,und,\"\",\"\",,Choctaw,Ben,2021-01-13T02:14:34Z"));
      } else {
        fail("Too many lines.");
      }
    }
  }

  /**
   * Test sorting multiple files into a single file. Second column, so Java sort.
   */
  @Test
  public void testMultiFileSort2ndColumn() throws IOException {
    final int IDCOLUMN = 1;
    File source1 = FileUtils.getClasspathFile("sorting/multi/VernacularNames-adai.csv");
    File source2 = FileUtils.getClasspathFile("sorting/multi/VernacularNames-choctaw.csv");
    File source3 = FileUtils.getClasspathFile("sorting/multi/VernacularNames-nahya.csv");
    List<File> sources = Arrays.asList(source1, source2, source3);
    File sorted = File.createTempFile("gbif-common-file-sort", "sorted.txt");
    sorted.deleteOnExit();
    FileUtils futils = new FileUtils();
    futils.sort(sources, sorted, ENCODING, IDCOLUMN, ",", '"', "\n", 1);

    // read file
    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(sorted), StandardCharsets.UTF_8));
    int line = 0;
    while (true) {
      line++;
      String row = br.readLine();
      if (row == null) {
        break;
      }

      if (line == 1) {
        assertTrue(row.startsWith("id,vernacularName,language"));
      } else if (line == 2) {
        assertTrue(row.startsWith("50897,Umbi,und,\"\",\"\",,Choctaw,Ben,2021-01-13T02:14:34Z"));
      } else if (line == 3) {
        assertTrue(row.startsWith("49662,heohè,und,\"\",\"\",,Adai,Ben,2021-01-26T16:07:11Z"));
      } else if (line == 4) {
        assertTrue(row.startsWith("122860,xoyamet,und,\"\",\"\",,nahya,,2013-05-16T08:27:53Z"));
      } else {
        fail("Too many lines.");
      }
    }
  }
}
