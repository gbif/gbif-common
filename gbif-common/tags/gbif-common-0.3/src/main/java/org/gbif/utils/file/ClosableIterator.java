package org.gbif.utils.file;

import java.util.Iterator;

/**
 * An iterator that needs to be explicitly closed when it is not used anymore
 *
 * @author markus
 */
public interface ClosableIterator<T> extends Iterator<T> {

  void close();
}
