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

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
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
   * @throws ParseException
   */
  T read() throws IOException, ParseException;

  /**
   * The line number of where the last record returned by {@link #read()} starts.
   * If no records have been returned yet this method is expected to return 0.
   * Once {@link #read()} returned null, this methods will return the number of the last line in the file.
   * Note that if the very last line is an empty line with only an endline character it will not be counted.
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
