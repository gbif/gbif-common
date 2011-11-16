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

import org.gbif.metadata.DateUtils;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

/**
 * This class can be used to encapsulate temporal coverage information.
 */
public class TemporalCoverage implements Serializable {

  /**
   * Generated
   */
  private static final long serialVersionUID = 898101764914677290L;

  /**
   * A single time stamp signifying the beginning of some time period.
   *
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-coverage.html#beginDate">EML Coverage
   *      beginDate keyword</a>
   */
  private Date startDate;

  /**
   * A single time stamp signifying the end of some time period.
   *
   * @see <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-coverage.html#endDate">EML Coverage endDate
   *      keyword</a>
   */
  private Date endDate;

  /**
   * Text description of the time period during which the collection was assembled e.g. "Victorian", or "1922 - 1932",
   * or "c. 1750".
   *
   * @see <a href="http://rs.tdwg.org/ontology/voc/Collection#formationPeriod">TDWG Natural Collection Description</a>
   */
  private String formationPeriod;

  /**
   * Time period during which biological material was alive. (for palaeontological collections).
   *
   * @see <a href="http://rs.tdwg.org/ontology/voc/Collection#livingTimePeriodCoverage">TDWG Natural Collection
   *      Description</a>
   */
  private String livingTimePeriod;

  public TemporalCoverage() {
  }

  public void correctDateOrder() {
    if (startDate == null && endDate != null) {
      startDate = endDate;
      endDate = null;
    }
    if (startDate != null && endDate != null && startDate.compareTo(endDate) > 0) {
      Date d = startDate;
      startDate = endDate;
      endDate = d;
    }
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof TemporalCoverage)) {
      return false;
    }
    TemporalCoverage o = (TemporalCoverage) other;
    return equal(formationPeriod, o.formationPeriod) && equal(endDate, o.endDate) &&
           equal(livingTimePeriod, o.livingTimePeriod) && equal(startDate, o.startDate);
  }

  public Date getEndDate() {
    if (endDate == null) {
      return endDate;
    }
    return new Date(endDate.getTime());
  }

  public String getFormationPeriod() {
    if (formationPeriod == null || formationPeriod.length() == 0) {
      return null;
    }
    return formationPeriod;
  }

  public String getLivingTimePeriod() {
    if (livingTimePeriod == null || livingTimePeriod.length() == 0) {
      return null;
    }
    return livingTimePeriod;
  }

  public Date getStartDate() {
    if (startDate == null) {
      return startDate;
    }
    return new Date(startDate.getTime());
  }

  public TemporalCoverageType getType() {
    if (this.formationPeriod != null && this.formationPeriod.length() > 0) {
      return TemporalCoverageType.FORMATION_PERIOD;
    }
    if (this.livingTimePeriod != null && this.livingTimePeriod.length() > 0) {
      return TemporalCoverageType.LIVING_TIME_PERIOD;
    }
    if (this.startDate != null && this.endDate != null && this.startDate.compareTo(endDate) != 0) {
      return TemporalCoverageType.DATE_RANGE;
    }
    return TemporalCoverageType.SINGLE_DATE;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(livingTimePeriod, endDate, formationPeriod, startDate);
  }

  /**
   * Utility to set the date with a textual format
   *
   * @param dateString To set
   * @param format     That the string is in
   *
   * @throws ParseException Should it be an erroneous format
   */
  public void setEnd(String start) throws ParseException {
    endDate = DateUtils.calendarDate(start);
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public void setFormationPeriod(String formationPeriod) {
    if (formationPeriod != null && formationPeriod.length() == 0) {
      this.formationPeriod = null;
    } else {
      this.formationPeriod = formationPeriod;
    }
  }

  public void setLivingTimePeriod(String livingTimePeriod) {
    if (livingTimePeriod != null && livingTimePeriod.length() == 0) {
      this.livingTimePeriod = null;
    } else {
      this.livingTimePeriod = livingTimePeriod;
    }
  }

  /**
   * Utility to set the date with a textual format
   *
   * @param dateString To set
   * @param format     That the string is in
   *
   * @throws ParseException Should it be an erroneous format
   */
  public void setStart(String start) throws ParseException {
    startDate = DateUtils.calendarDate(start);
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  @Override
  public String toString() {
    return String.format("StartDate=%s, EndDate=%s, Formation Period=%s, Living Time Period=%s", startDate, endDate,
      formationPeriod, livingTimePeriod);
  }
}
