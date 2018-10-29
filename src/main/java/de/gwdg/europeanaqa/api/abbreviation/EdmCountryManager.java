package de.gwdg.europeanaqa.api.abbreviation;

import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;

import java.util.Map;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmCountryManager extends AbbreviationManager {

	public EdmCountryManager() {
		super();
		initialize("abbreviations/countries-v1.csv", true);
	}

	public Map<String, Integer> getData() {
		return data;
	}
}
