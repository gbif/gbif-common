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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.gbif.utils.file.tabular.TabularFileMetadataExtractor.computeLineDelimiterStats;
import static org.gbif.utils.file.tabular.TabularFileMetadataExtractor.extractTabularFileMetadata;
import static org.gbif.utils.file.tabular.TabularFileMetadataExtractor.getDelimiterWithHighestCount;
import static org.gbif.utils.file.tabular.TabularFileMetadataExtractor.getQuoteCharWithHighestCount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests related to {@link TabularFileMetadataExtractor}
 */
public class TabularFileMetadataExtractorTest {

  @Test
  public void testComputeDelimiterFrequencySums() {
    List<String> sample = new ArrayList<>();
    sample.add("ID\tName\tName2\tName3");
    sample.add("1\ta\tb\tc,1");
    sample.add("2\tc\td\te,2");
    sample.add("3\tf\tg\th,3");

    List<TabularFileMetadataExtractor.LineDelimiterStats> linesStats =
            computeLineDelimiterStats(sample);
    Map<Character, Integer> delimiterFrequencySums = TabularFileMetadataExtractor.computeDelimiterFrequencySums(linesStats);
    // here, the delimiter that is used the most often is in fact the correct one
    assertEquals(12, delimiterFrequencySums.get('\t').intValue());
    assertEquals(3, delimiterFrequencySums.get(',').intValue());

    //add a "noise" line to demonstrate the impact on this function
    sample.add("4\ti\tj\tk,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,4");
    linesStats =
            computeLineDelimiterStats(sample);
    delimiterFrequencySums = TabularFileMetadataExtractor.computeDelimiterFrequencySums(linesStats);
    // here, the delimiter that is used the most often is the wrong one
    assertEquals(15, delimiterFrequencySums.get('\t').intValue());
    assertEquals(34, delimiterFrequencySums.get(',').intValue());
  }

  @Test
  public void testComputeDelimiterDistinctFrequency() {
    List<String> sample = new ArrayList<>();
    sample.add("ID\tName\tName2\tName3");
    sample.add("1\ta\tb\tc,1");
    sample.add("2\tc\td\te,2");
    sample.add("3\tf\tg\th,3");

    List<TabularFileMetadataExtractor.LineDelimiterStats> linesStats =
            computeLineDelimiterStats(sample);
    Map<Character, Set<Integer>> delimiterDistinctFrequency = TabularFileMetadataExtractor.computeDelimiterDistinctFrequency(linesStats);

    // here, the delimiter with the most stable frequency is the correct one
    assertEquals(1, delimiterDistinctFrequency.get('\t').size());
    assertEquals(2, delimiterDistinctFrequency.get(',').size());

    sample.add("4\ti\t\"j\t\"\tk,4");
    sample.add("5\tl\t\"m\t\t\"\tn,5");
    linesStats =
            computeLineDelimiterStats(sample);
    delimiterDistinctFrequency = TabularFileMetadataExtractor.computeDelimiterDistinctFrequency(linesStats);
    // here, the delimiter that is the most stable is now the wrong one (because of the delimiter inside the quoted text)
    assertEquals(3, delimiterDistinctFrequency.get('\t').size());
    assertEquals(2, delimiterDistinctFrequency.get(',').size());
  }

  @Test
  public void testComputeDelimiterHighestFrequencyPerLine() {
    List<String> sample = new ArrayList<>();
    sample.add("ID\tName\tName2\tName3");
    sample.add("1\ta\tb\tc,1");
    sample.add("2\tc\td\te,2");
    sample.add("3\tf\tg\th,3");

    Map<Character, Long> delimiterDistinctFrequency = TabularFileMetadataExtractor.
      computeDelimiterHighestFrequencyPerLine(sample);

    assertEquals(4, delimiterDistinctFrequency.get('\t').intValue());
    assertNull(delimiterDistinctFrequency.get(','));

    //this line alone won't have an impact on computeDelimiterHighestFrequencyPerLine result
    sample.add("4\ti\tj\tk,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,4");
    delimiterDistinctFrequency = TabularFileMetadataExtractor.
            computeDelimiterHighestFrequencyPerLine(sample);
    assertEquals(4, delimiterDistinctFrequency.get('\t').intValue());
    assertEquals(1, delimiterDistinctFrequency.get(',').intValue());
  }

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
    sample.add("ID\tName\tName1\tName2");
    sample.add("1\ta\tb\t,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
    sample.add("2\tc\td\te");
    sample.add("3\tf\tg\th");

    TabularFileMetadata metadata = TabularFileMetadataExtractor.extractTabularMetadata(sample);
    assertEquals(Character.valueOf('\t').charValue(), metadata.getDelimiter().charValue());
    assertNull(metadata.getQuotedBy());

    //try another version
    sample.clear();
    sample.add("1\tCarlos");
    sample.add("2\tPeter, Karl & Inge");
    sample.add("3\tCarla, Klara, Berit, Susanna");
    sample.add("4\tFoo & Bar");
    metadata = TabularFileMetadataExtractor.extractTabularMetadata(sample);
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
    assertEquals(',', tabFileMetadata.getDelimiter().charValue());
    assertEquals('"', tabFileMetadata.getQuotedBy().charValue());
  }

  @Test
  public void detectPipe() throws IOException {
    runExtractTabularFileMetadata("csv/pipe_separator.txt", '|', null, StandardCharsets.UTF_8);
  }

  @Test
  public void detectSemicolon() throws IOException {
    runExtractTabularFileMetadata("csv/semicolon_separator.csv", ';', null, StandardCharsets.UTF_8);
  }

  @Test
  public void detectTab() throws IOException {
    String[] files = {"csv/ipni.tab.txt", "csv/tab_separated_generic.txt", "csv/iucn100.tab.txt", "csv/ebird.tab.txt",
            "csv/empty_line.tab", "csv/irmng.tail", "csv/MOBOT.tab.csv"};
    for (String fn : files) {
      runExtractTabularFileMetadata(fn, '\t', null, StandardCharsets.UTF_8);
    }
  }

  @Test
  public void detectTabQuoted() throws IOException {
    String[] files = {"csv/eol/my_darwincore_tab_separated_quoted.txt",
            "csv/eol/my_dataobject_tab_separated_quoted.txt", "csv/borza_tab_separated_quoted.txt"};
    for (String fn : files) {
      runExtractTabularFileMetadata(fn,'\t', '"', StandardCharsets.UTF_8);
    }
  }

  private static void runExtractTabularFileMetadata(String classPathFile, Character expectedDelimiter, Character expectedQuoteChar,
                                                    Charset expectedCharset) throws IOException {
    Path source = FileUtils.getClasspathFile(classPathFile).toPath();
    TabularFileMetadata tabFileMetadata = extractTabularFileMetadata(source);
    assertEquals(expectedDelimiter.charValue(), tabFileMetadata.getDelimiter().charValue());

    if(expectedQuoteChar == null) {
      assertNull(tabFileMetadata.getQuotedBy());
    }
    else{
      assertNotNull(tabFileMetadata.getQuotedBy(), "Expect a quote character -> " + source);
      assertEquals(expectedQuoteChar, tabFileMetadata.getQuotedBy(), "Source file -> " + source);
    }

    assertEquals(expectedCharset, tabFileMetadata.getEncoding());
  }

  @Test
  public void detectEncoding() throws IOException {
    runExtractTabularFileMetadata("tabular/test_encoding_detection.iso-8859-1.csv",',', null, StandardCharsets.ISO_8859_1);
    runExtractTabularFileMetadata("tabular/test_encoding_detection.utf-8.csv",',', null, StandardCharsets.UTF_8);
  }
}
