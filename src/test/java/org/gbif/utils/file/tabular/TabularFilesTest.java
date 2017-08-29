package org.gbif.utils.file.tabular;

import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests related to {@link TabularFiles}.
 * This test class covers cases that require more than a simple TabularDataFileReader creation (already covered by
 * {@link TabularDataFileReaderTest}).
 */
public class TabularFilesTest {

  @Test
  public void testNewTabularFileReaderFromPath() throws IOException {
    File testFile = FileUtils.getClasspathFile("csv/semicolon_separator.csv");
    try (TabularDataFileReader<List<String>> reader = TabularFiles.newTabularFileReader(testFile.toPath())) {
      List<String> line1 = reader.read();
      assertEquals(7, line1.size());
      assertEquals("valid", line1.get(6));
    }
  }

}
