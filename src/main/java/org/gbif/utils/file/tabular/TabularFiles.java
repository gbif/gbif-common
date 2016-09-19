package org.gbif.utils.file.tabular;

import java.io.InputStream;
import java.nio.charset.Charset;

import com.google.common.base.Charsets;
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
  public static TabularDataFileReader newTabularFileReader(InputStream in, char quoteChar, char delimiterChar,
                                                           String endOfLineSymbols, Charset charset, boolean headerLine){
    return new SuperCsvFileReader(in, quoteChar, delimiterChar, endOfLineSymbols, charset, true);
  }


  /**
   * Get a new TabularDataFileReader using UTF-8 charset and default endOfLineSymbols.
   *
   * @param in
   * @param quoteChar
   * @param delimiterChar
   * @param headerLine
   */
  public static TabularDataFileReader newTabularFileReader(InputStream in, char quoteChar, char delimiterChar,
                                                           boolean headerLine){
    return newTabularFileReader( in, quoteChar, delimiterChar, CsvPreference.STANDARD_PREFERENCE.getEndOfLineSymbols(),
            Charsets.UTF_8, headerLine);
  }

}
