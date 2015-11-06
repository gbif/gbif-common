package org.gbif.utils.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ResourcesUtilTest {

  private InputStreamUtils isu = new InputStreamUtils();

  @Test
  public void testCopyResources() throws Exception {
    File tmp = FileUtils.createTempDir();
    tmp.deleteOnExit();
    ResourcesUtil.copy(tmp, "", true, "testNOT_EXISTING.txt", "test1/test.txt", "test1/test2/test.txt");

    // test
    File t1 = new File(tmp, "test1/test.txt");
    File t2 = new File(tmp, "test1/test2/test.txt");
    org.apache.commons.io.FileUtils.contentEquals(t1, t2);
    assertTestFile(t1);
    assertTestFile(t2);
  }

  @Test(expected = IOException.class)
  public void testCopyResourcesThrowing() throws Exception {
    File tmp = FileUtils.createTempDir();
    tmp.deleteOnExit();
    ResourcesUtil.copy(tmp, "", false, "testNOT_EXISTING.txt", "test1/test.txt", "test1/test2/test.txt");

    // test
    File t1 = new File(tmp, "test1/test.txt");
    File t2 = new File(tmp, "test1/test2/test.txt");
    org.apache.commons.io.FileUtils.contentEquals(t1, t2);
    assertTestFile(t1);
    assertTestFile(t2);
  }


  @Test
  public void testList() throws Exception {
    assertArrayEquals(new String[]{"test.txt", "test2"}, ResourcesUtil.list(ResourcesUtil.class, "test1"));
    assertArrayEquals(new String[]{"test.txt"}, ResourcesUtil.list(ResourcesUtil.class, "test1/test2"));
    assertArrayEquals(new String[]{"utf16be.xml", "utf16le.xml", "utf8.xml", "utf8bom.xml"}, ResourcesUtil.list(ResourcesUtil.class, "sax"));
    assertArrayEquals(new String[]{}, ResourcesUtil.list(ResourcesUtil.class, "abba"));
  }

  private void assertTestFile(File tf) throws FileNotFoundException {
    assertEquals("hallo\n", isu.readEntireStream(FileUtils.getInputStream(tf)));
  }
}
