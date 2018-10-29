package de.gwdg.europeanaqa.api.abbreviation;

import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmCountryManagerTest {

	public EdmCountryManagerTest() {
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
	public void testSize() {
		EdmCountryManager manager = new EdmCountryManager();

		assertEquals(1, manager.getData().keySet().size());
	}

	@Test
	public void testGetData() {
		EdmCountryManager manager = new EdmCountryManager();

		assertTrue(manager.getData().containsKey("Austria"));
		assertEquals(1, (int) manager.getData().get("Austria"));

		String name = manager.searchById(1);
		String expected = "Austria";
		assertEquals(expected, name);
	}

	@Test
	public void testGetData2() {
		EdmCountryManager manager = new EdmCountryManager();
		assertEquals(1, (int) manager.getData().get("Austria"));
	}

	@Test
	public void testLookup() {
		EdmCountryManager manager = new EdmCountryManager();

		assertEquals(1, (int) manager.lookup("Austria"));
	}
}
