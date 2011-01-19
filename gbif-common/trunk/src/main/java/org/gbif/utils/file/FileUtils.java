/**
 *
 */
package org.gbif.utils.file;

import org.gbif.utils.collection.CompactHashSet;
import org.gbif.utils.text.LineComparator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Collection of file utils.
 * 
 * @author timrobertson
 * @author Markus Döring
 */
public class FileUtils {
  public static final Pattern TAB_DELIMITED = Pattern.compile("\t");
  public static final String UTF8 = "UTF8";
  private static Logger log = LoggerFactory.getLogger(FileUtils.class);
  private static int linesPerMemorySort = 100000;

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
   * Reads a file and returns a unique set of multiple columns from lines which are no comments (starting with #) and trims whitespace
   * 
   * @param source the UTF-8 encoded text file with tab delimited columns
   * @param resultSet the set implementation to be used. Will not be cleared before reading!
   * @param column variable length argument of column indices to process
   * @return set of column rows
   * @throws IOException
   */
  public static Set<String> columnsToSet(InputStream source, Set<String> resultSet, int... column) throws IOException {
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

  /**
   * escapes a filename so it is a valid filename on all systems, replacing /. .. \t\r\n
   * 
   * @param filename to be escaped
   * @return
   */
  public static String escapeFilename(String filename) {
    return filename.replaceAll("[\\s./&]", "_");
  }

  /**
   * For the given file's path, returns a proposed new filename (including path) with the extension
   * index and suffix
   * So a file of "/tmp/input.txt" -> "/tmp/input_part_10.txt"
   * 
   * @param input File
   * @param suffix E.g. part
   * @param index E.g. 10
   * @return The proposed name
   */
  private static File getChunkFile(File original, int index) {
    return new File(original.getParentFile(), FilenameUtils.getBaseName(original.getName()) + "_" + index
        + FilenameUtils.getExtension(original.getName()));
  }

  public static File getClasspathFile(String path) {
    return new File(ClassLoader.getSystemResource(path).getFile());
  }

  public static InputStream getInputStream(File source) throws FileNotFoundException {
    return new FileInputStream(source);
  }

  public static Reader getInputStreamReader(InputStream input) throws FileNotFoundException {
    return getInputStreamReader(input, UTF8);
  }

  public static Reader getInputStreamReader(InputStream input, String encoding) throws FileNotFoundException {
    Reader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(input, encoding));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return reader;
  }

  /**
   * @param source the source input stream encoded in UTF8
   * @return
   */
  public static LineIterator getLineIterator(InputStream source) {
    return getLineIterator(source, UTF8);
  }

  /**
   * @param source the source input stream
   * @param encoding the encoding used by the input stream
   * @return
   * @throws UnsupportedEncodingException
   */
  public static LineIterator getLineIterator(InputStream source, String encoding) {
    try {
      return new LineIterator(new BufferedReader(new InputStreamReader(source, encoding)));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Unsupported encoding" + encoding, e);
    }
  }

  public static Reader getUtf8Reader(File file) throws FileNotFoundException {
    Reader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return reader;
  }

  private static boolean ignore(String line) {
    if (StringUtils.trimToNull(line) == null || line.startsWith("#")) {
      return true;
    }
    return false;
  }

  public static boolean isCompressedFile(File source) {
    String suffix = source.getName().substring(source.getName().lastIndexOf(".") + 1);
    if (suffix != null && suffix.length() > 0) {
      if (suffix.equalsIgnoreCase("zip")) {
        // try zip
        return true;
      } else if (suffix.equalsIgnoreCase("tgz") || suffix.equalsIgnoreCase("gz")) {
        // try gzip
        return true;
      }
    }
    return false;
  }

  /**
   * For the given list, finds the index of the lowest value using the given comparator
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
      if (lowestValue != null) {
        if (comparator.compare(lowestValue, value) > 0) {
          lowestValue = value;
          index = i;
        }
      } else {
        lowestValue = value;
        index = i;
      }
    }

    if (lowestValue != null) {
      return index;
    } else {
      return -1;
    }

  }

  /**
   * Reads a complete file into a byte buffer
   * 
   * @param file
   * @return
   * @throws IOException
   */
  public static ByteBuffer readByteBuffer(File file) throws IOException {
    byte[] content = org.apache.commons.io.FileUtils.readFileToByteArray(file);
    ByteBuffer bbuf = ByteBuffer.wrap(content);
    return bbuf;
  }

  /**
   * Reads the first bytes of a file into a byte buffer
   * 
   * @param file
   * @param bufferSize the number of bytes to read from the file
   * @return
   * @throws IOException
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
   *        segments to be sorted when doing a java file sort. Defaults to 100000, if you have memory available a higher
   *        value increases
   *        performance.
   */
  public static void setLinesPerMemorySort(int linesPerMemorySort) {
    FileUtils.linesPerMemorySort = linesPerMemorySort;
  }

  public static Writer startNewUtf8File(File file) throws IOException {
    Writer writer = null;
    org.apache.commons.io.FileUtils.touch(file);
    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), UTF8));
    return writer;
  }

  public static Writer startNewUtf8XmlFile(File file) throws IOException {
    Writer writer = startNewUtf8File(file);
    writer.write("<?xml version='1.0' encoding='utf-8'?>\n");
    return writer;
  }

  public static LinkedList<String> streamToList(InputStream source) throws IOException {
    return streamToList(source, "UTF-8");
  }

  /**
   * Reads a file and returns a list of all lines which are no comments (starting with #) and trims whitespace
   * 
   * @param source the UTF-8 encoded text file to read
   * @param resultList the list implementation to be used. Will not be cleared before reading!
   * @return list of lines
   * @throws IOException
   */
  public static List<String> streamToList(InputStream source, List<String> resultList) throws IOException {
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

  public static LinkedList<String> streamToList(InputStream source, String encoding) throws IOException {
    LinkedList<String> resultList = new LinkedList<String>();
    try {
      LineIterator lines = new LineIterator(new BufferedReader(new InputStreamReader(source, encoding)));
      while (lines.hasNext()) {
        String line = lines.nextLine();
        resultList.add(line);
      }
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Unsupported encoding " + encoding, e);
    }
    return resultList;
  }

  public static Map<String, String> streamToMap(InputStream source) throws IOException {
    return streamToMap(source, new HashMap<String, String>());
  }

  public static Map<String, String> streamToMap(InputStream source, int key, int value, boolean trimToNull) throws IOException {
    return streamToMap(source, new HashMap<String, String>(), key, value, trimToNull);
  }

  /**
   * Read a hashmap from a tab delimited file using the row number as an integer value, ignoring commented rows starting with #
   * 
   * @param source tab delimited text file to read
   * @param key column number to use as key
   * @param value column number to use as value
   * @return
   * @throws IOException
   */
  public static Map<String, String> streamToMap(InputStream source, Map<String, String> result) throws IOException {
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
   * Read a hashmap from a tab delimited file, ignoring commented rows starting with #
   * 
   * @param source tab delimited text file to read
   * @param key column number to use as key
   * @param value column number to use as value
   * @param trimToNull if true trims map entries to null
   * 
   * @return
   * @throws IOException
   */
  public static Map<String, String> streamToMap(InputStream source, Map<String, String> result, int key, int value, boolean trimToNull) throws IOException {
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
    return streamToSet(source, new CompactHashSet<String>());
  }

  /**
   * Reads a file and returns a unique set of all lines which are no comments (starting with #) and trims whitespace
   * 
   * @param source the UTF-8 encoded text file to read
   * @param resultSet the set implementation to be used. Will not be cleared before reading!
   * @return set of unique lines
   * @throws IOException
   */
  public static Set<String> streamToSet(InputStream source, Set<String> resultSet) throws IOException {
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
    String protocol = url.getProtocol() == null || url.getProtocol().equalsIgnoreCase("http") ? "" : "/__" + url.getProtocol() + "__";
    String domain = url.getAuthority() == null ? "__domainless" : url.getAuthority();
    return domain + protocol + url.getFile();
  }

  public int getLinesPerMemorySort() {
    return linesPerMemorySort;
  }

  /**
   * Merges the sorted files
   * 
   * @param sortFiles To merge
   * @param sortedFileWriter writer to merge to. Can already be open and contain data
   * @param lineComparator To use when determining the order (reuse the one that was used to sort the individual files)
   * @throws IOException
   */
  public void mergedSortedFiles(List<File> sortFiles, FileWriter sortedFileWriter, Comparator<String> lineComparator)
      throws IOException {
    List<BufferedReader> partReaders = new LinkedList<BufferedReader>();
    try {
      List<String> partReaderLine = new LinkedList<String>();
      for (File f : sortFiles) {
        partReaders.add(new BufferedReader(new FileReader(f)));
      }
      boolean moreData = false;
      // load first line in
      for (BufferedReader partReader : partReaders) {
        String partLine = partReader.readLine();
        if (partLine != null) {
          moreData = true;
        }
        // we still add the "null" to keep the partReaders and partLineReader indexes in sync - ALWAYS
        partReaderLine.add(partLine);
      }
      // keep going until all readers are exhausted
      while (moreData) {
        int index = FileUtils.lowestValueIndex(partReaderLine, lineComparator);
        if (index >= 0) {
          sortedFileWriter.write(partReaderLine.get(index));
          sortedFileWriter.write("\n");
          BufferedReader r = partReaders.get(index);
          String partLine = r.readLine();
          synchronized (partReaderLine) {
            partReaderLine.add(index, partLine);
            partReaderLine.remove(index + 1);
          }
        } else {
          moreData = false;
        }
      }
    } finally {
      for (BufferedReader b : partReaders) {
        try {
          b.close();
        } catch (RuntimeException e) {
        }
      }
      // I assume it periodically flushes anyway, so only need to do once at end...
      sortedFileWriter.flush();
      sortedFileWriter.close();
    }
  }

  /**
   * Sorts the input file into the output file using the supplied delimited line parameters.
   * The resulting rows will be sorted according to the @See UnixSortComparator with values taken from the specified column.
   * 
   * @param input To sort
   * @param sorted The sorted version of the input excluding ignored header lines (see ignoreHeaderLines)
   * @param column the column that keeps the values to sort on
   * @param columnDelimiter the delimiter that seperates columns in a row
   * @param enclosedBy optional column enclosing character, e.g. a double quote for CSVs
   * @param newlineDelimiter the chars used for new lines, usually \n, \n\r or \r
   * @param ignoreHeaderLines number of beginning lines to ignore, e.g. headers
   * @throws IOException
   */
  public void sort(File input, File sorted, String encoding, int column, String columnDelimiter, Character enclosedBy,
      String newlineDelimiter, int ignoreHeaderLines) throws IOException {
    log.debug("sorting " + input.getAbsolutePath() + " as new file " + sorted.getAbsolutePath());
    if (encoding == null) {
      log.warn("No encoding specified, assume UTF-8");
      encoding = "UTF-8";
    }
    // if the id is in the first column, first try sorting via unix shell as its the fastest we can get
    if (!sortInUnix(input, sorted, encoding, ignoreHeaderLines, column, columnDelimiter, newlineDelimiter)) {
      // not first column or doesnt work - maybe running on windows. Do native java sorting
      log.debug("No unix sort available, using native java sorting");
      Comparator<String> lineComparator;
      if (enclosedBy == null) {
        lineComparator = new LineComparator(column, columnDelimiter);
      } else {
        lineComparator = new LineComparator(column, columnDelimiter, enclosedBy);
      }
      sortInJava(input, sorted, encoding, lineComparator, ignoreHeaderLines);
    }
  }

  /**
   * Sorts the lines and writes to file using the
   * 
   * @param input File to base the name on
   * @param suffix to use as the extension for the intermediate chunk files
   * @param lineComparator To compare the lines for sorting
   * @param fileCount Used for the file name
   * @param linesToSort To actually sort
   * @return The written file
   * @throws IOException
   */
  private File sortAndWrite(File input, String encoding, Comparator<String> lineComparator, int fileCount, List<String> linesToSort)
      throws IOException {
    Collections.sort(linesToSort, lineComparator);
    // When implementing a comparator, make it SUPER quick!!!
    // log.debug("Collections.sort took msec[" + (System.currentTimeMillis() - timer) + "] to sort records[" +
// linesToSort.size() + "]");
    File sortFile = FileUtils.getChunkFile(input, fileCount);
    Writer fw = new OutputStreamWriter(new FileOutputStream(sortFile), encoding);
    try {
      for (String s : linesToSort) {
        fw.write(s);
        fw.write("\n");
      }
    } finally {
      fw.close();
    }
    return sortFile;
  }

  /**
   * Sorts the input file into the output file using the supplied lineComparator
   * 
   * @param input To sort
   * @param output The sorted version of the input excluding ignored header lines (see ignoreHeaderLines)
   * @param linesPerMemorySort number of lines that will be written in memory before flushed to disk
   * @param extension Of the temporary files (suggest "sort")
   * @param lineComparator To use during comparison
   * @param ignoreHeaderLines number of beginning lines to ignore, e.g. headers
   * @throws IOException
   */
  public void sortInJava(File input, File sorted, String encoding, Comparator<String> lineComparator, int ignoreHeaderLines)
      throws IOException {
    log.debug("Sorting File[" + input.getAbsolutePath() + "]");
    long timer = System.currentTimeMillis();
    List<File> sortFiles = new LinkedList<File>();
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), encoding));
    int fileCount;
    List<String> headerLines = new LinkedList<String>();
    List<String> linesToSort;
    try {
      String line = br.readLine();
      fileCount = 0;

      linesToSort = new LinkedList<String>();
      while (line != null) {
        if (ignoreHeaderLines > 0) {
          headerLines.add(line);
          ignoreHeaderLines--;
        } else {
          linesToSort.add(line);

          // if buffer is full, then sort and write to file
          if (linesToSort.size() == linesPerMemorySort) {
            sortFiles.add(sortAndWrite(input, encoding, lineComparator, fileCount, linesToSort));
            linesToSort = new LinkedList<String>();
            fileCount++;
          }
        }
        line = br.readLine();
      }
      // catch the last lot
      if (linesToSort.size() > 0) {
        sortFiles.add(sortAndWrite(input, encoding, lineComparator, fileCount, linesToSort));
      }

    } finally {
      br.close();
    }

    // now merge the sorted files into one single sorted file
    FileWriter sortedFileWriter = new FileWriter(sorted);
    // first write the old header lines if existing
    for (String h : headerLines) {
      sortedFileWriter.write(h);
      sortedFileWriter.write("\n");
    }
    mergedSortedFiles(sortFiles, sortedFileWriter, lineComparator);

    log.debug("File " + input.getAbsolutePath() + " sorted successfully using " + sortFiles.size()
        + " parts to do sorting in " + (1 + System.currentTimeMillis() - timer) / 1000 + " secs");
  }

  /**
   * sort a text file via an external unix sort command:
   * sorting tabs at 3rd column, numerical reverse order
   * sort -t$'\t' -k3 -o sorted.txt col2007.txt
   * 
   * The unix based sorting is extremely efficient and much, much faster than the current sortInJava method.
   * It is locale aware though and we only want the native C sorting locale.
   * See http://www.gnu.org/software/coreutils/faq/coreutils-faq.html#Sort-does-not-sort-in-normal-order_0021
   * 
   * Example C sort oder:
   * 
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
   * ånarnak Lacépède
   * 
   * 
   * 
   * @param input
   * @param sorted
   * @param column
   * @param columnDelimiter
   * @param enclosedBy
   * @param ignoreHeaderLines
   * @throws IOException
   */
  protected boolean sortInUnix(File input, File sorted, String encoding, int ignoreHeaderLines, int column, String columnDelimiter,
      String lineDelimiter) throws IOException {
    boolean success = false;
    String command;
    // disable unix sorting for now - behaves differently on various OSes
    if (column != 0 || lineDelimiter == null || !lineDelimiter.contains("\n") || columnDelimiter.contains("\n")) {
      log.debug("Cannot use unix sort on this file");
      return false;
    }
    // keep header rows
    try {
      LinkedList<String> cmds = new LinkedList<String>();
      cmds.add("/bin/sh");
      cmds.add("-c");
      cmds.add("");
      ProcessBuilder pb = new ProcessBuilder(cmds);
      Map<String, String> env = pb.environment();
      env.clear();
      // make sure we use the C locale for sorting
      env.put("LC_ALL", "C");
      if (ignoreHeaderLines > 0) {
        // use
        command = "head -n " + ignoreHeaderLines + " " + input.getAbsolutePath() + " > " + sorted.getAbsolutePath();
        log.debug("Issue unix sort cmd: " + command);
        cmds.removeLast();
        cmds.add(command);
        Process process = pb.start();
        process.waitFor();

        // do the sorting ignoring the header rows
        command = "sed " + ignoreHeaderLines + "d " + input.getAbsolutePath() + " | sort >> "
            + sorted.getAbsolutePath();
      } else {
        // do sorting directly, we dont have header rows
        command = "sort -o " + sorted.getAbsolutePath() + " " + input.getAbsolutePath();
      }

      log.debug("Issue unix sort cmd: " + command);
      cmds.removeLast();
      cmds.add(command);
      Process process = pb.start();
      // get the stdout and stderr from the command that was run
      InputStream err = process.getErrorStream();
      int exitValue = process.waitFor();
      if (exitValue == 0) {
        log.debug("Successfully sorted file with unix sort");
        success = true;
      } else {
        log.warn("Error sorting file with unix sort");
        InputStreamUtils isu = new InputStreamUtils();
        System.err.append(isu.readEntireStream(err));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return success;
  }

  /**
   * Splits the supplied file into files of set line size and with a suffix
   * 
   * @param input To split up
   * @param linesPerOutput Lines per split file
   * @param extension The file extension to use - e.g. ".txt"
   * @return The split files
   * @throws IOException
   */
  public List<File> split(File input, int linesPerOutput, String extension) throws IOException {
    log.debug("Splitting File[" + input.getAbsolutePath() + "]");
    long timer = System.currentTimeMillis();
    List<File> splitFiles = new LinkedList<File>();
    BufferedReader br = new BufferedReader(new FileReader(input));
    String line = br.readLine();
    int lineCount = 0;
    int fileCount = 0;
    File splitFile = FileUtils.getChunkFile(input, fileCount);
    fileCount++;
    splitFiles.add(splitFile);
    FileWriter fw = new FileWriter(splitFile);
    try {
      while (line != null) {
        if (lineCount == linesPerOutput) {
          fw.flush();
          fw.close();
          splitFile = FileUtils.getChunkFile(input, fileCount);
          splitFiles.add(splitFile);
          // is ok to reuse, as last one is closed, and this will always get closed - see finally below
          fw = new FileWriter(splitFile);
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
    log.debug("File[" + input.getAbsolutePath() + "] split successfully into[" + splitFiles.size() + "] parts in secs["
        + (1 + System.currentTimeMillis() - timer) / 1000 + "]");
    return splitFiles;
  }

}
