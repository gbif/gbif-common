package org.gbif.utils.text;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringUtilsTest {

	@Test
	public void testIncrease() {
		assertEquals("Carlb",StringUtils.increase("Carla"));
		assertEquals("Homa",StringUtils.increase("Holz"));
		assertEquals("Aua",StringUtils.increase("Atz"));
		assertEquals("b",StringUtils.increase("a"));
		assertEquals("aa",StringUtils.increase("z"));
    assertEquals("AAA",StringUtils.increase("ZZ"));
    assertEquals("Aaa",StringUtils.increase("Zz"));
    assertEquals("aaa",StringUtils.increase("zz"));
    assertEquals("Abiet aaa",StringUtils.increase("Abies zzz"));
    assertEquals("Alle31.3-a ",StringUtils.increase("Alld31.3-z "));
    assertEquals("31.3-a a",StringUtils.increase("31.3-z "));
    assertEquals("aAaa",StringUtils.increase("zZz"));
		assertEquals("",StringUtils.increase(""));
		assertNull(StringUtils.increase(null));
	}

  @Test
  public void testRandomString() {
    assertEquals(10, StringUtils.randomString(10).length());

    // all upper case
    String rnd = StringUtils.randomString(22);
    assertEquals(rnd, rnd.toUpperCase());
  }

  @Test
  public void testFoldToAscii() throws Exception {
    assertEquals(null, StringUtils.foldToAscii(null));
    assertEquals("", StringUtils.foldToAscii(""));
    assertEquals("Schulhof, Gymnasium Hurth", StringUtils.foldToAscii("Schulhof, Gymnasium Hürth"));
    assertEquals("Doring", StringUtils.foldToAscii("Döring"));
    assertEquals("Desireno", StringUtils.foldToAscii("Désírèñø"));
    assertEquals("Debreczy & I. Racz", StringUtils.foldToAscii("Debreçzÿ & Ï. Rácz"));
    assertEquals("Donatia novae-zelandiae", StringUtils.foldToAscii("Donatia novae-zelandiæ"));
    assertEquals("Carex ×cayouettei", StringUtils.foldToAscii("Carex ×cayouettei"));
    assertEquals("Carex comosa × Carex lupulina", StringUtils.foldToAscii("Carex comosa × Carex lupulina"));
    assertEquals("Aeropyrum coil-shaped virus", StringUtils.foldToAscii("Aeropyrum coil-shaped virus"));
    assertEquals("†Lachnus bonneti", StringUtils.foldToAscii("†Lachnus bonneti"));

    assertEquals("lachs", StringUtils.foldToAscii("łachs"));

    String test = "ŠŒŽšœžŸ¥µÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýÿ";
    assertEquals("SOEZsoezY¥µAAAAAAAECEEEEIIIIDNOOOOOOUUUUYssaaaaaaaeceeeeiiiidnoooooouuuuyy", StringUtils.foldToAscii(test));

  }
}
