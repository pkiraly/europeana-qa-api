package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.metadataqa.api.counter.Counters;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
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

	Counters counters = new Counters();
	EdmFieldExtractor calculator;
	JsonPathCache<EdmFieldInstance> cache;

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
		calculator = new EdmFieldExtractor();
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
		assertEquals("92062/BibliographicResource_1000126015451",
			calculator.getResultMap().get(calculator.FIELD_NAME));
	}

	@Test
	public void testDataProvider() throws URISyntaxException, IOException {

		calculator.measure(cache);
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
		assertEquals("92062_Ag_EU_TEL_a0480_Austria", 
			calculator.getResultMap().get("dataset"));

		calculator.abbreviate(true);
		calculator.measure(cache);
		assertEquals("1725", calculator.getResultMap().get("dataset"));
	}

}
