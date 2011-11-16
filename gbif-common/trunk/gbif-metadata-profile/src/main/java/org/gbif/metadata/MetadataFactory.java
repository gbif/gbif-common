package org.gbif.metadata;

import org.gbif.metadata.handler.BasicMetadataSaxHandler;
import org.gbif.metadata.handler.DcHandler;
import org.gbif.metadata.handler.EmlHandler;
import org.gbif.utils.file.BomSafeInputStreamWrapper;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MetadataFactory {

  private final Logger log = LoggerFactory.getLogger(MetadataFactory.class);
  private final SAXParserFactory saxFactory;
  private final List<BasicMetadataSaxHandler> handler = new ArrayList<BasicMetadataSaxHandler>();

  {
    handler.add(new EmlHandler());
    handler.add(new DcHandler());
  }

  public MetadataFactory() {
    saxFactory = SAXParserFactory.newInstance();
    saxFactory.setNamespaceAware(true);
    saxFactory.setValidating(false);
  }

  /**
   * @return a suitable reader or null if none can be found
   */

  private boolean hasContent(BasicMetadataImpl bm) {
    if (bm != null &&
        (bm.getTitle() != null || bm.getDescription() != null || bm.getSubject() != null || bm.getSourceId() != null ||
         bm.getHomeUrl() != null || bm.getPublished() != null)) {
      return true;
    }
    return false;
  }

  public BasicMetadataImpl read(File metadataFile) throws MetadataException {
    try {
      return read(new FileInputStream(metadataFile));
    } catch (FileNotFoundException e) {
      throw new MetadataException("FileNotFound", e);
    }
  }

  public BasicMetadataImpl read(InputStream stream) throws MetadataException {
    // in order to test different handlers we need to process the same stream several times
    // we therefore read the entire stream into memory first
    try {
      byte[] data = IOUtils.toByteArray(stream);
      // find handler by testing one by one
      for (BasicMetadataSaxHandler h : handler) {
        try {
          InputStream in = new ByteArrayInputStream(data);
          BasicMetadataImpl bm = read(in, h);
          if (bm != null && hasContent(bm)) {
            // works!
            log.debug("Using " + h.toString() + " for parsing metadata");
            return bm;
          }
        } catch (MetadataException ignored) {
          // just try another one
        }
      }
    } catch (IOException e1) {
      throw new MetadataException("Can't read input stream", e1);
    }
    throw new MetadataException("Can't find suitable metadata parser");
  }

  public BasicMetadataImpl read(InputStream stream, BasicMetadataSaxHandler handler) throws MetadataException {
    try {
      SAXParser p = saxFactory.newSAXParser();
      p.parse(new BomSafeInputStreamWrapper(stream), handler);
      BasicMetadataImpl bm = handler.yield();
      if (hasContent(bm)) {
        return bm;
      }
    } catch (Exception e) {
      log.error("Error parsing metadata document: " + e.getMessage());
    }
    return null;
  }
}
