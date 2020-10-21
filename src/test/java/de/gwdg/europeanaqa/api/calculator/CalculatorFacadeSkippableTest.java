package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.metadataqa.api.interfaces.Calculator;
import de.gwdg.metadataqa.api.util.CompressionLevel;
import de.gwdg.metadataqa.api.util.FileUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
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
public class CalculatorFacadeSkippableTest {

  private String record;
  private EdmCalculatorFacade facadeSkippable;
  private EdmCalculatorFacade facadeNormal;

  public CalculatorFacadeSkippableTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() throws URISyntaxException, IOException {
    record = FileUtils.readFirstLineFromResource("general/test.json");

    facadeSkippable = new EdmCalculatorFacade(true, true, true, false, true);
    facadeSkippable.abbreviate(false);
    facadeSkippable.setCheckSkippableCollections(true);
    facadeSkippable.configure();

    facadeNormal = new EdmCalculatorFacade(true, true, true, false, true);
    facadeNormal.abbreviate(false);
    facadeNormal.setCheckSkippableCollections(false);
    facadeNormal.configure();
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testCompareMap() throws URISyntaxException, IOException {
    facadeSkippable.measure(record);
    Map<String,? extends Object> skippableMap = getResultMap(facadeSkippable);

    facadeNormal.measure(record);
    Map<String,? extends Object> normalMap = getResultMap(facadeNormal);

    assertEquals(0, skippableMap.get("existence:Concept/rdf:about"));
    assertEquals(1, normalMap.get("existence:Concept/rdf:about"));
    assertEquals(0, skippableMap.get("existence:Concept/skos:prefLabel"));
    assertEquals(1, normalMap.get("existence:Concept/skos:prefLabel"));

    assertEquals(skippableMap.get("cardinality:Concept/rdf:about"), 0);
    assertEquals(normalMap.get("cardinality:Concept/rdf:about"), 1);
    assertEquals(skippableMap.get("cardinality:Concept/skos:prefLabel"), 0);
    assertEquals(normalMap.get("cardinality:Concept/skos:prefLabel"), 12);

    for (String key : skippableMap.keySet()) {
      if (key.contains("existence") || key.contains("cardinality")) {
        if (key.contains("Concept/rdf:about") || key.contains("Concept/skos:prefLabel")) {
          assertNotEquals(key, skippableMap.get(key), normalMap.get(key));
        } else {
          assertEquals(key, skippableMap.get(key), normalMap.get(key));
        }
      } else if (key.equals("TOTAL")) {
        assertNotEquals(key, skippableMap.get(key), normalMap.get(key));
      } else {
        assertEquals(key, skippableMap.get(key), normalMap.get(key));
      }
    }
  }

  @Test
  public void testCompareCsv() throws URISyntaxException, IOException {
    facadeSkippable.measure(record);
    String[] skippableItems = getCsv(facadeSkippable);

    facadeNormal.measure(record);
    String[] normalItems = getCsv(facadeNormal);

    for (int i = 0; i < normalItems.length; i++) {
      if (skippableItems[i].contains("Concept/rdf:about")
          || skippableItems[i].contains("Concept/skos:prefLabel")
          || skippableItems[i].contains("TOTAL")
      ) {
        assertNotEquals(skippableItems[i], normalItems[i]);
      } else {
        assertEquals(skippableItems[i], normalItems[i]);
      }
    }
  }

  @Test
  public void testNoAbbreviate() throws URISyntaxException, IOException {
    EdmCalculatorFacade facade = new EdmCalculatorFacade(true, true, true, false, true);
    facade.abbreviate(false);
    facade.setCheckSkippableCollections(false);
    facade.configure();
    String expected = "92062/BibliographicResource_1000126015451,92062_Ag_EU_TEL_a0480_Austria,Österreichische Nationalbibliothek - Austrian National Library,0.184,1.0,0.181818,0.388889,0.272727,0.5,0.357143,0.75,0.363636,0.4,1,1,1,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,12,0,0,0.0,0.0,0.0";

    String csv = facade.measure(record);
    assertEquals(expected, csv);
  }

  private String[] getCsv(EdmCalculatorFacade facade) {
    String allCsv = "";
    for (Calculator calculator : facade.getCalculators()) {
      if (!allCsv.isEmpty())
        allCsv += ",";
      allCsv += calculator.getCsv(true, CompressionLevel.NORMAL);
    }
    return allCsv.split(",");
  }

  private Map<String,? extends Object> getResultMap(EdmCalculatorFacade facade) {
    Map<String, Object> resultMap = new LinkedHashMap<>();
    for (Calculator calculator : facade.getCalculators())
      resultMap.putAll(calculator.getResultMap());
    return resultMap;
  }

  @Test
  public void testWithAbbreviate() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.abbreviate(true);
    calculator.configure();
    String expected = "92062/BibliographicResource_1000126015451,1725,2,0.184,1.0,0.181818,0.388889,0.272727,0.5,0.357143,0.75,0.363636,0.4,1,1,1,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,12,0,0,0.0,0.0,0.0";
    String csv = calculator.measure(record);
    assertEquals(expected, csv);
  }

  @Test
  public void testCalculate() throws URISyntaxException, IOException {
    EdmCalculatorFacade calculator = new EdmCalculatorFacade(true, true, true, false, true);
    calculator.abbreviate(true);
    calculator.configure();
    String expected = "92062/BibliographicResource_1000126015451,1725,2,0.184,1.0,0.181818,0.388889,0.272727,0.5,0.357143,0.75,0.363636,0.4,1,1,1,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,12,0,0,0.0,0.0,0.0";
    String csv = calculator.measure(record);
    assertEquals(expected, csv);
  }
}
