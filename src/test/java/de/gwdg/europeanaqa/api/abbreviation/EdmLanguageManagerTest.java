package de.gwdg.europeanaqa.api.abbreviation;

import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmLanguageManagerTest {

	public EdmLanguageManagerTest() {
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
		EdmLanguageManager manager = new EdmLanguageManager();

		assertEquals(1, manager.getData().keySet().size());
	}

	@Test
	public void testGetData() {
		EdmLanguageManager manager = new EdmLanguageManager();

		assertTrue(manager.getData().containsKey("de"));
		assertEquals(1, (int) manager.getData().get("de"));

		String name = manager.searchById(1);
		String expected = "de";
		assertEquals(expected, name);
	}

	@Test
	public void testGetData2() {
		EdmLanguageManager manager = new EdmLanguageManager();
		assertEquals(1, (int) manager.getData().get("de"));
	}

	@Test
	public void testLookup() {
		EdmLanguageManager manager = new EdmLanguageManager();

		assertEquals(1, (int) manager.lookup("de"));
	}
}
