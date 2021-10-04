/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.utils;

public final class ObjectUtils {

  private ObjectUtils() {}

  /**
   * Returns the first of the given parameters that is not null.
   * If all given parameters are null, returns null.
   *
   * @param items
   * @param <T>
   * @return
   */
  public static <T> T coalesce(T... items) {
    if (items != null) {
      for (T i : items) if (i != null) return i;
    }
    return null;
  }

  public static <T> T coalesce(Iterable<T> items) {
    if (items != null) {
      for (T i : items) if (i != null) return i;
    }
    return null;
  }
}
