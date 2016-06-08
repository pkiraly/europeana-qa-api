package com.nsdr.europeanaqa.api.abbreviation;

import com.nsdr.metadataqa.api.abbreviation.DataProviderManager;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmDataProviderManager extends DataProviderManager {

	public EdmDataProviderManager() {
		super();
		initialize("abbreviations/data-providers2.txt");
	}
}
