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
package org.gbif.utils.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.gbif.utils.file.CharsetDetection.detectEncoding;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author markus
 */
public class CharsetDetectionTest {

  @Test
  public void testCP1252Encoding() throws IOException {
    File test = FileUtils.getClasspathFile("charsets/cp1252-test.txt");

    Charset encoding = detectEncoding(test);

    assertEquals("windows1252", encoding.displayName().replace("-", "").toLowerCase());
  }

  @Test
  public void testEncodingDetection() throws IOException {
    String[] files =
        new String[] {
          "iso-8859-1_names.txt",
          "macroman_names.txt",
          "utf-16BE_bom_names.txt",
          "utf-16BE_names.txt",
          "utf-16LE_bom_names.txt",
          "utf-16LE_names.txt",
          "utf-8_bom_names.txt",
          "utf-8_names.txt",
          "windows1252_names.txt"
        };
    for (String fn : files) {
      File test = FileUtils.getClasspathFile("charsets/" + fn);

      Charset encoding = detectEncoding(test);
      String expected = StringUtils.substringBefore(fn, "_");

      // x-MacRoman is alias for MacRoman used on unix, therefore remove x-
      assertEquals(
          expected.replace("-", "").toLowerCase(),
          encoding.displayName().toLowerCase().replace("x-mac", "mac").replace("-", ""));
    }
  }

  @Test
  public void testEncodingDetectionKyles() throws IOException {
    String[] files =
        new String[] {
          "utf-8_arabic.csv",
          "utf-8_japanese.csv",
          "utf-8_korean.csv",
          "utf-8_latin.csv",
          "utf-8_no-bom.csv",
          "utf-8_traditional-chinese.csv",
          "utf-8_.csv",
          "utf-16BE_no-bom.csv",
          "utf-16BE_.csv",
          "utf-16LE_little-endian-no-bom.csv",
          "utf-16LE_little-endian.csv"
        };
    for (String fn : files) {
      File test = FileUtils.getClasspathFile("charsets/kyle/" + fn);

      Charset encoding = detectEncoding(test);
      String expected = StringUtils.substringBefore(fn, "_");

      assertEquals(
          expected.replace("-", "").toLowerCase(),
          encoding.displayName().toLowerCase().replace("-", ""));
    }
  }
}
