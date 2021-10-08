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
package org.gbif.utils.rs;

import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RsGbifOrgTest {

  @Test
  public void testUrl() throws Exception {
    assertEquals(
        new URL("http://rs.gbif.org/dictionaries/synonyms/f1.txt"),
        RsGbifOrg.url("dictionaries", "synonyms", "f1.txt"));
    assertEquals(new URL("http://rs.gbif.org/"), RsGbifOrg.url());
    assertEquals(new URL("http://rs.gbif.org/"), RsGbifOrg.url(null));
    assertEquals(new URL("http://rs.gbif.org/ aha "), RsGbifOrg.url(" aha "));
  }

  @Test
  public void testAuthorityUrl() throws Exception {
    assertEquals(
        new URL("http://rs.gbif.org/dictionaries/authority/f1.txt"),
        RsGbifOrg.authorityUrl("f1.txt"));
    assertEquals(
        new URL("http://rs.gbif.org/dictionaries/authority"), RsGbifOrg.authorityUrl(null));
    assertEquals(
        new URL("http://rs.gbif.org/dictionaries/authority/ aha "),
        RsGbifOrg.authorityUrl(" aha "));
  }

  @Test
  public void testDictionaryUrl() throws Exception {
    assertEquals(
        new URL("http://rs.gbif.org/dictionaries/f1.txt"), RsGbifOrg.dictionaryUrl("f1.txt"));
    assertEquals(new URL("http://rs.gbif.org/dictionaries"), RsGbifOrg.dictionaryUrl(null));
    assertEquals(
        new URL("http://rs.gbif.org/dictionaries/ aha "), RsGbifOrg.dictionaryUrl(" aha "));
  }

  @Test
  public void testSynonymUrl() throws Exception {
    assertEquals(
        new URL("http://rs.gbif.org/dictionaries/synonyms/f1.txt"), RsGbifOrg.synonymUrl("f1.txt"));
    assertEquals(new URL("http://rs.gbif.org/dictionaries/synonyms"), RsGbifOrg.synonymUrl(null));
    assertEquals(
        new URL("http://rs.gbif.org/dictionaries/synonyms/ aha "), RsGbifOrg.synonymUrl(" aha "));
  }
}
