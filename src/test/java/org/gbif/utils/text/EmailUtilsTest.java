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

import org.gbif.utils.text.EmailUtils.EmailWithName;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author markus
 */
public class EmailUtilsTest {

  @Test
  public void testEmailPatterns() {
    EmailWithName en = EmailUtils.parseEmail("mdoering@gbif.org");
    assertTrue(en.name == null);
    assertEquals("mdoering@gbif.org", en.email);

    en = EmailUtils.parseEmail("Manuel Möglich <manuel.moeglich@googlemail.com>");
    assertEquals("Manuel Möglich", en.name);
    assertEquals("manuel.moeglich@googlemail.com", en.email);

    en = EmailUtils.parseEmail("Manuel Möglich");
    assertEquals("Manuel Möglich", en.name);
    assertTrue(en.email == null);

    en = EmailUtils.parseEmail("Manuel Möglich manuel.moeglich@googlemail.com");
    assertEquals("Manuel Möglich", en.name);
    assertEquals("manuel.moeglich@googlemail.com", en.email);

  }
}
