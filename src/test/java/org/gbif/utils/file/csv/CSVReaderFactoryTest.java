/***************************************************************************
 * Copyright 2016 Global Biodiversity Information Facility Secretariat
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

package org.gbif.utils.file.csv;

import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class CSVReaderFactoryTest {

  @Test
  public void detectCsvAlwaysQuoted() throws IOException {
    File source = FileUtils.getClasspathFile("csv/csv_always_quoted.csv");
    CSVReader reader = CSVReaderFactory.build(source);
    assertEquals(",", reader.delimiter);
    assertEquals(new Character('"'), reader.quoteChar);
    assertEquals(1, reader.headerRows);
    reader.close();
  }

  /**
   * We dont want unquoted CSVs, See detectCsvOptionallyQuoted()
   */
  public void detectCsvUnquoted() throws IOException {
    String[] files = {"csv/csv_unquoted.txt"};
    for (String fn : files) {
      File source = FileUtils.getClasspathFile(fn);
      CSVReader reader = CSVReaderFactory.build(source);
      assertEquals(",", reader.delimiter);
      assertNull(reader.quoteChar);
      assertEquals(1, reader.headerRows);
      reader.close();
    }
  }

  @Test
  public void detectPipe() throws IOException {
    String[] files = new String[] {"csv/pipe_separator.txt"};
    for (String fn : files) {
      File source = FileUtils.getClasspathFile(fn);
      CSVReader reader = CSVReaderFactory.build(source);
      assertEquals("|", reader.delimiter);
      assertNull(reader.quoteChar);
      assertEquals(1, reader.headerRows);
      reader.close();
    }
  }

  @Test
  public void detectSemicolon() throws IOException {
    String[] files = {"csv/semicolon_separator.csv"};
    for (String fn : files) {
      File source = FileUtils.getClasspathFile(fn);
      CSVReader reader = CSVReaderFactory.build(source);
      assertEquals(";", reader.delimiter);
      assertNull(reader.quoteChar);
      assertEquals(1, reader.headerRows);
      reader.close();
    }
  }

  /**
   * As CSV files with rare optional quotes are hard to detect but cause problems
   * we prefer to default to the " quotation in case comma seperated files are used.
   * This is why test detectCsvUnquoted() is outcommented right now!
   */
  @Test
  public void detectCsvOptionallyQuoted() throws IOException {
    String[] files = {"csv/csv_optional_quotes_puma.csv", "csv/csv_optional_quotes_excel2008.csv",
            "csv/csv_incl_single_quotes.csv", "csv/iucn100.csv", "csv/csv_unquoted.txt", "csv/csv_unquoted_coordinates.txt"};
    for (String fn : files) {
      File source = FileUtils.getClasspathFile(fn);
      CSVReader reader = CSVReaderFactory.build(source);
      assertEquals(",", reader.delimiter);
      assertEquals(new Character('"'), reader.quoteChar);
      assertEquals(1, reader.headerRows);
      reader.close();
    }
  }

  @Test
  public void detectTab() throws IOException {
    String[] files = {"csv/ipni.tab.txt", "csv/tab_separated_generic.txt", "csv/iucn100.tab.txt", "csv/ebird.tab.txt",
            "csv/irmng.tail", "csv/MOBOT.tab.csv"};
    for (String fn : files) {
      File source = FileUtils.getClasspathFile(fn);
      CSVReader reader = CSVReaderFactory.build(source);
      assertEquals("\t", reader.delimiter, "Check " + fn);
      assertNull(reader.quoteChar);
      assertEquals(1, reader.headerRows);
      reader.close();
    }
  }

  @Test
  public void detectTabQuoted() throws IOException {
    String[] files = {"csv/eol/my_darwincore_tab_separated_quoted.txt",
            "csv/eol/my_dataobject_tab_separated_quoted.txt", "csv/borza_tab_separated_quoted.txt"};
    for (String fn : files) {
      File source = FileUtils.getClasspathFile(fn);
      CSVReader reader = CSVReaderFactory.build(source);
      assertEquals("\t", reader.delimiter);
      assertTrue(reader.quoteChar == '"');
      assertEquals(1, reader.headerRows);
      reader.close();
    }
  }

}
