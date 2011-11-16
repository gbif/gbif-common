/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;


/**
 * @author markus
 *
 */
public class DateUtilsTest {

  @Test
  public void testParse() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2010, Calendar.FEBRUARY, 2);
    assertEquals(cal.getTime(), DateUtils.parse("2010-02-02"));

    // testing pre 1900 dates
    cal.set(1847, Calendar.DECEMBER, 24);
    assertEquals(cal.getTime(), DateUtils.parse("1847-12-24"));

    // testing extra whitespace
    assertEquals(cal.getTime(), DateUtils.parse("1847 - 12 - 24"));

    // testing various delimiter
    assertEquals(cal.getTime(), DateUtils.parse("1847 / 12 / 24"));
    assertEquals(cal.getTime(), DateUtils.parse("1847.12.24"));

    // testing year parsing alone
    assertEquals(47, DateUtils.parse("1947").getYear());
    assertEquals(-53, DateUtils.parse(" 1847").getYear());

    assertTrue(DateUtils.parse(null)==null);
    assertTrue(DateUtils.parse("")==null);
    assertTrue(DateUtils.parse("  ")==null);
  }

  @Test
  public void testParseCalendarDate() {

    try {
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(2010, Calendar.FEBRUARY, 2);
      assertEquals(cal.getTime(), DateUtils.calendarDate("2010-02-02"));

      // testing pre 1900 dates
      cal.set(1847, Calendar.DECEMBER, 24);
      assertEquals(cal.getTime(), DateUtils.calendarDate("1847-12-24"));

      // testing extra whitespace
      assertEquals(cal.getTime(), DateUtils.calendarDate("1847 - 12 - 24"));

      // testing various delimiter
      assertEquals(cal.getTime(), DateUtils.calendarDate("1847 / 12 / 24"));
      assertEquals(cal.getTime(), DateUtils.calendarDate("1847.12.24"));

      // testing year parsing alone
      assertEquals(47, DateUtils.calendarDate("1947").getYear());
      assertEquals(-53, DateUtils.calendarDate(" 1847").getYear());

      assertTrue(DateUtils.calendarDate(null)==null);
      assertTrue(DateUtils.calendarDate("")==null);
      assertTrue(DateUtils.calendarDate("  ")==null);

      boolean failed=false;
      try {
        DateUtils.calendarDate("123s2");
      } catch (ParseException e) {
        // expected
        failed=true;
      }
      if (!failed){
        fail();
      }

    } catch (ParseException e) {
      e.printStackTrace();
      fail();
    }
  }


  @Test
  public void testParseXmlSchemaDate() {

    try {
      Calendar cal = Calendar.getInstance();
      cal.clear();
      TimeZone tz = new SimpleTimeZone(1000*60*60,"berlin");
      cal.setTimeZone(tz);
      cal.set(2002, Calendar.OCTOBER, 23, 18, 13, 51);
      cal.set(Calendar.MILLISECOND, 235);
      assertEquals(cal.getTime(), DateUtils.schemaDateTime("2002-10-23T18:13:51.235+01:00"));
      assertEquals(cal.getTime(), DateUtils.schemaDateTime("2002-10-23T17:23:51.235+00:10"));
      assertEquals(cal.getTime(), DateUtils.schemaDateTime("2002-10-23T10:43:51.235-06:30"));

    } catch (ParseException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testTimezone() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2002, Calendar.OCTOBER, 23, 18, 13, 51);
    SimpleTimeZone tz = new SimpleTimeZone(1000*60*60,"berlin");
    cal.setTimeZone(tz);

    Calendar cal2 = Calendar.getInstance();
    cal2.clear();
    cal2.set(2002, Calendar.OCTOBER, 23, 17, 13, 51);
    SimpleTimeZone tz2 = new SimpleTimeZone(0,"green witch");
    cal2.setTimeZone(tz2);

    assertEquals(cal.getTimeInMillis(), cal2.getTimeInMillis());

  }
}
