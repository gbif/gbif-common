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

import org.apache.commons.lang3.StringUtils;

public final class CommonStringUtils {

  /**
   * This includes some special whitespaces which not present in standard trim list:
   * <ul>
   *  <li>U+0085 Next Line (NEL)</li>
   *  <li>U+00A0 No-Break Space (NBSP)</li>
   *  <li>U+000C Form Feed (FF)</li>
   *  <li>U+2007 Figure Space </li>
   * </ul>
   */
  public static final String WHITESPACES_LIST = ""
      + "\u2002\u3000\r\u0085\u200A\u2005\u2000\u3000"
      + "\u2029\u000B\u3000\u2008\u2003\u205F\u3000\u1680"
      + "\u0009\u0020\u2006\u2001\u202F\u00A0\u000C\u2009"
      + "\u3000\u2004\u3000\u3000\u2028\n\u2007\u3000";

  private CommonStringUtils() {}

  /**
   * Strips a set of whitespace characters from the start and end of a String.
   * This is similar to String.trim() but also includes some specific characters.
   *
   * @param str String to be trimmed
   * @return trimmed String
   */
  public static String trim(String str) {
    return StringUtils.strip(str, WHITESPACES_LIST);
  }
}
