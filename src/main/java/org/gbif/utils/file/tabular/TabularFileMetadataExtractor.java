/*
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

import org.gbif.utils.PreconditionUtils;
import org.gbif.utils.file.CharsetDetection;
import org.gbif.utils.file.UnknownCharsetException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.reverseOrder;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.toSet;

/**
 * Utility class to extract metadata {@link TabularFileMetadata} from a tabular file.
 */
public class TabularFileMetadataExtractor {

  private static final Logger LOG = LoggerFactory.getLogger(TabularFileMetadataExtractor.class);
  private static final int MAX_SAMPLE_SIZE = 15;

  // This needs to be large enough to stumble upon a non-ASCII character.
  private static final int CHARSET_DETECTION_BUFFER_LENGTH = 1024 * 1024;

  private TabularFileMetadataExtractor() {}

  private static final Character[] POTENTIAL_DELIMITER_CHAR = {',', '\t', ';', '|'};
  private static final Character[] POTENTIAL_QUOTES_CHAR = {'"', '\''};

  private static final Predicate<LineDelimiterStats> CONTAINS_FREQUENCY =
      lineStats -> lineStats.getFrequency() > 0;
  private static final Comparator<Map.Entry<Character, Long>> BY_VALUE_LONG_DESC =
      Map.Entry.comparingByValue(reverseOrder());
  private static final BiFunction<Character, Character, Pattern> COMPILE_QUOTE_PATTERN_FCT =
      (delimiter, quoteChar) ->
          Pattern.compile("[" + delimiter + "][ ]*[" + quoteChar + "][ ]*[^" + delimiter + "]");

  /**
   * Extract metadata from a tabular file using a sample (defined by {@link #MAX_SAMPLE_SIZE}) of the file.
   * The extraction process is based on the frequency of character in the sample using 3 different approaches.
   * The method will not return any default value if no delimiter and/or quote character can be found in the sample.
   * The caller should decide which default values should be used to read the file.
   *
   * @param filePath a {@link Path} pointing to a file (not a folder).
   * @return new {@link TabularFileMetadata}, never null (but the content can be null).
   * @throws IOException
   * @throws UnknownCharsetException
   */
  public static TabularFileMetadata extractTabularFileMetadata(Path filePath)
      throws IOException, UnknownCharsetException {
    Objects.requireNonNull(filePath, "filePath shall be provided");
    PreconditionUtils.checkArgument(
        !Files.isDirectory(filePath), "filePath should point to a file, not a directory");

    Charset encoding;
    try {
      encoding =
          CharsetDetection.detectEncoding(filePath.toFile(), CHARSET_DETECTION_BUFFER_LENGTH);
      if (encoding == null) {
        throw new UnknownCharsetException("Unable to detect the file's character encoding");
      }
    } catch (IOException e) {
      throw new UnknownCharsetException(e);
    }

    // open a first stream to read a sample of the file
    List<String> lines = new ArrayList<>();
    try (BufferedReader bf = Files.newBufferedReader(filePath, encoding)) {
      String line;
      do {
        line = bf.readLine();
        if (line != null) {
          lines.add(line);
        }
      } while (line != null && lines.size() < MAX_SAMPLE_SIZE);
    }
    TabularFileMetadata tabularFileMetadata = extractTabularMetadata(lines);
    tabularFileMetadata.setEncoding(encoding);
    return tabularFileMetadata;
  }

  /**
   * Tries to extract the {@link TabularFileMetadata} from a sample of lines of a tabular file.
   *
   * @param sample
   * @return new {@link TabularFileMetadata}, never null (but the content can be null).
   */
  static TabularFileMetadata extractTabularMetadata(final List<String> sample) {
    Objects.requireNonNull(sample, "sample shall be provided");
    TabularFileMetadata tabularFileMetadata = new TabularFileMetadata();

    Optional<Character> delimiterFound = getDelimiterChar(sample);
    final Character delimiter = delimiterFound.orElse(null);
    if (delimiter == null) {
      return tabularFileMetadata;
    }

    Optional<Character> quoteFound =
        getHighestCountOf(sample, line -> getQuoteCharWithHighestCount(line, delimiter));
    final Character quote = quoteFound.orElse(null);

    tabularFileMetadata.setDelimiter(delimiter);
    tabularFileMetadata.setQuotedBy(quote);

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
  private static Optional<Character> getHighestCountOf(
      final List<String> sample, final Function<String, Optional<Character>> characterExtractor) {

    // remove Optional wrapper and ignore Optional.empty
    return sample.stream()
        .map(characterExtractor)
        .flatMap(
            o ->
                o.map(Stream::of)
                    .orElseGet(Stream::empty)) // remove Optional wrapper and ignore Optional.empty
        .collect(Collectors.groupingBy(Function.identity(), counting()))
        .entrySet()
        .stream()
        .min(BY_VALUE_LONG_DESC)
        .map(Map.Entry::getKey);
  }

  /**
   * Given a sample of line, this method tries to determine the delimiter char used.
   *
   * @param sample
   *
   * @return the determined delimiter or Optional.empty if it can not be determined.
   */
  public static Optional<Character> getDelimiterChar(final List<String> sample) {

    // count the frequency of all possible delimiter for each lines
    List<LineDelimiterStats> linesStats = computeLineDelimiterStats(sample);

    // get the distinct set of frequency for each delimiters to check the "stability"
    Map<Character, Set<Integer>> delimiterDistinctFrequency =
        computeDelimiterDistinctFrequency(linesStats).entrySet().stream()
            // filter out delimiter that we never saw
            .filter(entry -> entry.getValue().size() > 1 || !entry.getValue().contains(0))
            .sorted(Comparator.comparing(e -> e.getValue().size()))
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

    // we can have more than one
    Set<Character> mostStableDelimiter =
        getAllEqualsToFirst(delimiterDistinctFrequency, (s1, s2) -> s1.size() == s2.size());

    // get the most used delimiter to check the "overall usage"
    Map<Character, Integer> delimiterFrequencySums =
        computeDelimiterFrequencySums(linesStats).entrySet().stream()
            .sorted(Map.Entry.<Character, Integer>comparingByValue().reversed())
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

    Set<Character> mostFrequentDelimiter =
        getAllEqualsToFirst(delimiterFrequencySums, Integer::equals);

    // get the highest frequency per line to check for "usage per line"
    Map<Character, Long> delimiterHighestFrequencyPerLine =
        computeDelimiterHighestFrequencyPerLine(sample).entrySet().stream()
            .sorted(Map.Entry.<Character, Long>comparingByValue().reversed())
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    Set<Character> mostFrequentDelimiterPerLine =
        getAllEqualsToFirst(delimiterHighestFrequencyPerLine, Long::equals);

    // summary
    if (LOG.isDebugEnabled()) {
      LOG.debug("delimiterDistinctFrequency -> " + delimiterDistinctFrequency);
      LOG.debug("mostStableDelimiter -> " + mostStableDelimiter);
      LOG.debug("delimiterFrequencySums -> " + delimiterFrequencySums);
      LOG.debug("mostFrequentDelimiter -> " + mostFrequentDelimiter);
      LOG.debug("delimiterHighestFrequencyPerLine->" + delimiterHighestFrequencyPerLine);
      LOG.debug("mostFrequentDelimiterPerLine ->" + mostFrequentDelimiterPerLine);
    }

    // if the most stable is also the one that is used to most within the sample
    Optional<Character> resultCharacter =
        intersectSingle(mostStableDelimiter, mostFrequentDelimiter);
    if (resultCharacter.isPresent()) {
      return resultCharacter;
    }

    // otherwise, if the most stable is also the most used based on lines
    resultCharacter = intersectSingle(mostStableDelimiter, mostFrequentDelimiterPerLine);
    if (resultCharacter.isPresent()) {
      return resultCharacter;
    }

    // as last resort if the most frequent delimiter overall and by line is the same
    resultCharacter = intersectSingle(mostFrequentDelimiter, mostFrequentDelimiterPerLine);

    return resultCharacter;
  }

  /**
   * Return the {@link Character} represents the intersection between 2 sets only if the resulting set represents
   * a single element.
   * @param set1
   * @param set2
   * @return
   */
  private static Optional<Character> intersectSingle(Set<Character> set1, Set<Character> set2) {
    Set<Character> intersection = new HashSet<>(set1);
    intersection.retainAll(set2);

    return intersection.size() == 1 ? intersection.stream().findFirst() : Optional.empty();
  }

  /**
   * Given a {@link Map}, return all elements that are equals to the first element (including itself)
   * based on the provided equals function.
   * @param map
   * @param equalsPredicate
   * @param <T>
   * @return all elements that are equals to the first one or an empty set if the map is empty
   */
  private static <T> Set<Character> getAllEqualsToFirst(
      Map<Character, T> map, BiFunction<T, T, Boolean> equalsPredicate) {

    Optional<Map.Entry<Character, T>> firstMapEntry = map.entrySet().stream().findFirst();
    if (!firstMapEntry.isPresent()) {
      return Collections.EMPTY_SET;
    }

    final T firstValue = firstMapEntry.get().getValue();
    return map.entrySet().stream()
        .filter(e -> equalsPredicate.apply(firstValue, e.getValue()))
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  /**
   * For each element(line) of the sample, compute a {@link LineDelimiterStats} for each delimiter.
   * Note: delimiter that are not used within a line will be included with the frequency 0.
   * @param sample
   * @return new List, never null
   */
  static List<LineDelimiterStats> computeLineDelimiterStats(List<String> sample) {
    return sample.stream()
        .map(TabularFileMetadataExtractor::lineToLineDelimiterStats)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  /**
   * Compute the stats for each potential delimiters on a line.
   * @param line
   * @return
   */
  private static List<LineDelimiterStats> lineToLineDelimiterStats(String line) {
    return Arrays.stream(POTENTIAL_DELIMITER_CHAR)
        .map(
            delimiter ->
                new LineDelimiterStats(delimiter, StringUtils.countMatches(line, delimiter)))
        .collect(Collectors.toList());
  }

  /**
   * For each {@link LineDelimiterStats}, collect the distinct frequency (count) of each delimiter.
   * This gives us an idea of the "stability" of each delimiter across the sample.
   * Note that since quotes are not handled, noise can be introduced if a quoted cells use the delimiter.
   *
   * See unit test for examples of when this method will be affected by noise.
   *
   * The most stable delimiter is normally defined by the {@link Character} returned by the methods where
   * the list of distinct frequency is the smallest in size excluding cases where the list contains only the element
   * representing 0 as Integer (which means the delimiter was never used).
   *
   * @param linesStats
   *
   * @return
   */
  static Map<Character, Set<Integer>> computeDelimiterDistinctFrequency(
      List<LineDelimiterStats> linesStats) {
    return linesStats.stream()
        .collect(
            Collectors.groupingBy(
                LineDelimiterStats::getDelimiter,
                Collectors.mapping(LineDelimiterStats::getFrequency, toSet())));
  }

  /**
   * For each line, check the delimiter that is used the most.
   * Return the count of each delimiter.
   * @param lines
   * @return
   */
  static Map<Character, Long> computeDelimiterHighestFrequencyPerLine(List<String> lines) {
    return lines.stream()
        .map(TabularFileMetadataExtractor::getDelimiterWithHighestCount2)
        .flatMap(
            o ->
                o.map(Stream::of)
                    .orElseGet(Stream::empty)) // remove Optional wrapper and ignore Optional.empty
        .collect(Collectors.groupingBy(LineDelimiterStats::getDelimiter, counting()));
  }

  /**
   * For {@link LineDelimiterStats}, sum the frequency (count) of each delimiter.
   * This gives us an idea of the overall usage of each delimiter across the sample.
   * Note that since quotes are not handled, noise can be introduced if a quoted cell uses the delimiter.
   *
   * See unit test for examples of when this method will be affected by noise.
   *
   * @param linesStats
   *
   * @return
   */
  static Map<Character, Integer> computeDelimiterFrequencySums(
      List<LineDelimiterStats> linesStats) {
    return linesStats.stream()
        .filter(CONTAINS_FREQUENCY)
        .collect(
            Collectors.groupingBy(
                LineDelimiterStats::getDelimiter,
                Collectors.summingInt(LineDelimiterStats::getFrequency)));
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

  static Optional<LineDelimiterStats> getDelimiterWithHighestCount2(String line) {
    int highestCount = 0;
    // Character highestCountDelimiter = null;
    LineDelimiterStats lineDelimiterStats = null;
    for (Character delimiter : POTENTIAL_DELIMITER_CHAR) {
      int currentCount = StringUtils.countMatches(line, delimiter);
      if (currentCount > highestCount) {
        highestCount = currentCount;
        lineDelimiterStats = new LineDelimiterStats(delimiter, highestCount);
      }
    }
    return Optional.ofNullable(lineDelimiterStats);
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

  /**
   * Inner representation of stats (frequency) of a delimiter
   */
  static class LineDelimiterStats {
    private Character delimiter;
    private int frequency;

    LineDelimiterStats(Character delimiter, int frequency) {
      this.delimiter = delimiter;
      this.frequency = frequency;
    }

    Character getDelimiter() {
      return delimiter;
    }

    int getFrequency() {
      return frequency;
    }
  }
}
