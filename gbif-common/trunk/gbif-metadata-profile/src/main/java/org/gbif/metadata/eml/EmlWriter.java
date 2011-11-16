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

package org.gbif.metadata.eml;

import org.gbif.file.FreemarkerWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * @author markus
 */
public class EmlWriter extends FreemarkerWriter {

  private static final String EML_TEMPLATE = "eml.ftl";

  /**
   * Writes an {@link Eml} object to an XML file using a Freemarker {@link Configuration}.
   *
   * @param f   the XML file to write to
   * @param eml the EML object
   */
  public static void writeEmlFile(File f, Eml eml) throws IOException, TemplateException {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("eml", eml);
    writeFile(f, EML_TEMPLATE, map);
  }
}
