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

import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper for an input stream that removes UTF8 BOM sequences at the start of the file.
 * UTF8 BOMs can cause XML parser to fall over with a "Content is not allowed in prolog" Exception.
 * See:
 * <ul>
 *  <li>http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4508058</li>
 *  <li>https://de.wikipedia.org/wiki/Byte_Order_Mark</li>
 * </ul>
 *
 * @deprecated use org.apache.commons.io.input.BOMInputStream instead
 */
@Deprecated
public class BomSafeInputStreamWrapper extends InputStream {

  private static final int BUFFER_SIZE = 4;
  private final InputStream stream;
  private final byte[] buffer = new byte[BUFFER_SIZE];
  private int pointer = 0;

  public BomSafeInputStreamWrapper(InputStream stream) {
    this.stream = stream;
    skipBom();
  }

  @Override
  public int read() throws IOException {
    if (pointer < BUFFER_SIZE) {
      pointer++;
      return buffer[pointer - 1];
    } else {
      return stream.read();
    }
  }

  private void skipBom() {
    try {
      stream.read(buffer, 0, BUFFER_SIZE);
      if (CharsetDetection.hasUTF16BEBom(buffer) || CharsetDetection.hasUTF16LEBom(buffer)) {
        // SQX Parser handles UTF16 BOMs fine
        pointer = 2;
      } else if (CharsetDetection.hasUTF8Bom(buffer)) {
        pointer = 3;
      }
    } catch (IOException ignored) {
    }
  }

}
