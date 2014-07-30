package org.gbif.utils.rs;

import java.net.URL;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RsGbifOrgTest {

  @Test
  public void testUrl() throws Exception {
    assertEquals(new URL("http://rs.gbif.org/dictionaries/synonyms/f1.txt"), RsGbifOrg.url("dictionaries","synonyms","f1.txt"));
    assertEquals(new URL("http://rs.gbif.org/"), RsGbifOrg.url());
    assertEquals(new URL("http://rs.gbif.org/"), RsGbifOrg.url(null));
    assertEquals(new URL("http://rs.gbif.org/ aha "), RsGbifOrg.url(" aha "));
  }

  @Test
  public void testAuthorityUrl() throws Exception {
    assertEquals(new URL("http://rs.gbif.org/dictionaries/authority/f1.txt"), RsGbifOrg.authorityUrl("f1.txt"));
    assertEquals(new URL("http://rs.gbif.org/dictionaries/authority"), RsGbifOrg.authorityUrl(null));
    assertEquals(new URL("http://rs.gbif.org/dictionaries/authority/ aha "), RsGbifOrg.authorityUrl(" aha "));
  }

  @Test
  public void testDictionaryUrl() throws Exception {
    assertEquals(new URL("http://rs.gbif.org/dictionaries/f1.txt"), RsGbifOrg.dictionaryUrl("f1.txt"));
    assertEquals(new URL("http://rs.gbif.org/dictionaries"), RsGbifOrg.dictionaryUrl(null));
    assertEquals(new URL("http://rs.gbif.org/dictionaries/ aha "), RsGbifOrg.dictionaryUrl(" aha "));
  }

  @Test
  public void testSynonymUrl() throws Exception {
    assertEquals(new URL("http://rs.gbif.org/dictionaries/synonyms/f1.txt"), RsGbifOrg.synonymUrl("f1.txt"));
    assertEquals(new URL("http://rs.gbif.org/dictionaries/synonyms"), RsGbifOrg.synonymUrl(null));
    assertEquals(new URL("http://rs.gbif.org/dictionaries/synonyms/ aha "), RsGbifOrg.synonymUrl(" aha "));
  }
}
