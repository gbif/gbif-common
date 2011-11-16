/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.metadata;

import java.util.Date;

/**
 * @author markus
 */
public interface BasicMetadata {

  public String getCitationString();

  public String getCreatorEmail();

  public String getCreatorName();

  public String getDescription();

  public String getHomepageUrl();

  @Deprecated
  String getHomeUrl();

  public String getIdentifier();

  public String getLogoUrl();

  public Date getPublished();
  public String getPublisherEmail();

  public String getPublisherName();
  public String getRights();

  /** concatenated keywords
   * @return
   */
  public String getSubject();
  public String getTitle();
}
