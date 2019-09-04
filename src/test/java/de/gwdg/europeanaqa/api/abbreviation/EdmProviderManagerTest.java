package de.gwdg.europeanaqa.api.abbreviation;

import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmProviderManagerTest {

  private EdmProviderManager manager;

  public EdmProviderManagerTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    manager = new EdmProviderManager();
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testSize() {
    assertEquals(242, manager.getData().keySet().size());
  }

  @Test
  public void testGetData() {
    assertTrue(manager.getData().containsKey("3D ICONS"));
    assertEquals(2, (int) manager.getData().get("3D ICONS"));

    assertEquals("3D-COFORM consortium", manager.searchById(1));
    assertEquals("3D ICONS", manager.searchById(2));
  }

  @Test
  public void testGetData2() {
    assertEquals(2, (int) manager.getData().get("3D ICONS"));
  }

  @Test
  public void testLookup() {
    assertEquals(2, (int) manager.lookup("3D ICONS"));
  }
}
