package org.gbif.utils.file.tabular;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Internal {@link TabularDataFileReader} implementation backed by Super CSV.
 */
class SuperCsvFileReader implements TabularDataFileReader<List<String>> {

  //CsvListReader was chosen to allow the reading of a line with more (or less) data than declared (by the headers).
  private final CsvListReader csvListReader;
  private final boolean headerLineIncluded;

  private List<String> headerLine;
  private boolean headerLineRead = false;

  /**
   * Package protected constructor.
   *
   * @param in
   * @param quoteChar
   * @param delimiterChar
   * @param endOfLineSymbols
   * @param charset
   * @param headerLineIncluded
   */
  SuperCsvFileReader(InputStream in, Character quoteChar, char delimiterChar, String endOfLineSymbols,
                            Charset charset, boolean headerLineIncluded){

    Objects.requireNonNull(in, "InputStream shall be provided");
    Objects.requireNonNull(endOfLineSymbols, "endOfLineSymbols shall be provided");

    CsvPreference.Builder builder = new CsvPreference.Builder(quoteChar == null ?
            CsvPreference.STANDARD_PREFERENCE.getQuoteChar() : quoteChar, delimiterChar, endOfLineSymbols)
            .ignoreEmptyLines(true);

    //that doesn't work
    if(quoteChar == null){
      builder = builder.useQuoteMode((s, csvContext, csvPreference) -> false);
    }
    csvListReader = new CsvListReader(new InputStreamReader(in, charset), builder.build());
    this.headerLineIncluded = headerLineIncluded;
    headerLineRead = !headerLineIncluded;
  }

  @Override
  public List<String> getHeaderLine() throws IOException {
    if (headerLineIncluded && !headerLineRead) {
      headerLine = csvListReader.read();
      headerLineRead = true;
    }
    return headerLine;
  }

  /**
   * Read a line of the tabular data file.
   *
   * @return the line as List or null if the end of the file is reached.
   * @throws IOException
   */
  public List<String> read() throws IOException {
    if (headerLineRead) {
      return csvListReader.read();
    }
    headerLine = csvListReader.read();
    headerLineRead = true;
    return csvListReader.read();
  }

  @Override
  public long getLastRecordLineNumber() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getLastRecordNumber() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws IOException {
    if (csvListReader != null) {
        csvListReader.close();
    }
  }
}
