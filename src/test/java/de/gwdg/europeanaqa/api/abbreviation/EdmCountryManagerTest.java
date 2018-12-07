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
public class EdmCountryManagerTest {

  private EdmCountryManager manager;

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
    manager = new EdmCountryManager();
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testSize() {
    assertEquals(46, manager.getData().keySet().size());
  }

  @Test
  public void testGetData() {
    assertTrue(manager.getData().containsKey("austria"));
    assertEquals(2, (int) manager.getData().get("austria"));

    assertEquals("europe", manager.searchById(1));
    assertEquals("austria", manager.searchById(2));
  }

  @Test
  public void testGetData2() {
    assertEquals(2, (int) manager.getData().get("austria"));
  }

  @Test
  public void testLookup() {
    assertEquals(2, (int) manager.lookup("austria"));
  }
}
