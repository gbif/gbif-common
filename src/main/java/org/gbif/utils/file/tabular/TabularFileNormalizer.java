package org.gbif.utils.file.tabular;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * Utility class to rewrite a tabular file (e.g. CSV) into a normalized format.
 * The regular use case is to allow external tools to work as expected (e.g. unix split, unix sort)
 */
public class TabularFileNormalizer {

  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  public static final Charset OUTPUT_FILE_CHARSET = StandardCharsets.UTF_8;

  // A character is considered to be an ISO control character if its code is in
  // the range '\u0000' through '\u001F' or in the range '\u007F' through '\u009F'.
  private static CharMatcher CONTROL_CHAR_MATCHER = CharMatcher.JAVA_ISO_CONTROL;

  /**
   * Normalizes the provided tabular file.
   * Normalization includes: striping of Control Characters (see {@link #CONTROL_CHAR_MATCHER}),
   * usage of \n as end-line-character, ensuring there is an end-of-line character on the last line and
   * removing empty (completely empty) lines.
   * The normalized file will always be written in UTF-8 (this could be changed).
   * The normalized content will have unnecessary quotes removed.
   *
   * @param sourceFilePath        {@link Path} to the source file
   * @param normalizedFilePath    {@link Path} to the normalized file that will be generated (in UTF-8)
   * @param quoteChar
   * @param delimiterChar
   * @param endOfLineSymbols
   * @param sourceFilePathCharset {@link Charset} of the source file
   *
   * @return number of line written to the file represented by normalizedFilePath.
   */
  public static int normalizeFile(Path sourceFilePath, Path normalizedFilePath,
                                  char quoteChar, char delimiterChar,
                                  String endOfLineSymbols, Charset sourceFilePathCharset) throws IOException {
    Preconditions.checkArgument(!Files.isDirectory(sourceFilePath), "sourceFilePath must represent a file");
    Preconditions.checkArgument(!Files.isDirectory(normalizedFilePath), "normalizedFilePath must represent a file");

    CsvPreference csvPreference = new CsvPreference.Builder(quoteChar, delimiterChar, endOfLineSymbols)
            .ignoreEmptyLines(true).build();

    int numberOfLine = 0;
    try (ICsvListReader csvListReader = new CsvListReader(new InputStreamReader(new FileInputStream(sourceFilePath.toFile()),
            Optional.ofNullable(sourceFilePathCharset).orElse(DEFAULT_CHARSET)), csvPreference);
         ICsvListWriter writer = new CsvListWriter(new OutputStreamWriter(
                 new FileOutputStream(normalizedFilePath.toFile()), OUTPUT_FILE_CHARSET), csvPreference);
    ) {
      List<String> line;
      while ((line = csvListReader.read()) != null) {
        //we do not rewrite empty lines
        Optional<List<String>> nLine = normalizeLine(line);
        if (nLine.isPresent()) {
          writer.write(nLine.get());
          numberOfLine++;
        }
      }
    } catch (IOException ioEx) {
      Files.deleteIfExists(normalizedFilePath);
      throw ioEx;
    }
    return numberOfLine;
  }

  /**
   * For a given line in a tabular file, normalize it if it contains something.
   *
   * @param line
   *
   * @return normalized line as String or {@code Optional.empty()} is the line was empty
   */
  private static Optional<List<String>> normalizeLine(List<String> line) {
    if (line == null || line.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
            line.stream()
                    .map(s -> s == null ? "" : s)
                    .map(CONTROL_CHAR_MATCHER::removeFrom)
                    .collect(Collectors.toList()));
  }
}
