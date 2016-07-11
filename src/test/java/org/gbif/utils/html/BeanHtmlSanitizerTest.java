package org.gbif.utils.html;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for BeanHtmlSanitizer.
 *
 */
public class BeanHtmlSanitizerTest {

  @Test
  public void testBeanHtmlSanitizer(){
    //Pre-packaged HTML sanitizer policies that allows common block elements including <p>, <h1>, etc.
    PolicyFactory policy = Sanitizers.BLOCKS;
    TestBean testBean = new TestBean();
    testBean.setCount(1);
    testBean.setComment("Help to find your way on the Internet");
    testBean.setUnsafeHtml("<p>      <a href=\"http://perdu.com/\" class=\"important\">" +
            "        Help" +
            "      </a></p>");

    //demonstrate that inner beans are NOT sanitized
    TestInnerBean innerBean = new TestInnerBean();
    innerBean.setInnerString("<h1><script type=\"text/javascript\">echo(\"oops\");<script></h1");
    testBean.setInnerBean(innerBean);

    BeanHtmlSanitizer.sanitize(testBean, policy);

    assertEquals("<p>Help</p>", testBean.getUnsafeHtml().replaceAll(" ", ""));
    //ensure other fields were untouched
    assertEquals("Help to find your way on the Internet", testBean.getComment());
    assertEquals(1, testBean.getCount());
    assertEquals("<h1><script type=\"text/javascript\">echo(\"oops\");<script></h1", testBean.getInnerBean().getInnerString());

    // but we can sanitize the inner bean explicitly
    BeanHtmlSanitizer.sanitize(innerBean, policy);
    assertEquals("<h1></h1>", testBean.getInnerBean().getInnerString());
  }

  @Test
  public void testBeanHtmlSanitizerExclusionList(){
    PolicyFactory policy = Sanitizers.BLOCKS;
    TestBean testBean = new TestBean();
    testBean.setCount(1);
    testBean.setComment("Help to find your way on the Internet");
    testBean.setUnsafeHtml("<p>      <a href=\"http://perdu.com/\" class=\"important\">" +
            "        Help" +
            "      </a></p>");
    // ask to exclude "unsafeHtml" property from sanitization
    BeanHtmlSanitizer.sanitize(testBean, policy, ImmutableSet.of("unsafeHtml"));
    assertTrue(testBean.getUnsafeHtml().contains("<a href=\"http://perdu.com/\" class=\"important\">"));
  }


  /**
   * Requires to be public for testing BeanHtmlSanitizer.
   *
   */
  public static class TestBean{
    private String unsafeHtml;
    private String comment;
    private int count;

    private TestInnerBean innerBean;

    public String getUnsafeHtml() {
      return unsafeHtml;
    }

    public void setUnsafeHtml(String unsafeHtml) {
      this.unsafeHtml = unsafeHtml;
    }

    public String getComment() {
      return comment;
    }

    public void setComment(String comment) {
      this.comment = comment;
    }

    public int getCount() {
      return count;
    }

    public void setCount(int count) {
      this.count = count;
    }

    public TestInnerBean getInnerBean() {
      return innerBean;
    }

    public void setInnerBean(TestInnerBean innerBean) {
      this.innerBean = innerBean;
    }

  }

  public static class TestInnerBean{
    private String innerString;

    public String getInnerString() {
      return innerString;
    }

    public void setInnerString(String innerString) {
      this.innerString = innerString;
    }
  }
}
