/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
