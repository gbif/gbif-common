package org.gbif.utils.file.tabular;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Reader for tabular data file (e.g. CSV)
 */
public class TabularDataFileReader implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(TabularDataFileReader.class);

  private CsvListReader csvListReader;
  private boolean headerLineSkipped = false;

  /**
   * Get a new TabularDataFileReader using UTF-8 charset and default endOfLineSymbols.
   * @param in
   * @param quoteChar
   * @param delimiterChar
   * @param headerLine
   */
  public TabularDataFileReader(InputStream in, char quoteChar, char delimiterChar, boolean headerLine){
    this( in, quoteChar, delimiterChar, CsvPreference.STANDARD_PREFERENCE.getEndOfLineSymbols(), Charsets.UTF_8, headerLine);
  }

  /**
   *
   * @param in
   * @param quoteChar
   * @param delimiterChar
   * @param endOfLineSymbols
   * @param charset
   * @param headerLine
   */
  public TabularDataFileReader(InputStream in, char quoteChar, char delimiterChar, String endOfLineSymbols,
                               Charset charset, boolean headerLine){

    Preconditions.checkNotNull(in, "An InputStream must be provided");

    CsvPreference.Builder builder = new CsvPreference.Builder(quoteChar, delimiterChar, endOfLineSymbols)
            .ignoreEmptyLines(true);
    csvListReader = new CsvListReader(new InputStreamReader(in, charset), builder.build());
    headerLineSkipped = !headerLine;
  }

  /**
   * Read a line of the tabular data file
   * @return the line as List or null if the end of the file is reached.
   * @throws IOException
   */
  public List<String> read() throws IOException {
    if(headerLineSkipped){
      return csvListReader.read();
    }
    csvListReader.read();
    headerLineSkipped = true;
    return csvListReader.read();
  }

  @Override
  public void close() {
    if(csvListReader != null){
      try {
        csvListReader.close();
      } catch (IOException e) {
        LOG.warn("Exception while closing tabular data file", e);
      }
    }
  }
}
