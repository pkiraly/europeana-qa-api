package de.gwdg.europeanaqa.api.abbreviation;

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
public class EdmDatasetManagerTest {

	public EdmDatasetManagerTest() {
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
		EdmDatasetManager manager = new EdmDatasetManager();
		assertEquals(2312, manager.getData().keySet().size());
	}

	@Test
	public void testGetData() {
		EdmDatasetManager manager = new EdmDatasetManager();

		assertTrue(manager.getData().containsKey("00101_M_PT_Gulbenkian_biblioteca_digital"));
		assertEquals(1, (int) manager.getData().get("00101_M_PT_Gulbenkian_biblioteca_digital"));

		assertTrue(manager.getData().containsKey("9200221_Ag_EU_TEL_a1122_Romania_Academy"));
		assertEquals(1501, (int) manager.getData().get("9200221_Ag_EU_TEL_a1122_Romania_Academy"));

		assertTrue(manager.getData().containsKey("92062_Ag_EU_TEL_a0480_Austria"));
		assertEquals(1725, (int) manager.getData().get("92062_Ag_EU_TEL_a0480_Austria"));
	}

	@Test
	public void testGetDatasets() {
		EdmDatasetManager manager = new EdmDatasetManager();
		assertEquals(1, (int) manager.getDatasets().get("00101_M_PT_Gulbenkian_biblioteca_digital"));
		assertEquals(1501, (int) manager.getDatasets().get("9200221_Ag_EU_TEL_a1122_Romania_Academy"));
		assertEquals(1725, (int) manager.getDatasets().get("92062_Ag_EU_TEL_a0480_Austria"));
	}

	@Test
	public void testLookup() {
		EdmDatasetManager manager = new EdmDatasetManager();
		assertEquals(1, (int) manager.lookup("00101_M_PT_Gulbenkian_biblioteca_digital"));
		assertEquals(1501, (int) manager.lookup("9200221_Ag_EU_TEL_a1122_Romania_Academy"));
		assertEquals(1725, (int) manager.lookup("92062_Ag_EU_TEL_a0480_Austria"));
	}
}
