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
public class EdmDataProviderManagerTest {

  public EdmDataProviderManagerTest() {
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
    EdmDataProviderManager manager = new EdmDataProviderManager();
    assertEquals(5635, manager.getData().keySet().size());
  }

  @Test
  public void testGetData() {
    EdmDataProviderManager manager = new EdmDataProviderManager();

    assertTrue(manager.getData().containsKey("Preiser Records; Austria"));
    assertEquals(264, (int) manager.getData().get("Preiser Records; Austria"));

    assertTrue(manager.getData().containsKey("Pipeline Music"));
    assertEquals(1001, (int) manager.getData().get("Pipeline Music"));

    assertTrue(manager.getData().containsKey("Österreichische Nationalbibliothek - Austrian National Library"));
    assertEquals(2, (int) manager.getData().get("Österreichische Nationalbibliothek - Austrian National Library"));

    String name = manager.searchById(3716);
    String expected = "Mediterranean Archaeological Research Institute-Vrije Universiteit Brussel /\n          The Cyprus Institute - STARC";
    assertEquals(expected, name);
  }

  @Test
  public void testGetDatasets() {
    EdmDataProviderManager manager = new EdmDataProviderManager();
    assertEquals(264, (int) manager.getData().get("Preiser Records; Austria"));
    assertEquals(1001, (int) manager.getData().get("Pipeline Music"));
    assertEquals(2, (int) manager.getData().get("Österreichische Nationalbibliothek - Austrian National Library"));
  }

  @Test
  public void testLookup() {
    EdmDataProviderManager manager = new EdmDataProviderManager();

    assertEquals(264, (int) manager.lookup("Preiser Records; Austria"));
    assertEquals(1001, (int) manager.lookup("Pipeline Music"));
    assertEquals(2, (int) manager.lookup("Österreichische Nationalbibliothek - Austrian National Library"));
  }
}
