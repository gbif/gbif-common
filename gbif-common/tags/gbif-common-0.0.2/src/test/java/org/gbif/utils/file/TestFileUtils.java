package org.gbif.utils.file;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Testing the data layout
 */
public class TestFileUtils {

  @Test
  public void testSourceRepo() throws MalformedURLException {
    URL url = new URL("http://www.gbif.org:8081/~markus/testdata/geobotany.pdf");
    System.out.println(FileUtils.toFilePath(url));
    assertTrue(FileUtils.toFilePath(url).equals("www.gbif.org:8081/~markus/testdata/geobotany.pdf"));

    url = new URL("ftp://ftp.gbif.org/testdata/markus/geobotany.pdf");
    System.out.println(FileUtils.toFilePath(url));
    assertTrue(FileUtils.toFilePath(url).equals("ftp.gbif.org/__ftp__/testdata/markus/geobotany.pdf"));
  }
}
