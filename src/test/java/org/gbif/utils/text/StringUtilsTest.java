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
package org.gbif.utils.text;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringUtilsTest {

  @Test
  public void testIncrease() {
    assertEquals("Carlb", StringUtils.increase("Carla"));
    assertEquals("Homa", StringUtils.increase("Holz"));
    assertEquals("Aua", StringUtils.increase("Atz"));
    assertEquals("b", StringUtils.increase("a"));
    assertEquals("aa", StringUtils.increase("z"));
    assertEquals("AAA", StringUtils.increase("ZZ"));
    assertEquals("Aaa", StringUtils.increase("Zz"));
    assertEquals("aaa", StringUtils.increase("zz"));
    assertEquals("Abiet aaa", StringUtils.increase("Abies zzz"));
    assertEquals("Alle31.3-a ", StringUtils.increase("Alld31.3-z "));
    assertEquals("31.3-a a", StringUtils.increase("31.3-z "));
    assertEquals("aAaa", StringUtils.increase("zZz"));
    assertEquals("", StringUtils.increase(""));
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
  public void testDecodeUtf8Garbage() {
    assertUtf8(null, null);
    assertUtf8("", "");
    assertUtf8("a", "a");
    assertUtf8("ä-üOØ", "ä-üOØ");
    assertUtf8("(Günther, 1887)", "(GÃ¼nther, 1887)");
    assertUtf8("Böhlke, 1955", "BÃ¶hlke, 1955");
    assertUtf8("Nielsen & Quéro, 1991\n", "Nielsen & QuÃ©ro, 1991\n");
    assertUtf8("Rosinés", "RosinÃ©s");
    assertUtf8("S. Calderón & Standl.", "S. CalderÃ³n & Standl.");
    assertUtf8("Strömman, 1896", "StrÃ¶mman, 1896");
    assertUtf8("Sérus.", "SÃ©rus.");
    assertUtf8("Thér.", "ThÃ©r.");
    assertUtf8("Trécul", "TrÃ©cul");
    assertUtf8("Hale & López-Fig.\n", "Hale & LÃ³pez-Fig.\n");
  }

  private void assertUtf8(String expected, String src) {
    String decoded = StringUtils.decodeUtf8Garbage(src);
    assertEquals(expected, decoded);
    // make sure if we had gotten the correct string it would not be modified
    assertEquals(expected, StringUtils.decodeUtf8Garbage(decoded));
  }

  @Test
  public void testFoldToAscii() throws Exception {
    assertNull(StringUtils.foldToAscii(null));
    assertEquals("", StringUtils.foldToAscii(""));
    assertEquals("Schulhof, Gymnasium Hurth", StringUtils.foldToAscii("Schulhof, Gymnasium Hürth"));
    assertEquals("Doring", StringUtils.foldToAscii("Döring"));
    assertEquals("Desireno", StringUtils.foldToAscii("Désírèñø"));
    assertEquals("Debreczy & I. Racz", StringUtils.foldToAscii("Debreçzÿ & Ï. Rácz"));
    assertEquals("Donatia novae-zelandiae", StringUtils.foldToAscii("Donatia novae-zelandiæ"));
    assertEquals("Carex ×cayouettei", StringUtils.foldToAscii("Carex ×cayouettei"));
    assertEquals(
        "Carex comosa × Carex lupulina", StringUtils.foldToAscii("Carex comosa × Carex lupulina"));
    assertEquals(
        "Aeropyrum coil-shaped virus", StringUtils.foldToAscii("Aeropyrum coil-shaped virus"));
    assertEquals("†Lachnus bonneti", StringUtils.foldToAscii("†Lachnus bonneti"));

    assertEquals("lachs", StringUtils.foldToAscii("łachs"));

    String test = "ŠŒŽšœžŸ¥µÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýÿ";
    assertEquals(
        "SOEZsoezY¥µAAAAAAAECEEEEIIIIDNOOOOOOUUUUYssaaaaaaaeceeeeiiiidnoooooouuuuyy",
        StringUtils.foldToAscii(test));
  }

  @Test
  public void testThenJoin() {
    assertEquals(
        "",
        StringUtils.thenJoin(org.apache.commons.lang3.StringUtils::trimToNull, (String[]) null));
    assertEquals(
        "", StringUtils.thenJoin(org.apache.commons.lang3.StringUtils::trimToNull, (String) null));
    assertEquals(
        "", StringUtils.thenJoin(org.apache.commons.lang3.StringUtils::trimToNull, "", " "));
    assertEquals(
        "x", StringUtils.thenJoin(org.apache.commons.lang3.StringUtils::trimToNull, "", "  x "));
    assertEquals(
        "x y", StringUtils.thenJoin(org.apache.commons.lang3.StringUtils::trimToNull, "x", "  y "));
    assertEquals("x   y ", StringUtils.thenJoin(Function.identity(), "x", "  y "));
  }

  @Test
  public void testTrim() {
    assertEquals("str", StringUtils.trim(" str "));
    assertEquals("str StR", StringUtils.trim(" str StR "));
    assertEquals("STR  str", StringUtils.trim(" \n\u0085STR  str \u00A0\n\t"));
  }

  @Test
  public void testDeleteWhitespace() {
    assertEquals("str", StringUtils.deleteWhitespace(" str "));
    assertEquals("strStR", StringUtils.deleteWhitespace(" str StR "));
    assertEquals("STRstr", StringUtils.deleteWhitespace(" \n\u000CSTR\u00A0   str \u2007\n\t"));
  }
}
