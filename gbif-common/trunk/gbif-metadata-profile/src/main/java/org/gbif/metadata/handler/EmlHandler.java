package org.gbif.metadata.handler;

import org.gbif.metadata.DateUtils;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An EML sax handler building a BasicMetadata instance.
 * Ths handler requires a namespace aware parser, but internally ignores the namespace to allow parsing of all kind of
 * EML documents regardless their version!
 * 
 * @author markus
 * 
 */
public class EmlHandler extends BasicMetadataSaxHandler {
  private List<String> keywords;
  private List<String> description;

  @Override
  public void endDocument() throws SAXException {
    super.endDocument();
    bm.setSubject(StringUtils.trimToNull(StringUtils.join(keywords, "; ")));
    bm.setDescription(StringUtils.trimToNull(StringUtils.join(description, " \n")));
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    // calling the super method to stringify the character buffer
    super.endElement(uri, localName, qName);

    if (content != null) {
      if (parents.startsWith("/eml/additionalmetadata/metadata")) {
        bm.addAdditionalMetadata(localName, content);
      } else if (parents.startsWith("/eml/dataset/keywordset")) {
        // dataset/keywordSet/keyword
        if (localName.equalsIgnoreCase("keyword")) {
          keywords.add(content);
        }
      } else if (parents.startsWith("/eml/dataset/abstract")) {
        // dataset/abstract/para
        if (localName.equalsIgnoreCase("para")) {
          description.add(content);
        }
      } else if (parents.startsWith("/eml/dataset/creator")) {
        // dataset/creator/onlineUrl
        if (localName.equalsIgnoreCase("onlineUrl")) {
          // only use this url if no distribution url exists
          if (bm.getHomeUrl() == null) {
            bm.setHomeUrl(content);
          }
        }
      } else if (parents.startsWith("/eml/dataset/distribution/online")) {
        // dataset/distribution/online/url
        if (localName.equalsIgnoreCase("url")) {
          bm.setHomeUrl(content);
        }
      } else if (parents.startsWith("/eml/dataset")) {
        if (localName.equalsIgnoreCase("title")) {
          bm.setTitle(content);
        } else if (localName.equalsIgnoreCase("alternateIdentifier")) {
          bm.setSourceId(content);
        } else if (localName.equalsIgnoreCase("pubDate")) {
          Date published = DateUtils.parse(content, DateUtils.isoDateFormat);
          bm.setPublished(published);
        }
      }
    }
  }

  @Override
  public void startDocument() {
    super.startDocument();
    keywords = new ArrayList<String>();
    description = new ArrayList<String>();
  }

}
