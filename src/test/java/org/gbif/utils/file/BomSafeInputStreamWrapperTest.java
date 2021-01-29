/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.utils.file;

import java.io.InputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.junit.jupiter.api.Test;
import org.xml.sax.ext.DefaultHandler2;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author markus
 */
public class BomSafeInputStreamWrapperTest {


  static SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();

  static {
    SAX_FACTORY.setNamespaceAware(true);
    SAX_FACTORY.setValidating(false);
  }

  /**
   * The Java SAX Parser is known to have problems with UTF8 file that contain a proper BOM markup:
   * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4508058
   * https://de.wikipedia.org/wiki/Byte_Order_Mark
   *
   *  Make sure the SAX Parser can handle any valid UTF files by using a BomSafeInputStreamWrapper stream.
   */
  @Test
  public void testSaxParser() throws Exception {
    SAXParser p = SAX_FACTORY.newSAXParser();
    for (String f : new String[]{"utf8","utf8bom","utf16le","utf16be"}) {
      String fn = "/sax/" + f + ".xml";
      System.out.println(fn);
      InputStream is = getClass().getResourceAsStream(fn);
      p.parse(is, new DefaultHandler2());

      is = new BOMInputStream(getClass().getResourceAsStream(fn));
      p.parse(is, new DefaultHandler2());
    }
  }

  @Test
  public void testUTF16Stream() throws Exception {
    // should be the exact same bytes

    byte[] b1 = IOUtils.toByteArray(getClass().getResourceAsStream("/sax/utf16le.xml"));
    byte[] b2 = IOUtils.toByteArray(new BOMInputStream(getClass().getResourceAsStream("/sax/utf16le.xml")));

    assertEquals(b1.length, b2.length);
    int idx=0;
    for (byte b : b1) {
      assertEquals(b, b2[idx++]);
    }
  }


  @Test
  public void testBomSafeInputStreamWrapper() throws Exception {
    // test no bom
    InputStream in = new BomSafeInputStreamWrapper(getClass().getResourceAsStream("/charsets/utf-8_names.txt"));
    int x = in.read();
    int y = in.read();
    int z = in.read();
    in.close();
    assertEquals(35, x);
    assertEquals(35, y);
    assertEquals(35, z);

    in = new BomSafeInputStreamWrapper(getClass().getResourceAsStream("/charsets/utf-8_bom_names.txt"));
    x = in.read();
    y = in.read();
    z = in.read();
    in.close();
    assertEquals(35, x);
    assertEquals(35, y);
    assertEquals(35, z);

    in = new BomSafeInputStreamWrapper(getClass().getResourceAsStream("/charsets/utf-16LE_names.txt"));
    x = in.read();
    y = in.read();
    z = in.read();
    in.close();
    assertEquals(35, x);
    assertEquals(0, y);
    assertEquals(35, z);

    in =
      new BomSafeInputStreamWrapper(getClass().getResourceAsStream("/charsets/utf-16LE_bom_names.txt"));
    x = in.read();
    y = in.read();
    z = in.read();
    in.close();
    assertEquals(35, x);
    assertEquals(0, y);
    assertEquals(35, z);

    in =
      new BomSafeInputStreamWrapper(getClass().getResourceAsStream("/charsets/utf-16BE_bom_names.txt"));
    x = in.read();
    y = in.read();
    z = in.read();
    in.close();
    assertEquals(0, x);
    assertEquals(35, y);
    assertEquals(0, z);

    in =
      new BomSafeInputStreamWrapper(getClass().getResourceAsStream("/charsets/utf-16BE_bom_names.txt"));
    x = in.read();
    y = in.read();
    z = in.read();
    in.close();
    assertEquals(0, x);
    assertEquals(35, y);
    assertEquals(0, z);
  }
}
