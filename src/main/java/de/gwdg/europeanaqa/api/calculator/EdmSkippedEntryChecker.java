package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.metadataqa.api.calculator.SkippedEntryChecker;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.model.XmlFieldInstance;
import java.util.List;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmSkippedEntryChecker implements SkippedEntryChecker {

	@Override
	public List<String> getSkippableCollectionIds(JsonPathCache jsonPathCache) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public <T extends XmlFieldInstance> String extractId(T value) {
		if (value instanceof EdmFieldInstance && ((EdmFieldInstance)value).hasResource())
			return ((EdmFieldInstance)value).getResource();
		return value.getValue();
	}
}
