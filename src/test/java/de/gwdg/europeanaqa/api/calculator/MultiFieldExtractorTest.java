package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.FileUtils;
import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class MultiFieldExtractorTest {

	MultiFieldExtractor calculator;
	JsonPathCache<EdmFieldInstance> cache;
	Schema schema;

	public MultiFieldExtractorTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws URISyntaxException, IOException {
		schema = new EdmOaiPmhXmlSchema();
		calculator = new MultiFieldExtractor(schema);
		// calculator.setDataProviderManager(new EdmDataProviderManager());
		// calculator.setDatasetManager(new EdmDatasetManager());
		cache = new JsonPathCache<>(FileUtils.readFirstLine("general/test.json"));
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testId() throws URISyntaxException, IOException {
		calculator.measure(cache);
		assertEquals(3, calculator.getResultMap().size());
		System.err.println(calculator.getResultMap().get("recordId").getClass());

		assertEquals("92062/BibliographicResource_1000126015451",
			((ArrayList) calculator.getResultMap().get("recordId")).get(0));
		assertEquals("92062_Ag_EU_TEL_a0480_Austria",
			((ArrayList) calculator.getResultMap().get("dataset")).get(0));
		assertEquals("Österreichische Nationalbibliothek - Austrian National Library",
			((ArrayList) calculator.getResultMap().get("dataProvider")).get(0));
	}

	@Test
	public void testDataProvider() throws URISyntaxException, IOException {

		calculator.measure(cache);
		assertEquals(3, calculator.getResultMap().size());
		assertEquals(
			"Österreichische Nationalbibliothek - Austrian National Library",
			((ArrayList) calculator.getResultMap().get("dataProvider")).get(0));
	}

	@Test
	public void testDataset() throws URISyntaxException, IOException {
		calculator.measure(cache);
		assertEquals(3, calculator.getResultMap().size());
		assertEquals("92062_Ag_EU_TEL_a0480_Austria",
			((ArrayList) calculator.getResultMap().get("dataset")).get(0));
	}

	@Test
	public void testIdTruncationIssue() throws URISyntaxException, IOException {
		cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue41-truncatedID.json"));
		calculator.measure(cache);
		assertEquals(3, calculator.getResultMap().size());

		// assertEquals("9200365/BibliographicResource_3000059507130",
		// 	calculator.getResultMap().get(calculator.FIELD_NAME));

		assertEquals("National Library of France",
			((ArrayList) calculator.getResultMap().get("dataProvider")).get(0));

		assertEquals("9200365_Ag_EU_TEL_a0142_Gallica",
			((ArrayList) calculator.getResultMap().get("dataset")).get(0));

		// calculator.abbreviate(true);
		calculator.measure(cache);
		assertEquals("9200365_Ag_EU_TEL_a0142_Gallica", ((ArrayList) calculator.getResultMap().get("dataset")).get(0));
	}

	@Test
	public void testExtension() throws URISyntaxException, IOException {
		Schema schema = new EdmFullBeanSchema();
		calculator = new MultiFieldExtractor(schema);
		// calculator.setDataProviderManager(new EdmDataProviderManager());
		// calculator.setDatasetManager(new EdmDatasetManager());
		cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/missing-provider.json"));
		calculator.measure(cache);
		assertEquals(3, calculator.getResultMap().size());

		assertEquals("/91943/5B14E82B8060CE780394F215B9631E7432068659",
			((ArrayList) calculator.getResultMap().get("recordId")).get(0));

		assertEquals(0, ((ArrayList) calculator.getResultMap().get("dataProvider")).size());

		assertEquals("91943_L_Es_BibCatalunya_josepvinyal",
			((ArrayList) calculator.getResultMap().get("dataset")).get(0));
	}

	@Test
	public void testExtended() throws URISyntaxException, IOException {
		schema.addExtractableField(
			"country",
			schema.getPathByLabel("EuropeanaAggregation/edm:country").getAbsoluteJsonPath(0) + "[0]"
		);
		schema.addExtractableField(
			"language",
			schema.getPathByLabel("EuropeanaAggregation/edm:language").getAbsoluteJsonPath(0) + "[0]"
		);

		calculator.measure(cache);
		assertEquals(5, calculator.getResultMap().size());
		assertEquals("Austria", ((ArrayList) calculator.getResultMap().get("country")).get(0));
		assertEquals("de", ((ArrayList) calculator.getResultMap().get("language")).get(0));
	}

	@Test
	public void testExtendedWithAbbreviation() throws URISyntaxException, IOException {
		schema.addExtractableField(
			"country",
			schema.getPathByLabel("EuropeanaAggregation/edm:country").getAbsoluteJsonPath(0));
		schema.addExtractableField(
			"language",
			schema.getPathByLabel("EuropeanaAggregation/edm:language").getAbsoluteJsonPath(0)
		);

		calculator.measure(cache);
		assertEquals(5, calculator.getResultMap().size());
		assertEquals("Austria", ((ArrayList) calculator.getResultMap().get("country")).get(0));
		assertEquals("de", ((ArrayList) calculator.getResultMap().get("language")).get(0));
	}
}
