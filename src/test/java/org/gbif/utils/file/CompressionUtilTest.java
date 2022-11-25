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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.sun.management.UnixOperatingSystemMXBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CompressionUtilTest {

  public static File createTempDirectory() throws IOException {

    final File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

    if (!temp.delete()) {
      throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
    }

    if (!temp.mkdir()) {
      throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
    }
    temp.deleteOnExit();

    return temp;
  }

  private static void assureEqualContent(List<File> result, String metaContent, String dataContent)
      throws IOException {
    for (File f : result) {
      String x = FileUtils.readFileToString(f, "utf-8");
      if ("meta.xml".equals(f.getName())) {
        assertEquals(metaContent, x);
      } else if ("quote_in_quote.csv".equals(f.getName())) {
        assertEquals(dataContent, x);
      } else {
        fail("unexpected file");
      }
    }
  }

  public File classpathFile(String path) {
    File f = null;
    // relative path. Use classpath instead
    URL url = getClass().getClassLoader().getResource(path);
    if (url != null) {
      f = new File(url.getFile());
    }
    return f;
  }

  @Test
  public void testDecompress() throws IOException {
    // meta.xml
    File meta = classpathFile("compression/archive/meta.xml");
    String metaContent = FileUtils.readFileToString(meta, "utf-8");
    // quote_in_quote.csv
    File data = classpathFile("compression/archive/quote_in_quote.csv");
    String dataContent = FileUtils.readFileToString(data, "utf-8");

    File tmpDir = createTempDirectory();
    File testArchiveFile = classpathFile("compression/archive.zip");
    List<File> result = CompressionUtil.decompressFile(tmpDir, testArchiveFile);
    assertEquals(2, result.size());
    assureEqualContent(result, metaContent, dataContent);

    FileUtils.cleanDirectory(tmpDir);
    testArchiveFile = classpathFile("compression/archive.tgz");
    result = CompressionUtil.decompressFile(tmpDir, testArchiveFile);
    assertEquals(2, result.size());
    assureEqualContent(result, metaContent, dataContent);

    FileUtils.cleanDirectory(tmpDir);
    testArchiveFile = classpathFile("compression/archive-zip.dat");
    result = CompressionUtil.decompressFile(tmpDir, testArchiveFile);
    assertEquals(2, result.size());
    assureEqualContent(result, metaContent, dataContent);

    FileUtils.cleanDirectory(tmpDir);
    testArchiveFile = classpathFile("compression/archive-tgz.dat");
    result = CompressionUtil.decompressFile(tmpDir, testArchiveFile);
    assertEquals(2, result.size());
    assureEqualContent(result, metaContent, dataContent);

    FileUtils.cleanDirectory(tmpDir);
    testArchiveFile = classpathFile("compression/archive.tar");
    result = CompressionUtil.decompressFile(tmpDir, testArchiveFile);
    assertEquals(2, result.size());
    assureEqualContent(result, metaContent, dataContent);

    FileUtils.cleanDirectory(tmpDir);
    testArchiveFile = classpathFile("compression/cate.zip");
    result = CompressionUtil.decompressFile(tmpDir, testArchiveFile);
    assertEquals(3, result.size());
  }

  @Test
  public void testUnableToDecompress() throws IOException {
    File tmpDir = createTempDirectory();
    File testArchiveFile = classpathFile("compression/test.txt.gz");
    List<File> result = CompressionUtil.decompressFile(tmpDir, testArchiveFile);
    assertEquals(0, result.size());

    testArchiveFile = classpathFile("compression/empty-file");
    result = CompressionUtil.decompressFile(tmpDir, testArchiveFile);
    assertEquals(0, result.size());
  }

  @Test
  public void testGunzipWithTar() throws IOException {
    File tmpDir = createTempDirectory();
    FileUtils.cleanDirectory(tmpDir);
    File testArchiveFile = classpathFile("compression/archive-tgz.dat");
    List<File> result = CompressionUtil.ungzipFile(tmpDir, testArchiveFile, true);
    assertEquals(2, result.size());
  }

  @Test
  public void testGunzipNoTar() throws IOException {
    File tmpDir = createTempDirectory();
    FileUtils.cleanDirectory(tmpDir);
    File testArchiveFile = classpathFile("compression/test.txt.gz");
    List<File> result = CompressionUtil.ungzipFile(tmpDir, testArchiveFile, false);
    assertEquals(1, result.size());
    assertEquals("test.txt", result.get(0).getName());
  }

  /**
   * Test unzipping a folder, while NOT preserving subdirectories.
   */
  @Test
  public void testUnzipFolderDoNotKeepSubdirectoriesOrHiddenFiles() throws IOException {
    File tmpDir = createTempDirectory();
    File testZippedFolder = classpathFile("compression/withSubdirsAndHiddenFiles.zip");
    List<File> files = CompressionUtil.unzipFile(tmpDir, testZippedFolder, false);
    assertEquals(9, files.size()); // 9 files, 0 directories
    assertTrue(new File(tmpDir, "dwca.zip").exists());
    assertTrue(new File(tmpDir, "eml.xml").exists());
    assertTrue(new File(tmpDir, "publication.log").exists());
    assertTrue(new File(tmpDir, "resource.xml").exists());
    assertTrue(new File(tmpDir, "test4.rtf").exists());
    assertTrue(new File(tmpDir, "taxon.log").exists());
    assertTrue(new File(tmpDir, "taxon.txt").exists());
    assertTrue(new File(tmpDir, "taxonshort.log").exists());
    assertTrue(new File(tmpDir, "taxonshort.txt").exists());
    // assert subdirectory is removed
    File sourceDir = new File(tmpDir, "sources");
    assertFalse(sourceDir.exists());
    // assert wrapping root directory is removed
    File rootDir = new File(tmpDir, "withSubdirsAndHiddenFiles");
    assertFalse(rootDir.exists());
    // assert hidden files are removed
    assertFalse(new File(tmpDir, ".hidden1").exists());
    assertFalse(new File(tmpDir, "/sources/.hidden2").exists());
    // assert .DS_Store removed
    assertFalse(new File(tmpDir, ".DS_Store").exists());
    // assert __MACOSX removed
    assertFalse(new File(tmpDir, "__MACOSX").exists());
  }

  /**
   * Test unzipping a folder, while preserving subdirectories.
   */
  @Test
  public void testUnzipFolderKeepSubdirectoriesButNoHiddenFile() throws IOException {
    File tmpDir = createTempDirectory();
    File testZippedFolder = classpathFile("compression/withSubdirsAndHiddenFiles.zip");
    List<File> files = CompressionUtil.unzipFile(tmpDir, testZippedFolder, true);
    assertEquals(6, files.size()); // 5 files, 1 directory having 4 files inside
    assertTrue(new File(tmpDir, "dwca.zip").exists());
    assertTrue(new File(tmpDir, "eml.xml").exists());
    assertTrue(new File(tmpDir, "publication.log").exists());
    assertTrue(new File(tmpDir, "resource.xml").exists());
    assertTrue(new File(tmpDir, "test4.rtf").exists());
    // assert subdirectory was preserved
    File sourceDir = new File(tmpDir, "sources");
    assertTrue(sourceDir.isDirectory());
    assertTrue(sourceDir.exists());
    assertTrue(new File(sourceDir, "taxon.log").exists());
    assertTrue(new File(sourceDir, "taxon.txt").exists());
    assertTrue(new File(sourceDir, "taxonshort.log").exists());
    assertTrue(new File(sourceDir, "taxonshort.txt").exists());
    // assert wrapping root directory is removed
    File rootDir = new File(tmpDir, "withSubdirsAndHiddenFiles");
    assertFalse(rootDir.exists());
    // assert hidden files are removed
    assertFalse(new File(tmpDir, ".hidden1").exists());
    assertFalse(new File(tmpDir, "/sources/.hidden2").exists());
    // assert .DS_Store removed
    assertFalse(new File(tmpDir, ".DS_Store").exists());
    assertFalse(new File(sourceDir, ".DS_Store").exists());
    // assert __MACOSX removed
    assertFalse(new File(tmpDir, "__MACOSX").exists());
  }

  /**
   * Test unzipping a folder, while preserving subdirectories, but making sure the .svn directories and their subfiles
   * and subdirectories are not extracted.
   */
  @Test
  public void testUnzipFolderKeepSubdirectoriesButNoHiddenDirectories() throws IOException {
    File tmpDir = createTempDirectory();
    File testZippedFolder = classpathFile("compression/with_dot_svn.zip");
    List<File> files = CompressionUtil.unzipFile(tmpDir, testZippedFolder, true);
    assertEquals(7, files.size()); // 5 files, 2 directories
    // assert wrapping root directory is removed
    File rootDir = new File(tmpDir, "res1");
    assertFalse(rootDir.exists());
    assertTrue(new File(tmpDir, "dwca.zip").exists());
    assertTrue(new File(tmpDir, "eml.xml").exists());
    assertTrue(new File(tmpDir, "publication.log").exists());
    assertTrue(new File(tmpDir, "resource.xml").exists());
    assertTrue(new File(tmpDir, "test4.rtf").exists());

    // assert subdirectory sources was preserved
    File sourceDir = new File(tmpDir, "sources");
    assertTrue(sourceDir.isDirectory());
    assertTrue(sourceDir.exists());
    assertEquals(1, sourceDir.listFiles().length);
    assertTrue(new File(sourceDir, "occurrence.txt").exists());

    // assert subdirectory dwca was preserved
    File dwcaDir = new File(tmpDir, "dwca");
    assertTrue(dwcaDir.isDirectory());
    assertTrue(dwcaDir.exists());
    assertEquals(4, dwcaDir.listFiles().length);
    assertTrue(new File(dwcaDir, "occurrence.txt").exists());
    assertTrue(new File(dwcaDir, "image.txt").exists());
    assertTrue(new File(dwcaDir, "meta.xml").exists());
    assertTrue(new File(dwcaDir, "eml.xml").exists());

    // assert hidden files and directories are removed
    assertFalse(new File(tmpDir, ".svn").exists());
    assertFalse(new File(tmpDir, "/sources/.svn").exists());
    // assert .DS_Store removed
    assertFalse(new File(tmpDir, ".DS_Store").exists());
    // assert __MACOSX removed
    assertFalse(new File(tmpDir, "__MACOSX").exists());
  }

  @Test
  public void testDecompressZippedFolderWithNoSubdirectories() throws IOException {
    File tmpDir = createTempDirectory();
    File testZippedFolder = classpathFile("compression/archive.zip");
    List<File> files = CompressionUtil.unzipFile(tmpDir, testZippedFolder);
    assertEquals(2, files.size());
    File meta = new File(tmpDir, "meta.xml");
    assertTrue(meta.exists());
    File csv = new File(tmpDir, "quote_in_quote.csv");
    assertTrue(csv.exists());
  }

  @Test
  public void testZipFolder() throws IOException {
    File zipWithDirs = File.createTempFile("aha", ".zip");
    System.out.println(zipWithDirs.getAbsolutePath());

    File zipWithoutDirs = File.createTempFile("aha", ".zip");
    System.out.println(zipWithoutDirs.getAbsolutePath());
    // tmp.deleteOnExit();
    File testFolder = classpathFile("charsets");
    // remember how many files we have in the root folder, exlcuding files in subdirectories
    final int rootFileNum = testFolder.listFiles().length;

    CompressionUtil.zipDir(testFolder, zipWithDirs, true);
    CompressionUtil.zipDir(testFolder, zipWithoutDirs, false);

    assertNotEquals(zipWithDirs.length(), zipWithoutDirs.length());

    // now decompress the subdir zip and make sure we get the same amount of root files
    File tmpDir = org.gbif.utils.file.FileUtils.createTempDir();
    tmpDir.deleteOnExit();
    CompressionUtil.unzipFile(tmpDir, zipWithDirs, true);
    int decompressedRootFileNum = tmpDir.listFiles().length;
    assertEquals(rootFileNum, decompressedRootFileNum);
  }

  /**
   * Check we can unpack ZIP64 archives.
   *
   * infozip64.zip was created with <code>echo 'hello | zip infozip.zip -</code>, following comments on
   * https://bugs.openjdk.java.net/browse/JDK-8186464
   */
  @Test
  public void testDecompressZippedFolderWithNoSubdirectoriesx() throws IOException {
    File tmpDir = createTempDirectory();
    File testZippedFolder = classpathFile("compression/infozip64.zip");

    List<File> files = CompressionUtil.unzipFile(tmpDir, testZippedFolder);
    assertEquals(1, files.size());
    File dash = new File(tmpDir, "-");
    assertTrue(dash.exists());
  }

  /**
   * Check that files are closed after use.
   */
  @Test
  public void testFilesClosedCorrectly() throws Exception {
    File tmpDir = createTempDirectory();
    File testZippedFolder = classpathFile("compression/infozip64.zip");

    // Unzip first, to use the code path.  (Various things like /dev/random are opened.)
    CompressionUtil.unzipFile(tmpDir, testZippedFolder);

    OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    long openFiles = 0;
    if (os instanceof UnixOperatingSystemMXBean) {
      openFiles = ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount();
    }

    // From all the other tests.
    List<String> files =
        Arrays.asList(
            "compression/archive/meta.xml",
            "compression/archive/quote_in_quote.csv",
            "compression/archive.tgz",
            "compression/archive-tgz.dat",
            "compression/archive.zip",
            "compression/archive-zip.dat",
            "compression/cate.zip",
            "compression/infozip64.zip",
            "compression/test.txt.gz",
            "compression/with_dot_svn.zip",
            "compression/withSubdirsAndHiddenFiles.zip");

    for (String file : files) {
      tmpDir = createTempDirectory();
      CompressionUtil.decompressFile(tmpDir, classpathFile(file));
    }

    if (os instanceof UnixOperatingSystemMXBean) {
      assertEquals(openFiles, ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount());
    } else {
      System.err.println("Cannot check files are closed except on Unix.");
    }

    // Try ls -l /proc/`pgrep -f java -n`/fd
    // Thread.sleep(30000);
  }
}
