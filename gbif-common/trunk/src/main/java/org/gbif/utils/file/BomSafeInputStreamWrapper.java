package org.gbif.utils.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper for an input stream that removes any BOM sequence at the start of the file.
 * BOMs can cause XML parser to fall over with a "Content is not allowed in prolog" Exception
 *
 * @author markus
 */
public class BomSafeInputStreamWrapper extends InputStream {

  private static final int buffersize = 4;
  private InputStream stream;
  private byte[] buffer = new byte[buffersize];
  private int pointer = 0;

  public BomSafeInputStreamWrapper(InputStream stream) {
    this.stream = stream;
    testForBom();
  }

  @Override
  public int read() throws IOException {
    if (pointer < buffersize) {
      pointer++;
      return buffer[pointer - 1];
    } else {
      return stream.read();
    }
  }

  private void testForBom() {
    try {
      stream.read(buffer, 0, buffersize);
      if (CharsetDetection.hasUTF16BEBom(buffer) || CharsetDetection.hasUTF16LEBom(buffer)) {
        pointer = 2;
      } else if (CharsetDetection.hasUTF8Bom(buffer)) {
        pointer = 3;
      }
    } catch (IOException ignored) {
    }
  }

}
