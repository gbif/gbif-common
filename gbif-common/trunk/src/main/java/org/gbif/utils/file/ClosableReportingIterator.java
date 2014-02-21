package org.gbif.utils.file;

/**
 * Interface that extends the ClosableIterator providing a means to check if there is an error, log what it is, store
 * the actual Exception, and skip over it to continue reading.
 *
 * @param <T> the type of elements returned by the iterator
 */
public interface ClosableReportingIterator<T> extends ClosableIterator<T> {

  /**
   * Return true if an error was encountered while iterating over row.
   */
  boolean hasRowError();

  /**
   * Return an informative message about the error encountered while iterating over row.
   * Different from the Exception's message, used to store the row number, row string, etc.
   */
  String getErrorMessage();

  /**
   * Return the Exception encountered while iterating over row.
   */
  Exception getException();
}
