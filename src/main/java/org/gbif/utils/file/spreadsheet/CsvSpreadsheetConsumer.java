package org.gbif.utils.file.spreadsheet;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Convert rows from a spreadsheet into CSV.
 */
public class CsvSpreadsheetConsumer implements SpreadsheetConsumer {
  final SequenceWriter sequenceWriter;
  long count = 0;

  public CsvSpreadsheetConsumer(Writer writer) throws IOException {
    CsvMapper mapper = new CsvMapper();
    mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);

    CsvSchema schema = CsvSchema.emptySchema().withColumnSeparator(',').withLineSeparator("\n").withQuoteChar('"');

    sequenceWriter = mapper.writerFor(List.class).with(schema).writeValues(writer);
  }

  @Override
  public void write(List<String> row) throws IOException {
    sequenceWriter.write(row);
    count++;
  }

  @Override
  public void flush() throws IOException {
    sequenceWriter.flush();
  }

  @Override
  public long getCount() {
    return count;
  }
}
