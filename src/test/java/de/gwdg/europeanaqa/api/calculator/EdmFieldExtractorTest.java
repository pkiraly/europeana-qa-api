package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.europeanaqa.api.abbreviation.EdmCountryManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmLanguageManager;
import de.gwdg.metadataqa.api.counter.Counters;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.FileUtils;
import java.io.IOException;
import java.net.URISyntaxException;
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
public class EdmFieldExtractorTest {

	EdmFieldExtractor calculator;
	JsonPathCache<EdmFieldInstance> cache;
	Schema schema;

	public EdmFieldExtractorTest() {
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
		calculator = new EdmFieldExtractor(schema);
		calculator.setDataProviderManager(new EdmDataProviderManager());
		calculator.setDatasetManager(new EdmDatasetManager());
		cache = new JsonPathCache<>(FileUtils.readFirstLine("general/test.json"));
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testId() throws URISyntaxException, IOException {
		calculator.measure(cache);
		assertEquals(3, calculator.getResultMap().size());
		assertEquals("92062/BibliographicResource_1000126015451",
			calculator.getResultMap().get(calculator.FIELD_NAME));
	}

	@Test
	public void testDataProvider() throws URISyntaxException, IOException {

		calculator.measure(cache);
		assertEquals(3, calculator.getResultMap().size());
		assertEquals(
			"Österreichische Nationalbibliothek - Austrian National Library",
			calculator.getResultMap().get("dataProvider"));

		calculator.abbreviate(true);
		calculator.measure(cache);
		assertEquals("2", calculator.getResultMap().get("dataProvider"));
	}

	@Test
	public void testDataset() throws URISyntaxException, IOException {
		calculator.measure(cache);
		assertEquals(3, calculator.getResultMap().size());
		assertEquals("92062_Ag_EU_TEL_a0480_Austria",
			calculator.getResultMap().get("dataset"));

		calculator.abbreviate(true);
		calculator.measure(cache);
		assertEquals("1725", calculator.getResultMap().get("dataset"));
	}

	@Test
	public void testIdTruncationIssue() throws URISyntaxException, IOException {
		cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue41-truncatedID.json"));
		calculator.measure(cache);
		assertEquals(3, calculator.getResultMap().size());

		assertEquals("9200365/BibliographicResource_3000059507130",
			calculator.getResultMap().get(calculator.FIELD_NAME));

		assertEquals("National Library of France",
			calculator.getResultMap().get("dataProvider"));

		assertEquals("9200365_Ag_EU_TEL_a0142_Gallica",
			calculator.getResultMap().get("dataset"));

		calculator.abbreviate(true);
		calculator.measure(cache);
		assertEquals("1632", calculator.getResultMap().get("dataset"));
	}

	@Test
	public void testExtension() throws URISyntaxException, IOException {
		Schema schema = new EdmFullBeanSchema();
		calculator = new EdmFieldExtractor(schema);
		calculator.setDataProviderManager(new EdmDataProviderManager());
		calculator.setDatasetManager(new EdmDatasetManager());
		cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/missing-provider.json"));
		calculator.measure(cache);
		assertEquals(3, calculator.getResultMap().size());

		assertEquals("/91943/5B14E82B8060CE780394F215B9631E7432068659",
			calculator.getResultMap().get(calculator.FIELD_NAME));

		assertEquals("Universitat Autònoma de Barcelona",
			calculator.getResultMap().get("dataProvider"));

		assertEquals("91943_L_Es_BibCatalunya_josepvinyal",
			calculator.getResultMap().get("dataset"));

		calculator.abbreviate(true);
		calculator.measure(cache);
		assertEquals("1354", calculator.getResultMap().get("dataset"));
		assertEquals("1230", calculator.getResultMap().get("dataProvider"));
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
		assertEquals("austria", calculator.getResultMap().get("country"));
		assertEquals("de", calculator.getResultMap().get("language"));
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

		calculator.abbreviate(true);
		calculator.addAbbreviationManager("country", new EdmCountryManager());
		calculator.addAbbreviationManager("language", new EdmLanguageManager());

		calculator.measure(cache);
		assertEquals(5, calculator.getResultMap().size());
		assertEquals("2", calculator.getResultMap().get("country"));
		assertEquals("6", calculator.getResultMap().get("language"));
	}
}
