package de.gwdg.europeanaqa.api.abbreviation;

import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmCountryManager extends AbbreviationManager {

  /**
   * Constructor.
   */
  public EdmCountryManager() {
    super();
    initialize("abbreviations/countries-v1.csv", true);
  }
}
