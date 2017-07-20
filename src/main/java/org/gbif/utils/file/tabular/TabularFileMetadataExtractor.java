package org.gbif.utils.file.tabular;

import org.gbif.utils.file.CharsetDetection;
import org.gbif.utils.file.UnkownCharsetException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import static java.util.Collections.reverseOrder;

/**
 * Utility class to extract metadata {@link TabularFileMetadata} from a tabular file.
 */
public class TabularFileMetadataExtractor {

  private static final int MAX_SAMPLE_SIZE = 10;
  private static final int CHARSET_DETECTION_BUFFER_LENGTH = 16384;

  private TabularFileMetadataExtractor() {
  }

  private static final Character[] POTENTIAL_DELIMITER_CHAR = {',', '\t', ';', '|'};
  private static final Character[] POTENTIAL_QUOTES_CHAR = {'"', '\''};

  private static final Comparator<Map.Entry<Character, Long>> BY_VALUE_LONG_DESC = Comparator.comparing(Map.Entry::getValue, reverseOrder());
  private static final BiFunction<Character, Character, Pattern> COMPILE_QUOTE_PATTERN_FCT = (delimiter, quoteChar)
          -> Pattern.compile("[" + delimiter + "][ ]*[" + quoteChar + "][ ]*[^" + delimiter + "]");

  /**
   * Extract metadata from a tabular file using a sample (defined by {@link #MAX_SAMPLE_SIZE}) of the file.
   * The extraction process is based on the frequency of character in the sample. The method will return
   * a 'null' quote character if none were found in the sample. The caller should decide if a default value should be used
   * to read the file.
   *
   * @param filePath a {@link Path} pointing to a file (not a folder).
   * @return
   * @throws IOException
   * @throws UnkownCharsetException
   */
  public static TabularFileMetadata extractTabularFileMetadata(Path filePath) throws IOException, UnkownCharsetException {
    Objects.requireNonNull(filePath, "filePath shall be provided");
    Preconditions.checkArgument(!Files.isDirectory(filePath), "filePath should point to a file, not a directory");

    Charset encoding;
    try {
      encoding = CharsetDetection.detectEncoding(filePath.toFile(), CHARSET_DETECTION_BUFFER_LENGTH);
      if (encoding == null) {
        throw new UnkownCharsetException("Unable to detect the files character encoding");
      }
    } catch (IOException e) {
      throw new UnkownCharsetException(e);
    }

    // open a first stream to read a sample of the file
    List<String> lines = new ArrayList<>();
    try (BufferedReader bf = Files.newBufferedReader(filePath, encoding)) {
      String line;
      do {
        line = bf.readLine();
        if(line != null) {
          lines.add(line);
        }
      }
      while(line != null && lines.size() < MAX_SAMPLE_SIZE);
    }
    TabularFileMetadata tabularFileMetadata = extractTabularMetadata(lines);
    tabularFileMetadata.setEncoding(encoding);
    return tabularFileMetadata;
  }

  static TabularFileMetadata extractTabularMetadata(final List<String> sample) {
    TabularFileMetadata tabularFileMetadata = new TabularFileMetadata();

    Optional<Character> delimiterFound = getHighestCountOf(sample, TabularFileMetadataExtractor::getDelimiterWithHighestCount);
    final Character delimiter = delimiterFound.orElse(',');

    Optional<Character> quoteFound = getHighestCountOf(sample, line -> getQuoteCharWithHighestCount(line, delimiter));

    tabularFileMetadata.setDelimiter(delimiter);
    tabularFileMetadata.setQuotedBy(quoteFound.orElse(null));

    return tabularFileMetadata;
  }

  /**
   * Extract a character from a line using the given function.
   * Return the character with the highest counts.
   *
   * @param sample
   * @param characterExtractor function to apply on each line to extract a character
   *
   * @return
   */
  private static Optional<Character> getHighestCountOf(final List<String> sample, final Function<String,
          Optional<Character>> characterExtractor) {

    return sample.stream()
            .map(characterExtractor)
            .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty)) //remove Optional wrapper and ignore Optional.empty
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .sorted(BY_VALUE_LONG_DESC)
            .findFirst()
            .map(Map.Entry::getKey);
  }

  /**
   * Given a line, get the delimiter with the highest count if any can be found.
   * Note: quotes are ignored in the count so a delimiter used inside quotes will be counted.
   *
   * @param line line of text to analyse
   *
   * @return
   */
  static Optional<Character> getDelimiterWithHighestCount(String line) {
    int highestCount = 0;
    Character highestCountDelimiter = null;
    for (Character delimiter : POTENTIAL_DELIMITER_CHAR) {
      int currentCount = StringUtils.countMatches(line, delimiter);
      if (currentCount > highestCount) {
        highestCount = currentCount;
        highestCountDelimiter = delimiter;
      }
    }
    return Optional.ofNullable(highestCountDelimiter);
  }

  /**
   * Given a line and a delimiter, try to determine the quoting character if any can be found.
   * To check if a quote character is used we run a regex to check for a delimiter followed by a quoting character.
   *
   * @param line line of text to analyse
   * @param delimiter delimiter used in the line of text
   *
   * @return
   */
  static Optional<Character> getQuoteCharWithHighestCount(String line, Character delimiter) {
    int highestCount = 0;
    Character highestCountQuoteChar = null;
    for (Character quoteChar : POTENTIAL_QUOTES_CHAR) {
      int currentCount = 0;
      Matcher m = COMPILE_QUOTE_PATTERN_FCT.apply(delimiter, quoteChar).matcher(line);
      while (m.find()) {
        currentCount++;
      }
      if (currentCount > highestCount) {
        highestCount = currentCount;
        highestCountQuoteChar = quoteChar;
      }
    }
    return Optional.ofNullable(highestCountQuoteChar);
  }

}
