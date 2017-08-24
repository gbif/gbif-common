package org.gbif.utils.file.tabular;

import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link TabularDataFileReader}.
 */
public class TabularDataFileReaderTest {

  @Test
  public void testCsvOptionalQuotes() throws IOException {
    File csv = FileUtils.getClasspathFile("csv/csv_optional_quotes_excel2008.csv");

    try (TabularDataFileReader<List<String>> reader = TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), ',', true)) {
      List<String> rec = reader.read();
      assertEquals(3, rec.size());
      assertEquals("1", rec.get(0));
      assertEquals("This has a, comma", rec.get(2));

      rec = reader.read();
      assertEquals("I say this is only a \"quote\"", rec.get(2));

      while(rec != null){
        rec = reader.read();
      }
    }
  }

  /**
   * Test a CSV with all cells quoted.
   * @throws IOException
   */
  @Test
  public void testCsvAlwaysQuotes() throws IOException {
    File csv = FileUtils.getClasspathFile("csv/csv_always_quoted.csv");

    try (TabularDataFileReader<List<String>> reader = TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), ',', true)) {
      List<String> rec = reader.read();
      //the value we retrieve should not include the quotes
      assertEquals("8728372", rec.get(0));

      //read all records
      while(rec != null){
        rec = reader.read();
      }
      assertEquals(2, reader.getLastRecordNumber());
      assertEquals(3, reader.getLastRecordLineNumber());
    }
  }

  /**
   * Test a CSV file that includes a newline character (\n) inside a properly quoted cell.
   * @throws IOException
   */
  @Test
  public void testCsvMultiline() throws IOException {
    File csv = FileUtils.getClasspathFile("csv/csv_quote_endline.csv");

    try (TabularDataFileReader<List<String>> reader = TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), ',', true)) {

      //before we start reading, those methods are expected to return 0
      assertEquals(0, reader.getLastRecordNumber());
      assertEquals(0, reader.getLastRecordLineNumber());

      int numberOfRows = 0;
      List<String> rec = reader.read();
      while(rec != null){
        numberOfRows++;
        rec = reader.read();
      }

      assertEquals(3, numberOfRows);
      assertEquals(3, reader.getLastRecordNumber());
      assertEquals(7, reader.getLastRecordLineNumber());
    }
  }

  /**
   * Testing classic non quoted tab files with escaped \t tabs.
   */
  @Test
  public void testTab() throws IOException {
    File csv = FileUtils.getClasspathFile("csv/escapedTab.tab");
    try (TabularDataFileReader<List<String>> reader = TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), '\t', true)) {

      int numberOfRecords = 0;
      List<String> rec = reader.read();
      while(rec != null){
        numberOfRecords++;
        rec = reader.read();
      }

      assertEquals(8, numberOfRecords);
      assertEquals(8, reader.getLastRecordNumber());
      assertEquals(9, reader.getLastRecordLineNumber());
    }
  }

  @Test
  public void testCsvWithComment() throws IOException {
    File csv = FileUtils.getClasspathFile("csv/tab_separated_generic_comments.txt");
    try (TabularDataFileReader<List<String>> reader = TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), '\t', "\n", null, true, 2)) {

      int numberOfRecords = 0;
      List<String> rec = reader.read();
      while(rec != null){
        numberOfRecords++;
        rec = reader.read();
      }

      assertEquals(4, numberOfRecords);
      assertEquals(4, reader.getLastRecordNumber());
      assertEquals(7, reader.getLastRecordLineNumber());
    }
  }

  @Test
  public void testIgnoreEmptyLines() throws IOException {
    File csv = FileUtils.getClasspathFile("csv/empty_line.tab");
    try (TabularDataFileReader<List<String>> reader = TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), '\t', "\n", null, true)) {
      String[] ids = {"1", "5", "10", "12", "14", "20", "21", "", "30"};
      int row = 0;
      List<String> line = reader.read();
      while(line != null){
        assertEquals(ids[row], line.get(0));
        row++;
        line = reader.read();
      }
      assertEquals(9, reader.getLastRecordNumber());
      assertEquals(12, reader.getLastRecordLineNumber());
    }
  }

}
