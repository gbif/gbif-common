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

package org.gbif.utils.file;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author markus
 */
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

  private static void assureEqualContent(List<File> result, String metaContent, String dataContent) throws IOException {
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
    testArchiveFile = classpathFile("compression/cate.zip");
    result = CompressionUtil.decompressFile(tmpDir, testArchiveFile);
    assertEquals(3, result.size());
  }
}
