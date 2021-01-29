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
package org.gbif.utils.text;

import java.util.Comparator;

/**
 * A comparator based around Javas String.compareTo().
 */
public class StringComparator implements Comparator<String> {

  @Override
  public int compare(String arg0, String arg1) {
    if (arg0 == null && arg1 == null) {
      return 0;
    } else if (arg0 == null) {
      return 1;
    } else if (arg1 == null) {
      return -1;
    }
    return arg0.compareTo(arg1);
  }

}
