package com.nsdr.europeanaqa.api.abbreviation;

import com.nsdr.metadataqa.api.abbreviation.AbbreviationManager;
import java.util.Map;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmDataProviderManager extends AbbreviationManager {

	public EdmDataProviderManager() {
		super();
		initialize("abbreviations/data-providers2.txt", true);
	}

	public Map<String, Integer> getDataProviders() {
		return data;
	}

}
