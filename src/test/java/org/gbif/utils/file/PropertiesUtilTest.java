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
import static org.junit.Assert.assertNotNull;
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

  @Test
  public void testSubsetProperties(){

    Properties properties = new Properties();
    properties.put("prefix.key1", "value1");
    properties.put("key2", "value2");

    Properties newProperties = PropertiesUtil.subsetProperties(properties, "prefix.");
    assertEquals(1, newProperties.size());
    assertEquals(2, properties.size());

    assertTrue("Prefix is kept from the original key", newProperties.containsKey("prefix.key1"));
    assertTrue("key1 is still in original Properties", properties.containsKey("prefix.key1"));
    assertTrue("key2 is still in original Properties", properties.containsKey("key2"));
  }

  @Test
  public void testRemoveProperties(){

    Properties properties = new Properties();
    properties.put("prefix.key1", "value1");
    properties.put("key2", "value2");

    Properties newProperties = PropertiesUtil.removeProperties(properties, "prefix.");
    assertEquals(1, newProperties.size());
    assertEquals(1, properties.size());

    assertTrue("Prefix is kept from the original key", newProperties.containsKey("prefix.key1"));
    assertTrue("Other element is still present in original Propeties", properties.containsKey("key2"));
  }

  @Test
  public void testEmptyProperties(){
    Properties properties = new Properties();
    Properties newProperties = PropertiesUtil.removeProperties(properties, "prefix");
    assertNotNull(newProperties);
    assertEquals(0, properties.size());
  }

  @Test(expected = NullPointerException.class)
  public void testExceptionNoProperties(){
    PropertiesUtil.removeProperties(null, "prefix");
  }

  @Test(expected = IllegalStateException.class)
  public void testExceptionNoPrefix(){
    Properties properties = new Properties();
    properties.put("prefix.key1", "value1");
    PropertiesUtil.removeProperties(properties, null);
  }
}
