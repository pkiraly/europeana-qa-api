package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.europeanaqa.api.model.Format;
import de.gwdg.europeanaqa.api.model.SolrClientMock;
import de.gwdg.metadataqa.api.calculator.LanguageCalculator;
import de.gwdg.metadataqa.api.calculator.TfIdfCalculator;
import de.gwdg.metadataqa.api.calculator.UniquenessCalculator;
import de.gwdg.metadataqa.api.interfaces.Calculator;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.uniqueness.SolrConfiguration;
import de.gwdg.metadataqa.api.util.CompressionLevel;
import de.gwdg.metadataqa.api.util.FileUtils;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
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
    String csv = calculator.measure(FileUtils.readFirstLine("general/test.json"));
    assertEquals(expected, csv);
  }

  @Test
  public void testWithAbbreviate() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.abbreviate(true);
    calculator.configure();
    String expected = "92062/BibliographicResource_1000126015451,1725,2,0.184,1.0,0.181818,0.388889,0.272727,0.5,0.357143,0.75,0.363636,0.4,1,1,1,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,12,0,0,0.0,0.0,0.0";
    String csv = calculator.measure(FileUtils.readFirstLine("general/test.json"));
    assertEquals(expected, csv);
  }

  @Test
  public void testCalculate() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.abbreviate(true);
    calculator.configure();
    String expected = "92062/BibliographicResource_1000126015451,1725,2,0.184,1.0,0.181818,0.388889,0.272727,0.5,0.357143,0.75,0.363636,0.4,1,1,1,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,12,0,0,0.0,0.0,0.0";
    String csv = calculator.measure(FileUtils.readFirstLine("general/test.json"));
    assertEquals(expected, csv);
  }

  @Test
  public void testFormat() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    assertEquals(Format.OAI_PMH_XML, calculator.getFormat());
    assertEquals(EdmOaiPmhXmlSchema.class, calculator.getSchema().getClass());

    calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.setFormat(Format.FULLBEAN);
    assertEquals(Format.FULLBEAN, calculator.getFormat());
    assertEquals(EdmFullBeanSchema.class, calculator.getSchema().getClass());

    calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.setFormat(null);
    assertEquals(null, calculator.getFormat());
    assertEquals(EdmOaiPmhXmlSchema.class, calculator.getSchema().getClass());
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
    assertEquals(Format.OAI_PMH_XML, calculator.getFormat());
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
    calculatorFacade.enableTfIdfMeasurement(true);
    calculatorFacade.configure();
    assertTrue(calculatorFacade.isTfIdfMeasurementEnabled());
    TfIdfCalculator tfIdfCalculator = getTfIdfCalculator(calculatorFacade);
    assertNotNull(tfIdfCalculator);
    assertFalse(tfIdfCalculator.isTermCollectionEnabled());
  }

  @Test
  public void testTfIdfMeasurement_withTermCollection() {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.enableTfIdfMeasurement(true);
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
    calculatorFacade.enableLanguageMeasurement(true);
    assertTrue(calculatorFacade.isLanguageMeasurementEnabled());

    calculatorFacade.configure();

    LanguageCalculator calculator = getCalculator(calculatorFacade, LanguageCalculator.class);

    assertNotNull(calculator);
    assertEquals("languages", calculator.getCalculatorName());
  }

  @Test
  public void testMultilingualSaturation() {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.enableMultilingualSaturationMeasurement(true);
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
    calculatorFacade.enableMultilingualSaturationMeasurement(true);
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
    calculatorFacade.enableDisconnectedEntityMeasurement(true);
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
    calculatorFacade.enableUniquenessMeasurement(true);
    assertTrue(calculatorFacade.isUniquenessMeasurementEnabled());

    calculatorFacade.configure();
  }

  @Test
  public void testUniquenessCalculatorWitSolrConfiguration() throws IOException, URISyntaxException {
    EdmCalculatorFacade calculatorFacade = new EdmCalculatorFacade();
    calculatorFacade.enableFieldExistenceMeasurement(false);
    calculatorFacade.enableCompletenessMeasurement(false);
    calculatorFacade.enableFieldCardinalityMeasurement(false);
    calculatorFacade.enableUniquenessMeasurement(true);
    calculatorFacade.abbreviate(true);
    calculatorFacade.configureSolr("localhost", "8983", "solr");

    calculatorFacade.configure();

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
    calculatorFacade.enableFieldExistenceMeasurement(false);
    calculatorFacade.enableCompletenessMeasurement(false);
    calculatorFacade.enableFieldCardinalityMeasurement(false);
    calculatorFacade.enableUniquenessMeasurement(true);
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

    JsonPathCache cache = new JsonPathCache(FileUtils.readFirstLine("general/test.json"));
    calculator.measure(cache);
    assertEquals(
      "\"dc_title_ss/count\":3.000000,\"dc_title_ss/score\":0.711154,"
      + "\"dcterms_alternative_ss/count\":0.000000,\"dcterms_alternative_ss/score\":0.000000,"
      + "\"dc_description_ss/count\":0.000000,\"dc_description_ss/score\":0.000000",
      calculator.getCsv(true, CompressionLevel.ZERO));
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
