package de.gwdg.europeanaqa.api.abbreviation;

import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;
import java.util.Map;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmDataProviderManager extends AbbreviationManager {

	public EdmDataProviderManager() {
		super();
		initialize("abbreviations/data-providers-v2.txt", true);
	}

	public Map<String, Integer> getDataProviders() {
		return data;
	}
}
