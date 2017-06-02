package org.gbif.utils.file.tabular;

import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link TabularDataFileReader}.
 */
public class TabularDataFileReaderTest {

  @Test
  public void testCsvAllwaysQuotes() throws IOException {
    File csv = FileUtils.getClasspathFile("csv/csv_optional_quotes_excel2008.csv");

    try (TabularDataFileReader<List<String>> reader = TabularFiles.newTabularFileReader(new FileInputStream(csv), ',', true)) {

      List<String> rec = reader.read();
      assertEquals(3, rec.size());
      assertEquals("1", rec.get(0));
      assertEquals("This has a, comma", rec.get(2));

      rec = reader.read();
      assertEquals("I say this is only a \"quote\"", rec.get(2));
    }
  }

}
