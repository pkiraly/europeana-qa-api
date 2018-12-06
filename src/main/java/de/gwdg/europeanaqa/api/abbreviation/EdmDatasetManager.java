package de.gwdg.europeanaqa.api.abbreviation;

import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmDatasetManager extends AbbreviationManager {

  /**
   * Constructor.
   */
  public EdmDatasetManager() {
    super();
    initialize("abbreviations/datasets-v3.txt", true);
  }
}
