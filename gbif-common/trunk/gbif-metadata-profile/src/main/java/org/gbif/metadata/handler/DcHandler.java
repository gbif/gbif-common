package org.gbif.metadata.handler;

import org.gbif.metadata.DateUtils;
import org.gbif.utils.text.EmailUtils;
import org.gbif.utils.text.EmailUtils.EmailWithName;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.util.Date;

public class DcHandler extends BasicMetadataSaxHandler {

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    // calling the super method to stringify the character buffer
    super.endElement(uri, localName, qName);

    // dcPrefix
    if (uri != null && (uri.equalsIgnoreCase(NS_DC) || uri.equalsIgnoreCase(NS_DCTERMS))) {
      if (localName.equalsIgnoreCase("title")) {
        bm.setTitle(content);

      } else if (localName.equalsIgnoreCase("description")) {
        bm.setDescription(content);
      } else if (localName.equalsIgnoreCase("abstract") && bm.getDescription()==null) {
        // use abstracts if no full description is found
        bm.setDescription(content);

      } else if (localName.equalsIgnoreCase("subject")) {
        bm.addSubject(content);
      } else if (localName.equalsIgnoreCase("coverage")) {
        bm.addSubject(content);
      } else if (localName.equalsIgnoreCase("spatial")) {
        bm.addSubject(content);
      } else if (localName.equalsIgnoreCase("temporal")) {
        bm.addSubject(content);

      } else if (localName.equalsIgnoreCase("created")) {
        Date published = DateUtils.parse(content, DateUtils.isoDateFormat);
        bm.setPublished(published);

      } else if (localName.equalsIgnoreCase("relation")) {
        if (bm.getHomeUrl() == null) {
          bm.setHomeUrl(content);
        }
      } else if (localName.equalsIgnoreCase("identifier")) {
        bm.setSourceId(content);

      } else if (localName.equalsIgnoreCase("rights")) {
        bm.setRights(content);

      } else if (localName.equalsIgnoreCase("bibliographicCitation")) {
        bm.setCitationString(content);

      } else if (localName.equalsIgnoreCase("creator")) {
        // try to parse our email and name
        String creator = StringUtils.trimToNull(content);
        if (creator!=null){
          EmailWithName n = EmailUtils.parseEmail(creator);
          bm.setCreatorEmail(n.email);
          bm.setCreatorName(n.name);
        }

      } else if (localName.equalsIgnoreCase("publisher")) {
        // try to parse our email and name
        String publisher = StringUtils.trimToNull(content);
        if (publisher!=null){
          EmailWithName n = EmailUtils.parseEmail(publisher);
          bm.setCreatorEmail(n.email);
          bm.setCreatorName(n.name);
        }

      } else if (localName.equalsIgnoreCase("source")) {
        bm.setHomepageUrl(content);

      }
    } else if (uri == null && localName.equalsIgnoreCase("onlineurl")) {
      bm.setHomepageUrl(content);
    } else if (uri == null && localName.equalsIgnoreCase("homepage")) {
      bm.setHomepageUrl(content);
    }
  }
}
