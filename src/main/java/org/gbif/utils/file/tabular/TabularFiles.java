package org.gbif.utils.file.tabular;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import org.supercsv.prefs.CsvPreference;

/**
 * Static utility methods related to {@link TabularDataFileReader} instances.
 *
 */
public class TabularFiles {


  /**
   * Get a new TabularDataFileReader.
   *
   * @param in
   * @param quoteChar
   * @param delimiterChar
   * @param endOfLineSymbols
   * @param charset
   * @param headerLine
   * @return
   */
  public static TabularDataFileReader<List<String>> newTabularFileReader(InputStream in, char quoteChar, char delimiterChar,
                                                           String endOfLineSymbols, Charset charset, boolean headerLine){

    Preconditions.checkNotNull(in, "An InputStream must be provided");
    Preconditions.checkNotNull(charset, "A Charset must be provided");
    return new SuperCsvFileReader(in, quoteChar, delimiterChar, endOfLineSymbols, charset, true);
  }


  /**
   * Get a new TabularDataFileReader using UTF-8 charset, default quote char and default endOfLineSymbols.
   *
   * @param in
   * @param delimiterChar
   * @param headerLine
   */
  public static TabularDataFileReader<List<String>> newTabularFileReader(InputStream in, char delimiterChar,
                                                                         boolean headerLine){
    return newTabularFileReader(in, CsvPreference.STANDARD_PREFERENCE.getQuoteChar(),
            delimiterChar, CsvPreference.STANDARD_PREFERENCE.getEndOfLineSymbols(), Charsets.UTF_8, headerLine);
  }

}
