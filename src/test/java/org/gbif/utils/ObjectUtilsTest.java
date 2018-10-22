/***************************************************************************
 * Copyright 2016 Global Biodiversity Information Facility Secretariat
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

package org.gbif.utils;

import java.util.Collection;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class ObjectUtilsTest {

  @Test
  public void testCoalesce() throws Exception {
    assertNull(ObjectUtils.coalesce());
    assertNull(ObjectUtils.coalesce((Integer)null));
    assertNull(ObjectUtils.coalesce((Integer)null, (Integer)null));

    assertEquals((Integer)13, ObjectUtils.coalesce(null, 13));
    assertEquals((Integer)13, ObjectUtils.coalesce(null, 13, 14));
    assertEquals((Integer)13, ObjectUtils.coalesce(13, 15));
  }

  @Test
  public void testCoalesce1() throws Exception {
    assertNull(ObjectUtils.coalesce((Collection) null));
    assertNull(ObjectUtils.coalesce(Lists.newArrayList()));
    assertNull(ObjectUtils.coalesce(Lists.newArrayList(null, null)));

    assertEquals((Integer)13, ObjectUtils.coalesce(Lists.newArrayList(null, null, 13)));
    assertEquals((Integer)13, ObjectUtils.coalesce(Lists.newArrayList(null, null, 13, 14)));
    assertEquals((Integer)13, ObjectUtils.coalesce(Lists.newArrayList(13, null)));

  }
}