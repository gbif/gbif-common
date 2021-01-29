/***************************************************************************
 * Copyright 2014 Global Biodiversity Information Facility Secretariat
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

package org.gbif.utils.file;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing the data layout
 */
public class TestFileUtils {

  @Test
  public void testSourceRepo() throws MalformedURLException {
    URL url = new URL("http://www.gbif.org:8081/~markus/testdata/geobotany.pdf");
    assertTrue(FileUtils.toFilePath(url).equals("www.gbif.org:8081/~markus/testdata/geobotany.pdf"));

    url = new URL("ftp://ftp.gbif.org/testdata/markus/geobotany.pdf");
    assertTrue(FileUtils.toFilePath(url).equals("ftp.gbif.org/__ftp__/testdata/markus/geobotany.pdf"));
  }
}
