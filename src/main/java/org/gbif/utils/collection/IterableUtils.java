package org.gbif.utils.collection;

import java.util.Iterator;

public class IterableUtils {

  /**
   * Converts an interator into an Iterable for one time use only.
   * If used more than once with the same iterator this will not reset/restart the iteration!
   */
  public static <T> Iterable<T> iterable(final Iterator<T> it){
    return new Iterable<T>(){
      public Iterator<T> iterator(){
        return it;
      }
    };
  }
}
