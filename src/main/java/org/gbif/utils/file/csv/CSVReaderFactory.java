/***************************************************************************
 * Copyright 2010-2015 Global Biodiversity Information Facility Secretariat
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/
package org.gbif.utils.file.csv;

import org.gbif.utils.file.CharsetDetection;
import org.gbif.utils.file.UnkownCharsetException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CSVReaderFactory {

  private static final Logger LOG = LoggerFactory.getLogger(CSVReaderFactory.class);
  private static final String[] POTENTIAL_DELIMITERS = {",", "\t", ";", "|"};
  private static final int ROWS_TO_INSPECT = 10;

  /**
   * Data about the CSV file
   */
  public static class CSVMetadata {
    String delimiter;
    Character quotedBy;

    public String getDelimiter() {
      return delimiter;
    }

    public void setDelimiter(String delimiter) {
      this.delimiter = delimiter;
    }

    public Character getQuotedBy() {
      return quotedBy;
    }

    public void setQuotedBy(Character quotedBy) {
      this.quotedBy = quotedBy;
    }
  }

  /**
   * Build a CSVReader with specific delimiter and presence or not of a header line.
   * Encoding detection will be attempted.
   * @param source
   * @param delimiter
   * @param header Does the file include a header line ?
   * @return
   * @throws IOException
   */
  public static CSVReader build(File source, String delimiter, boolean header) throws IOException {
    return new CSVReader(source, detectEncoding(source), delimiter, null, header ? 1 : 0);
  }

  /**
   * Build a CSVReader with specific encoding, delimiter, quotes character and number of header row(s).
   *
   * @param source
   * @param encoding
   * @param delimiter
   * @param quotes
   * @param headerRows
   * @return
   * @throws IOException
   */
  public static CSVReader build(File source, String encoding, String delimiter, Character quotes, Integer headerRows)
          throws IOException {
    return new CSVReader(source, encoding, delimiter, quotes, headerRows);
  }

  /**
   * Build a CSVReader with specific encoding, delimiter and number of header row(s) but default quote character
   * (quotation marks)
   * @param source
   * @param encoding
   * @param delimiter
   * @param headerRows
   * @return
   * @throws IOException
   */
  public static CSVReader build(File source, String encoding, String delimiter, Integer headerRows) throws IOException {
    return new CSVReader(source, encoding, delimiter, '"', headerRows);
  }

  /**
   * Build a CSVReader with specific encoding, delimiter quotes and number of header row(s)
   *
   * @param stream
   * @param encoding
   * @param delimiter
   * @param quotes
   * @param headerRows
   * @return
   * @throws IOException
   */
  public static CSVReader build(InputStream stream, String encoding, String delimiter, Character quotes,
                                Integer headerRows) throws IOException {
    return new CSVReader(stream, encoding, delimiter, quotes, headerRows);
  }

  /**
   * Build a CSVReader and try to detect the encoding, delimiter and quotes.
   *
   * @param source
   * @param headerRows
   * @return
   * @throws IOException
   */
  public static CSVReader build(File source, Integer headerRows) throws IOException {
    String encoding = detectEncoding(source);
    CSVMetadata csvMeta  = extractCsvMetadata(source, encoding);
    return new CSVReader(source, encoding, csvMeta.getDelimiter(), csvMeta.getQuotedBy(), headerRows);
  }

  /**
   * Assumes 1 header row
   *
   * @param source
   * @return
   * @throws IOException
   */
  public static CSVReader build(File source) throws IOException {
    return build(source, 1);
  }

  public static CSVReader buildTabReader(InputStream stream, String encoding, Integer headerRows) throws IOException {
    return new CSVReader(stream, encoding, "\t", null, headerRows);
  }

  public static CSVReader buildUtf8TabReader(InputStream stream) throws IOException {
    return buildTabReader(stream, "utf8", 0);
  }

  /**
   * Extract metadata from a CSV file.
   * Metadata includes delimiter and quotes character.
   * 
   * @param source
   * @param encoding
   * @return
   * @throws UnkownDelimitersException
   */
  public static CSVMetadata extractCsvMetadata(File source, String encoding) throws UnkownDelimitersException {
    CSVMetadata csvMetadata = new CSVMetadata();
    // try csv, tab and then other popular delimiters
    // keep number of resulting columns for comparisons
    int maxColumns = 0;

    for (String delim : POTENTIAL_DELIMITERS) {
      // test with various quotes including a dynamic one if the first char in each field is consistently the same
      List<Character> potentialQuotes = new ArrayList<Character>();

      CSVReader reader;
      try {
        reader = build(source, encoding, delim, null, 1);
        Character firstChar = likelyQuoteChar(reader);
        reader.close();
        if (firstChar != null) {
          potentialQuotes.add(firstChar);
        }
      } catch (IOException ignored) {
      }
      // prefer quotes for CSVs
      if (delim.equals(",")) {
        potentialQuotes.add('"');
        potentialQuotes.add('\'');
        potentialQuotes.add(null);
      } else {
        potentialQuotes.add(null);
        potentialQuotes.add('"');
        potentialQuotes.add('\'');
      }

      for (Character quote : potentialQuotes) {
        try {
          reader = build(source, encoding, delim, quote, 0);
          int x = consistentRowSize(reader);
          // try to find the delimiter and quote that will give us the maximum number of rows
          if (x > maxColumns) {
            csvMetadata.setDelimiter(delim);
            csvMetadata.setQuotedBy(quote);
            maxColumns = x;
          }
          reader.close();
        } catch (IOException ignored) {
          // swallow, maybe different delimiters work
          // if all fail we will throw an exception at the end
        }
      }
    }

    if (maxColumns < 1) {
      throw new UnkownDelimitersException("Unable to detect field delimiter");
    }

    return csvMetadata;
  }

  /**
   * @return the number of consistent columns, -1 if non consistent or column numbers-2 in case the column numbers only
   * differ by 1 at max.
   */
  private static int consistentRowSize(CSVReader reader) {
    int rowNum = 0;
    int columns = 0;
    boolean plusMinusOne = false;
    while (reader.hasNext() && rowNum < ROWS_TO_INSPECT) {
      String[] row = reader.next();
      if (rowNum == 0) {
        columns = row.length;
      }
      if (Math.abs(columns - row.length) > 1) {
        return -1;
      }
      if (columns != row.length) {
        plusMinusOne = true;
      }
      rowNum++;
    }
    if (plusMinusOne) {
      return columns - 2;
    }
    return columns;
  }

  private static String detectEncoding(File source) throws UnkownCharsetException {
    Charset encoding;
    try {
      encoding = CharsetDetection.detectEncoding(source, 16384);
      if (encoding == null) {
        throw new UnkownCharsetException("Unable to detect the files character encoding");
      }
    } catch (IOException e) {
      throw new UnkownCharsetException(e);
    }
    return encoding.displayName();
  }

  /**
   * Checks if all non empty/null fields start with the same character.
   *
   * @return the first character if consistent, otherwise null
   */
  private static Character likelyQuoteChar(CSVReader reader) {
    Character quote = null;
    int line = 0;
    while (reader.hasNext() && line < 10) {
      line++;
      String[] row = reader.next();
      if (row != null) {
        for (String col : row) {
          if (col != null && col.length() > 0) {
            // same char at start & end?
            if (col.length() > 1 && col.charAt(0) == col.charAt(col.length() - 1)) {
              // only consider non alphanumerics
              char potQuote = col.charAt(0);
              if (Character.isLetterOrDigit(potQuote)) {
                break;
              }
              if (quote == null) {
                quote = potQuote;
              } else {
                if (!quote.equals(potQuote)) {
                  quote = null;
                  break;
                }
              }
            }
          }
        }
      }
    }
    return quote;
  }

}

