package org.gbif.metadata.eml;


import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * For validation of EML files @see also http://knb.ecoinformatics.org/emlparser/
 *
 * @author markus
 */
public class ValidatorFactory {

  //public static final String EML_SCHEMA_URL = "http://rs.gbif.org/schema/eml-2.1.1/eml.xsd";
  public static final String EML_SCHEMA_URL = "https://code.ecoinformatics.org/code/eml/tags/RELEASE_EML_2_1_1_RC_3/eml.xsd";
  public static final String EML_GBIF_PROFILE_SCHEMA_URL = "http://rs.gbif.org/schema/eml-gbif-profile/1.0.1/eml-gbif-profile.xsd";

  /**
   * @return an xml validator based on the official eml 2.1.1 xml schema hosted at GBIF for network performance issues
   *         only.
   */
  public static Validator getEmlValidator() throws MalformedURLException, SAXException {
    return getValidator(EML_SCHEMA_URL);
  }

  public static Validator getGbifValidator() throws MalformedURLException, SAXException {
    return getValidator(EML_GBIF_PROFILE_SCHEMA_URL);

  }

  private static Validator getValidator(String schemaUrl) throws MalformedURLException, SAXException {
    // define the type of schema - we use W3C:
    String schemaLang = "http://www.w3.org/2001/XMLSchema";
    // get validation driver:
    SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
    // create schema by reading it from an URL:
    Schema schema = factory.newSchema(new URL(schemaUrl));
    Validator validator = schema.newValidator();
    return validator;
  }
}
