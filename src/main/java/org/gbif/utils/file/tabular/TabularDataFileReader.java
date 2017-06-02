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
   * Read a line of the tabular data file.
   *
   * @return the next line of the tabular data file or null if the end of the file is reached.
   * @throws IOException
   */
  T read() throws IOException;

}
