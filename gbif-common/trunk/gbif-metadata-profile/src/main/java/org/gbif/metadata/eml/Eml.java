/*
 * Copyright 2009 GBIF.
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

import org.gbif.metadata.BasicMetadata;
import org.gbif.metadata.DateUtils;

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The EML model is a POJO representing the GBIF Extended Metadata Profile for the IPT 1.1 In addition to standard Bean
 * encapsulation, additional methods exist to simplify the implementation of an EML XML parser.
 * 
 * @see EmlFactory
 */
public class Eml implements Serializable, BasicMetadata {

  private static final Pattern PACKAGED_ID_PATTERN = Pattern.compile("/v([0-9]+)$");

  /**
   * Generated
   */
  private static final long serialVersionUID = 770733523572837495L;

  private String description;

  /**
   * This is not in the GBIF extended metadata document, but seems like a sensible placeholder that can be used to
   * capture anything missing, and maps nicely in EML, therefore is added
   */
  private String additionalInfo;

  private List<String> alternateIdentifiers = Lists.newArrayList();

  /**
   * The 'associatedParty' element provides the full name of other people, organizations, or positions who should be
   * associated with the resource. These parties might play various roles in the creation or maintenance of the
   * resource, and these roles should be indicated in the "role" element.
   */
  private List<Agent> associatedParties = Lists.newArrayList();
  // private List<String> bibliographicCitations = Lists.newArrayList();
  private BibliographicCitationSet bibliographicCitationSet = new BibliographicCitationSet();

  /**
   * A resource that describes a literature citation for the resource, one that might be found in a bibliography. We
   * cannot use http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml.html#citation because the IPT deals with
   * /eml/dataset and not /eml/citation therefore these are found in the additionalMetadata section of the EML.
   */
  private Citation citation;
  /**
   * The URI (LSID or URL) of the collection. In RDF, used as URI of the collection resource.
   * 
   * @see <a href="http://rs.tdwg.org/ontology/voc/Collection#collectionId">TDWG Natural Collection Description</a>
   */
  private String collectionId;
  /**
   * Official name of the Collection in the local language.
   * Note: this could potentially be sourced from the resource title, but this is declared explicitly in the GBIF IPT
   * metadata profile, so must assume that this is required for a title in a different language, presumably to aid free
   * text discovery in original language
   * 
   * @see <a href="http://purl.org/dc/elements/1.1/title">DublinCore</a>
   */
  private String collectionName;

  private Agent contact = new Agent();

  /**
   * Date of metadata creation or the last metadata update Default to now(), but can be overridden
   */
  private Date dateStamp = new Date();
  /**
   * The distributionType URL is generally meant for informational purposes, and the "function" attribute should be set
   * to "information".
   */
  private String distributionUrl;
  /**
   * Serialised data
   */
  private int emlVersion = 0;
  private List<GeospatialCoverage> geospatialCoverages = Lists.newArrayList();

  /**
   * Dataset level to which the metadata applies. The default value for GBIF is "dataset"
   * 
   * @see <a href="http://www.fgdc.gov/standards/projects/incits-l1-standards-projects/NAP-Metadata
   *      /napMetadataProfileV101.pdf>NAP Metadata</a>
   */
  private String hierarchyLevel = "dataset";

  /**
   * A rights management statement for the resource, or reference a service providing such information. Rights
   * information encompasses Intellectual Property Rights (IPR), Copyright, and various Property Rights. In the case of
   * a data set, rights might include requirements for use, requirements for attribution, or other requirements the
   * owner would like to impose.
   * 
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#intellectualRights>EML
   *      Resource intellectualRights keyword</a>
   */
  private String intellectualRights;

  /**
   * A quantitative descriptor (number of specimens, samples or batches). The actual quantification could be covered by
   * 1) an exact number of �JGI-units� in the collection plus a measure of uncertainty (+/- x); 2) a range of numbers (x
   * to x), with the lower value representing an exact number, when the higher value is omitted.
   */
  private List<JGTICuratorialUnit> jgtiCuratorialUnits = Lists.newArrayList();

  // Note that while Sets would be fine, to ease testing, Lists are
  // used to preserve ordering. A Set implementation that respects ordering
  // would also suffice
  // please refer to typed classes for descriptions of the properties and how
  // they map to EML
  private List<KeywordSet> keywords = Lists.newArrayList();

  /**
   * The language in which the resource is written. This can be a well-known language name, or one of the ISO language
   * codes to be more precise.
   * The IPT will always use ISO language codes.
   * 
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#language">EML Resource
   *      language keyword</a>
   */
  private String language = "en";

  /**
   * URL of the logo associated with a resource.
   */
  private String logoUrl;

  /**
   * Language of the metadata composed of an ISO639-2/T three letter language code and an ISO3166-1 three letter country
   * code.
   */
  private String metadataLanguage = "en";

  /**
   * The GBIF metadata profile states "Describes other languages used in metadata free text description. Consists of
   * language, country and characterEncoding" In Java world, a LocaleBundle handles this concisely
   */
  private LocaleBundle metadataLocale;

  /**
   * The 'metadataProvider' element provides the full name of the person, organization, or position who created
   * documentation for the resource.
   * 
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#metadataProvider">EML Resource
   *      metadataProvider keyword</a>
   */
  private Agent metadataProvider = new Agent();

  /**
   * Identifier for the parent collection for this sub-collection. Enables a hierarchy of collections and sub
   * collections to be built.
   * 
   * @see <a href="http://rs.tdwg.org/ontology/voc/Collection#isPartOfCollection">TDWG Natural Collection
   *      Description</a>
   */
  private String parentCollectionId;

  private List<PhysicalData> physicalData = Lists.newArrayList();

  /**
   * The project this resource is associated with
   */
  private Project project = new Project();

  /**
   * The date that the resource was published. The format should be represented as: CCYY, which represents a 4 digit
   * year, or as CCYY-MM-DD, which denotes the full year, month, and day. Note that month and day are optional
   * components. Formats must conform to ISO 8601. http://knb.ecoinformatics.org/
   * software/eml/eml-2.1.0/eml-resource.html#pubDate
   */
  private Date pubDate;

  /**
   * This is not in the GBIF extended metadata document, but seems like a sensible field to maintain, and maps nicely in
   * EML, therefore is added
   */
  private String purpose;

  /**
   * The 'creator' element provides the full name of the person, organization, or position who created the resource.
   * 
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-resource.html#creator">EML Resource creator
   *      keyword</a>
   */
  private Agent resourceCreator = new Agent();

  /**
   * Picklist keyword indicating the process or technique used to prevent physical deterioration of non-living
   * collections. Expected to contain an instance from the Specimen Preservation Method Type Term vocabulary.
   * 
   * @see <a href="http://rs.tdwg.org/ontology/voc/Collection#specimenPreservationMethod">TDWG Natural Collection
   *      Description</a>
   */
  private String specimenPreservationMethod;

  private List<TaxonomicCoverage> taxonomicCoverages = Lists.newArrayList();

  private List<TemporalCoverage> temporalCoverages = Lists.newArrayList();

  /**
   * URL linking to the resource homepage
   */
  private String link;

  private String guid;

  private String title;

  /**
   * "The coverage field allows for a textual description of the specific sampling area, the sampling frequency
   * (temporal boundaries, frequency of occurrence), and groups of living organisms sampled (taxonomic coverage)." This
   * implementation allows only the declaration of the extent description
   * 
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-methods.html#studyExtent">EML Methods
   *      studyExtent keyword</a>
   */
  private String studyExtent;

  /**
   * The samplingDescription field allows for a text-based/human readable description of the sampling procedures used in
   * the research project. The content of this element would be similar to a description of sampling procedures found in
   * the methods section of a journal article.
   * 
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-methods.html#samplingDescription">EML
   *      Methods samplingDescription keyword</a>
   */
  private String sampleDescription;

  /**
   * The qualityControl field provides a location for the description of actions taken to either control or assess the
   * quality of data resulting from the associated method step.
   * 
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-methods.html#qualityControl">EML Methods
   *      qualityControl keyword</a>
   */
  private String qualityControl;

  /**
   * "The methodStep field allows for repeated sets of elements that document a series of procedures followed to produce
   * a data object. These include text descriptions of the procedures, relevant literature, software, instrumentation,
   * source data and any quality control measures taken." This implementation allows only the declaration of the step
   * description
   * 
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-methods.html#methodStep">EML Methods
   *      methodStep keyword</a>
   */
  private List<String> methodSteps = Lists.newArrayList();

  /**
   * Default constructor needed by Struts2
   */
  public Eml() {
    this.pubDate = new Date();
    this.resourceCreator.setRole("Originator");
    this.metadataProvider.setRole("MetadataProvider");
    this.contact.setRole("PointOfContact");
  }

  public void addAlternateIdentifier(String alternateIdentifier) {
    this.alternateIdentifiers.add(alternateIdentifier);
  }

  /**
   * utility to add Agents to the primary contacts This method was introduced to ease the Digester rules for parsing of
   * EML
   * 
   * @param agent to add
   */
  public void addAssociatedParty(Agent agent) {
    if (agent.getRole() == null) {
      agent.setRole("AssociatedParty");
    }
    associatedParties.add(agent);
  }

  /**
   * utility to add a bibliographic citation to the bibliographicCitations. This method was introduced to ease the
   * Digester rules for parsing of EML.
   * 
   * @param bibliographic citation to add
   */
  public void addBibliographicCitations(List<Citation> citations) {
    bibliographicCitationSet.getBibliographicCitations().addAll(citations);
  }

  // /**
  // * utility to add a citation to the citations. This method was introduced to
  // * ease the Digester rules for parsing of EML
  // *
  // * @param citation to add
  // */
  // public void addCitation(String citation) {
  // this.citation=citation;
  // }

  /**
   * utility to add a coverage to the coverages This method was introduced to ease the Digester rules for parsing of
   * EML
   * 
   * @param coverage to add
   */
  public void addGeospatialCoverage(GeospatialCoverage geospatialCoverage) {
    geospatialCoverages.add(geospatialCoverage);
  }

  /**
   * utility to add a jgtiCuratorialUnit to the list. This method was introduced to ease the Digester rules for parsing
   * of EML
   * 
   * @param jgtiCuratorialUnit to add
   */
  public void addJgtiCuratorialUnit(JGTICuratorialUnit unit) {
    jgtiCuratorialUnits.add(unit);
  }

  /**
   * utility to add keywords to the keyword sets This method was introduced to ease the Digester rules for parsing of
   * EML
   * 
   * @param agent to add
   */
  public void addKeywordSet(KeywordSet keywordSet) {
    keywords.add(keywordSet);
  }

  /**
   * utility to add steps to the methodSteps list. This method was introduced to ease the Digester rules for parsing of
   * EML
   * 
   * @param step to add
   */
  public void addMethodStep(String step) {
    methodSteps.add(step);
  }

  /**
   * utility to add a PhysicalData instance to the physicalData list. This method was introduced to ease the Digester
   * rules for parsing of EML
   * 
   * @param PhysicalData to add
   */
  public void addPhysicalData(PhysicalData physicalData) {
    this.physicalData.add(physicalData);
  }

  /**
   * utility to add a coverage to the coverages This method was introduced to ease the Digester rules for parsing of
   * EML
   * 
   * @param coverage to add
   */
  public void addTaxonomicCoverage(TaxonomicCoverage coverage) {
    taxonomicCoverages.add(coverage);
  }

  /**
   * utility to add a coverage to the coverages This method was introduced to ease the Digester rules for parsing of
   * EML
   * 
   * @param coverage to add
   */
  public void addTemporalCoverage(TemporalCoverage coverage) {
    temporalCoverages.add(coverage);
  }

  public String getAbstract() {
    return description;
  }

  public String getAdditionalInfo() {
    if (additionalInfo == null || additionalInfo.length() == 0) {
      return null;
    }
    return additionalInfo;
  }

  public List<String> getAlternateIdentifiers() {
    return alternateIdentifiers;
  }

  public List<Agent> getAssociatedParties() {
    return associatedParties;
  }

  public List<Citation> getBibliographicCitations() {
    return bibliographicCitationSet.getBibliographicCitations();
  }

  public BibliographicCitationSet getBibliographicCitationSet() {
    return bibliographicCitationSet;
  }

  public Citation getCitation() {
    return citation;
  }

  /* (non-Javadoc)
   * @see org.gbif.metadata.BasicMetadata#getCitationString()
   */
  public String getCitationString() {
    if (citation!=null){
      return citation.getCitation();
    }
    return null;
  }

  public String getCollectionId() {
    if (collectionId == null || collectionId.length() == 0) {
      return null;
    }
    return collectionId;
  }

  public String getCollectionName() {
    if (collectionName == null || collectionName.length() == 0) {
      return null;
    }
    return collectionName;
  }

  public Agent getContact() {
    return contact;
  }

  private Agent getCreator() {
    Agent creator = getResourceCreator();
    if (creator==null){
      creator=getContact();
    }
    return creator;
  }

  public String getCreatorEmail() {
    Agent creator = getCreator();
    if (creator!=null){
      return creator.getEmail();
    }
    return null;
  }

  public String getCreatorName() {
    Agent creator = getCreator();
    if (creator!=null){
      return creator.getFullName();
    }
    return null;
  }

  public Date getDateStamp() {
    return dateStamp;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.metadata.BasicMetadata#getDescription()
   */
  public String getDescription() {
    return getAbstract();
  }

  public String getDistributionUrl() {
    if (distributionUrl == null || distributionUrl.length() == 0) {
      return null;
    }
    return distributionUrl;
  }

  public int getEmlVersion() {
    return emlVersion;
  }

  public List<GeospatialCoverage> getGeospatialCoverages() {
    return geospatialCoverages;
  }

  public String getGuid() {
    return guid;
  }

  public String getHierarchyLevel() {
    if (hierarchyLevel == null || hierarchyLevel.length() == 0) {
      return null;
    }
    return hierarchyLevel;
  }

  public String getHomepageUrl() {
    return resourceCreator.getHomepage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.metadata.BasicMetadata#getHomeUrl()
   */
  public String getHomeUrl() {
    return resourceCreator.getHomepage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.metadata.BasicMetadata#getId()
   */
  public String getIdentifier() {
    return guid;
  }

  public String getIntellectualRights() {
    if (intellectualRights == null || intellectualRights.length() == 0) {
      return null;
    }
    return intellectualRights;
  }

  public List<JGTICuratorialUnit> getJgtiCuratorialUnits() {
    return jgtiCuratorialUnits;
  }

  public List<KeywordSet> getKeywords() {
    return keywords;
  }

  public String getLanguage() {
    if (language == null || language.length() == 0) {
      return null;
    }
    return language;
  }

  public String getLink() {
    return link;
  }

  public String getLogoUrl() {
    if (logoUrl == null || logoUrl.length() == 0) {
      return null;
    }
    return logoUrl;
  }

  public String getMetadataLanguage() {
    if (metadataLanguage == null || metadataLanguage.length() == 0) {
      return null;
    }
    return metadataLanguage;
  }

  public LocaleBundle getMetadataLocale() {
    return metadataLocale;
  }

  public Agent getMetadataProvider() {
    return metadataProvider;
  }

  public List<String> getMethodSteps() {
    return methodSteps;
  }

  public String getPackageId() {
    return guid + "/v" + emlVersion;
  }

  public String getParentCollectionId() {
    if (parentCollectionId == null || parentCollectionId.length() == 0) {
      return null;
    }
    return parentCollectionId;
  }

  public List<PhysicalData> getPhysicalData() {
    return physicalData;
  }

  public Project getProject() {
    return project;
  }

  public Date getPubDate() {
    return pubDate;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.metadata.BasicMetadata#getPublished()
   */
  public Date getPublished() {
    return getPubDate();
  }

  public String getPublisherEmail() {
    Agent creator = getMetadataProvider();
    if (creator!=null){
      return creator.getEmail();
    }
    return null;
  }

  public String getPublisherName() {
    Agent creator = getMetadataProvider();
    if (creator!=null){
      return creator.getFullName();
    }
    return null;
  }

  public String getPurpose() {
    if (purpose == null || purpose.length() == 0) {
      return null;
    }
    return purpose;
  }

  public String getQualityControl() {
    return qualityControl;
  }

  public Agent getResourceCreator() {
    return resourceCreator;
  }

  /* (non-Javadoc)
   * @see org.gbif.metadata.BasicMetadata#getRights()
   */
  public String getRights() {
    return this.intellectualRights;
  }

  public String getSampleDescription() {
    return sampleDescription;
  }

  public String getSpecimenPreservationMethod() {
    if (specimenPreservationMethod == null || specimenPreservationMethod.length() == 0) {
      return null;
    }
    return specimenPreservationMethod;
  }

  public String getStudyExtent() {
    return studyExtent;
  }

  public String getSubject() {
    List<String> subjects = new ArrayList<String>();
    for (KeywordSet ks : getKeywords()) {
      subjects.add(StringUtils.join(ks.getKeywords(), "; "));
    }
    return StringUtils.join(subjects, "; ");
  }

  public List<TaxonomicCoverage> getTaxonomicCoverages() {
    return taxonomicCoverages;
  }

  public List<TemporalCoverage> getTemporalCoverages() {
    return temporalCoverages;
  }

  public String getTitle() {
    return title;
  }

  public int increaseEmlVersion() {
    this.emlVersion += 1;
    return this.emlVersion;
  }

  public Agent resourceCreator() {
    return resourceCreator;
  }

  public void setAbstract(String description) {
    this.description = description;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public void setAlternateIdentifiers(List<String> alternateIdentifiers) {
    this.alternateIdentifiers = alternateIdentifiers;
  }

  public void setAssociatedParties(List<Agent> associatedParties) {
    this.associatedParties = associatedParties;
  }

  public void setBibliographicCitations(List<Citation> val) {
    bibliographicCitationSet.setBibliographicCitations(val);
  }

  public void setBibliographicCitationSet(BibliographicCitationSet val) {
    bibliographicCitationSet = val;
  }

  public void setCitation(Citation citation) {
    this.citation = citation;
  }

  public void setCitation(String citation, String identifier) {
    this.citation = new Citation(citation, identifier);
  }

  public void setCollectionId(String collectionId) {
    this.collectionId = collectionId;
  }

  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }

  public void setContact(Agent contact) {
    this.contact = contact;
  }

  public void setDateStamp(Date dateStamp) {
    this.dateStamp = dateStamp;
  }

  /**
   * Utility to set the date with a textual format
   * 
   * @param dateString To set
   * @param format That the string is in
   * @throws ParseException Should it be an erroneous format
   */
  public void setDateStamp(String dateString) throws ParseException {
    dateStamp = DateUtils.schemaDateTime(dateString);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.metadata.BasicMetadata#setDescription(java.lang.String)
   */
  public void setDescription(String description) {
    setAbstract(description);
  }

  public void setDistributionUrl(String distributionUrl) {
    this.distributionUrl = distributionUrl;
  }

  public void setEmlVersion(int emlVersion) {
    this.emlVersion = emlVersion;
  }

  public void setGeospatialCoverages(List<GeospatialCoverage> geospatialCoverages) {
    this.geospatialCoverages = geospatialCoverages;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public void setHierarchyLevel(String hierarchyLevel) {
    this.hierarchyLevel = hierarchyLevel;
  }

  public void setHomepageUrl(String homeUrl) {
    this.resourceCreator.setHomepage(homeUrl);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.metadata.BasicMetadata#setHomeUrl(java.lang.String)
   */
  public void setHomeUrl(String homeUrl) {
    this.resourceCreator.setHomepage(homeUrl);
  }

  public void setIntellectualRights(String intellectualRights) {
    this.intellectualRights = intellectualRights;
  }

  public void setJgtiCuratorialUnits(List<JGTICuratorialUnit> jgtiCuratorialUnit) {
    this.jgtiCuratorialUnits = jgtiCuratorialUnit;
  }

  public void setKeywords(List<KeywordSet> keywords) {
    this.keywords = keywords;
  }

  public void setKeywordSet(List<KeywordSet> keywords) {
    this.keywords = keywords;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public void setMetadataLanguage(String language) {
    this.metadataLanguage = language;
  }

  public void setMetadataLocale(LocaleBundle metadataLocale) {
    this.metadataLocale = metadataLocale;
  }

  public void setMetadataProvider(Agent metadataProvider) {
    this.metadataProvider = metadataProvider;
  }

  public void setMethodSteps(List<String> methodSteps) {
    this.methodSteps = methodSteps;
  }

  public void setPackageId(String packageId) {
    Matcher m = PACKAGED_ID_PATTERN.matcher(packageId);
    if (m.find()) {
      this.emlVersion = Integer.valueOf(m.group(1));
      packageId = m.replaceAll("");
    }
    this.guid = packageId;
  }

  public void setParentCollectionId(String parentCollectionId) {
    this.parentCollectionId = parentCollectionId;
  }

  public void setPhysicalData(List<PhysicalData> physicalData) {
    this.physicalData = physicalData;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public void setPubDate(Date pubDate) {
    this.pubDate = pubDate;
  }

  /**
   * Utility to set the date with a textual format
   * 
   * @param dateString To set
   * @param format That the string is in
   * @throws ParseException Should it be an erroneous format
   */
  public void setPubDateAsString(String dateString) throws ParseException {
    pubDate = DateUtils.calendarDate(dateString);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.metadata.BasicMetadata#setPublished(java.util.Date)
   */
  public void setPublished(Date published) {
    setPubDate(published);
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  public void setQualityControl(String qualityControl) {
    this.qualityControl = qualityControl;
  }

  public void setResourceCreator(Agent resourceCreator) {
    this.resourceCreator = resourceCreator;
  }

  public void setSampleDescription(String sampleDescription) {
    this.sampleDescription = sampleDescription;
  }

  public void setSpecimenPreservationMethod(String specimenPreservationMethod) {
    this.specimenPreservationMethod = specimenPreservationMethod;
  }

  public void setStudyExtent(String studyExtent) {
    this.studyExtent = studyExtent;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.metadata.BasicMetadata#setSubject(java.util.Collection)
   */
  public void setSubject(List<String> keywords) {
    KeywordSet ks = new KeywordSet(keywords);
    List<KeywordSet> list = new ArrayList<KeywordSet>();
    list.add(ks);
    setKeywordSet(list);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.metadata.BasicMetadata#setKeywords(java.lang.String)
   */
  public void setSubject(String keywords) {
    if (keywords != null) {
      String[] tokens;
      int commas = StringUtils.countMatches(keywords, ",");
      int semicolon = StringUtils.countMatches(keywords, ";");
      int pipes = StringUtils.countMatches(keywords, "|");
      if (semicolon >= commas && semicolon >= pipes) {
        // semicolons
        tokens = StringUtils.split(keywords, ";");
      } else if (pipes >= semicolon && pipes >= commas) {
        // pipes
        tokens = StringUtils.split(keywords, "|");
      } else {
        // commas
        tokens = StringUtils.split(keywords, ",");
      }
      List<String> keyList = new ArrayList<String>();
      for (String kw : tokens) {
        String k = StringUtils.trimToNull(kw);
        keyList.add(k);
      }
      setSubject(keyList);
    }
  }

  public void setTaxonomicCoverages(List<TaxonomicCoverage> taxonomicCoverages) {
    this.taxonomicCoverages = taxonomicCoverages;
  }
  public void setTemporalCoverages(List<TemporalCoverage> temporalCoverages) {
    this.temporalCoverages = temporalCoverages;
  }


  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Sets the title also given the language. Used to support multiple translated titles in the eml source document while
   * the Eml java classes still only support a single title, preferrably in english. The setter will use the first title
   * but prefer any english title over any other language.
   */
  public void setTitle(String title, String language) {
    if (this.title == null || ("en".equalsIgnoreCase(language) || "eng".equalsIgnoreCase(language))) {
      this.title = title;
    }
  }

  @Override
  public String toString() {
    return "Eml [\n description=" + description + "\n additionalInfo=" + additionalInfo + "\n alternateIdentifiers="
    + alternateIdentifiers + "\n associatedParties=" + associatedParties + "\n bibliographicCitationSet="
    + bibliographicCitationSet + "\n citation=" + citation + "\n collectionId=" + collectionId
    + "\n collectionName=" + collectionName + "\n contact=" + contact + "\n dateStamp=" + dateStamp
    + "\n distributionUrl=" + distributionUrl + "\n emlVersion=" + emlVersion + "\n geospatialCoverages="
    + geospatialCoverages + "\n hierarchyLevel=" + hierarchyLevel + "\n intellectualRights=" + intellectualRights
    + "\n jgtiCuratorialUnits=" + jgtiCuratorialUnits + "\n keywords=" + keywords + "\n language=" + language
    + "\n logoUrl=" + logoUrl + "\n metadataLanguage=" + metadataLanguage + "\n metadataLocale=" + metadataLocale
    + "\n metadataProvider=" + metadataProvider + "\n parentCollectionId=" + parentCollectionId
    + "\n physicalData=" + physicalData + "\n project=" + project + "\n pubDate=" + pubDate + "\n purpose="
    + purpose + "\n resourceCreator=" + resourceCreator + "\n specimenPreservationMethod="
    + specimenPreservationMethod + "\n taxonomicCoverages=" + taxonomicCoverages + "\n temporalCoverages="
    + temporalCoverages + "\n link=" + link + "\n guid=" + guid + "\n title=" + title + "\n studyExtent="
    + studyExtent + "\n sampleDescription=" + sampleDescription + "\n qualityControl=" + qualityControl
    + "\n methodSteps=" + methodSteps + "]";
  }

}
