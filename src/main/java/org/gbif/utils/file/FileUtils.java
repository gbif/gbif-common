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
package org.gbif.utils.file;

import org.gbif.utils.collection.CompactHashSet;
import org.gbif.utils.text.LineComparator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of file utils.
 * <br>
 * This class has only been tested for use with a UTF-8 system encoding.
 */
public final class FileUtils {

  private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

  public static final String UTF8 = StandardCharsets.UTF_8.name();
  public static final Pattern TAB_DELIMITED = Pattern.compile("\t");
  private static int linesPerMemorySort = 100000;
  private static Boolean gnuSortAvailable = null;
  private static final Object sortLock = new Object();

  static {
    /* Warn when the software is not run in a Unicode environment.  This library has not been
     * tested to run in a non-Unicode environment, and may cause data corruption.
     */
    if (Charset.defaultCharset().equals(StandardCharsets.US_ASCII)) {
      System.err.println(
          "The default character set is US ASCII.  It is strongly recommended to "
              + "run this software in a Unicode environment.");
    }
  }

  public static String classpath2Filepath(String path) {
    return new File(ClassLoader.getSystemResource(path).getFile()).getAbsolutePath();
  }

  public static InputStream classpathStream(String path) throws IOException {
    InputStream in = null;
    // relative path. Use classpath instead
    URL url = FileUtils.class.getClassLoader().getResource(path);
    if (url != null) {
      in = url.openStream();
    }
    return in;
  }

  public static Set<String> columnsToSet(InputStream source, int... column) throws IOException {
    return columnsToSet(source, new CompactHashSet<String>(), column);
  }

  /**
   * Reads a file and returns a unique set of multiple columns from lines which are no comments (starting with #) and
   * trims whitespace.
   *
   * @param source the UTF-8 encoded text file with tab delimited columns
   * @param resultSet the set implementation to be used. Will not be cleared before reading!
   * @param column variable length argument of column indices to process
   * @return set of column rows
   */
  public static Set<String> columnsToSet(InputStream source, Set<String> resultSet, int... column)
      throws IOException {
    LineIterator lines = getLineIterator(source);
    int maxCols = 0;
    for (int c : column) {
      if (c > maxCols) {
        maxCols = c;
      }
    }
    while (lines.hasNext()) {
      String line = lines.nextLine().trim();
      // ignore comments
      if (!ignore(line)) {
        String[] parts = TAB_DELIMITED.split(line);
        if (maxCols <= parts.length) {
          for (int c : column) {
            String cell = parts[c].trim();
            resultSet.add(cell);
          }
        }
      }
    }
    return resultSet;
  }

  public static void copyStreams(InputStream in, OutputStream out) throws IOException {
    // write the file to the file specified
    int bytesRead;
    byte[] buffer = new byte[8192];

    while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
      out.write(buffer, 0, bytesRead);
    }

    out.close();
    in.close();
  }

  public static void copyStreamToFile(InputStream in, File out) throws IOException {
    copyStreams(in, new FileOutputStream(out));
  }

  public static File createTempDir() throws IOException {
    return createTempDir("gbif-futil", ".tmp");
  }

  /**
   * @param prefix The prefix string to be used in generating the file's name; must be at least three characters long
   * @param suffix The suffix string to be used in generating the file's name; may be null, in which case the suffix
   *        ".tmp" will be used
   */
  public static File createTempDir(String prefix, String suffix) throws IOException {
    File dir = File.createTempFile(prefix, suffix);
    if (!dir.delete()) {
      throw new IOException("Could not delete temp file: " + dir.getAbsolutePath());
    }
    if (!dir.mkdir()) {
      throw new IOException("Could not create temp directory: " + dir.getAbsolutePath());
    }
    return dir;
  }

  /**
   * Delete directory recursively, including all its files, sub-folders, and sub-folder's files.
   *
   * @param directory directory to delete recursively
   */
  public static void deleteDirectoryRecursively(File directory) {
    File[] list = directory.listFiles();
    for (File file : list) {
      if (file.isDirectory()) {
        deleteDirectoryRecursively(file);
        file.delete();
      } else {
        file.delete();
      }
    }
    directory.delete();
  }

  /**
   * Escapes a filename so it is a valid filename on all systems, replacing /. .. \t\r\n.
   *
   * @param filename to be escaped
   */
  public static String escapeFilename(String filename) {
    return filename.replaceAll("[\\s./&]", "_");
  }

  public static File getClasspathFile(String path) {
    return new File(ClassLoader.getSystemResource(path).getFile());
  }

  public static InputStream getInputStream(File source) throws FileNotFoundException {
    return new FileInputStream(source);
  }

  public static BufferedReader getInputStreamReader(InputStream input)
      throws FileNotFoundException {
    return getInputStreamReader(input, UTF8);
  }

  public static BufferedReader getInputStreamReader(InputStream input, String encoding)
      throws FileNotFoundException {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(input, encoding));
    } catch (UnsupportedEncodingException e) {
      LOG.warn("Caught Exception", e);
    }
    return reader;
  }

  /**
   * @param source the source input stream encoded in UTF-8
   */
  public static LineIterator getLineIterator(InputStream source) {
    return getLineIterator(source, UTF8);
  }

  /**
   * @param source the source input stream
   * @param encoding the encoding used by the input stream
   */
  public static LineIterator getLineIterator(InputStream source, String encoding) {
    try {
      return new LineIterator(new BufferedReader(new InputStreamReader(source, encoding)));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Unsupported encoding" + encoding, e);
    }
  }

  public static BufferedReader getUtf8Reader(File file) throws FileNotFoundException {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8));
    } catch (UnsupportedEncodingException e) {
      LOG.warn("Caught Exception", e);
    }
    return reader;
  }

  /**
   * Converts the byte size into human-readable format.
   * Support both SI and byte format.
   */
  public static String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  public static boolean isCompressedFile(File source) {
    String suffix = source.getName().substring(source.getName().lastIndexOf('.') + 1);
    return suffix != null
        && suffix.length() > 0
        && ("zip".equalsIgnoreCase(suffix)
            || "tgz".equalsIgnoreCase(suffix)
            || "gz".equalsIgnoreCase(suffix));
  }

  /**
   * Reads a complete file into a byte buffer.
   */
  public static ByteBuffer readByteBuffer(File file) throws IOException {
    byte[] content = org.apache.commons.io.FileUtils.readFileToByteArray(file);
    return ByteBuffer.wrap(content);
  }

  /**
   * Reads the first bytes of a file into a byte buffer.
   *
   * @param bufferSize the number of bytes to read from the file
   */
  public static ByteBuffer readByteBuffer(File file, int bufferSize) throws IOException {
    ByteBuffer bbuf = ByteBuffer.allocate(bufferSize);
    BufferedInputStream f = new BufferedInputStream(new FileInputStream(file), bufferSize);

    int b;
    while ((b = f.read()) != -1) {
      if (!bbuf.hasRemaining()) {
        break;
      }
      bbuf.put((byte) b);
    }
    f.close();

    return bbuf;
  }

  /**
   * @param linesPerMemorySort are the number of lines that should be sorted in memory, determining the number of file
   *        segments to be sorted when doing a Java file sort. Defaults to 100000, if you have
   *        memory available a higher value increases performance.
   */
  public static void setLinesPerMemorySort(int linesPerMemorySort) {
    FileUtils.linesPerMemorySort = linesPerMemorySort;
  }

  public static Writer startNewUtf8File(File file) throws IOException {
    touch(file);
    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), UTF8));
  }

  public static Writer startNewUtf8XmlFile(File file) throws IOException {
    Writer writer = startNewUtf8File(file);
    writer.write("<?xml version='1.0' encoding='utf-8'?>\n");
    return writer;
  }

  /**
   * Takes a utf8 encoded input stream and reads in every line/row into a list.
   *
   * @return list of rows
   */
  public static LinkedList<String> streamToList(InputStream source) throws IOException {
    return streamToList(source, FileUtils.UTF8);
  }

  /**
   * Reads a file and returns a list of all lines which are no comments (starting with #) and trims whitespace.
   *
   * @param source the UTF-8 encoded text file to read
   * @param resultList the list implementation to be used. Will not be cleared before reading!
   * @return list of lines
   */
  public static List<String> streamToList(InputStream source, List<String> resultList)
      throws IOException {
    LineIterator lines = getLineIterator(source);
    while (lines.hasNext()) {
      String line = lines.nextLine().trim();
      // ignore comments
      if (!ignore(line)) {
        resultList.add(line);
      }
    }
    return resultList;
  }

  public static LinkedList<String> streamToList(InputStream source, String encoding)
      throws IOException {
    LinkedList<String> resultList = new LinkedList<>();
    try {
      LineIterator lines =
          new LineIterator(new BufferedReader(new InputStreamReader(source, encoding)));
      while (lines.hasNext()) {
        String line = lines.nextLine();
        resultList.add(line);
      }
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Unsupported encoding " + encoding, e);
    }
    return resultList;
  }

  /**
   * Reads a utf8 encoded inut stream, splits
   */
  public static Map<String, String> streamToMap(InputStream source) throws IOException {
    return streamToMap(source, new HashMap<>());
  }

  public static Map<String, String> streamToMap(
      InputStream source, int key, int value, boolean trimToNull) throws IOException {
    return streamToMap(source, new HashMap<>(), key, value, trimToNull);
  }

  /**
   * Read a hashmap from a tab delimited utf8 input stream using the row number as an integer value and the entire row
   * as the value. Ignores commented rows starting with #.
   *
   * @param source tab delimited text file to read
   */
  public static Map<String, String> streamToMap(InputStream source, Map<String, String> result)
      throws IOException {
    LineIterator lines = getLineIterator(source);
    Integer row = 0;
    while (lines.hasNext()) {
      row++;
      String line = lines.nextLine().trim();
      // ignore comments
      if (!ignore(line)) {
        result.put(line, row.toString());
      }
    }
    return result;
  }

  /**
   * Read a hashmap from a tab delimited utf8 file, ignoring commented rows starting with #.
   *
   * @param source tab delimited input stream to read
   * @param key column number to use as key
   * @param value column number to use as value
   * @param trimToNull if true trims map entries to null
   */
  public static Map<String, String> streamToMap(
      InputStream source, Map<String, String> result, int key, int value, boolean trimToNull)
      throws IOException {
    LineIterator lines = getLineIterator(source);
    int maxCols = key > value ? key : value + 1;
    while (lines.hasNext()) {
      String line = lines.nextLine();
      // ignore comments
      if (!ignore(line)) {
        String[] parts = TAB_DELIMITED.split(line);
        if (maxCols <= parts.length) {
          if (trimToNull) {
            result.put(StringUtils.trimToNull(parts[key]), StringUtils.trimToNull(parts[value]));
          } else {
            result.put(parts[key], parts[value]);
          }
        }
      }
    }
    return result;
  }

  public static Set<String> streamToSet(InputStream source) throws IOException {
    return streamToSet(source, new CompactHashSet<>());
  }

  /**
   * Reads a file and returns a unique set of all lines which are no comments (starting with #) and trims whitespace.
   *
   * @param source the UTF-8 encoded text file to read
   * @param resultSet the set implementation to be used. Will not be cleared before reading!
   * @return set of unique lines
   */
  public static Set<String> streamToSet(InputStream source, Set<String> resultSet)
      throws IOException {
    LineIterator lines = getLineIterator(source);
    while (lines.hasNext()) {
      String line = lines.nextLine().trim();
      // ignore comments
      if (!ignore(line)) {
        resultSet.add(line);
      }
    }
    return resultSet;
  }

  public static String toFilePath(URL url) {
    String protocol =
        url.getProtocol() == null || "http".equalsIgnoreCase(url.getProtocol())
            ? ""
            : "/__" + url.getProtocol() + "__";
    String domain = url.getAuthority() == null ? "__domainless" : url.getAuthority();
    return domain + protocol + url.getFile();
  }

  public static File url2file(URL url) {
    File f = null;
    try {
      f = new File(url.toURI());
    } catch (URISyntaxException e) {
      f = new File(url.getPath());
    }
    return f;
  }

  /**
   * For the given list, finds the index of the lowest value using the given comparator.
   *
   * @param values To compare
   * @param comparator To use
   * @return The index of the lowest value, or -1 if they are all null
   */
  static int lowestValueIndex(List<String> values, Comparator<String> comparator) {
    int index = 0;
    String lowestValue = null;
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      if (lowestValue == null) {
        lowestValue = value;
        index = i;
      } else if (comparator.compare(lowestValue, value) > 0) {
        lowestValue = value;
        index = i;
      }
    }

    return lowestValue == null ? -1 : index;
  }

  /**
   * For the given file's path, returns a proposed new filename (including path) with the extension
   * index and suffix. So a file of "/tmp/input.txt" -> "/tmp/input_part_10.txt".
   *
   * @param original File
   * @param index E.g. 10
   * @return The proposed name
   */
  private static File getChunkFile(File original, int index) {
    return new File(
        original.getParentFile(),
        FilenameUtils.getBaseName(original.getName())
            + '_'
            + index
            + getFileExtension(original.getName()));
  }

  private static boolean ignore(String line) {
    return StringUtils.trimToNull(line) == null || line.startsWith("#");
  }

  public int getLinesPerMemorySort() {
    return linesPerMemorySort;
  }

  /**
   * Merges a list of intermediary sort chunk files into a single sorted file. On completion, the intermediary sort
   * chunk files are deleted.
   *
   * @param sortFiles sort chunk files to merge
   * @param sortedFileWriter writer to merge to. Can already be open and contain data
   * @param lineComparator To use when determining the order (reuse the one that was used to sort the individual
   *        files)
   */
  public void mergeSortedFiles(
      List<File> sortFiles, Writer sortedFileWriter, Comparator<String> lineComparator)
      throws IOException {
    LinkedList<Pair<String, BufferedReader>> partReaders = new LinkedList<>();
    try {
      for (File f : sortFiles) {
        // Use UTF-8 sort order.
        BufferedReader partReader =
            new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
        // Load first lines
        String partLine = partReader.readLine();
        if (partLine != null) {
          partReaders.add(Pair.of(partLine, partReader));
        }
      }
      // Sort the first lines
      Collections.sort(partReaders, (Comparator.comparing(Pair::getLeft, lineComparator)));

      // Start with the first reader
      while (partReaders.size() > 1) {
        BufferedReader currentBuffer = partReaders.get(0).getRight();
        String currentLine = partReaders.get(0).getLeft();
        String nextFilesFirstLine = partReaders.get(1).getLeft();

        // Read from it, until its value is greater than the second reader.
        while (currentLine != null
            && lineComparator.compare(currentLine, nextFilesFirstLine) <= 0) {
          sortedFileWriter.write(currentLine);
          sortedFileWriter.write('\n');

          currentLine = currentBuffer.readLine();
        }
        partReaders.remove(0);

        if (currentLine == null) {
          // If it's completed, close and remove it.
          currentBuffer.close();
        } else {
          // Otherwise, insert it into the list, maintaining the order
          Pair<String, BufferedReader> currentReaderPair = Pair.of(currentLine, currentBuffer);

          // Start at 1, as we are always larger than the first (was the second) entry
          for (int i = 1; i < partReaders.size(); i++) {
            if (lineComparator.compare(partReaders.get(i).getLeft(), currentLine) >= 0) {
              partReaders.add(i, currentReaderPair);
              break;
            }

            // If we get here, it goes at the end of the list.
            if (i + 1 == partReaders.size()) {
              partReaders.add(i + 1, currentReaderPair);
              break;
            }
          }
        }
      }

      // Read the remainder of the final buffer
      BufferedReader currentBuffer = partReaders.get(0).getRight();
      String current = partReaders.get(0).getLeft();
      while (current != null) {
        sortedFileWriter.write(current);
        sortedFileWriter.write('\n');

        current = currentBuffer.readLine();
      }
      currentBuffer.close();

    } finally {
      for (Pair<String, BufferedReader> pair : partReaders) {
        try {
          pair.getRight().close();
        } catch (RuntimeException e) {
        }
      }
      // I assume it periodically flushes anyway, so only need to do once at end...
      sortedFileWriter.flush();
      sortedFileWriter.close();
      // delete (intermediary) sort chunk files, only the sorted file remains
      for (File f : sortFiles) {
        f.delete();
      }
    }
  }

  /**
   * Sorts the input file into the output file using the supplied delimited line parameters.
   *
   * This method is not reliable when the sort field may contain Unicode codepoints outside the Basic Multilingual Plane,
   * i.e. above \uFFFF. In that case, the sort order differs from Java's String sort order.  This should not be a problem
   * for most usage; the Supplementary Multilingual Planes contain ancient scripts, emojis, arrows and so on.
   *
   * @param input To sort
   * @param sorted The sorted version of the input excluding ignored header lines (see ignoreHeaderLines)
   * @param column the column that keeps the values to sort on
   * @param columnDelimiter the delimiter that separates columns in a row
   * @param enclosedBy optional column enclosing character, e.g. a double quote for CSVs
   * @param newlineDelimiter the chars used for new lines, usually \n, \n\r or \r
   * @param ignoreHeaderLines number of beginning lines to ignore, e.g. headers
   */
  public void sort(
      File input,
      File sorted,
      String encoding,
      int column,
      String columnDelimiter,
      Character enclosedBy,
      String newlineDelimiter,
      int ignoreHeaderLines)
      throws IOException {
    sort(
        Collections.singletonList(input),
        sorted,
        encoding,
        column,
        columnDelimiter,
        enclosedBy,
        newlineDelimiter,
        ignoreHeaderLines);
  }

  /**
   * Sorts the input file into the output file using the supplied delimited line parameters.
   *
   * This method is not reliable when the sort field may contain Unicode codepoints outside the Basic Multilingual Plane,
   * i.e. above \uFFFF. In that case, the sort order differs from Java's String sort order.  This should not be a problem
   * for most usage; the Supplementary Multilingual Planes contain ancient scripts, emojis, arrows and so on.
   *
   * @param inputs To sort
   * @param sorted The sorted version of the input excluding ignored header lines (see ignoreHeaderLines)
   * @param column the column that keeps the values to sort on
   * @param columnDelimiter the delimiter that separates columns in a row
   * @param enclosedBy optional column enclosing character, e.g. a double quote for CSVs
   * @param newlineDelimiter the chars used for new lines, usually \n, \n\r or \r
   * @param ignoreHeaderLines number of beginning lines to ignore, e.g. headers
   */
  public void sort(
      List<File> inputs,
      File sorted,
      String encoding,
      int column,
      String columnDelimiter,
      Character enclosedBy,
      String newlineDelimiter,
      int ignoreHeaderLines)
      throws IOException {
    Comparator<String> lineComparator;
    if (enclosedBy == null) {
      lineComparator = new LineComparator(column, columnDelimiter);
    } else {
      lineComparator = new LineComparator(column, columnDelimiter, enclosedBy);
    }
    sort(
        inputs,
        sorted,
        encoding,
        column,
        columnDelimiter,
        enclosedBy,
        newlineDelimiter,
        ignoreHeaderLines,
        lineComparator,
        false);
  }

  /**
   * Sorts the input file into the output file using the supplied delimited line parameters.
   *
   * This method is not reliable when the sort field may contain Unicode codepoints outside the Basic Multilingual Plane,
   * i.e. above \uFFFF. In that case, the sort order differs from Java's String sort order.  This should not be a problem
   * for most usage; the Supplementary Multilingual Planes contain ancient scripts, emojis, arrows and so on.
   *
   * This method is globally synchronized, in case multiple sorts are attempted to the same file simultaneously.
   * This could be improved to allow synchronizing against the destination file, rather than for all sorts.
   *
   * @param input To sort
   * @param sorted The sorted version of the input excluding ignored header lines (see ignoreHeaderLines)
   * @param column the column that keeps the values to sort on
   * @param columnDelimiter the delimiter that separates columns in a row
   * @param enclosedBy optional column enclosing character, e.g. a double quote for CSVs
   * @param newlineDelimiter the chars used for new lines, usually \n, \r\n or \r
   * @param ignoreHeaderLines number of beginning lines to ignore, e.g. headers
   * @param lineComparator used to sort the output
   * @param ignoreCase ignore case order, this parameter couldn't have any effect if the LineComparator is used
   */
  public void sort(
      File input,
      File sorted,
      String encoding,
      int column,
      String columnDelimiter,
      Character enclosedBy,
      String newlineDelimiter,
      int ignoreHeaderLines,
      Comparator<String> lineComparator,
      boolean ignoreCase)
      throws IOException {
    sort(
        Collections.singletonList(input),
        sorted,
        encoding,
        column,
        columnDelimiter,
        enclosedBy,
        newlineDelimiter,
        ignoreHeaderLines,
        lineComparator,
        ignoreCase);
  }

  /**
   * Sorts the input file into the output file using the supplied delimited line parameters.
   *
   * This method is not reliable when the sort field may contain Unicode codepoints outside the Basic Multilingual Plane,
   * i.e. above \uFFFF. In that case, the sort order differs from Java's String sort order.  This should not be a problem
   * for most usage; the Supplementary Multilingual Planes contain ancient scripts, emojis, arrows and so on.
   *
   * This method is globally synchronized, in case multiple sorts are attempted to the same file simultaneously.
   * This could be improved to allow synchronizing against the destination file, rather than for all sorts.
   *
   * @param inputs To sort
   * @param sorted The sorted version of the input excluding ignored header lines (see ignoreHeaderLines)
   * @param column the column that keeps the values to sort on
   * @param columnDelimiter the delimiter that separates columns in a row
   * @param enclosedBy optional column enclosing character, e.g. a double quote for CSVs
   * @param newlineDelimiter the chars used for new lines, usually \n, \r\n or \r
   * @param ignoreHeaderLines number of beginning lines to ignore, e.g. headers
   * @param lineComparator used to sort the output
   * @param ignoreCase ignore case order, this parameter couldn't have any effect if the LineComparator is used
   */
  public void sort(
      List<File> inputs,
      File sorted,
      String encoding,
      int column,
      String columnDelimiter,
      Character enclosedBy,
      String newlineDelimiter,
      int ignoreHeaderLines,
      Comparator<String> lineComparator,
      boolean ignoreCase)
      throws IOException {
    LOG.debug(
        "Sorting file(s) {} as new file {}",
        inputs.stream().map(File::getAbsolutePath),
        sorted.getAbsolutePath());
    if (encoding == null) {
      LOG.warn("No encoding specified, assume UTF-8");
      encoding = FileUtils.UTF8;
    }
    synchronized (sortLock) {
      if (sorted.exists()) {
        // Delete a file, which will allow processes with it open to continue reading it.
        // The GNU sort truncates and appends, which would mean a partial read otherwise.
        LOG.warn("Deleting existed sorted file {}", sorted.getAbsoluteFile());
        sorted.delete();
      }
      // if the id is in the first column, first try sorting via shell as its the fastest we can get
      if (!sortInGnu(
          inputs,
          sorted,
          encoding,
          ignoreHeaderLines,
          column,
          columnDelimiter,
          enclosedBy,
          newlineDelimiter,
          ignoreCase)) {
        LOG.debug("No GNU sort available, using native Java sorting");
        sortInJava(inputs, sorted, encoding, lineComparator, ignoreHeaderLines);
      }
    }
  }

  /**
   * Sorts the input file into the output file using the supplied lineComparator.
   *
   * @param input To sort
   * @param sorted The sorted version of the input excluding ignored header lines (see ignoreHeaderLines)
   * @param lineComparator To use during comparison
   * @param ignoreHeaderLines number of beginning lines to ignore, e.g. headers
   */
  public void sortInJava(
      File input,
      File sorted,
      String encoding,
      Comparator<String> lineComparator,
      int ignoreHeaderLines)
      throws IOException {
    sortInJava(
        Collections.singletonList(input), sorted, encoding, lineComparator, ignoreHeaderLines);
  }

  /**
   * Sorts the input file into the output file using the supplied lineComparator.
   *
   * @param inputs To sort
   * @param sorted The sorted version of the input excluding ignored header lines (see ignoreHeaderLines)
   * @param lineComparator To use during comparison
   * @param ignoreHeaderLines number of beginning lines to ignore, e.g. headers
   */
  public void sortInJava(
      List<File> inputs,
      File sorted,
      String encoding,
      Comparator<String> lineComparator,
      int ignoreHeaderLines)
      throws IOException {
    LOG.debug("Sorting file(s) {}", inputs);
    long start = System.currentTimeMillis();

    List<File> sortFiles = new LinkedList<>();
    List<String> headerLines = new LinkedList<>();
    for (File input : inputs) {
      BufferedReader br =
          new BufferedReader(new InputStreamReader(new FileInputStream(input), encoding));
      int skipHeaderLines = ignoreHeaderLines;
      try {
        String line = br.readLine();
        int fileCount = 0;

        List<String> linesToSort = new LinkedList<>();
        while (line != null) {
          if (skipHeaderLines > 0) {
            // Only add the header lines for the first file
            if (headerLines.size() < ignoreHeaderLines) {
              headerLines.add(line);
            }
            skipHeaderLines--;
          } else {
            linesToSort.add(line);

            // if buffer is full, then sort and write to file
            if (linesToSort.size() == linesPerMemorySort) {
              sortFiles.add(sortAndWrite(input, encoding, lineComparator, fileCount, linesToSort));
              linesToSort = new LinkedList<>();
              fileCount++;
            }
          }
          line = br.readLine();
        }
        // catch the last lot
        if (!linesToSort.isEmpty()) {
          sortFiles.add(sortAndWrite(input, encoding, lineComparator, fileCount, linesToSort));
        }
      } finally {
        br.close();
      }
    }
    LOG.debug(
        sortFiles.size()
            + " sorted file chunks created in "
            + (System.currentTimeMillis() - start) / 1000
            + " secs");

    // now merge the sorted files into one single sorted file
    Writer sortedFileWriter = new BufferedWriter(new FileWriter(sorted));
    // first write the old header lines if existing
    for (String h : headerLines) {
      sortedFileWriter.write(h);
      sortedFileWriter.write("\n");
    }
    mergeSortedFiles(sortFiles, sortedFileWriter, lineComparator);

    LOG.debug(
        "Fils(s) {} sorted successfully using {} parts to do sorting in {}s",
        inputs.stream().map(File::getAbsolutePath),
        sortFiles.size(),
        (System.currentTimeMillis() - start) / 1000);
  }

  /**
   * Splits the supplied file into files of set line size and with a suffix.
   *
   * @param input To split up
   * @param linesPerOutput Lines per split file
   * @param extension The file extension to use - e.g. ".txt"
   * @return The split files
   */
  public List<File> split(File input, int linesPerOutput, String extension) throws IOException {
    LOG.debug("Splitting File[" + input.getAbsolutePath() + ']');
    long timer = System.currentTimeMillis();
    List<File> splitFiles = new LinkedList<>();
    // Use ISO-8859-1 as a binary-safe encoding.
    BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(input), StandardCharsets.ISO_8859_1));
    String line = br.readLine();
    int fileCount = 0;
    File splitFile = getChunkFile(input, fileCount);
    fileCount++;
    splitFiles.add(splitFile);
    OutputStreamWriter fw =
        new OutputStreamWriter(new FileOutputStream(splitFile), StandardCharsets.ISO_8859_1);
    try {
      int lineCount = 0;
      while (line != null) {
        if (lineCount == linesPerOutput) {
          fw.flush();
          fw.close();
          splitFile = getChunkFile(input, fileCount);
          splitFiles.add(splitFile);
          // is ok to reuse, as last one is closed, and this will always get closed - see finally
          // below
          fw = new OutputStreamWriter(new FileOutputStream(splitFile), StandardCharsets.ISO_8859_1);
          fileCount++;
          lineCount = 0;
        }
        fw.write(line);
        fw.write("\n");
        line = br.readLine();
        lineCount++;
      }
      fw.flush();
    } finally {
      fw.close();
    }
    LOG.debug(
        "File["
            + input.getAbsolutePath()
            + "] split successfully into["
            + splitFiles.size()
            + "] parts in secs["
            + (1 + System.currentTimeMillis() - timer) / 1000
            + "]");
    return splitFiles;
  }

  /**
   * Test whether we have a new enough version of GNU Sort that supports (primarily) the -k option with a start and end
   * column.
   *
   * Mac OS only includes an old version of GNU sort, and will fail this test.
   */
  private boolean gnuSortAvailable() {
    if (gnuSortAvailable != null) {
      return gnuSortAvailable;
    }

    try {
      String command = "sort -k1,1 -t',' --ignore-case /dev/null";
      LOG.debug("Testing capability of 'sort' with command: {}", command);

      Process process = new ProcessBuilder("/bin/sh", "-c", command).start();
      int exitValue = process.waitFor();

      if (exitValue == 0) {
        LOG.debug("GNU sort is capable");
        gnuSortAvailable = true;
      } else {
        LOG.warn(
            "GNU sort does not exist or is too old, and will not be used.  Sorting large files will be slow.",
            new InputStreamUtils().readEntireStream(process.getErrorStream()).replace('\n', ' '));
        gnuSortAvailable = false;
      }
    } catch (Exception e) {
      LOG.warn(
          "GNU sort does not exist or is too old, and will not be used.  Sorting large files will be slow.",
          e);
      gnuSortAvailable = false;
    }

    return gnuSortAvailable;
  }

  /**
   * sort a text file via an external GNU sort command:
   * sorting tabs at 3rd column, numerical reverse order
   * sort -t$'\t' -k3 -o sorted.txt col2007.txt
   * <p/>
   * The GNU sort based sorting is extremely efficient and much, much faster than the current sortInJava method. It is
   * locale aware though and we only want the native C sorting locale. See
   * http://www.gnu.org/software/coreutils/faq/coreutils-faq.html#Sort-does-not-sort-in-normal-order_0021
   * <p/>
   * Example C sort order:
   * <p/>
   * <pre>
   * 1 oOdontoceti
   * 10 gGlobicephala melaena melaena Traill
   * 100 gGlobicephala melaena melaena Traill
   * 101 gGlobicephala melaena melaena Traill
   * 11 pPontoporia Gray
   * 12 pPontoporia blainvillei Gervais and d'Orbigny
   * 120 iInia d'Orbigny
   * 121 iInia geoffrensis Blainville
   * 2 sSusuidae
   * 20 cCetacea
   * Amphiptera
   * Amphiptera pacifica Giglioli
   * Anarnak Lacépède
   * Balaena mangidach Chamisso
   * amphiptera
   * amphiptera pacifica Giglioli
   * anarnak Lacépède
   * balaena mangidach Chamisso
   * </pre>
   */
  protected boolean sortInGnu(
      List<File> inputs,
      File sorted,
      String encoding,
      int ignoreHeaderLines,
      int column,
      String columnDelimiter,
      Character enclosedBy,
      String lineDelimiter,
      boolean ignoreCase)
      throws IOException {
    String command;
    // GNU sort is available for use when:
    // • line delimiter is \n
    // • no enclosed by/quote character is in use
    // • sorting is using the first column
    // • sort version is sufficient to include start and end column (-k 1,1).
    // Use the --debug option to sort if working on this code.
    if (lineDelimiter == null || !lineDelimiter.contains("\n")) {
      LOG.debug("Cannot use GNU sort on this file: line delimiter does not contain newline.");
      return false;
    } else if (columnDelimiter != null && column > 0) {
      LOG.debug("Cannot use GNU sort on this file: sort column is not the first.");
      return false;
    } else if (enclosedBy != null) {
      LOG.debug("Cannot use GNU sort on this file: enclosed by character set.");
      return false;
    } else if (!gnuSortAvailable()) {
      LOG.debug("Cannot use GNU sort on this file: command unavailable.");
      return false;
    }

    // keep header rows
    boolean success = false;
    try {
      LinkedList<String> cmds = new LinkedList<>();
      cmds.add("/bin/sh");
      cmds.add("-c");
      cmds.add("");
      ProcessBuilder pb = new ProcessBuilder(cmds);
      Map<String, String> env = pb.environment();

      // clear the environment, but keep specified temp working directory
      env.keySet().removeIf(key -> !(key.equals("TMPDIR")));
      if (System.getProperty("java.io.tmpdir") != null) {
        env.put("TMPDIR", System.getProperty("java.io.tmpdir"));
      }
      // make sure we use the C locale for sorting
      env.put("LC_ALL", "C");

      String sortArgs =
          String.format(
              " %s -k%d,%d -t'%s'",
              ignoreCase ? "--ignore-case" : "", column + 1, column + 1, columnDelimiter);

      String fileList = inputs.stream().map(File::getAbsolutePath).collect(Collectors.joining(" "));
      if (ignoreHeaderLines > 0) {
        // copy header lines
        command =
            "head -n "
                + ignoreHeaderLines
                + ' '
                + inputs.get(0).getAbsolutePath()
                + " > "
                + sorted.getAbsolutePath();
        LOG.debug("Issue external command: {}", command);
        cmds.removeLast();
        cmds.add(command);
        Process process = pb.start();
        int exitValue = process.waitFor();
        if (exitValue != 0) {
          LOG.warn("Error sorting file (copying header lines) with GNU head");
          return false;
        }

        // do the sorting ignoring the header rows
        command =
            "tail -q -n +"
                + (ignoreHeaderLines + 1)
                + " "
                + fileList
                + " | "
                + "sort "
                + sortArgs
                + " >> "
                + sorted.getAbsolutePath();
      } else {
        // do sorting directly, we don't have header rows
        command = "sort " + sortArgs + " -o " + sorted.getAbsolutePath() + ' ' + fileList;
      }

      LOG.debug("Issue external command: {}", command);
      cmds.removeLast();
      cmds.add(command);
      Process process = pb.start();
      // get the stdout and stderr from the command that was run
      InputStream err = process.getErrorStream();
      int exitValue = process.waitFor();
      if (exitValue == 0) {
        LOG.debug("Successfully sorted file with GNU sort");
        success = true;
      } else {
        LOG.warn("Error sorting file with GNU sort");
        InputStreamUtils isu = new InputStreamUtils();
        System.err.append(isu.readEntireStream(err));
      }
    } catch (Exception e) {
      LOG.warn("Caught Exception using GNU sort", e);
    }
    return success;
  }

  /**
   * Sorts the lines and writes to file using the
   *
   * @param input File to base the name on
   * @param lineComparator To compare the lines for sorting
   * @param fileCount Used for the file name
   * @param linesToSort To actually sort
   * @return The written file
   */
  private File sortAndWrite(
      File input,
      String encoding,
      Comparator<String> lineComparator,
      int fileCount,
      List<String> linesToSort)
      throws IOException {
    long start = System.currentTimeMillis();
    linesToSort.sort(lineComparator);
    // When implementing a comparator, make it SUPER quick!!!
    LOG.debug(
        "Collections.sort took msec["
            + (System.currentTimeMillis() - start)
            + "] to sort records["
            + linesToSort.size()
            + ']');
    File sortFile = getChunkFile(input, fileCount);
    try (Writer fw = new OutputStreamWriter(new FileOutputStream(sortFile), encoding)) {
      for (String s : linesToSort) {
        fw.write(s);
        fw.write("\n");
      }
    }
    return sortFile;
  }

  /**
   * Creates an empty file or updates the last updated timestamp on the same as the unix command of
   * the same name.
   *
   * <p>From Guava.
   *
   * @param file the file to create or update
   * @throws IOException if an I/O error occurs
   */
  public static void touch(File file) throws IOException {
    Objects.requireNonNull(file);
    if (!file.createNewFile() && !file.setLastModified(System.currentTimeMillis())) {
      throw new IOException("Unable to update modification time of " + file);
    }
  }

  /**
   * Returns the <a href="http://en.wikipedia.org/wiki/Filename_extension">file extension</a> for
   * the given file name, or the empty string if the file has no extension. The result does not
   * include the '{@code .}'.
   *
   * <p><b>Note:</b> This method simply returns everything after the last '{@code .}' in the file's
   * name as determined by {@link File#getName}. It does not account for any filesystem-specific
   * behavior that the {@link File} API does not already account for. For example, on NTFS it will
   * report {@code "txt"} as the extension for the filename {@code "foo.exe:.txt"} even though NTFS
   * will drop the {@code ":.txt"} part of the name when the file is actually created on the
   * filesystem due to NTFS's <a href="https://goo.gl/vTpJi4">Alternate Data Streams</a>.
   *
   * <p>From Guava.
   */
  public static String getFileExtension(String fullName) {
    Objects.requireNonNull(fullName);
    String fileName = new File(fullName).getName();
    int dotIndex = fileName.lastIndexOf('.');
    return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
  }

  /**
   * Creates any necessary but nonexistent parent directories of the specified file. Note that if
   * this operation fails it may have succeeded in creating some (but not all) of the necessary
   * parent directories.
   *
   * <p>From Guava.
   *
   * @throws IOException if an I/O error occurs, or if any necessary but nonexistent parent
   *     directories of the specified file could not be created.
   */
  public static void createParentDirs(File file) throws IOException {
    Objects.requireNonNull(file);
    File parent = file.getCanonicalFile().getParentFile();
    if (parent == null) {
      /*
       * The given directory is a filesystem root. All zero of its ancestors exist. This doesn't
       * mean that the root itself exists -- consider x:\ on a Windows machine without such a drive
       * -- or even that the caller can create it, but this method makes no such guarantees even for
       * non-root files.
       */
      return;
    }
    //noinspection ResultOfMethodCallIgnored
    parent.mkdirs();
    if (!parent.isDirectory()) {
      throw new IOException("Unable to create parent directories of " + file);
    }
  }
}
