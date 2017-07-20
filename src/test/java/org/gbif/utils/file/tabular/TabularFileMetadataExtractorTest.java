package org.gbif.utils.file.tabular;

import org.gbif.utils.file.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import static org.gbif.utils.file.tabular.TabularFileMetadataExtractor.extractTabularFileMetadata;
import static org.gbif.utils.file.tabular.TabularFileMetadataExtractor.getDelimiterWithHighestCount;
import static org.gbif.utils.file.tabular.TabularFileMetadataExtractor.getQuoteCharWithHighestCount;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests related to {@link TabularFileMetadataExtractor}
 */
public class TabularFileMetadataExtractorTest {

  @Test
  public void testExtractTabularMetadata() {
    List<String> sample = new ArrayList<>();
    sample.add("OccurrenceID,ScientificName,Locality");
    sample.add("1,Gadus morhua,\"This has a, comma\"");
    sample.add("2,Abies alba,\"I say this is only a \"\"quote\"\"\"");
    sample.add("3,Pomatoma saltatrix,\"What though, \"\"if you have a quote\"\" and a comma\"");
    sample.add("4,Yikes ofcourses,\"What, if we have a \"\"quote, which has a comma, or 2\"\"\"");

    TabularFileMetadata metadata = TabularFileMetadataExtractor.extractTabularMetadata(sample);
    assertEquals(Character.valueOf(',').charValue(), metadata.getDelimiter().charValue());
    assertEquals(Character.valueOf('\"'), metadata.getQuotedBy());
  }

  @Test
  public void testSingleLineWithSeparatorAsValue() {
    List<String> sample = new ArrayList<>();
    sample.add("OccurrenceID,ScientificName,Locality");
    sample.add("1\ta\tb\t,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
    sample.add("2\tc\td\te");
    sample.add("3\tf\tg\th");

    TabularFileMetadata metadata = TabularFileMetadataExtractor.extractTabularMetadata(sample);
    assertEquals(Character.valueOf('\t').charValue(), metadata.getDelimiter().charValue());
    assertNull(metadata.getQuotedBy());
  }

  @Test
  public void testGetDelimiterWithHighestCount() {
    //no delimiter
    assertFalse(getDelimiterWithHighestCount("there is no delimiter here").isPresent());

    assertEquals(Character.valueOf(',').charValue(), getDelimiterWithHighestCount("a,b,c,d,e").get().charValue());
    assertEquals(Character.valueOf('|').charValue(), getDelimiterWithHighestCount("a|b,c|d|e").get().charValue());
    assertEquals(Character.valueOf('\t').charValue(), getDelimiterWithHighestCount("a\tb\tc\td\te").get().charValue());
    assertEquals(Character.valueOf(';').charValue(), getDelimiterWithHighestCount("a; b; c; d; e").get().charValue());
  }

  @Test
  public void testGetQuoteCharWithHighestCount() {
    //no quote character
    assertFalse(getQuoteCharWithHighestCount("a,b,c,d", ',').isPresent());

    // test double quote character and ensure the result is not affected by another quote character that is not used for quoting
    assertEquals(Character.valueOf('\"').charValue(), getQuoteCharWithHighestCount("a,\"b,8\",c\'\'\'\'\'\'\'\'\'\'\',d", ',').get().charValue());

    // test single quote character
    assertEquals(Character.valueOf('\'').charValue(), getQuoteCharWithHighestCount("a,\'b,8\',c,d", ',').get().charValue());
  }


  @Test
  public void detectCsvAlwaysQuoted() throws IOException {
    TabularFileMetadata tabFileMetadata = extractTabularFileMetadata(FileUtils.getClasspathFile("csv/csv_always_quoted.csv").toPath());
    Assert.assertEquals(',', tabFileMetadata.getDelimiter().charValue());
    Assert.assertEquals('"', tabFileMetadata.getQuotedBy().charValue());
  }

  @Test
  public void detectPipe() throws IOException {
    runExtractTabularFileMetadata(FileUtils.getClasspathFile("csv/pipe_separator.txt").toPath(),
            '|', null);
  }

  @Test
  public void detectSemicolon() throws IOException {
    runExtractTabularFileMetadata(FileUtils.getClasspathFile("csv/semicolon_separator.csv").toPath(),
            ';', null);
  }

  @Test
  public void detectTab() throws IOException {
    String[] files = {"csv/ipni.tab.txt", "csv/tab_separated_generic.txt", "csv/iucn100.tab.txt", "csv/ebird.tab.txt",
            "csv/empty_line.tab", "csv/irmng.tail", "csv/MOBOT.tab.csv"};
    for (String fn : files) {
      runExtractTabularFileMetadata(FileUtils.getClasspathFile(fn).toPath(),
              '\t', null);
    }
  }

  @Test
  public void detectTabQuoted() throws IOException {
    String[] files = {"csv/eol/my_darwincore_tab_separated_quoted.txt",
            "csv/eol/my_dataobject_tab_separated_quoted.txt", "csv/borza_tab_separated_quoted.txt"};
    for (String fn : files) {
      runExtractTabularFileMetadata(FileUtils.getClasspathFile(fn).toPath(),
              '\t', '"');
    }
  }

  private static void runExtractTabularFileMetadata(Path source, Character expectedDelimiter, Character expectedQuoteChar) throws IOException {
    TabularFileMetadata tabFileMetadata = extractTabularFileMetadata(source);
    Assert.assertEquals(expectedDelimiter.charValue(), tabFileMetadata.getDelimiter().charValue());

    if(expectedQuoteChar == null) {
      assertNull(tabFileMetadata.getQuotedBy());
    }
    else{
      assertNotNull("Expect a quote character -> " + source, tabFileMetadata.getQuotedBy());
      Assert.assertEquals("Source file -> " + source, expectedQuoteChar.charValue(), tabFileMetadata.getQuotedBy().charValue());
    }
  }

}
