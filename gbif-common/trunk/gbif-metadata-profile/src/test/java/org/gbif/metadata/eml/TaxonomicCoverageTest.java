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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * @author markus
 *
 */
public class TaxonomicCoverageTest {
  @Test
  public final void testConcatAdd() {
    TaxonomicCoverage tc = new TaxonomicCoverage();
    assertTrue(tc.getTaxonKeywords().isEmpty());
    tc.addTaxonKeywords("Abies alba; Puma concolor L.;Ea Distant 1911; Ge Nicéville 1895; Hydnellum (Hydnellum) scrobiculatum zonatum (Banker) D. Hall & D.E. Stuntz 1972 ");
    assertEquals(5, tc.getTaxonKeywords().size());
    assertEquals("Hydnellum (Hydnellum) scrobiculatum zonatum (Banker) D. Hall & D.E. Stuntz 1972", tc.getTaxonKeywords().get(4).getScientificName());

    tc.addTaxonKeywords("Ge Nicéville 1895   |  Hydnellum (Hydnellum) scrobiculatum zonatum (Banker) D. Hall & D.E. Stuntz 1972 ");
    assertEquals(7, tc.getTaxonKeywords().size());
    assertEquals("Hydnellum (Hydnellum) scrobiculatum zonatum (Banker) D. Hall & D.E. Stuntz 1972", tc.getTaxonKeywords().get(6).getScientificName());

    tc.addTaxonKeywords("Ge Nicéville 1895   \n  Hydnellum (Hydnellum) scrobiculatum zonatum (Banker) D. Hall & D.E. Stuntz 1972 ");
    assertEquals(9, tc.getTaxonKeywords().size());
    assertEquals("Hydnellum (Hydnellum) scrobiculatum zonatum (Banker) D. Hall & D.E. Stuntz 1972", tc.getTaxonKeywords().get(8).getScientificName());
  }
}
