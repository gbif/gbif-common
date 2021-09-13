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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Utils class adding specific string methods to existing guava Strings and
 * commons {@link org.apache.commons.lang3.StringUtils}.
 */
public final class StringUtils {

  private static final Pattern MARKER = Pattern.compile("\\p{M}");
  public static final int LINNEAN_YEAR = 1751;
  private static final String CONS = "BCDFGHJKLMNPQRSTVWXYZ";
  private static final Pattern OCT = Pattern.compile("^[0-7]+$");
  private static final Pattern HEX = Pattern.compile("^[0-9abcdefABCDEF]+$");

  private static final String VOC = "AEIOU";
  private static Random rnd = new Random();

  private StringUtils() {
  }

  /**
   * Removes accents & diacretics and converts ligatures into several chars
   * @param x string to fold into ASCII
   * @return string converted to ASCII equivalent, expanding common ligatures
   */
  public static String foldToAscii(String x) {
    if (x == null) {
      return null;
    }
    x = replaceSpecialCases(x);
    // use java unicode normalizer to remove accents
    x = Normalizer.normalize(x, Normalizer.Form.NFD);
    return MARKER.matcher(x).replaceAll("");
  }

  /**
   * Apply a function then join the result using a space if not null.
   * E.g. can be used with apache.commons.lang3.StringUtils::trimToNull to compose a name when some parts are
   * optionals.
   *
   * @param fct   the function to apply or Function.identity() if none
   * @param parts
   *
   * @return a String that represents all parts joined by a space or empty String. Never null.
   */
  public static String thenJoin(Function<String, String> fct, String... parts) {
    Objects.requireNonNull(fct, "fct shall be provided, use Function.identity() is you want to use the String as is");
    return Arrays.stream(parts != null ? parts : new String[0])
            .map(fct)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
  }

  /**
   * The Normalizer misses a few cases and 2 char ligatures which we deal with here
   */
  private static String replaceSpecialCases(String x) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < x.length(); i++) {
      char c = x.charAt(i);
      switch (c) {
        case 'ß':
          sb.append("ss");
          break;
        case 'Æ':
          sb.append("AE");
          break;
        case 'æ':
          sb.append("ae");
          break;
        case 'Ð':
          sb.append("D");
          break;
        case 'đ':
          sb.append("d");
          break;
        case 'ð':
          sb.append("d");
          break;
        case 'Ø':
          sb.append("O");
          break;
        case 'ø':
          sb.append("o");
          break;
        case 'Œ':
          sb.append("OE");
          break;
        case 'œ':
          sb.append("oe");
          break;
        case 'Ŧ':
          sb.append("T");
          break;
        case 'ŧ':
          sb.append("t");
          break;
        case 'Ł':
          sb.append("L");
          break;
        case 'ł':
          sb.append("l");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Increase a given string by 1, i.e. increase the last char in that string by one.
   * If its a z or Z the char before is increased instead and a new char a is appended.
   * Only true letters are increased, but spaces, punctuation or numbers remain unchanged.
   * Null values stay null and empty strings empty.
   * The case of existing characters will be kept and the appended chars will use the case of the last char of the
   * original string.
   *
   * For example "Carlb" becomes "Carla", "Aua" "Atz", "zZz" "aAaa" or "Abies zzz" "Abiet aaa".
   *
   * @param x
   * @return
   */
  public static String increase(String x) {
    if (x == null) {
      return null;
    }
    if (x.equals("")) {
      return x;
    }

    char[] chars = x.toCharArray();
    int idx = chars.length - 1;
    boolean appendingNeeded = false;
    Character lastOriginalChar = null;

    while (idx >= 0){
      char c = chars[idx];
      if (!Character.isLetter(c)){
        idx--;
        continue;
      }

      if (lastOriginalChar == null){
        lastOriginalChar = c;
      }

      if (c == 'z'){
        chars[idx] = 'a';
        appendingNeeded = true;

      } else if (c == 'Z'){
        chars[idx] = 'A';
        appendingNeeded = true;

      } else {
        c++;
        chars[idx] = c;
        appendingNeeded = false;
        break;
      }
      idx--;
    }

    // first char, also append to end
    if (appendingNeeded){
      char append = (lastOriginalChar==null || Character.isLowerCase(lastOriginalChar)) ? 'a' : 'A';
      return String.valueOf(chars) + append;

    } else {
      return String.valueOf(chars);
    }
  }

  /**
   * Creates a random species binomial with no meaning at all, but highly randomized.
   *
   * @return a random canonical species name
   */
  public static String randomSpecies() {
    return randomGenus() + " " + randomEpithet();
  }

  public static String randomGenus() {
    return WordUtils.capitalize(randomString(rnd.nextInt(9) + 3).toLowerCase());
  }

  public static String randomEpithet() {
    return randomString(rnd.nextInt(12) + 4).toLowerCase();
  }
  public static String randomFamily() {
      return WordUtils.capitalize(StringUtils.randomString(rnd.nextInt(15) + 5).toLowerCase()) + "idae";
  }

  public static String randomAuthor() {
    return WordUtils.capitalize(StringUtils.randomString(rnd.nextInt(12) + 1).toLowerCase());
  }

  /**
   * Creates a random string in upper case of given length with purely latin characters only.
   * Vocals are used much more frequently than consonants
   * @param len
   * @return a random string in upper case
   */
  public static String randomString(int len) {
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      if (rnd.nextInt(3) > 1) {
        sb.append(CONS.charAt(rnd.nextInt(CONS.length())));
      } else {
        sb.append(VOC.charAt(rnd.nextInt(VOC.length())));
      }
    }

    return sb.toString();
  }

  /**
   * @return a year since Linnéan times 1751 before now as a 4 character long string
   */
  public static String randomSpeciesYear() {
    int maxYear = Calendar.getInstance().get(Calendar.YEAR);
    return String.valueOf(LINNEAN_YEAR + rnd.nextInt(maxYear - LINNEAN_YEAR + 1));
  }

  /**
   * Simple integer parsing method that does not throw any exception but
   * returns null instead.
   *
   * @param x
   * @return the parsed integer or null
   */
  public static Integer parseInteger(String x) {
    try {
      return Integer.valueOf(x);
    } catch (NumberFormatException e) {

    }
    return null;
  }

  /**
   * Simple boolean parsing method that understands yes,y,true,t or 1 as true and respective values for false.
   * It does not throw any exception but returns null instead.
   *
   * @param x
   * @return the parsed integer or null
   */
  public static Boolean parseBoolean(String x) {
    x = org.apache.commons.lang3.StringUtils.trimToEmpty(x).toLowerCase();
    if (x.equals("true") || x.equals("t") || x.equals("1") || x.equals("yes") || x.equals("y")) {
      return true;
    }
    if (x.equals("false") || x.equals("f") || x.equals("0") || x.equals("no") || x.equals("n")) {
      return false;
    }
    return null;
  }

  /**
   * Unescapes various unicode escapes if existing:
   *
   * java unicode escape, four hexadecimal digits
   * \ uhhhh
   *
   * octal escape
   * \nnn
   * The octal value nnn, where nnn stands for 1 to 3 digits between ‘0’ and ‘7’. For example, the code for the ASCII
   * ESC (escape) character is ‘\033’.
   *
   * hexadecimal escape
   * \xhh...
   * The hexadecimal value hh, where hh stands for a sequence of hexadecimal digits (‘0’–‘9’, and either ‘A’–‘F’ or
   * ‘a’–‘f’).Like the same construct in ISO C, the escape sequence continues until the first nonhexadecimal digit is seen.
   * However, using more than two hexadecimal digits produces undefined results. (The ‘\x’ escape sequence is not allowed
   * in POSIX awk.)
   *
   * @param text string potentially containing unicode escape chars
   * @return the unescaped string
   */
  public static String unescapeUnicodeChars(String text) {
    if (text == null) {
      return null;
    }
    // replace unicode, hexadecimal or octal character encodings by iterating over the chars once
    //
    // java unicode escape, four hexadecimal digits
    // \ uhhhh
    //
    // octal escape
    // \nnn
    // The octal value nnn, where nnn stands for 1 to 3 digits between ‘0’ and ‘7’. For example, the code for the ASCII
    // ESC (escape) character is ‘\033’.
    //
    // hexadecimal escape
    // \xhh...
    // The hexadecimal value hh, where hh stands for a sequence of hexadecimal digits (‘0’–‘9’, and either ‘A’–‘F’ or
    // ‘a’–‘f’).
    // Like the same construct in ISO C, the escape sequence continues until the first nonhexadecimal digit is seen.
    // However, using more than two hexadecimal digits produces undefined results. (The ‘\x’ escape sequence is not allowed
    // in POSIX awk.)
    int i = 0, len = text.length();
    char c;
    StringBuffer sb = new StringBuffer(len);
    while (i < len) {
      c = text.charAt(i++);
      if (c == '\\') {
        if (i < len) {
          c = text.charAt(i++);
          try {
            if (c == 'u' && text.length() >= i + 4) {
              // make sure we have only hexadecimals
              String hex = text.substring(i, i + 4);
              if (HEX.matcher(hex).find()) {
                c = (char) Integer.parseInt(hex, 16);
                i += 4;
              } else {
                throw new NumberFormatException("No hex value: " + hex);
              }
            } else if (c == 'n' && text.length() >= i + 2) {
              // make sure we have only 0-7 digits
              String oct = text.substring(i, i + 2);
              if (OCT.matcher(oct).find()) {
                c = (char) Integer.parseInt(oct, 8);
                i += 2;
              } else {
                throw new NumberFormatException("No octal value: " + oct);
              }
            } else if (c == 'x' && text.length() >= i + 2) {
              // make sure we have only hexadecimals
              String hex = text.substring(i, i + 2);
              if (HEX.matcher(hex).find()) {
                c = (char) Integer.parseInt(hex, 16);
                i += 2;
              } else {
                throw new NumberFormatException("No hex value: " + hex);
              }
            } else if (c == 'r' || c == 'n' || c == 't') {
              // escaped newline or tab. Replace with simple space
              c = ' ';
            } else {
              throw new NumberFormatException("No char escape");
            }
          } catch (NumberFormatException e) {
            // keep original characters including \ if escape sequence was invalid
            // but replace \n with space instead
            if (c == 'n') {
              c = ' ';
            } else {
              c = '\\';
              i--;
            }
          }
        }
      } // fall through: \ escapes itself, quotes any character but u
      sb.append(c);
    }
    return sb.toString();
  }

  /**
   * Tries to decode a UTF8 string only if common UTF8 character combinations are found which are unlikely to be correctly encoded text.
   * E.g. Ã¼ is the German Umlaut ü and indicates we have encoded utf8 text still.
   */
  public static String decodeUtf8Garbage(String text) {
    Pattern UTF8_TEST = Pattern.compile("(Ã¤|Ã¼|Ã¶|Ã\u0084|Ã\u009C|Ã\u0096|" + // äüöÄÜÖ
        "Ã±|Ã¸|Ã§|Ã®|Ã´|Ã»|Ã\u0091|Ã\u0098|Ã\u0087|Ã\u008E|Ã\u0094|Ã\u009B"  + // ñøçîôûÑØÇÎÔÛ
        "Ã¡|Ã©|Ã³|Ãº|Ã\u00AD|Ã\u0081|Ã\u0089|Ã\u0093|Ã\u009A|Ã\u008D)"         // áéóúíÁÉÓÚÍ
        , Pattern.CASE_INSENSITIVE);
    if (text != null && UTF8_TEST.matcher(text).find()) {
      // typical utf8 combinations found. Try to decode from latin1 to utf8
      byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
      final CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder();
      ByteBuffer buffer = ByteBuffer.wrap(bytes);
      try {
        return utf8Decoder.decode(buffer).toString();
      } catch (CharacterCodingException e) {
        // maybe wasnt a good idea, return original
      }
    }
    return text;
  }

  /**
   * Joins a list of objects into a string, skipping null values and calling toString on each object.
   * @param delimiter to join the values with
   * @param values to be joined
   * @return
   */
  public static String joinIfNotNull(String delimiter, Object... values) {
    return Arrays.stream(values)
        .filter(Objects::nonNull)
        .map(Object::toString)
        .collect(Collectors.joining(delimiter));
  }

  /**
   * Uppercases all keys and values in a Map.
   * If keys clash only one entry will remain, which is not guaranteed.
   *
   * @param map
   * @return new map with keys and values upper cased.
   */
  public static Map<String, String> upper(Map<String, String> map) {
    Map<String, String> upperMap = new HashMap<>();
    for (String k : map.keySet()) {
      String v = map.get(k);
      if (v != null) {
        v = v.trim().toUpperCase();
      }
      upperMap.put(k.toUpperCase(), v);
    }
    return upperMap;
  }

  /**
   * Returns an empty string or the trimmed lower case version of any input, but never NULL.
   */
  public static String emptyLowerCase(String str) {
    return org.apache.commons.lang3.StringUtils.trimToEmpty(str).toLowerCase();
  }

  /**
   * Reads a stack trace from an exception and returns it as a String.
   * @param aThrowable
   * @return teh full stack trace as a String
   */
  public static String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

}
