package org.gbif.utils.text;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Utils class adding specific string methods to existing guava {@link Strings} and
 * commons {@link org.apache.commons.lang3.StringUtils}.
 */
public class StringUtils {
  public static final int LINNEAN_YEAR = 1751;
  private static final String CONS = "BCDFGHJKLMNPQRSTVWXYZ";
  private static final Pattern OCT = Pattern.compile("^[0-7]+$");
  private static final Pattern HEX = Pattern.compile("^[0-9abcdefABCDEF]+$");

  private static final String VOC = "AEIOU";
  private static Random rnd = new Random();

  private StringUtils() {
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
    return WordUtils.capitalize(randomString(rnd.nextInt(9) + 3)) + " " + randomString(rnd.nextInt(11) + 4).toLowerCase();
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
   * Joins a list of objects into a string, skipping null values and calling toString on each object.
   * @param delimiter to join the values with
   * @param values to be joined
   * @return
   */
  public static String joinIfNotNull(String delimiter, Object... values) {
    return Joiner.on(delimiter).skipNulls().join(values);
  }

  /**
   * Uppercases all keys and values in a Map.
   * If keys clash only one entry will remain, which is not guaranteed.
   *
   * @param map
   * @return new map with keys and values upper cased.
   */
  public static Map<String, String> upper(Map<String, String> map) {
    Map<String, String> upperMap = Maps.newHashMap();
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
   *
   * @param x
   * @return
   */
  public static String emptyLowerCase(String x) {
    if (Strings.isNullOrEmpty(x)) {
      return "";
    }
    return x.trim().toLowerCase();
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
