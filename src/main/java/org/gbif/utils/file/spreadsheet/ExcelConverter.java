package org.gbif.utils.file.spreadsheet;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This converter reads a Workbook (.xlsx, .xls) file and produces a CSV file.
 *
 * Inspired from Apache POI example:
 * http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/ss/examples/ToCSV.java
 */
public class ExcelConverter {

  private static final Logger LOG = LoggerFactory.getLogger(ExcelConverter.class);
  private static final int FLUSH_INTERVAL = 1000;
  private static final int FIRST_SHEET_INDEX = 0;

  private final DataFormatter formatter = new IsoDateDataFormatter();

  private final Workbook workbook;
  /**
   * Destination for data
   */
  private final SpreadsheetConsumer writer;

  ExcelConverter(Workbook workbook, SpreadsheetConsumer writer) {
    this.workbook = workbook;
    this.writer = writer;
  }

  /**
   * Convert a workbook (.xls format) to some other format like CSV.
   *
   * Example: ExcelConverter.convert(xlsPath, new CsvSpreadsheetConsumer(new FileWriter(outputCsvFile)));
   *
   * @param workbookFile Path to the workbook file
   * @param writer A writer which will be given many List<String>s
   * @throws IOException
   * @throws InvalidFormatException Thrown if invalid XML is found whilst parsing an input SpreadsheetML file.
   */
  public static long convert(Path workbookFile, SpreadsheetConsumer writer) throws IOException, InvalidFormatException {
    Workbook workbook = WorkbookFactory.create(workbookFile.toFile());

    ExcelConverter converter = new ExcelConverter(workbook, writer);

    long count = converter.process();

    writer.flush();
    return count;
  }

  /**
   * Initiates the processing of the XLS workbook file to CSV.
   *
   * @throws IOException If reading the data from the package fails.
   */
  private int process() throws IOException {
    int numberOfLineWritten = 0;

    if (workbook.getNumberOfSheets() == 0) {
      LOG.warn("No sheet found in the workbook");
      return numberOfLineWritten;
    }

    int sheetIndex = FIRST_SHEET_INDEX;
    if (workbook.getNumberOfSheets() > 1) {
      List<String> sheetNames = IntStream.range(0, workbook.getNumberOfSheets())
              .mapToObj(idx -> workbook.getSheetName(idx))
              .collect(Collectors.toList());
      //run the sheetSelector function to see if we can get a sheet name from it otherwise, keep the first sheet
      Optional<String> sheetName = Optional.empty(); //sheetSelector.apply(sheetNames);
      if (sheetName.isPresent()) {
        sheetIndex = sheetName.map(name -> workbook.getSheetIndex(name)).get();
      } else{
        LOG.warn("Detected more than 1 sheet and can not get a selection from sheetSelector(), only reading the first one.");
      }
    }

    Sheet sheet = workbook.getSheetAt(sheetIndex); //get the first Sheet, and only get this
    if (sheet.getPhysicalNumberOfRows() > 0) {
      //pass the evaluator to other methods since it's unknown how expensive is to get one!
      numberOfLineWritten = writeSheetContent(sheet, workbook.getCreationHelper().createFormulaEvaluator());
    }

    return numberOfLineWritten;
  }

  /**
   * Writes the content of a Sheet into the csvWriter.
   */
  private int writeSheetContent(Sheet sheet, FormulaEvaluator evaluator) throws IOException {
    // extract headers (first line)
    List<String> headers = getHeaders(sheet, evaluator);
    writer.write(headers);

    int rowSize = headers.size() - 1; //this is done avoid the calculation on each call
    for (int j = 1; j <= sheet.getLastRowNum(); j++) {
      writer.write(rowToCSV(sheet.getRow(j), evaluator, rowSize));
      if (j % FLUSH_INTERVAL == 0) {
        writer.flush();
      }
    }
    // +1 to add the header line
    return sheet.getLastRowNum() + 1;
  }

  /**
   * Extract the first row from as the header.
   */
  private List<String> getHeaders(Sheet sheet, FormulaEvaluator evaluator) {
    Row headerRow = sheet.getRow(0);
    //we want to loop until maxColumnIdx (if provided) even if it's greater than getLastCellNum()
    //we shall have the same number of entries on every line in the CSV
    List<String> headers = rowToCSV(headerRow, evaluator, headerRow.getLastCellNum());

    //reverse iteration (from the right side of the Workbook) to remove empty columns
    ListIterator<String> iterator = headers.listIterator(headers.size());
    while (iterator.hasPrevious()) {
      if (StringUtils.isBlank(iterator.previous())) {
        iterator.remove();
      }
    }
    return headers;
  }

  /**
   * Called to convert a row of cells into a line of data that can later be
   * output to the CSV file.
   *
   * @param row can be HSSFRow or XSSFRow classes or null
   * @param evaluator workbook formula evaluator
   * @param maxCellNum maximum number of cell to evaluate
   * @return
   */
  private List<String> rowToCSV(Row row, FormulaEvaluator evaluator, int maxCellNum) {
    List<String> csvLine = new ArrayList<>();
    if (row != null) {
      for (int i = 0; i <= maxCellNum; i++) {
        Cell cell = row.getCell(i);
        //add an empty string when we have no data
        if (cell == null) {
          csvLine.add("");
        } else {
          //getCellTypeEnum deprecation explanation: see https://bz.apache.org/bugzilla/show_bug.cgi?id=60228
          if (CellType.FORMULA == cell.getCellTypeEnum()) {
            csvLine.add(formatter.formatCellValue(cell, evaluator));
          } else if (cell.getCellTypeEnum() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            csvLine.add(formatter.formatRawCellContents(cell.getNumericCellValue(), cell.getCellStyle().getIndex(), cell.getCellStyle().getDataFormatString()));
          } else {
            csvLine.add(formatter.formatCellValue(cell));
          }
        }
      }
    }
    return csvLine;
  }
}
