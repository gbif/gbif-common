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
package org.gbif.utils.file.tabular;

import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link TabularDataFileReader}.
 */
public class TabularDataFileReaderTest {

  @Test
  public void testCsvOptionalQuotes() throws IOException, ParseException {
    File csv = FileUtils.getClasspathFile("csv/csv_optional_quotes_excel2008.csv");

    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), ',', true)) {
      List<String> rec = reader.read();
      assertEquals(3, rec.size());
      assertEquals("1", rec.get(0));
      assertEquals("This has a, comma", rec.get(2));

      rec = reader.read();
      assertEquals("I say this is only a \"quote\"", rec.get(2));

      while (rec != null) {
        rec = reader.read();
      }
    }
  }

  /**
   * Ensure if we can escape a quote character with a backslash
   */
  @Test
  public void testEscapedQuotes() throws IOException, ParseException {
    File tsv = FileUtils.getClasspathFile("csv/csv_escaped_quotes.csv");
    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(tsv.toPath(), StandardCharsets.UTF_8),
            ',',
            "\n",
            '"',
            false,
            1)) {

      List<String> rec = reader.read();
      assertEquals(12, rec.size());
      assertEquals(
          "Danish Mycological Society (2017-09-04). Fungal records database (http://svampe.databasen.org), contributed by Frøslev, T., Heilmann-Clausen, J., Jeppesen, T.S., Lange, C., Læssøe, T., Petersen, J.H., Søchting, U., \"Vesterholt\", J.",
          rec.get(5));
      assertEquals("{\"Substrate\":\"wood\"}", rec.get(10));
    }
  }

  @Test
  public void testWrongEscapedQuotes1() throws IOException {
    File tsv = FileUtils.getClasspathFile("csv/csv_wrong_escaped_quotes_1.csv");
    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(tsv.toPath(), StandardCharsets.UTF_8),
            ',',
            "\n",
            '"',
            false,
            1)) {
      assertThrows(ParseException.class, reader::read);
    }
  }

  @Test
  public void testWrongEscapedQuotes2() throws IOException {
    File tsv = FileUtils.getClasspathFile("csv/csv_wrong_escaped_quotes_2.csv");
    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(tsv.toPath(), StandardCharsets.UTF_8),
            ',',
            "\n",
            '"',
            false,
            1)) {
      assertThrows(ParseException.class, reader::read);
    }
  }

  /**
   * Test a CSV with all cells quoted.
   */
  @Test
  public void testCsvAlwaysQuotes() throws IOException, ParseException {
    File csv = FileUtils.getClasspathFile("csv/csv_always_quoted.csv");

    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), ',', true)) {
      List<String> rec = reader.read();
      // the value we retrieve should not include the quotes
      assertEquals("8728372", rec.get(0));

      // read all records
      while (rec != null) {
        rec = reader.read();
      }
      assertEquals(2, reader.getLastRecordNumber());
      assertEquals(3, reader.getLastRecordLineNumber());
    }
  }

  /**
   * Test a CSV file that includes a newline character (\n) inside a properly quoted cell.
   */
  @Test
  public void testCsvMultiline() throws IOException, ParseException {
    File csv = FileUtils.getClasspathFile("csv/csv_quote_endline.csv");

    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), ',', true)) {

      // before we start reading, those methods are expected to return 0
      assertEquals(0, reader.getLastRecordNumber());
      assertEquals(0, reader.getLastRecordLineNumber());

      int numberOfRows = 0;
      List<String> rec = reader.read();
      while (rec != null) {
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
  public void testTab() throws IOException, ParseException {
    File csv = FileUtils.getClasspathFile("csv/escapedTab.tab");
    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), '\t', true)) {

      int numberOfRecords = 0;
      List<String> rec = reader.read();
      while (rec != null) {
        numberOfRecords++;
        rec = reader.read();
      }

      assertEquals(8, numberOfRecords);
      assertEquals(8, reader.getLastRecordNumber());
      assertEquals(9, reader.getLastRecordLineNumber());
    }
  }

  @Test
  public void testCsvWithComment() throws IOException, ParseException {
    File csv = FileUtils.getClasspathFile("csv/tab_separated_generic_comments.txt");
    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8),
            '\t',
            "\n",
            null,
            true,
            2)) {

      int numberOfRecords = 0;
      List<String> rec = reader.read();
      while (rec != null) {
        numberOfRecords++;
        rec = reader.read();
      }

      assertEquals(4, numberOfRecords);
      assertEquals(4, reader.getLastRecordNumber());
      assertEquals(7, reader.getLastRecordLineNumber());
    }
  }

  @Test
  public void testIgnoreEmptyLines() throws IOException, ParseException {
    File csv = FileUtils.getClasspathFile("csv/empty_line.tab");
    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8),
            '\t',
            "\n",
            null,
            true)) {
      String[] ids = {"1", "5", "10", "12", "14", "20", "21", "", "30"};
      int row = 0;
      List<String> line = reader.read();
      while (line != null) {
        assertEquals(ids[row], line.get(0));
        row++;
        line = reader.read();
      }
      assertEquals(9, reader.getLastRecordNumber());
      assertEquals(12, reader.getLastRecordLineNumber());
    }
  }

  /**
   * Test extracting a CSV file containing embedded JSON, which itself contains escaped quotes.
   *
   * JSON value like: { "test": "value, \"like\" this" }
   *
   * Would become in CSV: "{ ""test"": ""value, \""like\"" this"" }"
   */
  @Test
  public void testCsvJsonEscapedQuotes() throws IOException, ParseException {
    File csv = FileUtils.getClasspathFile("csv/csv_json_escaped_quotes2.csv");
    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8), ',', "\n", '"', true)) {

      List<String> atom = reader.read();
      assertEquals(3, atom.size());
      assertEquals("779", atom.get(0));
      // Without the Java escapes: {"chronostratigraphy": "Cretaceous, Early Cretaceous, Albian -
      // Late Cretaceous, Cenomanian", "cataloguedescription": "Very worn vertebra. Old catalogue
      // says \"fragments of bone\".", "created": "2009-05-13", "barcode": "010039076", "project":
      // "eMesozoic", "determinationnames": "Ornithocheirus", "subdepartment": "Vertebrates",
      // "lithostratigraphy": "Selborne Group, Upper Greensand Formation, Cambridge Greensand
      // Member", "imagecategory": ["Register;Specimen"]}
      assertEquals("{\"jsonKey\": \"jsonValue\"}", atom.get(1));
      assertEquals("Cambridge, Cambridge", atom.get(2));

      atom = reader.read();
      assertEquals(
          "{\"jsonKey\": \"jsonValue with a \"quote\" in the middle (invalid JSON)\"}",
          atom.get(1));

      atom = reader.read();
      assertEquals(
          "{\"jsonKey\": \"jsonValue with a \\\"quote\\\" in the middle (valid JSON)\"}",
          atom.get(1));
    }
  }

  /**
   * TSV cannot encode tabs or newlines, so there is no escape character.
   */
  @Test
  public void testTsvBackslashes() throws IOException, ParseException {
    File csv = FileUtils.getClasspathFile("tabular/with_backslashes.tsv");
    try (TabularDataFileReader<List<String>> reader =
        TabularFiles.newTabularFileReader(
            Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8),
            '\t',
            "\n",
            null,
            true)) {

      List<String> atom = reader.read();
      assertEquals(2, atom.size());
      assertEquals("key", atom.get(0));
      assertEquals("value", atom.get(1));
      atom = reader.read();
      assertEquals("Around 1\\4 mile along the road", atom.get(1));
      atom = reader.read();
      assertEquals("Near the Cloud\\Mitchell county line", atom.get(1));
      atom = reader.read();
      assertEquals("{\"jKey\": \"jValue with \\\"quotes\\\"\"}", atom.get(1));
    }
  }
}
