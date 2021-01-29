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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapUtilsTest {


  @Test
  public void testMap() {
    Map<String, Double> map = new HashMap<String, Double>(4);
    map.put("A", 99.5);
    map.put("B", 67.4);
    map.put("C", 67.5);
    map.put("D", 67.3);

    Map<String, Double> mapSorted = MapUtils.sortByValue(map);

    assertEquals(4, mapSorted.size());

    Queue<String> expected = new LinkedList<String>();
    expected.add("D");
    expected.add("B");
    expected.add("C");
    expected.add("A");
    for (String k : mapSorted.keySet()) {
      assertEquals(expected.poll(), k);
    }
  }

  @Test
  public void testMap2() {
    Map<Integer, String> map = new HashMap<Integer, String>(4);
    map.put(5, "C");
    map.put(4, "B");
    map.put(99, "A");
    map.put(3, "D");

    Map<Integer, String> mapSorted = MapUtils.sortByValue(map);

    assertEquals(4, mapSorted.size());

    Queue<Integer> expected = new LinkedList<Integer>();
    expected.add(99);
    expected.add(4);
    expected.add(5);
    expected.add(3);
    for (Integer k : mapSorted.keySet()) {
      assertEquals(expected.poll(), k);
    }
  }
}
