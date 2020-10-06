package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.europeanaqa.api.model.Format;
import de.gwdg.europeanaqa.api.model.OaiPmhUtil;
import de.gwdg.europeanaqa.api.model.SolrClientMock;
import de.gwdg.metadataqa.api.calculator.LanguageCalculator;
import de.gwdg.metadataqa.api.calculator.TfIdfCalculator;
import de.gwdg.metadataqa.api.calculator.UniquenessCalculator;
import de.gwdg.metadataqa.api.interfaces.Calculator;
import de.gwdg.metadataqa.api.model.pathcache.JsonPathCache;
import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhJsonSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.uniqueness.SolrConfiguration;
import de.gwdg.metadataqa.api.util.CompressionLevel;
import de.gwdg.metadataqa.api.util.FileUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class CalculatorFacadeTest {

  public CalculatorFacadeTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testNoAbbreviate() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.abbreviate(false);
    calculator.configure();
    String expected = "92062/BibliographicResource_1000126015451,92062_Ag_EU_TEL_a0480_Austria,Österreichische Nationalbibliothek - Austrian National Library,0.184,1.0,0.181818,0.388889,0.272727,0.5,0.357143,0.75,0.363636,0.4,1,1,1,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,12,0,0,0.0,0.0,0.0";
    String csv = calculator.measure(FileUtils.readFirstLineFromResource("general/test.json"));
    assertEquals(expected, csv);
  }

  @Test
  public void testWithAbbreviate() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.abbreviate(true);
    calculator.configure();
    String expected = "92062/BibliographicResource_1000126015451,1725,2,0.184,1.0,0.181818,0.388889,0.272727,0.5,0.357143,0.75,0.363636,0.4,1,1,1,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,12,0,0,0.0,0.0,0.0";
    String csv = calculator.measure(FileUtils.readFirstLineFromResource("general/test.json"));
    assertEquals(expected, csv);
  }

  @Test
  public void testCalculate() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.abbreviate(true);
    calculator.configure();
    String expected = "92062/BibliographicResource_1000126015451,1725,2,0.184,1.0,0.181818,0.388889,0.272727,0.5,0.357143,0.75,0.363636,0.4,1,1,1,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,12,0,0,0.0,0.0,0.0";
    String csv = calculator.measure(FileUtils.readFirstLineFromResource("general/test.json"));
    assertEquals(expected, csv);
  }

  @Test
  public void testFormat() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    assertEquals(Format.OAI_PMH_JSON, calculator.getFormat());
    assertEquals(EdmOaiPmhJsonSchema.class, calculator.getSchema().getClass());

    calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.setFormat(Format.FULLBEAN);
    assertEquals(Format.FULLBEAN, calculator.getFormat());
    assertEquals(EdmFullBeanSchema.class, calculator.getSchema().getClass());

    calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.setFormat(null);
    assertEquals(null, calculator.getFormat());
    assertEquals(EdmOaiPmhJsonSchema.class, calculator.getSchema().getClass());
  }

  @Test
  public void testAbbreviate() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    assertFalse(calculator.abbreviate());

    calculator.abbreviate(false);
    assertFalse(calculator.abbreviate());

    calculator.abbreviate(true);
    assertTrue(calculator.abbreviate());

    calculator = new EdmCalculatorFacade(true, true, true, false, true, false);
    assertFalse(calculator.abbreviate());

    calculator = new EdmCalculatorFacade(true, true, true, false, true, true);
    assertTrue(calculator.abbreviate());
  }

  @Test
  public void testExtendedFieldExtraction() {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.setFormat(Format.FULLBEAN);
    calculator.setExtendedFieldExtraction(true);
    assertTrue(calculator.isExtendedFieldExtraction());
    calculator.configure();

    Map<String, String> extractableFields = calculator.getSchema().getExtractableFields();
    assertEquals(6, extractableFields.size());
    assertEquals(
        "recordId, dataset, dataProvider, provider, country, language",
        StringUtils.join(extractableFields.keySet(), ", ")
    );

    calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.setFormat(Format.OAI_PMH_XML);
    calculator.setExtendedFieldExtraction(true);
    assertTrue(calculator.isExtendedFieldExtraction());
    calculator.configure();

    extractableFields = calculator.getSchema().getExtractableFields();
    assertEquals(6, extractableFields.size());
    assertEquals(
        "recordId, dataset, dataProvider, provider, country, language",
        StringUtils.join(extractableFields.keySet(), ", ")
    );

    calculator = new EdmCalculatorFacade(true, true, true, false, true);
    assertFalse(calculator.isExtendedFieldExtraction());

    calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.setExtendedFieldExtraction(false);
    assertFalse(calculator.isExtendedFieldExtraction());
    calculator.configure();

    extractableFields = calculator.getSchema().getExtractableFields();
    assertEquals(3, extractableFields.size());
    assertEquals(
        "recordId, dataset, dataProvider",
        StringUtils.join(extractableFields.keySet(), ", ")
    );
  }

  @Test
  public void testEmptyConstructor() {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade();
    assertNotNull(calculator);
    assertEquals(Format.OAI_PMH_JSON, calculator.getFormat());
    assertFalse(calculator.isExtendedFieldExtraction());
    assertTrue(calculator.isFieldExistenceMeasurementEnabled());
    assertTrue(calculator.isFieldCardinalityMeasurementEnabled());
    assertTrue(calculator.isCompletenessMeasurementEnabled());
    assertFalse(calculator.isTfIdfMeasurementEnabled());
    assertFalse(calculator.isProblemCatalogMeasurementEnabled());
    assertFalse(calculator.abbreviate());
  }

  @Test
  public void testTfIdfMeasurement_withoutTermCollection() {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.enableTfIdfMeasurement();
    calculatorFacade.configure();
    assertTrue(calculatorFacade.isTfIdfMeasurementEnabled());
    TfIdfCalculator tfIdfCalculator = getTfIdfCalculator(calculatorFacade);
    assertNotNull(tfIdfCalculator);
    assertFalse(tfIdfCalculator.isTermCollectionEnabled());
  }

  @Test
  public void testTfIdfMeasurement_withTermCollection() {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.enableTfIdfMeasurement();
    calculatorFacade.collectTfIdfTerms(true);

    calculatorFacade.configure();
    assertTrue(calculatorFacade.isTfIdfMeasurementEnabled());

    TfIdfCalculator calculator = getTfIdfCalculator(calculatorFacade);
    assertNotNull(calculator);
    assertTrue(calculator.isTermCollectionEnabled());
  }

  @Test
  public void testLanguageMeasurement() {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.enableLanguageMeasurement();
    assertTrue(calculatorFacade.isLanguageMeasurementEnabled());

    calculatorFacade.configure();

    LanguageCalculator calculator = getCalculator(calculatorFacade, LanguageCalculator.class);

    assertNotNull(calculator);
    assertEquals("languages", calculator.getCalculatorName());
  }

  @Test
  public void testMultilingualSaturation() {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.enableMultilingualSaturationMeasurement();
    assertTrue(calculatorFacade.isMultilingualSaturationMeasurementEnabled());

    calculatorFacade.configure();

    EdmMultilingualitySaturationCalculator calculator = getCalculator(
        calculatorFacade,
        EdmMultilingualitySaturationCalculator.class
    );

    assertNotNull(calculator);
    assertEquals("edmMultilingualitySaturation", calculator.getCalculatorName());
    assertNull(calculator.getSkippedEntryChecker());
  }


  @Test
  public void testMultilingualSaturation_withSkippableCollections() {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.enableMultilingualSaturationMeasurement();
    calculatorFacade.setCheckSkippableCollections(true);
    assertTrue(calculatorFacade.isMultilingualSaturationMeasurementEnabled());
    assertTrue(calculatorFacade.isCheckSkippableCollections());

    calculatorFacade.configure();

    EdmMultilingualitySaturationCalculator calculator = getCalculator(
        calculatorFacade,
        EdmMultilingualitySaturationCalculator.class
    );

    assertNotNull(calculator);
    assertEquals("edmMultilingualitySaturation", calculator.getCalculatorName());
    assertNotNull(calculator.getSkippedEntryChecker());
  }

  @Test
  public void testDisconnectedEntity() {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.enableDisconnectedEntityMeasurement();
    assertTrue(calculatorFacade.isDisconnectedEntityEnabled());

    calculatorFacade.configure();

    assertEquals(3, calculatorFacade.getCalculators().size());

    DisconnectedEntityCalculator calculator = getCalculator(
        calculatorFacade,
        DisconnectedEntityCalculator.class
    );

    assertNotNull(calculator);
    assertEquals("disconnectedEntityCalculator", calculator.getCalculatorName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUniquenessCalculatorWithoutSolrConfiguration() {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.enableUniquenessMeasurement();
    assertTrue(calculatorFacade.isUniquenessMeasurementEnabled());

    calculatorFacade.configure();
  }

  @Test
  public void testUniquenessCalculatorWitSolrConfiguration() throws IOException, URISyntaxException {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.disableFieldExistenceMeasurement();
    calculatorFacade.disableCompletenessMeasurement();
    calculatorFacade.disableFieldCardinalityMeasurement();
    calculatorFacade.enableUniquenessMeasurement();
    calculatorFacade.configureSolr("localhost", "8983", "solr");
    calculatorFacade.abbreviate(true);

    calculatorFacade.configure();
    assertEquals(false, calculatorFacade.isCompletenessMeasurementEnabled());

    System.err.println(calculatorFacade.getCalculators());
    assertEquals(2, calculatorFacade.getCalculators().size());

    UniquenessCalculator calculator = getCalculator(
        calculatorFacade,
        UniquenessCalculator.class
    );

    assertNotNull(calculator);
    assertEquals("uniqueness", calculator.getCalculatorName());
    assertEquals(1, calculator.getSolrFields().get(0).getTotal());
    assertEquals(1, calculator.getSolrFields().get(1).getTotal());
    assertEquals(1, calculator.getSolrFields().get(2).getTotal());
  }

  @Test
  public void testUniquenessCalculator() throws IOException, URISyntaxException {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.disableFieldExistenceMeasurement();
    calculatorFacade.disableCompletenessMeasurement();
    calculatorFacade.disableFieldCardinalityMeasurement();
    calculatorFacade.enableUniquenessMeasurement();
    calculatorFacade.abbreviate(true);

    calculatorFacade.setSolrClient(
        new SolrClientMock(
            new SolrConfiguration("localhost", "8983", "solr")));
    assertTrue(calculatorFacade.isUniquenessMeasurementEnabled());

    calculatorFacade.configure();

    assertEquals(2, calculatorFacade.getCalculators().size());

    UniquenessCalculator calculator = getCalculator(
        calculatorFacade,
        UniquenessCalculator.class
    );

    assertNotNull(calculator);
    assertEquals("uniqueness", calculator.getCalculatorName());
    assertEquals(4000, calculator.getSolrFields().get(0).getTotal());
    assertEquals(1000, calculator.getSolrFields().get(1).getTotal());
    assertEquals(2000, calculator.getSolrFields().get(2).getTotal());

    JsonPathCache cache = new JsonPathCache(FileUtils.readFirstLineFromResource("general/test.json"));
    calculator.measure(cache);
    assertEquals(
      "\"dc_title_ss/count\":3.000000,\"dc_title_ss/score\":0.711154,"
      + "\"dcterms_alternative_ss/count\":0.000000,\"dcterms_alternative_ss/score\":0.000000,"
      + "\"dc_description_ss/count\":0.000000,\"dc_description_ss/score\":0.000000",
      calculator.getCsv(true, CompressionLevel.ZERO));
  }

  @Test
  public void testJsonAndXml() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculatorJson = new EdmCalculatorFacade(true, true, true, false, true);
    calculatorJson.setFormat(Format.FULLBEAN);
    assertEquals(Format.FULLBEAN, calculatorJson.getFormat());
    assertEquals(EdmFullBeanSchema.class, calculatorJson.getSchema().getClass());
    calculatorJson.configure();
    String csvFromJson = calculatorJson.measure(FileUtils.readFirstLineFromResource("general/2048081-_O_532.json"));

    EdmCalculatorFacade calculatorXml = new EdmCalculatorFacade(true, true, true, false, true);
    calculatorXml.setFormat(Format.OAI_PMH_XML);
    assertEquals(Format.OAI_PMH_XML, calculatorXml.getFormat());
    assertEquals(EdmOaiPmhXmlSchema.class, calculatorXml.getSchema().getClass());
    calculatorXml.configure();
    String csvFromXml = calculatorXml.measure(FileUtils.readContentFromResource("general/2048081-_O_532.xml"));

    List<String> header = calculatorJson.getHeader();
    String[] json = csvFromJson.split(",");
    String[] xml = csvFromXml.split(",");

    assertEquals(header, calculatorXml.getHeader());
    assertEquals(json.length, xml.length);
    assertEquals(header.size(), json.length);
    for (int i = 0; i < json.length; i++) {
      if (!header.get(i).equals("completeness:TOTAL")
          && !header.get(i).equals("existence:Aggregation/edm:ugc")
          && !header.get(i).equals("cardinality:Aggregation/edm:ugc")) {
        assertEquals(header.get(i) + " should be equal", json[i], xml[i]);
      }
    }
  }

  @Test
  public void testOaiPmhXml_completeness() throws URISyntaxException, IOException {
    String id = "/2048081/_O_532";

    EdmCalculatorFacade calculatorXml = new EdmCalculatorFacade(true, true, true, false, true);
    calculatorXml.setFormat(Format.OAI_PMH_XML);
    assertEquals(Format.OAI_PMH_XML, calculatorXml.getFormat());
    assertEquals(EdmOaiPmhXmlSchema.class, calculatorXml.getSchema().getClass());
    calculatorXml.configure();
    String csvFromXml = calculatorXml.measure(FileUtils.readFromUrl(OaiPmhUtil.getRecordUrl(id)));
    System.err.println(csvFromXml);
    System.err.println(calculatorXml.getResults());
  }

  @Test
  public void testOaiPmhXml_multilinguality_nonMultilingual() throws IOException, URISyntaxException {
    String id = "/2048081/_O_532";
    // String id = "/92064/bildarchivaustria_Preview_305640";

    EdmCalculatorFacade calculator = new EdmCalculatorFacade();
    calculator.abbreviate(true);
    calculator.disableCompletenessMeasurement();
    calculator.disableFieldCardinalityMeasurement();
    calculator.disableFieldExistenceMeasurement();
    calculator.disableProblemCatalogMeasurement();
    calculator.disableTfIdfMeasurement();
    calculator.disableLanguageMeasurement();
    calculator.enableMultilingualSaturationMeasurement();
    calculator.setCompressionLevel(CompressionLevel.WITHOUT_TRAILING_ZEROS);
    calculator.setSaturationExtendedResult(true);
    calculator.setCheckSkippableCollections(true);
    calculator.setFormat(Format.OAI_PMH_XML);

    calculator.configure();
    assertEquals(EdmOaiPmhXmlSchema.class, calculator.getSchema().getClass());

    String csvFromXml = calculator.measure(FileUtils.readFromUrl(OaiPmhUtil.getRecordUrl(id)));
    assertEquals(
      "/2048081/_O_532,989,206,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,0,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0",
      csvFromXml
    );
    //System.err.println(calculator.getResults());

    csvFromXml = calculator.measure(FileUtils.readContentFromResource("general/2048081-_O_532.xml"));
    assertEquals(
        "/2048081/_O_532,989,206,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,0,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0",
        csvFromXml
    );
  }

  @Test
  public void testOaiPmhXml_multilinguality_multilingual() throws IOException, URISyntaxException {
    String id = "/92064/bildarchivaustria_Preview_305640";

    EdmCalculatorFacade calculator = new EdmCalculatorFacade();
    calculator.abbreviate(true);
    calculator.disableCompletenessMeasurement()
      .disableFieldCardinalityMeasurement()
      .disableFieldExistenceMeasurement()
      .disableProblemCatalogMeasurement()
      .disableTfIdfMeasurement()
      .disableLanguageMeasurement()
      .enableMultilingualSaturationMeasurement()
      .setCompressionLevel(CompressionLevel.WITHOUT_TRAILING_ZEROS);
    calculator.setSaturationExtendedResult(true);
    calculator.setCheckSkippableCollections(true);
    calculator.setFormat(Format.OAI_PMH_XML);

    calculator.configure();
    assertEquals(EdmOaiPmhXmlSchema.class, calculator.getSchema().getClass());

    String expectation = "/92064/bildarchivaustria_Preview_305640,2560,2,1,1,1,-1,0,0,-1,0,0,-1,0,0,1,1,1,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,0,0,0,0,0,0,8,2,4,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,0,0,0,-1,0,0,1,1,1,-1,0,0,0,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,-1,0,0,1.25,0,1.25,11,0,2,0,11,2,5.5,0,5.5";
    String csvFromXml = calculator.measure(FileUtils.readFromUrl(OaiPmhUtil.getRecordUrl(id)));
    assertEquals(expectation, csvFromXml);
    //System.err.println(calculator.getResults());

    csvFromXml = calculator.measure(FileUtils.readContentFromResource("general/92064-bildarchivaustria_Preview_305640.xml"));
    assertEquals(expectation, csvFromXml);
  }

  @Nullable
  private TfIdfCalculator getTfIdfCalculator(EdmCalculatorFacade calculatorFacade) {
    return getCalculator(calculatorFacade, TfIdfCalculator.class);
  }

  @Nullable
  private <T extends Calculator> T getCalculator(EdmCalculatorFacade calculatorFacade, Class<T> clazz) {
    T calculator = null;
    for (Calculator embeddedCalculator : calculatorFacade.getCalculators()) {
      if (embeddedCalculator.getClass().equals(clazz)) {
        calculator = (T) embeddedCalculator;
      }
    }
    return calculator;
  }
}
