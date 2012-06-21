package org.gbif.utils.file;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResourcesUtilTest {

  private InputStreamUtils isu = new InputStreamUtils();

  @Test
  public void testCopyResourcesRecursively() throws Exception {
    File tmp = FileUtils.createTempDir();
    tmp.deleteOnExit();
    ResourcesUtil.copy(tmp, "", "test.txt", "test1/test.txt", "test1/test2/test.txt");

    // test
    File t1 = new File(tmp, "test1/test.txt");
    File t2 = new File(tmp, "test1/test2/test.txt");
    org.apache.commons.io.FileUtils.contentEquals(t1, t2);
    assertTestFile(t1);
    assertTestFile(t2);
  }

  private void assertTestFile(File tf) throws FileNotFoundException {
    assertEquals("hallo\n", isu.readEntireStream(FileUtils.getInputStream(tf)));
  }
}
