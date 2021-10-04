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
package org.gbif.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectUtilsTest {

  @Test
  public void testCoalesce() {
    assertNull(ObjectUtils.coalesce());
    assertNull(ObjectUtils.coalesce((Integer) null));
    assertNull(ObjectUtils.coalesce(null, (Integer) null));

    assertEquals((Integer) 13, ObjectUtils.coalesce(null, 13));
    assertEquals((Integer) 13, ObjectUtils.coalesce(null, 13, 14));
    assertEquals((Integer) 13, ObjectUtils.coalesce(13, 15));
  }

  @Test
  public void testCoalesce1() {
    assertNull(ObjectUtils.coalesce((Collection) null));
    assertNull(ObjectUtils.coalesce(new ArrayList<>()));
    assertNull(ObjectUtils.coalesce(newArrayList(null, null)));

    assertEquals((Integer) 13, ObjectUtils.coalesce(newArrayList(null, null, 13)));
    assertEquals((Integer) 13, ObjectUtils.coalesce(newArrayList(null, null, 13, 14)));
    assertEquals((Integer) 13, ObjectUtils.coalesce(newArrayList(13, null)));
  }

  public ArrayList<Integer> newArrayList(Integer... elements) {
    ArrayList<Integer> list = new ArrayList<>(elements.length);
    Collections.addAll(list, elements);
    return list;
  }
}
