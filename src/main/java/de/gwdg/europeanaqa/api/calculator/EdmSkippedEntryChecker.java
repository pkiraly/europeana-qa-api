package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.metadataqa.api.calculator.SkippedEntryChecker;
import de.gwdg.metadataqa.api.calculator.edm.EnhancementIdExtractor;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.pathcache.PathCache;
import de.gwdg.metadataqa.api.model.XmlFieldInstance;
import de.gwdg.metadataqa.api.schema.Schema;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmSkippedEntryChecker implements SkippedEntryChecker, Serializable {

  private Schema schema;

  public EdmSkippedEntryChecker(Schema schema) {
    this.schema = schema;
  }

  @Override
  public List<String> getSkippableCollectionIds(PathCache pathCache) {
    return EnhancementIdExtractor.extractIds(pathCache, schema);
  }

  @Override
  public <T extends XmlFieldInstance> String extractId(T value) {
    if (value instanceof EdmFieldInstance
      && ((EdmFieldInstance) value).hasResource()) {
      return ((EdmFieldInstance) value).getResource();
    }
    return value.getValue();
  }
}
