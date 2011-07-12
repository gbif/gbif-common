package org.gbif.utils.collection;

import java.util.*;

public class MapUtils {

  private MapUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  /**
   * This orders a Map by its values and returns a new {@link LinkedHashMap} which maintains that order.
   *
   * @param map to sort
   * @param <K> type of the map key
   * @param <V> type of the map value, this needs to implement {@link Comparable}
   * @return a map ordered by the values of the input map
   */
  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        return o1.getValue().compareTo(o2.getValue());
      }
    });

    Map<K, V> result = new LinkedHashMap<K, V>(list.size());
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
