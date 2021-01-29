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

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

/**
 * Collections of utilities related to Jackson
 */
class JacksonUtils {

  private JacksonUtils() { /*utility class*/}

  /**
   * Build the default {@link CsvSchema}.
   * @param delimiterChar
   * @param endOfLineSymbols
   * @param quoteChar
   * @return
   */
  static CsvSchema buildCsvSchema(char delimiterChar, String endOfLineSymbols, Character quoteChar) {
    CsvSchema schema = CsvSchema.emptySchema();
    schema = schema
            .withColumnSeparator(delimiterChar)
            .withLineSeparator(endOfLineSymbols);

    //quote character is optional
    schema = quoteChar == null ? schema.withoutQuoteChar() : schema.withQuoteChar(quoteChar);

    return schema;
  }

}
