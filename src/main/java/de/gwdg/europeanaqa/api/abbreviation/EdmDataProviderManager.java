package de.gwdg.europeanaqa.api.abbreviation;

import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmDataProviderManager extends AbbreviationManager {

	/**
	 * Constructor.
	 */
	public EdmDataProviderManager() {
		super();
		initialize("abbreviations/data-providers-v3.txt", true);
	}
}
