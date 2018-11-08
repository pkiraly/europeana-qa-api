package de.gwdg.europeanaqa.api.abbreviation;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmLanguageManagerTest {

	private EdmLanguageManager manager;

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
		manager = new EdmLanguageManager();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testSize() {
		assertEquals(38, manager.getData().keySet().size());
	}

	@Test
	public void testGetData() {
		assertTrue(manager.getData().containsKey("de"));
		assertEquals(6, (int) manager.getData().get("de"));

		assertEquals("mul", manager.searchById(1));
		assertEquals("de", manager.searchById(6));
	}

	@Test
	public void testGetData2() {
		assertEquals(6, (int) manager.getData().get("de"));
	}

	@Test
	public void testLookup() {
		assertEquals(6, (int) manager.lookup("de"));
	}
}
