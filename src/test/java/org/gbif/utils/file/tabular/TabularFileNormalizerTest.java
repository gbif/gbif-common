package org.gbif.utils.file.tabular;

import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit test related to {@link TabularFileNormalizer}
 */
public class TabularFileNormalizerTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testTabularFileNormalizer() throws IOException {
    //this file includes a null character (\0) that is expected to be removed
    File csvFile = FileUtils.getClasspathFile("tabular/test_normalize.csv");
    File normalizedFile = folder.newFile();

    int numberOfLine = TabularFileNormalizer.normalizeFile(csvFile.toPath(), normalizedFile.toPath(), '\"', ',', "\n",
            StandardCharsets.UTF_8);

    List<String> rows = org.apache.commons.io.FileUtils.readLines(normalizedFile, StandardCharsets.UTF_8);
    assertEquals("1,\"a,\",b", rows.get(0));
    assertEquals("2,c,d", rows.get(1));
    assertEquals("3,é,f", rows.get(2));
    assertEquals(3, numberOfLine);
  }
}
