/**
 *
 */
package org.gbif.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility to split files into chucks.
 * This is done on file size, but then extended to read to the end of the current line.
 * Therefore a chunksize of 32meg will result in a split files of slightly more (assuming lines are
 * not very long).
 * This is done using NIO libraries for high performance.
 */
public class FileSplitter {

  private static final Logger LOG = LoggerFactory.getLogger(FileSplitter.class);
  public static final String SEPARATOR = "_";
  public static final int READ_AHEAD_BYTES = 256;

  // for the file, gives the Byte markers for reading lines, such that the lines read will approximately
  // equate be the chunk size (slightly more as it reads to the end of the row)
  public static List<Long> scanToChunk(File from, long chunkSizeBytes) throws IOException {
    List<Long> chunkBytes = new ArrayList<Long>();
    FileInputStream fis = new FileInputStream(from);
    FileChannel fcin = fis.getChannel();

    long byteCount = chunkSizeBytes;

    // now we need to read and transfer to the end of the line...
    ByteBuffer bb = ByteBuffer.allocate(READ_AHEAD_BYTES);

    while (byteCount < fcin.size()) {
      fcin.read(bb, byteCount);
      int i = 0;
      for (i = 0; i < bb.limit(); i++) {
        if ((char) bb.get(i) == '\n') {
          i++;
          break;
        }
      }
      // bb.rewind();
      chunkBytes.add(i + byteCount);
      byteCount += chunkSizeBytes;
    }
    fcin.close();
    fis.close();
    return chunkBytes;
  }

  /**
   * Splits a file "pumaConcolor.txt" into the target directory using the suffix ("part") like so:
   * - pumaConcolor_part_0.txt
   * - pumaConcolor_part_1.txt
   * - pumaConcolor_part_2.txt
   * Returns the files parts
   */
  public static List<File> split(File from, File targetDirectory, String suffix, long chunkSizeBytes)
    throws IOException {
    List<File> files = new ArrayList<File>();
    FileInputStream fis = new FileInputStream(from);
    FileChannel fcin = fis.getChannel();

    String filePartNamePrefix = "";
    String filePartNameSuffix = "";
    if (from.getName().contains(".")) {
      filePartNamePrefix = from.getName().substring(0, from.getName().indexOf("."));
      filePartNameSuffix = from.getName().substring(from.getName().indexOf("."));
    } else {
      filePartNamePrefix = from.getName();
    }

    long byteCount = 0;
    int filePartCount = 0;

    while (byteCount < fcin.size()) {
      long time = System.currentTimeMillis();
      // create the output file
      String fileName = filePartNamePrefix + SEPARATOR + suffix + SEPARATOR + filePartCount + filePartNameSuffix;
      File to = new File(targetDirectory, fileName);
      files.add(to);

      // copy to the new file
      FileOutputStream fos = new FileOutputStream(to);
      FileChannel fcout = fos.getChannel();
      fcin.transferTo(byteCount, chunkSizeBytes, fcout);
      byteCount += chunkSizeBytes;

      // now we need to read and transfer to the end of the line...
      ByteBuffer bb = ByteBuffer.allocate(READ_AHEAD_BYTES);
      fcin.read(bb, byteCount);
      int i = 0;
      for (i = 0; i < bb.limit(); i++) {
        if ((char) bb.get(i) == '\n') {
          i++;
          break;
        }
      }
      bb.rewind();
      bb.limit(i);
      fcout.write(bb);
      byteCount += i;

      fcout.close();
      fos.close();
      filePartCount++;
      LOG.debug("Filepart[" + fileName + "] created in " + (1 + System.currentTimeMillis() - time) / 1000 + " secs");
    }

    // TODO - have tested but need to test thoroughly...
    // what if the file was smaller than 32 meg?
    // what if the chunk size was exactly the file size?
    // did the joins line up properly?
    // what if the read ahead line did not get to the end of the line?

    fcin.close();
    fis.close();
    return files;
  }
}
