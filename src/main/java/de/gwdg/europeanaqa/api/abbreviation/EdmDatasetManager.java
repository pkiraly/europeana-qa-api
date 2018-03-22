package de.gwdg.europeanaqa.api.abbreviation;

import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;
import java.util.Map;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmDatasetManager extends AbbreviationManager {

	public EdmDatasetManager() {
		super();
		initialize("abbreviations/datasets-v2.txt", true);
	}

	public Map<String, Integer> getDatasets() {
		return data;
	}
}
