/*
 * Source: https://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/xssf/eventusermodel/XLSX2CSV.java
 * With significant modifications from Global Biodiversity Information Facility, GBIF.org.
 */
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.gbif.utils.file.spreadsheet;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * An XLSX → CSV processor, which is a copy of <a href="https://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/xssf/eventusermodel/XLSX2CSV.java">ExcelXmlConverter.java</a>
 * modified to choose a particular sheet and output to a {@link Writer}. Dates are formatted to ISO 8601 standard,
 * otherwise the formats defined in Excel are used.
 * <p>
 * The SAX parser should keep memory usage low, so large workbooks can be handled.
 * <p>
 * Original JavaDoc follows:
 * <p>
 * A rudimentary XLSX -> CSV processor modeled on the
 * POI sample program XLS2CSVmra from the package
 * org.apache.poi.hssf.eventusermodel.examples.
 * As with the HSSF version, this tries to spot missing
 *  rows and cells, and output empty entries for them.
 * <p>
 * Data sheets are read using a SAX parser to keep the
 * memory footprint relatively small, so this should be
 * able to read enormous workbooks.  The styles table and
 * the shared-string table must be kept in memory.  The
 * standard POI styles table class is used, but a custom
 * (read-only) class is used for the shared string table
 * because the standard POI SharedStringsTable grows very
 * quickly with the number of unique strings.
 * <p>
 * For a more advanced implementation of SAX event parsing
 * of XLSX files, see {@link XSSFEventBasedExcelExtractor}
 * and {@link XSSFSheetXMLHandler}. Note that for many cases,
 * it may be possible to simply use those with a custom
 * {@link SheetContentsHandler} and no SAX code needed of
 * your own!
 */
public class ExcelXmlConverter {
  private static final Logger LOG = LoggerFactory.getLogger(ExcelXmlConverter.class);

  /**
   * Convert a workbook (.xlsx format) to some other format like CSV.
   *
   * Example: ExcelXmlConverter.convert(xlsxPath, new CsvSpreadsheetConsumer(new FileWriter(outputCsvFile)));
   *
   * @param workbookFile Path to the workbook file
   * @param writer A writer which will be given many List<String>s
   * @throws IOException
   * @throws InvalidFormatException Thrown if invalid XML is found whilst parsing an input SpreadsheetML file.
   */
  public static long convert(Path workbookFile, SpreadsheetConsumer writer) throws IOException, SAXException, OpenXML4JException {
    OPCPackage p = OPCPackage.open(workbookFile.toFile(), PackageAccess.READ);
    ExcelXmlConverter converter = new ExcelXmlConverter(p, writer);

    long count = converter.process();

    writer.flush();
    return count;
  }

  /**
   * Uses the XSSF Event SAX helpers to do most of the work of parsing the Sheet XML, and output the contents
   * to a SpreadsheetConsumer.
   */
  private class SheetToCSV implements SheetContentsHandler {
    private int currentRow = -1;
    private int currentCol = -1;

    /**
     * Number of columns to read starting with leftmost
     */
    private final long numColumns;

    /**
     * Destination for data
     */
    private final SpreadsheetConsumer writer;

    private List<String> row = new ArrayList<>();

    public SheetToCSV(SpreadsheetConsumer writer, long minColumns) {
      this.writer = writer;
      this.numColumns = minColumns;
    }

    /**
     * Blank rows are skipped; this creates them.
     */
    private void outputMissingRows(int number) {
      List<String> blankRow = new ArrayList<>();
      for (int j = 0; j < numColumns; j++) {
        blankRow.add("");
      }

      try {
        for (int i = 0; i < number; i++) {
          writer.write(blankRow);
        }
      } catch (IOException e) {
        LOG.error("Exception adding blank rows", e);
        e.printStackTrace();
      }
    }

    @Override
    public void startRow(int rowNum) {
      LOG.trace("Start row {} (current: {})", rowNum, currentRow);
      // If there were gaps, output the missing rows
      outputMissingRows(rowNum - currentRow - 1);
      // Prepare for this row
      currentRow = rowNum;
      currentCol = -1;
    }

    @Override
    public void endRow(int rowNum) {
      LOG.trace("End row {} (current: {})", rowNum, currentRow);
      // Ensure the correct number of columns
      for (int i = currentCol+1; i < numColumns; i++) {
        row.add("");
      }
      try {
        writer.write(row);
        row = new ArrayList<>(row.size());
      } catch (IOException e) {
        LOG.error("Error reading spreadsheet " + e.getMessage(), e);
      }
    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
      LOG.trace("Cell {} “{}”", cellReference, formattedValue);
      // gracefully handle missing CellRef here in a similar way as XSSFCell does
      if (cellReference == null) {
        cellReference = new CellAddress(currentRow, currentCol).formatAsString();
      }

      int thisCol = (new CellReference(cellReference)).getCol();

      if (thisCol < numColumns) {
        // Did we miss any cells?
        int missedCols = thisCol - currentCol - 1;
        for (int i = 0; i < missedCols; i++) {
          row.add("");
        }
        currentCol = thisCol;

        row.add(formattedValue);
      } else {
        LOG.debug("Not including cell {} “{}”; it is outside the main table", cellReference, formattedValue);
      }
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {}
  }

  /**
   * Counts the number of cells with data in the sheet.
   */
  private class CountSheetData implements SheetContentsHandler {

    final Long[] cellCount;
    final Long[] headerLength;

    CountSheetData(Long[] cellCount, Long[] headerLength) {
      this.cellCount = cellCount;
      this.headerLength = headerLength;
      cellCount[0] = 0L;
      headerLength[0] = 0L;
    }

    @Override
    public void startRow(int rowNum) {}

    @Override
    public void endRow(int rowNum) {
      if (rowNum == 0) {
        headerLength[0] = cellCount[0];
      }
    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
      if (formattedValue != null) {
        cellCount[0]++;
      }
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {}
  }

  ///////////////////////////////////////

  private final OPCPackage xlsxPackage;
  private final SpreadsheetConsumer writer;

  /**
   * Creates a new XLSX -> CSV examples
   *
   * @param pkg    The XLSX package to process
   * @param writer The writer to output to
   */
  public ExcelXmlConverter(OPCPackage pkg, SpreadsheetConsumer writer) {
    this.xlsxPackage = pkg;
    this.writer = writer;
  }

  /**
   * Parses and shows the content of one sheet
   * using the specified styles and shared-strings tables.
   *
   * @param styles The table of styles that may be referenced by cells in the sheet
   * @param strings The table of strings that may be referenced by cells in the sheet
   * @param sheetInputStream The stream to read the sheet-data from.

   * @exception java.io.IOException An IO exception from the parser,
   *      possibly from a byte stream or character stream
   *      supplied by the application.
   * @throws SAXException if parsing the XML data fails.
   */
  public long processSheet(
      StylesTable styles,
      ReadOnlySharedStringsTable strings,
      SheetContentsHandler sheetHandler,
      InputStream sheetInputStream
  ) throws IOException, SAXException {
    IsoDateDataFormatter formatter = new IsoDateDataFormatter();
    InputSource sheetSource = new InputSource(sheetInputStream);
    XMLReader sheetParser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
    ContentHandler handler = new XSSFSheetXMLHandler(styles, null, strings, sheetHandler, formatter, false);
    sheetParser.setContentHandler(handler);
    sheetParser.parse(sheetSource);
    return writer.getCount();
  }

  /**
   * Initiates the processing of the XLS workbook file to CSV.
   *
   * @throws IOException If reading the data from the package fails.
   * @throws SAXException if parsing the XML data fails.
   */
  public long process() throws IOException, OpenXML4JException, SAXException {
    ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.xlsxPackage);

    // First find the sheet with the most data.
    XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
    StylesTable styles = xssfReader.getStylesTable();
    XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

    long maxCount = -1;
    int maxSheetIndex = -1;
    long maxHeaderLength = -1;

    int index = 0;

    while (iter.hasNext()) {
      try (InputStream stream = iter.next()) {
        Long[] cellCount = new Long[1];
        Long[] headerLength = new Long[1];

        String sheetName = iter.getSheetName();
        LOG.debug("Counting cells in spreadsheet {} “{}”", index, sheetName);
        processSheet(styles, strings, new CountSheetData(cellCount, headerLength), stream);

        if (cellCount[0] > maxCount) {
          maxSheetIndex = index;
          maxCount = cellCount[0];
          maxHeaderLength = headerLength[0];
        }
      }
      index++;
    }


    iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
    for (index = 0; index++ < maxSheetIndex; iter.next()) {}

    try (InputStream stream = iter.next()) {
      String sheetName = iter.getSheetName();
      LOG.info("Reading spreadsheet {} “{}” which has {} cells in {} columns", maxSheetIndex, sheetName, maxCount, maxHeaderLength);

      processSheet(styles, strings, new SheetToCSV(writer, maxHeaderLength), stream);
    }

    // Then read and process that sheet.
    return writer.getCount();
  }
}
