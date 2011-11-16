package org.gbif.metadata;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author markus
 */
public class DateUtils {

  public static ThreadSafeSimpleDateFormat isoDateFormat = new ThreadSafeSimpleDateFormat("yyyy-MM-dd");  //2010-02-22

  public static List<ThreadSafeSimpleDateFormat> allDateFormats = new ArrayList<ThreadSafeSimpleDateFormat>(10);

  static {
    allDateFormats.add(new ThreadSafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));  // 2001-07-04T12:08:56.235-0700
    allDateFormats.add(new ThreadSafeSimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ"));  // 2001-07-04 12:08:56.235-0700
    allDateFormats.add(new ThreadSafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"));  // 2001-07-04T12:08:56.235
    allDateFormats.add(new ThreadSafeSimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));  // 2001-07-04 12:08:56.235
    allDateFormats.add(new ThreadSafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));  // 2001-07-04T12:08:56
    allDateFormats.add(new ThreadSafeSimpleDateFormat("yyyy-MM-dd HH:mm:ss"));  // 2001-07-04 12:08:56
    allDateFormats.add(new ThreadSafeSimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z"));  // Wed, 4 Jul 2001 12:08:56 -0700
    allDateFormats.add(new ThreadSafeSimpleDateFormat("EEE, d MMM yyyy HH:mm:ss"));  // Wed, 4 Jul 2001 12:08:56
    allDateFormats.add(isoDateFormat);
    allDateFormats.add(new ThreadSafeSimpleDateFormat("yyyy.MM.dd"));  // 2010.03.22
    allDateFormats.add(new ThreadSafeSimpleDateFormat("dd.MM.yyyy"));  // 22.03.2010
    allDateFormats.add(new ThreadSafeSimpleDateFormat("yyyy/MM/dd"));  // 2010/02/24
//allDateFormats.add(new ThreadSafeSimpleDateFormat("MM/dd/yyyy"));  // 02/24/2010
  }

  /**
   * Utility to parse an EML calendarDate in a textual format. Can be ISO date or just the year, ignoring whitespace
   *
   * @param dateString To set
   *
   * @return the parsed date
   *
   * @throws ParseException Should it be an erroneous format
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-coverage.html#calendarDate">EML Coverage
   *      calendarDate keyword</a>
   */
  public static Date calendarDate(String dateString) throws ParseException {
    if (StringUtils.isBlank(dateString)) {
      return null;
    }
    // kill whitespace
    dateString = dateString.replaceAll("\\s", "");
    dateString = dateString.replaceAll("[\\,._#//]", "-");
    Date date;
    try {
      ThreadSafeSimpleDateFormat sdf = new ThreadSafeSimpleDateFormat("yyyy-MM-dd");
      date = sdf.parse(dateString);
    } catch (ParseException e) {
      if (dateString.length() == 4) {
        ThreadSafeSimpleDateFormat sdf = new ThreadSafeSimpleDateFormat("yyyy");
        date = sdf.parse(dateString);
        date = new Date(date.getTime() + 1);
      } else {
        throw e;
      }
    }
    return date;
  }

  /**
   * Parses a string date trying various common formats, starting with the most complex ones
   *
   * @param x the date as a string or null if not parsable
   */
  public static Date parse(String x) {
    if (StringUtils.isBlank(x)){
      return null;
    }
    Date date = null;
    for (ThreadSafeSimpleDateFormat df : allDateFormats) {
      try {
        // alternatively try others
        date = df.parse(x);
        break;
      } catch (ParseException ignored) {
      }
    }
    // if date is still null, try schema date
    if (date == null) {
      try {
        date = schemaDateTime(x);
      } catch (ParseException ignored) {
      }
    }
    return date;
  }

  /**
   * Parses a string date trying the given preferred format first
   *
   * @param x date as a string
   */
  public static Date parse(String x, ThreadSafeSimpleDateFormat preferredFormat) {
    Date date = null;
    try {
      // first try with preferred format
      date = preferredFormat.parse(x);
    } catch (ParseException ignored) {
      date = parse(x);
    }
    return date;
  }

  /**
   * Parses a string date trying the ISO format first
   *
   * @param x the date as a string
   */
  public static Date parseIso(String x) {
    return parse(x, isoDateFormat);
  }

  /**
   * Utility to parse an XML schema datetime in a textual format
   *
   * @param dateString To set
   *
   * @throws ParseException Should it be an erroneous format
   */
  public static Date schemaDateTime(String dateString) throws ParseException {
    dateString = StringUtils.trimToEmpty(dateString);
    Date date;
    try {
      Pattern timezone = Pattern.compile("([+-]\\d\\d:\\d\\d)$");
      dateString = timezone.matcher(dateString).replaceAll("GMT$1");
      ThreadSafeSimpleDateFormat sdf = new ThreadSafeSimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ");
      date = sdf.parse(dateString);
    } catch (ParseException ignored) {
      try {
        ThreadSafeSimpleDateFormat sdf = new ThreadSafeSimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        date = sdf.parse(dateString);
      } catch (ParseException e1) {
        date = calendarDate(dateString);
      }
    }
    return date;
  }
}
