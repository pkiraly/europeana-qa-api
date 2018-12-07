package de.gwdg.europeanaqa.api.model;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class EntityTypeTest {

  @Test
  public void generalTests() {
    assertEquals(4, EntityType.values().length);
    assertEquals(EntityType.AGENT, EntityType.valueOf("AGENT"));
    assertEquals(EntityType.AGENT, EntityType.valueOf("Agent".toUpperCase()));
    assertEquals("Agent", EntityType.AGENT.getName());
    assertEquals("Agent/rdf:about", EntityType.AGENT.getBranchId());
  }
}
