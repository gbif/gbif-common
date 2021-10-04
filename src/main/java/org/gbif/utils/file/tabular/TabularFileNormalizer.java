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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import static org.gbif.utils.file.tabular.JacksonUtils.buildCsvSchema;

/**
 * Utility class to rewrite a tabular file (e.g. CSV) into a normalized format.
 * The regular use case is to allow external tools to work as expected (e.g. unix split, unix sort).
 */
public class TabularFileNormalizer {

  // A character is considered to be an ISO control character if its code is in
  // the range '\u0000' through '\u001F' or in the range '\u007F' through '\u009F'.
  private static final String CONTROL_CHAR_REGEX = "\\p{Cntrl}";

  public static final String NORMALIZED_END_OF_LINE = "\n";

  /**
   * Normalizes the provided tabular "file" (provided as {@link Reader} to let the caller deal with charset).
   * Normalization includes: striping of Control Characters (see {@link #CONTROL_CHAR_REGEX}),
   * usage of \n as end-line-character, ensuring there is an end-of-line character on the last line and
   * removing empty (completely empty) lines.
   * The normalized content will have unnecessary quotes removed.
   *
   * @param source           {@link Path} representing the source
   * @param destination      {@link Path} representing the destination. If the file already exists it will be overwritten.
   * @param sourceCharset    optionally, the {@link Charset} of the source. If null UTF-8 will be used.
   * @param delimiterChar
   * @param endOfLineSymbols
   * @param quoteChar        optional
   *
   * @return number of lines written
   *
   * @throws IOException
   */
  public static int normalizeFile(
      Path source,
      Path destination,
      Charset sourceCharset,
      char delimiterChar,
      String endOfLineSymbols,
      Character quoteChar)
      throws IOException {
    Objects.requireNonNull(source, "source path shall be provided");
    Objects.requireNonNull(destination, "normalizedWriter shall be provided");

    int numberOfLine = 0;
    CsvMapper mapper = new CsvMapper();
    mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);

    mapper.configure(CsvGenerator.Feature.STRICT_CHECK_FOR_QUOTING, true);

    CsvSchema readerSchema = buildCsvSchema(delimiterChar, endOfLineSymbols, quoteChar);
    CsvSchema writerSchema = buildCsvSchema(delimiterChar, NORMALIZED_END_OF_LINE, quoteChar);

    Charset charset = Optional.ofNullable(sourceCharset).orElse(StandardCharsets.UTF_8);
    try (Reader sourceReader = Files.newBufferedReader(source, charset);
        Writer writer = Files.newBufferedWriter(destination, charset);
        MappingIterator<List<String>> it =
            mapper.readerFor(List.class).with(readerSchema).readValues(sourceReader);
        SequenceWriter csvWriter =
            mapper.writerFor(List.class).with(writerSchema).writeValues(writer)) {
      List<String> line;
      while (it.hasNext()) {
        line = normalizeLine(it.next());
        if (!line.isEmpty()) {
          csvWriter.write(line);
          numberOfLine++;
        }
      }
    } catch (IOException ioEx) {
      // avoid keeping incomplete file
      Files.deleteIfExists(destination);
      throw ioEx;
    }
    return numberOfLine;
  }

  /**
   * For a given line in a tabular file, normalize it if it contains something.
   *
   * @param line
   *
   * @return normalized line or an empty list if source is null or empty
   */
  private static List<String> normalizeLine(List<String> line) {
    if (line == null || line.isEmpty()) {
      return new ArrayList<>();
    }

    return line.stream()
        .map(s -> s == null ? "" : s)
        .map(str -> str.replaceAll(CONTROL_CHAR_REGEX, ""))
        .collect(Collectors.toList());
  }
}
