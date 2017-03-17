package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;
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
public class OrphanedEntityCalculatorTest {

	JsonPathCache<EdmFieldInstance> cache;
	Schema schema;

	public OrphanedEntityCalculatorTest() {
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
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testMain() throws URISyntaxException, IOException {
		cache = new JsonPathCache<>(FileUtils.readFirstLine("general/test.json"));
		OrphanedEntityCalculator calculator = new OrphanedEntityCalculator(schema);
		calculator.measure(cache);
		assertEquals("0,0,0", calculator.getCsv(false, CompressionLevel.NORMAL));
	}

	@Test
	public void testPlace() throws URISyntaxException, IOException {
		cache = new JsonPathCache<>(FileUtils.readFirstLine("general/test-place.json"));
		OrphanedEntityCalculator calculator = new OrphanedEntityCalculator(schema);
		calculator.measure(cache);
		assertEquals("0,0,0", calculator.getCsv(false, CompressionLevel.NORMAL));
	}

	@Test
	public void testissue41() throws URISyntaxException, IOException {
		cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue41-truncatedID.json"));
		OrphanedEntityCalculator calculator = new OrphanedEntityCalculator(schema);
		calculator.measure(cache);
		assertEquals("0,0,0", calculator.getCsv(false, CompressionLevel.NORMAL));
	}

	@Test
	public void testissue5() throws URISyntaxException, IOException {
		cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue5-array-in-innerarray.json"));
		OrphanedEntityCalculator calculator = new OrphanedEntityCalculator(schema);
		calculator.measure(cache);
		assertEquals("0,0,0", calculator.getCsv(false, CompressionLevel.NORMAL));
	}

	@Test
	public void testissue6() throws URISyntaxException, IOException {
		cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue6-handling-missing-provider.json"));
		OrphanedEntityCalculator calculator = new OrphanedEntityCalculator(schema);
		calculator.measure(cache);
		assertEquals("0,0,0", calculator.getCsv(false, CompressionLevel.NORMAL));
	}

	@Test
	public void testissue8() throws URISyntaxException, IOException {
		cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/issue8-multiple-same-languages.json"));
		OrphanedEntityCalculator calculator = new OrphanedEntityCalculator(schema);
		calculator.measure(cache);
		assertEquals("2,0,0", calculator.getCsv(false, CompressionLevel.NORMAL));
	}

	@Test
	public void test92062a() throws URISyntaxException, IOException {
		cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/orphaned-entities.json"));
		OrphanedEntityCalculator calculator = new OrphanedEntityCalculator(schema);
		calculator.measure(cache);
		assertEquals("2,0,0", calculator.getCsv(false, CompressionLevel.NORMAL));
	}

	@Test
	public void test92062b() throws URISyntaxException, IOException {
		cache = new JsonPathCache<>(FileUtils.readFirstLine("issue-examples/orphaned-entities-2.json"));
		OrphanedEntityCalculator calculator = new OrphanedEntityCalculator(schema);
		calculator.measure(cache);
		assertEquals("3,0,0", calculator.getCsv(false, CompressionLevel.NORMAL));
	}
}
