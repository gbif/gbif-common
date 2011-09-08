package org.gbif.utils.text;

import java.util.Comparator;

import org.apache.commons.lang3.text.StrTokenizer;

/**
 * A comparator for delimited lines that compares the content of a given column number for all rows.
 * This allows to sort for example tab delimited files by any column and not only the first one.
 * <p/>
 * If no explicit comparator is given a string comparison is done for the actual column content.
 *
 * @author markus
 */
public class LineComparator implements Comparator<String> {

  private final StrTokenizer tokenizer;
  private int column;
  private final Comparator<String> comp;

  public LineComparator(int column, String columnDelimiter) {
    this(column, columnDelimiter, null, null);
  }

  public LineComparator(int column, String columnDelimiter, Character quoteChar) {
    this(column, columnDelimiter, quoteChar, null);
  }

  public LineComparator(int column, String columnDelimiter, Character quoteChar, Comparator<String> columnComparator) {
    this.column = column;
    this.comp = columnComparator != null ? columnComparator : new CCollationComparator();
    tokenizer = new StrTokenizer();
    tokenizer.setEmptyTokenAsNull(true);
    tokenizer.setIgnoreEmptyTokens(false);
    if (quoteChar != null) {
      tokenizer.setQuoteChar(quoteChar);
    }
    tokenizer.setDelimiterString(columnDelimiter);
  }

  public LineComparator(int column, String columnDelimiter, Comparator<String> columnComparator) {
    this(column, columnDelimiter, null, null);
  }

  public int compare(String o1, String o2) {
    if (o1 != null && o2 != null) {
      tokenizer.reset(o1);
      String[] parts = tokenizer.getTokenArray();
      String s1 = null;
      if (parts != null && parts.length > column) {
        s1 = parts[column];
      }
      if (s1 == null) {
        return 1;
      }

      tokenizer.reset(o2);
      parts = tokenizer.getTokenArray();
      String s2 = null;
      if (parts != null && parts.length > column) {
        s2 = parts[column];
      }
      if (s2 == null) {
        return -1;
      }

      return comp.compare(s1, s2);
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

  public Comparator<String> getColumnComparator() {
    return comp;
  }

}
