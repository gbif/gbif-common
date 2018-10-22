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
