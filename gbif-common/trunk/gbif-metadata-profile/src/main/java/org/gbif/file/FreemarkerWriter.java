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

package org.gbif.file;

import org.gbif.utils.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author markus
 */
public class FreemarkerWriter {

  protected static final Configuration ftl = provideFreemarker();

  private static String processTemplateIntoString(Template template, Object model)
    throws IOException, TemplateException {
    StringWriter result = new StringWriter();
    template.process(model, result);
    return result.toString();
  }

  /**
   * Provides a freemarker template loader. It is configured to access the utf8 templates folder on the classpath, i.e.
   * /src/resources/templates
   */
  private static Configuration provideFreemarker() {
    // load templates from classpath by prefixing /templates
    TemplateLoader tl = new ClassTemplateLoader(FreemarkerWriter.class, "/templates");

    Configuration fm = new Configuration();
    fm.setDefaultEncoding("utf8");
    fm.setTemplateLoader(tl);

    return fm;
  }

  /**
   * Writes a map of data to a utf8 encoded file using a Freemarker {@link Configuration}.
   */
  public static void writeFile(File f, String template, Object data) throws IOException, TemplateException {
    String result = processTemplateIntoString(ftl.getTemplate(template), data);
    Writer out = FileUtils.startNewUtf8File(f);
    out.write(result);
    out.close();
  }

}
