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
package org.gbif.utils.collection;

import java.util.Iterator;

public class IterableUtils {

  /**
   * Converts an interator into an Iterable for one time use only.
   * If used more than once with the same iterator this will not reset/restart the iteration!
   */
  public static <T> Iterable<T> iterable(final Iterator<T> it) {
    return () -> it;
  }
}
