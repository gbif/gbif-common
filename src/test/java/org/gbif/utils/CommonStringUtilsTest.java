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

import org.junit.jupiter.api.Test;

import static org.gbif.utils.CommonStringUtils.deleteWhitespace;
import static org.gbif.utils.CommonStringUtils.trim;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonStringUtilsTest {

  @Test
  public void testTrim() {
    assertEquals("str", trim(" str "));
    assertEquals("str StR", trim(" str StR "));
    assertEquals("STR  str", trim(" \n\u0085STR  str \u00A0\n\t"));
  }

  @Test
  public void testDeleteWhitespace() {
    assertEquals("str", deleteWhitespace(" str "));
    assertEquals("strStR", deleteWhitespace(" str StR "));
    assertEquals("STRstr", deleteWhitespace(" \n\u000CSTR\u00A0   str \u2007\n\t"));
  }
}
