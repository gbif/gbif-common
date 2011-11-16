package org.gbif.metadata;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicMetadataImpl implements Serializable, BasicMetadata {

  private static final long serialVersionUID = 12073642352837495L;

  private String title;
  private String sourceId;
  private String description;
  private String homeUrl;
  private String logoUrl;
  private String subject;
  private String rights;
  private String citation;
  private String creatorName;
  private String creatorEmail;
  private String publisherName;
  private String publisherEmail;
  private Date published;
  private Map<String, String> additionalMetadata = new HashMap<String, String>();

  public void addAdditionalMetadata(String key, String value) {
    this.additionalMetadata.put(key, value);
  }

  /** adds more subjects/keywords, concatenating it to the existing one
   * @param subject
   */
  public void addSubject(String subject) {
    if (StringUtils.isBlank(this.subject)){
      this.subject = subject;
    }else{
      this.subject += "; "+StringUtils.trimToEmpty(subject);
    }
  }

  public Map<String, String> getAdditionalMetadata() {
    return additionalMetadata;
  }

  public String getAdditionalMetadata(String key) {
    return additionalMetadata.get(key);
  }

  public String getCitationString() {
    return citation;
  }

  public void getCitationString(String citation) {
    this.citation = citation;
  }

  public String getCreatorEmail() {
    return creatorEmail;
  }

  public String getCreatorName() {
    return creatorName;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#getDescription()
   */
  public String getDescription() {
    return description;
  }

  public String getHomepageUrl() {
    return homeUrl;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#getHomeUrl()
   */
  public String getHomeUrl() {
    return homeUrl;
  }

  public String getIdentifier() {
    return sourceId;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#getLogoUrl()
   */
  public String getLogoUrl() {
    return logoUrl;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#getPublished()
   */
  public Date getPublished() {
    return published;
  }

  public String getPublisherEmail() {
    return publisherEmail;
  }

  public String getPublisherName() {
    return publisherName;
  }

  public String getRights() {
    return rights;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#getSourceId()
   */
  public String getSourceId() {
    return sourceId;
  }

  public String getSubject() {
    return subject;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#getTitle()
   */
  public String getTitle() {
    return title;
  }

  public void setAdditionalMetadata(Map<String, String> additionalMetadata) {
    this.additionalMetadata = additionalMetadata;
  }

  public void setCitationString(String citation) {
    this.citation = citation;
  }

  public void setCreatorEmail(String creatorEmail) {
    this.creatorEmail = creatorEmail;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#setDescription(java.lang.String)
   */
  public void setDescription(String description) {
    this.description = description;
  }

  public void setHomepageUrl(String homeUrl) {
    this.homeUrl = homeUrl;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#setHomeUrl(java.lang.String)
   */
  public void setHomeUrl(String homeUrl) {
    this.homeUrl = homeUrl;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#setLogoUrl(java.lang.String)
   */
  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#setPublished(java.util.Date)
   */
  public void setPublished(Date published) {
    this.published = published;
  }

  public void setPublisherEmail(String publisherEmail) {
    this.publisherEmail = publisherEmail;
  }

  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }

  public void setRights(String rights) {
    this.rights = rights;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#setSourceId(java.lang.String)
   */
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.BasicMetadata#setSubject(java.util.Collection)
   */
  public void setSubject(List<String> keywords) {
    setSubject(StringUtils.join(keywords, "; "));
  }
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.metadata.Metadata#setTitle(java.lang.String)
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("title", this.title).append("sourceId", this.sourceId)
    .append("published", this.published).append("logoUrl", this.logoUrl).append("homeUrl", this.homeUrl)
    .append("keywords", this.subject).append("description", this.description).toString();
  }


}
