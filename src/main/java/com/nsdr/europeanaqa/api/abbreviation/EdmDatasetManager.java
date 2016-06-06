package com.nsdr.europeanaqa.api.abbreviation;

import com.nsdr.metadataqa.api.abbreviation.DatasetManager;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmDatasetManager extends DatasetManager {

	public EdmDatasetManager() {
		super();
		initialize("abbreviations/datasets.txt");
	}
}
