/***************************************************************************
 * Copyright 2014 Global Biodiversity Information Facility Secretariat
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

package org.gbif.utils.text;

import java.util.Comparator;

import com.google.common.collect.Ordering;
import org.apache.commons.lang3.text.StrTokenizer;

/**
 * A comparator for delimited lines that compares the content of a given column number for all rows.
 * This allows to sort for example tab delimited files by any column and not only the first one.
 * <p/>
 * If no explicit comparator is given a string comparison is done for the actual column content.
 */
public class LineComparator implements Comparator<String> {

  private final StrTokenizer tokenizer;
  private final int column;
  private final Comparator<String> comp;

  public LineComparator(int column, String columnDelimiter) {
    this(column, columnDelimiter, null, null);
  }

  public LineComparator(int column, String columnDelimiter, Character quoteChar) {
    this(column, columnDelimiter, quoteChar, null);
  }

  public LineComparator(int column, String columnDelimiter, Character quoteChar, Comparator<String> columnComparator) {
    this.column = column;
    this.comp = columnComparator == null ? Ordering.<String>natural().nullsFirst() : columnComparator;
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
    if (o1 == null || o2 == null) {
      if (o1 == null && o2 == null) {
        return 0;
      } else if (o1 == null) {
        return 1;
      } else {
        return -1;
      }
    } else {
      tokenizer.reset(o1);
      String[] parts = tokenizer.getTokenArray();
      String s1 = null;
      if (parts != null && parts.length > column) {
        s1 = parts[column];
      }
      tokenizer.reset(o2);
      parts = tokenizer.getTokenArray();
      String s2 = null;
      if (parts != null && parts.length > column) {
        s2 = parts[column];
      }

      if (s1 == null && s2 == null) {
        return 0;
      } else if (s1 == null) {
        return 1;
      } else if (s2 == null)  {
        return -1;
      } else {
        return comp.compare(s1, s2);
      }

    }
  }

  public Comparator<String> getColumnComparator() {
    return comp;
  }

}
