package org.gbif.utils.file.tabular;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Interface defining a reader for tabular data file (e.g. CSV)
 */
public interface TabularDataFileReader<T> extends Closeable {

  /**
   * Get the header line of the tabular data file (if possible).
   *
   * @return headers or null
   */
  List<String> getHeaderLine() throws IOException;

  /**
   * Read a non-empty line of the tabular data file.
   * An empty line represents a line with no printable characters. A line with only the defined separators is
   * expected to be returned.
   *
   * @return the next line of the tabular data file or null if the end of the file is reached.
   *
   * @throws IOException
   */
  T read() throws IOException;


  /**
   * The line number of where the last record returned by {@link #read()} starts.
   * If no records have been returned yet this method is expected to return 0.
   * Line number includes header line and empty lines (if applicable).
   *
   * @return line number of where the last record starts
   */
  long getLastRecordLineNumber();

  /**
   * Represents the number of record returned by the {@link #read()} method.
   * If no records have been returned yet this method is expected to return 0.
   * A record can span over multiple line.
   *
   * @return
   */
  long getLastRecordNumber();

}
