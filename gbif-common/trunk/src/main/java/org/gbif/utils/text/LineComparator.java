package org.gbif.utils.text;

import org.apache.commons.lang.text.StrTokenizer;

import java.util.Comparator;

public class LineComparator implements Comparator<String> {
  private final StrTokenizer tokenizer;
  private int column;

  public LineComparator(int column, String columnDelimiter) {
    this(column, columnDelimiter, null);
  }

  public LineComparator(int column, String columnDelimiter, Character quoteChar) {
    super();
    this.column = column;
    tokenizer = new StrTokenizer();
    tokenizer.setEmptyTokenAsNull(true);
    tokenizer.setIgnoreEmptyTokens(false);
    if (quoteChar != null) {
      tokenizer.setQuoteChar(quoteChar);
    }
    tokenizer.setDelimiterString(columnDelimiter);
  }

// public static LineComparator build(ArchiveFile file){
// return new LineComparator(file.getId().getIndex(), file.getFieldsTerminatedBy(), file.getFieldsEnclosedBy());
// }

  public int compare(String o1, String o2) {
    if (o1 != null && o2 != null) {
      tokenizer.reset(o1);
      String[] parts = tokenizer.getTokenArray();
      String s1 = null;
      if (parts != null && parts.length >= column) {
        s1 = parts[column];
      }
      if (s1 == null) {
        return 1;
      }

      tokenizer.reset(o2);
      parts = tokenizer.getTokenArray();
      String s2 = null;
      if (parts != null && parts.length >= column) {
        s2 = parts[column];
      }
      if (s2 == null) {
        return -1;
      }

      return s1.compareTo(s2);
    } else {
      if (o1 == null && o2 == null) {
        return 0;
      } else if (o1 == null) {
        return 1;
      } else {
        return -1;
      }
    }
  }

}
