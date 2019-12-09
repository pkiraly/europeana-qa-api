package de.gwdg.europeanaqa.api.model;

import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.schema.Schema;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EntityAddressPatternBuilder implements Serializable {
  private Schema schema;
  private final static Map<EntityType, String> cache = new HashMap<>();

  public EntityAddressPatternBuilder(Schema schema) {
    this.schema = schema;
  }

  public String getOrCreate(EntityType entityType) {
    if (!cache.containsKey(entityType)) {
      cache.put(entityType, createPattern(entityType));
    }
    return cache.get(entityType);
  }

  private String createPattern(EntityType entityType) {
    String label = entityType.getBranchId();
    JsonBranch aboutPath = schema.getPathByLabel(label);
    String pattern = aboutPath.getAbsoluteJsonPath(schema.getFormat()).replace("[*]", "[?(@") + " == '%s')]";
    return pattern;
  }

}
