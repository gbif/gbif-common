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

package org.gbif.utils.collection;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
   *
   * @return a map ordered by the values of the input map
   */
  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
    return sortByValueInternal(map, new Comparator<Map.Entry<K, V>>() {
      @Override
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        return o1.getValue().compareTo(o2.getValue());
      }
    });
  }

  /**
   * Order a Map by its values using the given value comparator and return a new {@link LinkedHashMap} which maintains that order.
   *
   * @param map to sort
   * @param <K> type of the map key
   * @param <V> type of the map value
   *
   * @return a map ordered by the values of the input map
   */
  public static <K, V> Map<K, V> sortByValue(Map<K, V> map, final Comparator<V> comparator) {
    return sortByValueInternal(map, new Comparator<Map.Entry<K, V>>() {
        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
          return comparator.compare(o1.getValue(), o2.getValue());
        }
      });
  }

  private static <K, V> Map<K, V> sortByValueInternal(Map<K, V> map, final Comparator<Map.Entry<K, V>> comp) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort(list, comp);

    Map<K, V> result = new LinkedHashMap<K, V>(list.size());
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * Returns a maps value or a given default if not present.
   * For pre java8 code since j8 introduced Map.getOrDefault.
   */
  public static <K, V> V getOrDefault(Map<K,V> map, K key, V defaultValue) {
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
}
