package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.pathcache.JsonPathCache;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhJsonSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;
import de.gwdg.metadataqa.api.util.FileUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class DisconnectedEntityCalculatorTest {

  JsonPathCache<EdmFieldInstance> cache;
  Schema schema;

  public DisconnectedEntityCalculatorTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() throws URISyntaxException, IOException {
    schema = new EdmOaiPmhJsonSchema();
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testMain() throws URISyntaxException, IOException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("general/test.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals("0,0,0,0,1,0,1,0", calculator.getCsv(false, CompressionLevel.NORMAL));
  }

  @Test
  public void testPlace() throws URISyntaxException, IOException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("general/test-place.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals("0,0,0,0,7,0,4,3", calculator.getCsv(false, CompressionLevel.NORMAL));
  }

  @Test
  public void testissue41() throws URISyntaxException, IOException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue41-truncatedID.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals("0,0,0,0,1,0,1,0", calculator.getCsv(false, CompressionLevel.NORMAL));
  }

  @Test
  public void testissue5() throws URISyntaxException, IOException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue5-array-in-innerarray.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals("0,1,0,0,4,0,1,2", calculator.getCsv(false, CompressionLevel.NORMAL));
  }

  @Test
  public void testissue6() throws URISyntaxException, IOException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue6-handling-missing-provider.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals("0,0,0,0,0,0,0,0", calculator.getCsv(false, CompressionLevel.NORMAL));
  }

  @Test
  public void testissue8() throws URISyntaxException, IOException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue8-multiple-same-languages.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals("2,0,0,0,6,3,1,0", calculator.getCsv(false, CompressionLevel.NORMAL));
  }

  @Test
  public void test92062a() throws URISyntaxException, IOException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/orphaned-entities.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals("2,1,0,0,6,0,1,2", calculator.getCsv(false, CompressionLevel.NORMAL));
  }

  @Test
  public void test92062b() throws URISyntaxException, IOException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/orphaned-entities-2.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals("3,0,0,0,7,0,2,2", calculator.getCsv(false, CompressionLevel.NORMAL));
  }

  @Test
  public void testE2DD942FC1F8519066C56D1136D99B8093A83727() throws URISyntaxException, IOException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("disconnected-entities/E2DD942FC1F8519066C56D1136D99B8093A83727.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals("0,1,0,0,10,0,7,2", calculator.getCsv(false, CompressionLevel.NORMAL));
  }

  @Test
  public void testHeaders() throws IOException, URISyntaxException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("disconnected-entities/E2DD942FC1F8519066C56D1136D99B8093A83727.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    assertEquals(
      Arrays.asList(
        "orphanedEntities", "selfLinkedEntities", "brokenProviderLinks",
        "brokenEuropeanaLinks", "contextualEntityCount",
        "providerProxyLinksCount", "europeanaProxyLinksCount",
        "contextualLinksCount"
      ),
      calculator.getHeader()
    );
  }

  @Test
  public void testCalculatorName() throws IOException, URISyntaxException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("disconnected-entities/E2DD942FC1F8519066C56D1136D99B8093A83727.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    assertEquals(
        "disconnectedEntityCalculator",
        calculator.getCalculatorName()
    );
  }

  @Test
  public void testEdmStructure() throws URISyntaxException, IOException {
    cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue5-array-in-innerarray.json"));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals(0, calculator.getEdmStructure().getProviderProxyLinks().size());
    assertEquals(1, calculator.getEdmStructure().getEuropeanaProxyLinks().size());
    Map<String, ? extends Object> resultMap = calculator.getResultMap();
    Map<String, ? extends Object> result = calculator
        .getLabelledResultMap()
        .get(calculator.getCalculatorName());
    assertEquals(resultMap, result);
    assertEquals(0, result.get("orphanedEntities"));
    assertEquals(0, result.get("brokenProviderLinks"));
    assertEquals(0, result.get("brokenEuropeanaLinks"));
    assertEquals(4, result.get("contextualEntityCount"));
    assertEquals(0, result.get("providerProxyLinksCount"));
    assertEquals(1, result.get("selfLinkedEntities"));
    assertEquals(1, result.get("europeanaProxyLinksCount"));
    assertEquals(2, result.get("contextualLinksCount"));
  }

  @Test
  public void testEdmStructure1() throws URISyntaxException, IOException {
    String fileName = "general/test-place.json";
    cache = new JsonPathCache<>(FileUtils.readFirstLine(fileName));
    DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
    calculator.measure(cache);
    assertEquals(0, calculator.getEdmStructure().getProviderProxyLinks().size());
    assertEquals(4, calculator.getEdmStructure().getEuropeanaProxyLinks().size());
    Map<String, ? extends Object> resultMap = calculator.getResultMap();
    Map<String, ? extends Object> result = calculator
        .getLabelledResultMap()
        .get(calculator.getCalculatorName());
    assertEquals(resultMap, result);
    assertEquals(0, result.get("orphanedEntities"));
    assertEquals(0, result.get("brokenProviderLinks"));
    assertEquals(0, result.get("brokenEuropeanaLinks"));
    assertEquals(7, result.get("contextualEntityCount"));
    assertEquals(0, result.get("providerProxyLinksCount"));
    assertEquals(0, result.get("selfLinkedEntities"));
    assertEquals(4, result.get("europeanaProxyLinksCount"));
    assertEquals(3, result.get("contextualLinksCount"));
  }
}
