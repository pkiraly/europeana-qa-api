package de.gwdg.europeanaqa.api.abbreviation;

import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;

import java.util.Map;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmLanguageManager extends AbbreviationManager {

	public EdmLanguageManager() {
		super();
		initialize("abbreviations/languages-v1.txt", true);
	}

	public Map<String, Integer> getData() {
		return data;
	}
}
