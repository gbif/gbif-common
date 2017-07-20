package org.gbif.utils.file.tabular;

import java.nio.charset.Charset;

/**
 * Data about a tabular data file.
 */
public class TabularFileMetadata {

  private Charset encoding;
  private Character delimiter;
  private Character quotedBy;

  public Charset getEncoding() {
    return encoding;
  }

  public void setEncoding(Charset encoding) {
    this.encoding = encoding;
  }

  public Character getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(Character delimiter) {
    this.delimiter = delimiter;
  }

  public Character getQuotedBy() {
    return quotedBy;
  }

  public void setQuotedBy(Character quotedBy) {
    this.quotedBy = quotedBy;
  }
}
