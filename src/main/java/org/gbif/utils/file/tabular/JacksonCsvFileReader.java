package org.gbif.utils.file.tabular;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.lang3.StringUtils;

/**
 * Internal {@link TabularDataFileReader} implementation backed by Jackson CSV.
 */
class JacksonCsvFileReader implements TabularDataFileReader<List<String>> {

  private List<String> headerLine;
  private final MappingIterator<List<String>> it;

  private long lastLineNumber = 0;
  private long recordNumber = 0;

  /**
   *
   * @param reader
   * @param delimiterChar
   * @param endOfLineSymbols
   * @param quoteChar
   * @param headerLineIncluded
   * @throws IOException
   */
  JacksonCsvFileReader(Reader reader, char delimiterChar, String endOfLineSymbols, Character quoteChar,
                       boolean headerLineIncluded) throws IOException {

    CsvMapper mapper = new CsvMapper();
    mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
    CsvSchema schema = CsvSchema.emptySchema();

    schema = schema
            .withColumnSeparator(delimiterChar)
            .withLineSeparator(endOfLineSymbols);

    if(quoteChar != null) {
      schema = schema.withQuoteChar(quoteChar);
    }
    else{
      schema = schema.withoutQuoteChar();
    }

    it = mapper.readerFor(List.class)
                    .with(schema)
                    .readValues(reader);

    //ensure to pull the header line if we need to
    if(headerLineIncluded && it.hasNext()) {
      headerLine = it.next();
    }
  }

  @Override
  public List<String> getHeaderLine() throws IOException {
    return headerLine;
  }

  @Override
  public long getLastRecordLineNumber() {
    return lastLineNumber;
  }

  @Override
  public long getLastRecordNumber() {
    return recordNumber;
  }

  @Override
  public List<String> read() throws IOException {
    while(it.hasNext()) {
      //get the current line number before we read the next record
      lastLineNumber = it.getCurrentLocation().getLineNr();
      List<String> row = it.next();
      if(row.size() != 1 && StringUtils.isNotBlank(row.get(0))){
        recordNumber++;
        return row;
      }
    }
    return null;
  }

  @Override
  public void close() throws IOException {
    it.close();
  }
}
