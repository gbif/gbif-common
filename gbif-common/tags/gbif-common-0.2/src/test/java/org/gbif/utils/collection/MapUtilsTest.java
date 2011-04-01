package org.gbif.utils.collection;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.junit.Assert.assertEquals;

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
