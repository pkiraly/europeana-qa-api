package com.nsdr.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import com.nsdr.europeanaqa.api.TestUtils;
import com.nsdr.europeanaqa.api.counter.Counters;
import com.nsdr.europeanaqa.api.interfaces.Calculator;
import com.nsdr.europeanaqa.api.model.JsonPathCache;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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

	private void run(CalculatorFacade calculatorFacade, String expected) throws URISyntaxException, IOException {
		String jsonRecord = TestUtils.readFirstLine("general/test.json");

		try {
			Counters counters = new Counters();
			calculatorFacade.configureCounter(counters);

			JsonPathCache cache = new JsonPathCache(jsonRecord);

			for (Calculator calculator : calculatorFacade.getCalculators()) {
				calculator.calculate(cache, counters);
			}

			// return the result of calculations
			String csv = counters.getFullResults(false, true);
			assertEquals(expected, csv);
			// store csv to somewhere
		} catch (InvalidJsonException e) {
			// log problem
		}
	}

	@Test
	public void testNoAbbreviate() throws URISyntaxException, IOException {
		CalculatorFacade calculatorFacade = new CalculatorFacade(true, true, true, false, true);
		calculatorFacade.doAbbreviate(false);
		String expected = "92062_Ag_EU_TEL_a0480_Austria,Österreichische Nationalbibliothek - Austrian National Library,92062/BibliographicResource_1000126015451,0.4,1.0,0.181818,0.388889,0.272727,0.5,0.357143,0.75,0.363636,0.4,1,1,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,1,1,1,1,1,0,1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,1,1,1,1,1,0,0.0,0.0,0.0";
		run(calculatorFacade, expected);
	}

	@Test
	public void testWithAbbreviate() throws URISyntaxException, IOException {
		CalculatorFacade calculatorFacade = new CalculatorFacade(true, true, true, false, true);
		calculatorFacade.doAbbreviate(true);
		String expected = "1,2,92062/BibliographicResource_1000126015451,0.4,1.0,0.181818,0.388889,0.272727,0.5,0.357143,0.75,0.363636,0.4,1,1,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,1,1,1,1,1,0,1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,1,1,1,1,1,0,0.0,0.0,0.0";
		run(calculatorFacade, expected);
	}
}
