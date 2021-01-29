/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    assertTrue(filteredProperties.containsKey("key1"), "Prefix is removed from the original key");
    assertEquals(properties.get("prefix.key1"), filteredProperties.getProperty("key1"), "Value remains the same");
  }

  @Test
  public void testSubsetProperties(){

    Properties properties = new Properties();
    properties.put("prefix.key1", "value1");
    properties.put("key2", "value2");

    Properties newProperties = PropertiesUtil.subsetProperties(properties, "prefix.");
    assertEquals(1, newProperties.size());
    assertEquals(2, properties.size());

    assertTrue(newProperties.containsKey("prefix.key1"), "Prefix is kept from the original key");
    assertTrue(properties.containsKey("prefix.key1"), "key1 is still in original Properties");
    assertTrue(properties.containsKey("key2"), "key2 is still in original Properties");
  }

  @Test
  public void testRemoveProperties(){

    Properties properties = new Properties();
    properties.put("prefix.key1", "value1");
    properties.put("key2", "value2");

    Properties newProperties = PropertiesUtil.removeProperties(properties, "prefix.");
    assertEquals(1, newProperties.size());
    assertEquals(1, properties.size());

    assertTrue(newProperties.containsKey("prefix.key1"), "Prefix is kept from the original key");
    assertTrue(properties.containsKey("key2"), "Other element is still present in original Propeties");
  }

  @Test
  public void testEmptyProperties(){
    Properties properties = new Properties();
    Properties newProperties = PropertiesUtil.removeProperties(properties, "prefix");
    assertNotNull(newProperties);
    assertEquals(0, properties.size());
  }

  @Test
  public void testBooleanProperties(){
    final String KEY = "key";
    Properties p = new Properties();
    p.put(KEY, "value1");
    assertFalse(PropertiesUtil.propertyAsBool(p, KEY, false));
    assertTrue(PropertiesUtil.propertyAsBool(p, KEY, true));

    p.put(KEY, "false");
    assertFalse(PropertiesUtil.propertyAsBool(p, KEY, true));

    p.put(KEY, "f");
    assertFalse(PropertiesUtil.propertyAsBool(p, KEY, true));

    p.put(KEY, "csy");
    assertTrue(PropertiesUtil.propertyAsBool(p, KEY, true));

    p.put(KEY, "no");
    assertFalse(PropertiesUtil.propertyAsBool(p, KEY, true));

    p.remove(KEY);
    assertFalse(PropertiesUtil.propertyAsBool(p, KEY, false));
    assertTrue(PropertiesUtil.propertyAsBool(p, KEY, true));

    p.put(KEY, "yes");
    assertTrue(PropertiesUtil.propertyAsBool(p, KEY, false));

    p.put(KEY, "y");
    assertTrue(PropertiesUtil.propertyAsBool(p, KEY, false));

    p.put(KEY, "Yes");
    assertTrue(PropertiesUtil.propertyAsBool(p, KEY, false));

    p.put(KEY, "True");
    assertTrue(PropertiesUtil.propertyAsBool(p, KEY, false));

    p.put(KEY, "t");
    assertTrue(PropertiesUtil.propertyAsBool(p, KEY, false));

    p.put(KEY, "on");
    assertTrue(PropertiesUtil.propertyAsBool(p, KEY, false));
  }

  @Test
  public void testExceptionNoProperties(){
    assertThrows(NullPointerException.class, () -> PropertiesUtil.removeProperties(null, "prefix"));
  }

  @Test
  public void testExceptionNoPrefix(){
    Properties properties = new Properties();
    properties.put("prefix.key1", "value1");
    assertThrows(IllegalStateException.class, () -> PropertiesUtil.removeProperties(properties, null));
  }
}
