package org.gbif.metadata.eml;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * A single literature citation with an optional identifier TODO Documentation
 */
public class Citation implements Serializable {

  /**
   * Generated
   */
  private static final long serialVersionUID = 8611377167438888243L;

  private String identifier;
  private String citation;

  /**
   * Default constructor required by Struts2
   */
  public Citation() {
  }

  public Citation(String citation, String identifier) {
    this.citation = citation;
    this.identifier = identifier;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Citation)) {
      return false;
    }
    Citation o = (Citation) other;
    return equal(identifier, o.identifier) && equal(citation, o.citation);
  }

  /**
   * @return the name
   */
  public String getCitation() {
    return citation;
  }

  /**
   * @return the identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(citation, identifier);
  }

  public void setCitation(String citation) {
    this.citation = citation;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  @Override
  public String toString() {
    return String.format("Citation=%s, Identifier=%s", citation, identifier);
  }
}
