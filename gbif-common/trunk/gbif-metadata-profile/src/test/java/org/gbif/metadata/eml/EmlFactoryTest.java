package org.gbif.metadata.eml;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.FileInputStream;
import java.util.Calendar;
import java.util.SimpleTimeZone;

public class EmlFactoryTest extends TestCase {

  @Test
  public void testAlternateJGTIBuild() {
    try {
      Eml eml = EmlFactory.build(new FileInputStream("./src/test/resources/eml/sample2.xml"));

      assertNotNull(eml);

      // JGTI curatorial unit tests
      // A separate test for the alternate JGTI structure, which includes
      // uncertainty is in sample2.xml
      assertNotNull(eml.getJgtiCuratorialUnits());
      assertEquals("jars", eml.getJgtiCuratorialUnits().get(0).getUnitType());
      assertEquals(new Integer("2000"), eml.getJgtiCuratorialUnits().get(0).getRangeMean());
      assertEquals(new Integer("50"), eml.getJgtiCuratorialUnits().get(0).getUncertaintyMeasure());

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testBuild() {
    try {
      Eml eml = EmlFactory.build(new FileInputStream("./src/test/resources/eml/sample.xml"));
      Calendar cal = Calendar.getInstance();
      cal.clear();

      assertNotNull(eml);
      assertEquals("619a4b95-1a82-4006-be6a-7dbe3c9b33c5/v7", eml.getPackageId());
      assertEquals("619a4b95-1a82-4006-be6a-7dbe3c9b33c5", eml.getGuid());

      assertTrue(eml.getAlternateIdentifiers().contains("619a4b95-1a82-4006-be6a-7dbe3c9b33c5"));
      assertTrue(eml.getAlternateIdentifiers().contains("doi:10.1093/ageing/29.1.57"));
      assertTrue(eml.getAlternateIdentifiers().contains("http://ageing.oxfordjournals.org/content/29/1/57"));

      assertEquals(7, eml.getEmlVersion());
      assertEquals("Tanzanian Entomological Collection", eml.getTitle());

      // this is complete test for agents so subsequent agent tests will not be
      // so extensive
      assertNotNull(eml.getResourceCreator());
      assertNotNull(eml.getResourceCreator().getFirstName());
      assertEquals("David", eml.getResourceCreator().getFirstName());
      assertNotNull(eml.getResourceCreator().getLastName());
      assertEquals("Remsen", eml.getResourceCreator().getLastName());
      assertNull(eml.getResourceCreator().getRole());
      assertNotNull(eml.getResourceCreator().getPosition());
      assertEquals("ECAT Programme Officer", eml.getResourceCreator().getPosition());
      assertNotNull(eml.getResourceCreator().getOrganisation());
      assertEquals("GBIF", eml.getResourceCreator().getOrganisation());
      assertNotNull(eml.getResourceCreator().getAddress());
      assertNotNull(eml.getResourceCreator().getAddress().getAddress());
      assertEquals("Universitestparken 15", eml.getResourceCreator().getAddress().getAddress());
      assertNotNull(eml.getResourceCreator().getAddress().getCity());
      assertEquals("Copenhagen", eml.getResourceCreator().getAddress().getCity());
      assertNotNull(eml.getResourceCreator().getAddress().getProvince());
      assertEquals("Sjaelland", eml.getResourceCreator().getAddress().getProvince());
      assertNotNull(eml.getResourceCreator().getAddress().getPostalCode());
      assertEquals("2100", eml.getResourceCreator().getAddress().getPostalCode());
      assertNotNull(eml.getResourceCreator().getAddress().getCountry());
      assertEquals("DK", eml.getResourceCreator().getAddress().getCountry());
      assertNotNull(eml.getResourceCreator().getPhone());
      assertEquals("+4528261487", eml.getResourceCreator().getPhone());
      assertEquals("dremsen@gbif.org", eml.getResourceCreator().getEmail());
      assertNotNull(eml.getResourceCreator().getHomepage());
      assertEquals("http://www.gbif.org", eml.getResourceCreator().getHomepage());

      // agent test with some null values
      assertNotNull(eml.getMetadataProvider());
      assertNotNull(eml.getMetadataProvider().getFirstName());
      assertEquals("Tim", eml.getMetadataProvider().getFirstName());
      assertNotNull(eml.getMetadataProvider().getLastName());
      assertEquals("Robertson", eml.getMetadataProvider().getLastName());
      assertNotNull(eml.getMetadataProvider().getAddress());
      assertNotNull(eml.getMetadataProvider().getAddress().getAddress());
      assertEquals("Universitestparken 15", eml.getMetadataProvider().getAddress().getAddress());
      assertEquals("Copenhagen", eml.getMetadataProvider().getAddress().getCity());
      assertEquals("Copenhagen", eml.getMetadataProvider().getAddress().getProvince());
      assertEquals("2100", eml.getMetadataProvider().getAddress().getPostalCode());
      assertEquals("DK", eml.getMetadataProvider().getAddress().getCountry());
      assertNotNull(eml.getMetadataProvider().getPhone());
      assertEquals("+4528261487", eml.getMetadataProvider().getPhone());
      assertEquals("trobertson@gbif.org", eml.getMetadataProvider().getEmail());
      assertNotNull(eml.getMetadataProvider().getHomepage());
      assertEquals("http://www.gbif.org", eml.getMetadataProvider().getHomepage());

      // agent test for contact
      assertNotNull(eml.getContact());
      assertNotNull(eml.getContact().getFirstName());
      assertEquals("David", eml.getContact().getFirstName());
      assertNotNull(eml.getContact().getLastName());
      assertEquals("Remsen", eml.getContact().getLastName());
      assertNull(eml.getContact().getRole());
      assertNotNull(eml.getContact().getPosition());
      assertEquals("ECAT Programme Officer", eml.getContact().getPosition());
      assertNotNull(eml.getContact().getOrganisation());
      assertEquals("GBIF", eml.getContact().getOrganisation());
      assertNotNull(eml.getContact().getAddress());
      assertNotNull(eml.getContact().getAddress().getAddress());
      assertEquals("Universitestparken 15", eml.getContact().getAddress().getAddress());
      assertNotNull(eml.getContact().getAddress().getCity());
      assertEquals("Copenhagen", eml.getContact().getAddress().getCity());
      assertNotNull(eml.getContact().getAddress().getProvince());
      assertEquals("Sjaelland", eml.getContact().getAddress().getProvince());
      assertNotNull(eml.getContact().getAddress().getPostalCode());
      assertEquals("2100", eml.getContact().getAddress().getPostalCode());
      assertNotNull(eml.getContact().getAddress().getCountry());
      assertEquals("DK", eml.getContact().getAddress().getCountry());
      assertNotNull(eml.getContact().getPhone());
      assertEquals("+4528261487", eml.getContact().getPhone());
      assertEquals("dremsen@gbif.org", eml.getContact().getEmail());
      assertNotNull(eml.getContact().getHomepage());
      assertEquals("http://www.gbif.org", eml.getContact().getHomepage());

      // limited agent with role tests
      assertNotNull(eml.getAssociatedParties());
      assertEquals(2, eml.getAssociatedParties().size());
      assertEquals("principleInvestigator", eml.getAssociatedParties().get(0).getRole());
      assertEquals("pointOfContact", eml.getAssociatedParties().get(1).getRole());

      cal.clear();
      cal.set(2010, Calendar.FEBRUARY, 2);
      assertEquals(cal.getTime(), eml.getPubDate());

      assertEquals("en_US", eml.getLanguage());
      assertEquals("Specimens in jars", eml.getAbstract());

      // multiple KeywordSets tests
      assertNotNull(eml.getKeywords());
      assertEquals(2, eml.getKeywords().size());
      assertNotNull(eml.getKeywords().get(0).getKeywords());
      assertEquals(3, eml.getKeywords().get(0).getKeywords().size());
      assertEquals("Insect", eml.getKeywords().get(0).getKeywords().get(0));
      assertEquals("Fly", eml.getKeywords().get(0).getKeywords().get(1));
      assertEquals("Bee", eml.getKeywords().get(0).getKeywords().get(2));
      assertEquals("Zoology Vocabulary Version 1", eml.getKeywords().get(0).getKeywordThesaurus());
      assertEquals(1, eml.getKeywords().get(1).getKeywords().size());
      assertEquals("Spider", eml.getKeywords().get(1).getKeywords().get(0));
      assertEquals("Zoology Vocabulary Version 1", eml.getKeywords().get(1).getKeywordThesaurus());

      assertEquals("Where can the additional information possibly come from?!", eml.getAdditionalInfo());

      // intellectual rights tests
      assertNotNull(eml.getIntellectualRights());
      assertTrue(eml.getIntellectualRights().startsWith("Owner grants"));
      assertTrue(eml.getIntellectualRights().endsWith("Site)."));
      assertNotNull(eml.getDistributionUrl());
      assertEquals("http://www.any.org/fauna/coleoptera/beetleList.html", eml.getDistributionUrl());

      // geospatial coverages tests
      assertNotNull(eml.getGeospatialCoverages());
      assertEquals(2, eml.getGeospatialCoverages().size());
      assertEquals("Bounding Box 1", eml.getGeospatialCoverages().get(0).getDescription());
      assertEquals(new Double("23.975"),
          eml.getGeospatialCoverages().get(0).getBoundingCoordinates().getMax().getLatitude());
      assertEquals(new Double("0.703"),
          eml.getGeospatialCoverages().get(0).getBoundingCoordinates().getMax().getLongitude());
      assertEquals(new Double("-22.745"),
          eml.getGeospatialCoverages().get(0).getBoundingCoordinates().getMin().getLatitude());
      assertEquals(new Double("-1.564"),
          eml.getGeospatialCoverages().get(0).getBoundingCoordinates().getMin().getLongitude());
      assertEquals("Bounding Box 2", eml.getGeospatialCoverages().get(1).getDescription());
      assertEquals(new Double("43.975"),
          eml.getGeospatialCoverages().get(1).getBoundingCoordinates().getMax().getLatitude());
      assertEquals(new Double("11.564"),
          eml.getGeospatialCoverages().get(1).getBoundingCoordinates().getMax().getLongitude());
      assertEquals(new Double("-32.745"),
          eml.getGeospatialCoverages().get(1).getBoundingCoordinates().getMin().getLatitude());
      assertEquals(new Double("-10.703"),
          eml.getGeospatialCoverages().get(1).getBoundingCoordinates().getMin().getLongitude());

      // temporal coverages tests
      assertEquals(4, eml.getTemporalCoverages().size());
      cal.clear();
      cal.set(2009, Calendar.DECEMBER, 1);
      assertEquals(cal.getTime(), eml.getTemporalCoverages().get(0).getStartDate());
      cal.set(2009, Calendar.DECEMBER, 30);
      assertEquals(cal.getTime(), eml.getTemporalCoverages().get(0).getEndDate());
      cal.set(2008, Calendar.JUNE, 1);
      assertEquals(cal.getTime(), eml.getTemporalCoverages().get(1).getStartDate());
      assertEquals(cal.getTime(), eml.getTemporalCoverages().get(1).getEndDate());
      assertEquals("During the 70s", eml.getTemporalCoverages().get(2).getFormationPeriod());
      assertEquals("Jurassic", eml.getTemporalCoverages().get(3).getLivingTimePeriod());

      // taxonomic coverages tests
      assertEquals(2, eml.getTaxonomicCoverages().size());
      assertEquals("This is a general taxon coverage with only the scientific name", eml.getTaxonomicCoverages().get(0).getDescription());
      assertEquals("Mammalia", eml.getTaxonomicCoverages().get(0).getTaxonKeywords().get(0).getScientificName());
      assertEquals("Reptilia", eml.getTaxonomicCoverages().get(0).getTaxonKeywords().get(1).getScientificName());
      assertEquals("Coleoptera", eml.getTaxonomicCoverages().get(0).getTaxonKeywords().get(2).getScientificName());

      assertEquals("This is a second taxon coverage with all fields", eml.getTaxonomicCoverages().get(1).getDescription());
      assertEquals("Class", eml.getTaxonomicCoverages().get(1).getTaxonKeywords().get(0).getRank());
      assertEquals("Aves", eml.getTaxonomicCoverages().get(1).getTaxonKeywords().get(0).getScientificName());
      assertEquals("Birds", eml.getTaxonomicCoverages().get(1).getTaxonKeywords().get(0).getCommonName());

      assertEquals("Provide data to the whole world.", eml.getPurpose());

      // sampling methods tests
      assertNotNull(eml.getMethodSteps());
      assertEquals(3, eml.getMethodSteps().size());
      assertEquals("Took picture, identified", eml.getMethodSteps().get(0));
      assertEquals("Themometer based test", eml.getMethodSteps().get(1));
      assertEquals("Visual based test", eml.getMethodSteps().get(2));
      assertNotNull(eml.getStudyExtent());
      assertEquals("Daily Obersevation of Pigeons Eating Habits", eml.getStudyExtent());
      assertNotNull(eml.getSampleDescription());
      assertEquals("44KHz is what a CD has... I was more like one a day if I felt like it",
          eml.getSampleDescription());
      assertNotNull(eml.getQualityControl());
      assertEquals("None", eml.getQualityControl());

      // project tests
      assertNotNull(eml.getProject());
      assertEquals("Documenting Some Asian Birds and Insects", eml.getProject().getTitle());
      assertNotNull(eml.getProject().getPersonnel());
      assertEquals("My Deep Pockets", eml.getProject().getFunding());
      assertEquals(StudyAreaDescriptor.GENERIC, eml.getProject().getStudyAreaDescription().getName());
      assertEquals("false", eml.getProject().getStudyAreaDescription().getCitableClassificationSystem());
      assertEquals("Turkish Mountains", eml.getProject().getStudyAreaDescription().getDescriptorValue());
      assertEquals("This was done in Avian Migration patterns", eml.getProject().getDesignDescription());
      assertEquals("doi:tims-ident.2135.ex43.33.d", eml.getCitation().getIdentifier());
      assertEquals("Tims assembled checklist", eml.getCitation().getCitation());
      assertEquals("en", eml.getMetadataLanguage());
      cal.clear();
      // 2002-10-23T18:13:51
      SimpleTimeZone tz = new SimpleTimeZone(1000*60*60,"berlin");
      cal.setTimeZone(tz);
      cal.set(2002, Calendar.OCTOBER, 23, 18, 13, 51);
      cal.set(Calendar.MILLISECOND, 235);
      System.out.println(cal.getTimeZone());
      System.out.println(cal.getTimeInMillis());
      System.out.println(eml.getDateStamp().getTimezoneOffset());
      System.out.println(eml.getDateStamp().getTime());
      assertEquals(cal.getTime(), eml.getDateStamp());

      // bibliographic citations tests
      assertNotNull(eml.getBibliographicCitations());
      assertEquals(3, eml.getBibliographicCitations().size());
      // assertNotNull(eml.getBibliographicCitations().get(0));
      assertEquals("title 1", eml.getBibliographicCitations().get(0).getCitation());
      assertEquals("title 2", eml.getBibliographicCitations().get(1).getCitation());
      assertEquals("title 3", eml.getBibliographicCitations().get(2).getCitation());

      assertEquals("dataset", eml.getHierarchyLevel());

      // physical data tests
      assertNotNull(eml.getPhysicalData());
      assertEquals(2, eml.getPhysicalData().size());
      assertEquals("INV-GCEM-0305a1_1_1.shp", eml.getPhysicalData().get(0).getName());
      assertEquals("ASCII", eml.getPhysicalData().get(0).getCharset());
      assertEquals("shapefile", eml.getPhysicalData().get(0).getFormat());
      assertEquals("2.0", eml.getPhysicalData().get(0).getFormatVersion());
      assertEquals(
          "http://metacat.lternet.edu/knb/dataAccessServlet?docid=knb-lter-gce.109.10&urlTail=accession=INV-GCEM-0305a1&filename=INV-GCEM-0305a1_1_1.TXT",
          eml.getPhysicalData().get(0).getDistributionUrl());
      assertEquals("INV-GCEM-0305a1_1_2.shp", eml.getPhysicalData().get(1).getName());
      assertEquals("ASCII", eml.getPhysicalData().get(1).getCharset());
      assertEquals("shapefile", eml.getPhysicalData().get(1).getFormat());
      assertEquals("2.0", eml.getPhysicalData().get(1).getFormatVersion());
      assertEquals(
          "http://metacat.lternet.edu/knb/dataAccessServlet?docid=knb-lter-gce.109.10&urlTail=accession=INV-GCEM-0305a1&filename=INV-GCEM-0305a1_1_2.TXT",
          eml.getPhysicalData().get(1).getDistributionUrl());

      // JGTI curatorial unit tests
      // A separate for the alternate JGTI structure that includes uncertainty
      // is in sample2.xml
      assertNotNull(eml.getJgtiCuratorialUnits());
      assertEquals("jars", eml.getJgtiCuratorialUnits().get(0).getUnitType());
      assertEquals(new Integer("500"), eml.getJgtiCuratorialUnits().get(0).getRangeStart());
      assertEquals(new Integer("600"), eml.getJgtiCuratorialUnits().get(0).getRangeEnd());

      assertEquals("alcohol", eml.getSpecimenPreservationMethod());
      assertEquals("http://www.tim.org/logo.jpg", eml.getLogoUrl());

      assertEquals("urn:lsid:tim.org:12:1", eml.getParentCollectionId());
      assertEquals("urn:lsid:tim.org:12:2", eml.getCollectionId());
      assertEquals("Mammals", eml.getCollectionName());

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }


}
