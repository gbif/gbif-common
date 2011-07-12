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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author markus
 */
public class BomSafeInputStreamWrapperTest {
  @Test
  public void testOpenArchive() throws Exception {
    // test no bom
    InputStream in = new BomSafeInputStreamWrapper(new FileInputStream(
        FileUtils.getClasspathFile("charsets/utf-8_names.txt")));
    int x = in.read();
    int y = in.read();
    int z = in.read();
    in.close();
    assertEquals(35, x);
    assertEquals(35, y);
    assertEquals(35, z);

    in = new BomSafeInputStreamWrapper(new FileInputStream(FileUtils.getClasspathFile("charsets/utf-8_bom_names.txt")));
    x = in.read();
    y = in.read();
    z = in.read();
    in.close();
    assertEquals(35, x);
    assertEquals(35, y);
    assertEquals(35, z);

    in = new BomSafeInputStreamWrapper(new FileInputStream(FileUtils.getClasspathFile("charsets/utf-16LE_names.txt")));
    x = in.read();
    y = in.read();
    z = in.read();
    in.close();
    assertEquals(35, x);
    assertEquals(0, y);
    assertEquals(35, z);

    in = new BomSafeInputStreamWrapper(new FileInputStream(
        FileUtils.getClasspathFile("charsets/utf-16LE_bom_names.txt")));
    x = in.read();
    y = in.read();
    z = in.read();
    in.close();
    assertEquals(35, x);
    assertEquals(0, y);
    assertEquals(35, z);

    in = new BomSafeInputStreamWrapper(new FileInputStream(
        FileUtils.getClasspathFile("charsets/utf-16BE_bom_names.txt")));
    x = in.read();
    y = in.read();
    z = in.read();
    in.close();
    assertEquals(0, x);
    assertEquals(35, y);
    assertEquals(0, z);

    in = new BomSafeInputStreamWrapper(new FileInputStream(
        FileUtils.getClasspathFile("charsets/utf-16BE_bom_names.txt")));
    x = in.read();
    y = in.read();
    z = in.read();
    in.close();
    assertEquals(0, x);
    assertEquals(35, y);
    assertEquals(0, z);
  }
}
