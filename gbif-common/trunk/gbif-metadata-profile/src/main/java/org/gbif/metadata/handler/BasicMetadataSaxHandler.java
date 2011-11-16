package org.gbif.metadata.handler;

import org.gbif.metadata.BasicMetadataImpl;

/**
 * A simple metadata sax base handler that collects all character data inside elements into a string buffer, resetting
 * the buffer with every element start and storing the string version of the buffer in this.content when the end of the
 * element is reached.
 * Make sure to call the super methods when implementing this handler!
 * 
 * @author markus
 * 
 */
public abstract class BasicMetadataSaxHandler extends SimpleSaxHandler {
  protected BasicMetadataImpl bm;
  protected static final String NS_DC = "http://purl.org/dc/terms/";
  protected static final String NS_DCTERMS = "http://purl.org/dc/elements/1.1/";

  @Override
  public void startDocument() {
    super.startDocument();
    bm = new BasicMetadataImpl();
  }

  public BasicMetadataImpl yield() {
    return bm;
  }
}
