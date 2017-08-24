package org.gbif.utils.file.csv;

import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
      assertEquals("Check " + fn, "\t", reader.delimiter);
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
