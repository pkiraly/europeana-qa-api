package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.europeanaqa.api.model.Format;
import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.util.FileUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
	public void testExtendedFieldExtraction() throws URISyntaxException, IOException {
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
		assertEquals(6, extractableFields.size());
		assertEquals(
			"recordId, dataset, dataProvider",
			StringUtils.join(extractableFields.keySet(), ", ")
		);
	}
}
