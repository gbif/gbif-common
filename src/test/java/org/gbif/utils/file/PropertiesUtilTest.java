/*
 * Copyright 2015 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.utils.file;

import org.gbif.utils.file.properties.PropertiesUtil;

import java.util.Properties;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests related to {@link PropertiesUtil}
 */
public class PropertiesUtilTest {

  @Test
  public void testFilterProperties(){

    Properties properties = new Properties();
    properties.put("prefix.key1", "value1");
    properties.put("key2", "value2");

    Properties filteredProperties = PropertiesUtil.filterProperties(properties, "prefix.");
    assertEquals(1, filteredProperties.size());
    assertTrue("Prefix is removed from the original key", filteredProperties.containsKey("key1"));
    assertEquals("Value remains the same", properties.get("prefix.key1"), filteredProperties.getProperty("key1"));
  }
}
