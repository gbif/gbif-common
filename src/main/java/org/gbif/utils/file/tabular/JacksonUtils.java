package org.gbif.utils.file.tabular;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

/**
 * Collections of utilities related to Jackson
 */
class JacksonUtils {

  private JacksonUtils() { /*utility class*/}

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
