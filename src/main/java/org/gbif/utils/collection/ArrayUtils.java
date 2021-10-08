/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.utils.collection;

import java.lang.reflect.Array;

public class ArrayUtils {

  public static <T> T[] arrayMerge(T[]... arrays) {
    // Determine required size of new array
    int count = 0;
    for (T[] array : arrays) {
      count += array.length;
    }

    // create new array of required class
    T[] mergedArray = (T[]) Array.newInstance(arrays[0][0].getClass(), count);

    // Merge each array into new array
    int start = 0;
    for (T[] array : arrays) {
      System.arraycopy(array, 0, mergedArray, start, array.length);
      start += array.length;
    }
    return mergedArray;
  }

  public static byte[] arrayMerge(byte[]... arrays) {
    // Determine required size of new array
    int count = 0;
    for (byte[] array : arrays) {
      count += array.length;
    }

    // create new array of required class
    byte[] mergedArray = new byte[count];

    // Merge each array into new array
    int start = 0;
    for (byte[] array : arrays) {
      System.arraycopy(array, 0, mergedArray, start, array.length);
      start += array.length;
    }
    return mergedArray;
  }

  public static byte[] intToByteArray(int value) {
    byte[] b = new byte[4];
    for (int i = 0; i < 4; i++) {
      int offset = (b.length - 1 - i) * 8;
      b[i] = (byte) ((value >>> offset) & 0xFF);
    }
    return b;
  }

  public static int byteArrayToInt(byte[] b) {
    return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
  }

  private ArrayUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }
}
