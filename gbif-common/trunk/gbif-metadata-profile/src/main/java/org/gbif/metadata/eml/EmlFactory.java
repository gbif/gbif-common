/*
 * Copyright 2010 GBIF.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.metadata.eml;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is considered a utility for testing but should be migrated to the source when stable, as this is an EML
 * Model Factory based on the Apache Commons Digester and will be used when importing DwC-A
 *
 * @author Tim Robertson
 */
public class EmlFactory {

  /**
   * This is a reusable set of rules to build Agents and their Addresses, and add the Agent to the predecessor object on
   * the Stack Note that we are ignoring the userId as there have been no requests for the IPT to support this
   *
   * @param digester     to add the rules to
   * @param prefix       The XPath prefix to prepend for extracting the Agent information
   * @param parentMethod Of the previous stack object to call and add the Agent to
   */
  private static void addAgentRules(Digester digester, String prefix, String parentMethod) {
    digester.addObjectCreate(prefix, Agent.class);
    digester.addBeanPropertySetter(prefix + "/individualName/givenName", "firstName");
    digester.addBeanPropertySetter(prefix + "/individualName/surName", "lastName");
    digester.addBeanPropertySetter(prefix + "/organizationName", "organisation");
    digester.addBeanPropertySetter(prefix + "/positionName", "position");
    digester.addBeanPropertySetter(prefix + "/phone", "phone");
    digester.addBeanPropertySetter(prefix + "/electronicMailAddress", "email");
    digester.addBeanPropertySetter(prefix + "/onlineUrl", "homepage");

    digester.addBeanPropertySetter(prefix + "/role", "role");

    digester.addObjectCreate(prefix + "/address", Address.class);
    digester.addBeanPropertySetter(prefix + "/address/city", "city");
    digester.addBeanPropertySetter(prefix + "/address/administrativeArea", "province");
    digester.addBeanPropertySetter(prefix + "/address/postalCode", "postalCode");
    digester.addBeanPropertySetter(prefix + "/address/country", "country");
    digester.addBeanPropertySetter(prefix + "/address/deliveryPoint", "address");
    digester.addSetNext(prefix + "/address", "setAddress"); // called on
    // </address> to set
    // on parent Agent
    digester.addSetNext(prefix, parentMethod); // method called on parent
    // object which is the
    // previous stack object
  }

  /**
   * Add rules to extract the bibliographic citations
   *
   * @param digester to add the rules to
   */
  private static void addBibliographicCitations(Digester digester) {
    digester.addObjectCreate("eml/additionalMetadata/metadata/gbif/bibliography", BibliographicCitationSet.class);
    digester.addCallMethod("eml/additionalMetadata/metadata/gbif/bibliography/citation", "add", 2);
    digester.addCallParam("eml/additionalMetadata/metadata/gbif/bibliography/citation", 0);
    digester.addCallParam("eml/additionalMetadata/metadata/gbif/bibliography/citation", 1, "identifier");
    // add the BibliographicCitations to the list in EML
    digester.addSetNext("eml/additionalMetadata/metadata/gbif/bibliography", "setBibliographicCitationSet");
  }

  /**
   * Adds rules to extract the formationPeriod temporal coverage
   *
   * @param digester to add the rules to
   */
  private static void addFormationPeriodRules(Digester digester) {
    digester.addObjectCreate("eml/additionalMetadata/metadata/gbif/formationPeriod", TemporalCoverage.class);
    digester.addCallMethod("eml/additionalMetadata/metadata/gbif/formationPeriod", "setFormationPeriod", 1);
    digester.addCallParam("eml/additionalMetadata/metadata/gbif/formationPeriod", 0);
    digester.addSetNext("eml/additionalMetadata/metadata/gbif/formationPeriod", "addTemporalCoverage"); // add the
// TemporalCoverage to the list in EML
  }

  /**
   * Adds rules to get the geographic coverage
   *
   * @param digester to add the rules to
   */
  private static void addGeographicCoverageRules(Digester digester) {
    digester.addObjectCreate("eml/dataset/coverage/geographicCoverage", GeospatialCoverage.class);
    digester.addBeanPropertySetter("eml/dataset/coverage/geographicCoverage/geographicDescription", "description");
    digester.addObjectCreate("eml/dataset/coverage/geographicCoverage/boundingCoordinates", BBox.class);
    digester.addBeanPropertySetter("eml/dataset/coverage/geographicCoverage/boundingCoordinates/westBoundingCoordinate",
    "minX");
    digester.addBeanPropertySetter("eml/dataset/coverage/geographicCoverage/boundingCoordinates/eastBoundingCoordinate",
    "maxX");
    digester
    .addBeanPropertySetter("eml/dataset/coverage/geographicCoverage/boundingCoordinates/northBoundingCoordinate",
    "maxY");
    digester
    .addBeanPropertySetter("eml/dataset/coverage/geographicCoverage/boundingCoordinates/southBoundingCoordinate",
    "minY");
    digester.addSetNext("eml/dataset/coverage/geographicCoverage/boundingCoordinates", "setBoundingCoordinates"); // add
// the BBox to the GeospatialCoverage
    digester.addSetNext("eml/dataset/coverage/geographicCoverage", "addGeospatialCoverage"); // add the
// GeospatialCoverage to the list in
    // EML
  }

  /**
   * Add rules to extract the jgtiCuratorialUnit
   *
   * @param digester to add the rules to
   */
  private static void addJGTICuratorialIUnit(Digester digester) {
    digester.addObjectCreate("eml/additionalMetadata/metadata/gbif/jgtiCuratorialUnit", JGTICuratorialUnit.class);
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/jgtiCuratorialUnit/jgtiUnitType", "unitType");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/jgtiCuratorialUnit/jgtiUnitRange/beginRange",
    "rangeStart");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/jgtiCuratorialUnit/jgtiUnitRange/endRange",
    "rangeEnd");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/jgtiCuratorialUnit/jgtiUnits", "rangeMean");
    // sets attributes of jgtiUnits (uncertaintyMeasure)
    digester.addSetProperties("eml/additionalMetadata/metadata/gbif/jgtiCuratorialUnit/jgtiUnits");
    digester.addSetNext("eml/additionalMetadata/metadata/gbif/jgtiCuratorialUnit", "addJgtiCuratorialUnit"); // add the
// JGTICuratorialIUnit to the list in
    // EML

  }

  /**
   * Add rules to extract the keywords
   *
   * @param digester to add the rules to
   */
  private static void addKeywordRules(Digester digester) {
    digester.addObjectCreate("eml/dataset/keywordSet", KeywordSet.class);
    digester.addCallMethod("eml/dataset/keywordSet/keyword", "add", 1);
    digester.addCallParam("eml/dataset/keywordSet/keyword", 0);
    digester.addBeanPropertySetter("eml/dataset/keywordSet/keywordThesaurus", "keywordThesaurus");
    digester.addSetNext("eml/dataset/keywordSet", "addKeywordSet"); // add the
    // KeywordSet
    // to the
    // list in
    // EML
  }

  /**
   * Adds rules to extract the livingTimePeriod temporal coverage
   *
   * @param digester to add the rules to
   */
  private static void addLivingTimePeriodRules(Digester digester) {
    digester.addObjectCreate("eml/additionalMetadata/metadata/gbif/livingTimePeriod", TemporalCoverage.class);
    digester.addCallMethod("eml/additionalMetadata/metadata/gbif/livingTimePeriod", "setLivingTimePeriod", 1);
    digester.addCallParam("eml/additionalMetadata/metadata/gbif/livingTimePeriod", 0);
    digester.addSetNext("eml/additionalMetadata/metadata/gbif/livingTimePeriod", "addTemporalCoverage"); // add the
// TemporalCoverage to the list in EML
  }

  /**
   * Add rules to extract the physicalData
   *
   * @param digester to add the rules to
   */
  private static void addPhysicalDataRules(Digester digester) {
    digester.addObjectCreate("eml/additionalMetadata/metadata/gbif/physical", PhysicalData.class);
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/physical/objectName", "name");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/physical/characterEncoding", "charset");
    digester.addBeanPropertySetter(
        "eml/additionalMetadata/metadata/gbif/physical/dataFormat/externallyDefinedFormat/formatName", "format");
    digester.addBeanPropertySetter(
        "eml/additionalMetadata/metadata/gbif/physical/dataFormat/externallyDefinedFormat/formatVersion",
    "formatVersion");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/physical/distribution/online/url",
    "distributionUrl");
    digester
    .addSetNext("eml/additionalMetadata/metadata/gbif/physical", "addPhysicalData"); // add the PhysicalData to the
// list in EML
  }

  /**
   * Add rules for pulling the project details
   *
   * @param digester to add the rules to
   */
  private static void addProjectRules(Digester digester) {
    digester.addObjectCreate("eml/dataset/project", Project.class);
    digester.addBeanPropertySetter("eml/dataset/project/title", "title");
    addAgentRules(digester, "eml/dataset/project/personnel", "setPersonnel");
    // digester.addBeanPropertySetter("eml/dataset/project/abstract/para",
    // "projectAbstract");
    digester.addBeanPropertySetter("eml/dataset/project/funding/para", "funding");
    addStudyAreaDescriptionRules(digester);
    digester.addBeanPropertySetter("eml/dataset/project/designDescription/description/para", "designDescription");
    digester.addSetNext("eml/dataset/project", "setProject"); // add the Project
    // to the list in
    // EML
  }

  /**
   * Adds rules for the study area description: <studyAreaDescription> <descriptor name="generic"
   * citableClassificationSystem="false"> <descriptorValue>Turkish Mountains</descriptorValue> </descriptor>
   * </studyAreaDescription>
   *
   * @param digester To add the rules to
   */
  private static void addStudyAreaDescriptionRules(Digester digester) {
    digester.addObjectCreate("eml/dataset/project/studyAreaDescription", StudyAreaDescription.class);

    // get the descriptor@name attribute and set it
    digester.addCallMethod("eml/dataset/project/studyAreaDescription/descriptor", "setName", 1);
    digester.addCallParam("eml/dataset/project/studyAreaDescription/descriptor", 0, "name");

    // get the descriptor@citableClassificationSystem and set it
    digester.addCallMethod("eml/dataset/project/studyAreaDescription/descriptor", "setCitableClassificationSystem", 1);
    digester.addCallParam("eml/dataset/project/studyAreaDescription/descriptor", 0, "citableClassificationSystem");

    // set the value of the StudyAreaDescription
    digester
    .addBeanPropertySetter("eml/dataset/project/studyAreaDescription/descriptor/descriptorValue", "descriptorValue");

    // add the StudyAreaDescription to the project
    digester.addSetNext("eml/dataset/project/studyAreaDescription", "setStudyAreaDescription");

  }

  /**
   * Adds rules to extract the taxonomic coverage
   *
   * @param digester to add the rules to
   */
  private static void addTaxonomicCoverageRules(Digester digester) {
    digester.addObjectCreate("eml/dataset/coverage/taxonomicCoverage", TaxonomicCoverage.class);
    digester.addBeanPropertySetter("eml/dataset/coverage/taxonomicCoverage/generalTaxonomicCoverage", "description");
    digester.addObjectCreate("eml/dataset/coverage/taxonomicCoverage/taxonomicClassification", TaxonKeyword.class);
    digester
    .addBeanPropertySetter("eml/dataset/coverage/taxonomicCoverage/taxonomicClassification/taxonRankName", "rank");
    digester.addBeanPropertySetter("eml/dataset/coverage/taxonomicCoverage/taxonomicClassification/taxonRankValue",
    "scientificName");
    digester
    .addBeanPropertySetter("eml/dataset/coverage/taxonomicCoverage/taxonomicClassification/commonName", "commonName");
    digester.addSetNext("eml/dataset/coverage/taxonomicCoverage/taxonomicClassification",
    "addTaxonKeyword"); // adds the TaxonKeyword part of the
// TaxonomicCoverage
    digester.addSetNext("eml/dataset/coverage/taxonomicCoverage",
    "addTaxonomicCoverage"); // add the TaxonomicCoverage to the list in EML
  }

  /**
   * Adds rules to extract the temporal coverage
   *
   * @param digester to add the rules to
   */
  private static void addTemporalCoverageRules(Digester digester) {
    digester.addObjectCreate("eml/dataset/coverage/temporalCoverage", TemporalCoverage.class);
    digester.addCallMethod("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", "setStart", 1);
    digester.addCallParam("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", 0);
    digester.addCallMethod("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", "setEnd", 1);
    digester.addCallParam("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", 0);
    digester.addCallMethod("eml/dataset/coverage/temporalCoverage/rangeOfDates/beginDate/calendarDate", "setStart", 1);
    digester.addCallParam("eml/dataset/coverage/temporalCoverage/rangeOfDates/beginDate/calendarDate", 0);
    digester.addCallMethod("eml/dataset/coverage/temporalCoverage/rangeOfDates/endDate/calendarDate", "setEnd", 1);
    digester.addCallParam("eml/dataset/coverage/temporalCoverage/rangeOfDates/endDate/calendarDate", 0);
    // add the TemporalCoverage to the list in EML
    digester.addSetNext("eml/dataset/coverage/temporalCoverage", "addTemporalCoverage");
  }

  /**
   * Uses rule based parsing to read the EML XML and build the EML model
   *
   * Note the following: - Metadata provider rules are omitted on the assumption that the provider is the same as the
   * creator - Contact rules are omitted on the assumption that contacts are covered by the creator and associated
   * parties - Publisher rules are omitted on the assumption the publisher is covered by the creator and associated
   * parties
   *
   * @param xml To read. Note this will be closed before returning
   *
   * @return The EML populated
   *
   * @throws IOException  If the Stream cannot be read from
   * @throws SAXException If the XML is not well formed
   */
  public static Eml build(InputStream xml) throws IOException, SAXException {
    Digester digester = new Digester();
    digester.setNamespaceAware(true);

    // push the EML object onto the stack
    Eml eml = new Eml();
    digester.push(eml);

    // add the rules

    // language as xml:lang attribute
    digester.addCallMethod("eml", "setMetadataLanguage", 1);
    digester.addCallParam("eml", 0, "xml:lang");
    // guid as packageId attribute
    digester.addCallMethod("eml", "setPackageId", 1);
    digester.addCallParam("eml", 0, "packageId");

    // alternative ids
    digester.addCallMethod("eml/dataset/alternateIdentifier", "addAlternateIdentifier", 1);
    digester.addCallParam("eml/dataset/alternateIdentifier", 0);

    // title together with language
    digester.addCallMethod("eml/dataset/title", "setTitle", 2);
    digester.addCallParam("eml/dataset/title", 0);
    digester.addCallParam("eml/dataset/title", 1, "xml:lang");

    digester.addBeanPropertySetter("eml/dataset/language", "language");
    digester.addBeanPropertySetter("eml/dataset/abstract/para", "abstract");
    digester.addBeanPropertySetter("eml/dataset/additionalInfo/para", "additionalInfo");
    digester.addBeanPropertySetter("eml/dataset/intellectualRights/para", "intellectualRights");
    digester.addCallMethod("eml/dataset/methods/methodStep/description/para", "addMethodStep", 1);
    digester.addCallParam("eml/dataset/methods/methodStep/description/para", 0);
    digester.addBeanPropertySetter("eml/dataset/methods/sampling/studyExtent/description/para", "studyExtent");
    digester.addBeanPropertySetter("eml/dataset/methods/sampling/samplingDescription/para", "sampleDescription");
    digester.addBeanPropertySetter("eml/dataset/methods/qualityControl/description/para", "qualityControl");
    digester.addBeanPropertySetter("eml/dataset/distribution/online/url", "distributionUrl");
    digester.addBeanPropertySetter("eml/dataset/purpose/para", "purpose");
    //digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/citation", "citation");
    digester.addCallMethod("eml/additionalMetadata/metadata/gbif/citation", "setCitation", 2);
    digester.addCallParam("eml/additionalMetadata/metadata/gbif/citation", 0);
    digester.addCallParam("eml/additionalMetadata/metadata/gbif/citation", 1, "identifier");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/specimenPreservationMethod",
    "specimenPreservationMethod");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/resourceLogoUrl", "logoUrl");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/hierarchyLevel", "hierarchyLevel");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/collection/parentCollectionIdentifier",
    "parentCollectionId");
    digester
    .addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/collection/collectionIdentifier", "collectionId");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/collection/collectionName", "collectionName");

    digester.addCallMethod("eml/dataset/pubDate", "setPubDateAsString", 1);
    digester.addCallParam("eml/dataset/pubDate", 0);

    digester.addCallMethod("eml/additionalMetadata/metadata/gbif/dateStamp", "setDateStamp", 1);
    digester.addCallParam("eml/additionalMetadata/metadata/gbif/dateStamp", 0);

    addAgentRules(digester, "eml/dataset/creator", "setResourceCreator");
    addAgentRules(digester, "eml/dataset/metadataProvider", "setMetadataProvider");
    addAgentRules(digester, "eml/dataset/contact", "setContact");
    addAgentRules(digester, "eml/dataset/associatedParty", "addAssociatedParty");
    addKeywordRules(digester);
    addBibliographicCitations(digester);
    addGeographicCoverageRules(digester);
    addTemporalCoverageRules(digester);
    addLivingTimePeriodRules(digester);
    addFormationPeriodRules(digester);
    addTaxonomicCoverageRules(digester);
    addProjectRules(digester);
    addPhysicalDataRules(digester);
    addJGTICuratorialIUnit(digester);

    // now parse and return the EML
    try {
      digester.parse(xml);
    } finally {
      xml.close();
    }

    return eml;
  }

}
