package org.gbif.utils.collection;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HashSetPerformance {
  private static final int TIMES = 100000;
  private static final int MAX = 500000;

  public static void main(String[] argv) {
    // first, get the JIT going
    test(false, new CompactHashSet());
    test(false, new HashSet());

    // then, do real timings
    System.out.println("*** HashSet ***");
    for (int ix = 0; ix < 3; ix++) {
      test(true, new HashSet());
    }
    System.out.println("*** CompactHashSet ***");
    for (int ix = 0; ix < 3; ix++) {
      test(true, new CompactHashSet());
    }

  }

  public static void test(boolean output, Set set) {
    long start = System.currentTimeMillis();

    if (output) {
      System.gc();
      System.gc();
    }
    long before = Runtime.getRuntime().totalMemory() -
        Runtime.getRuntime().freeMemory();

    // add
    for (int ix = 0; ix < TIMES; ix++) {
      set.add(new Long(Math.round(Math.random() * MAX)));
    }

    if (output) {
      System.gc();
      System.gc();
      long after = Runtime.getRuntime().totalMemory() -
          Runtime.getRuntime().freeMemory();
      System.out.println("Memory before: " + before);
      System.out.println("Memory after: " + after);
      System.out.println("Memory usage: " + (after - before));
    }

    // lookup
    int count = 0;
    for (int ix = 0; ix < TIMES; ix++) {
      Long number = new Long(Math.round(Math.random() * MAX));
      if (set.contains(number)) {
        count++;
      }
    }

    // iterate
    Iterator it = set.iterator();
    while (it.hasNext()) {
      Long number = (Long) it.next();
    }

    // remove
    for (int ix = 0; ix < TIMES; ix++) {
      Long number = new Long(Math.round(Math.random() * MAX));
      set.remove(number);
    }

    if (output) {
      System.out.println("TIME: " + (System.currentTimeMillis() - start));
    }
  }
}
