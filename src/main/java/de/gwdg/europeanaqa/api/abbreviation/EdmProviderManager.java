package de.gwdg.europeanaqa.api.abbreviation;

import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmProviderManager extends AbbreviationManager {

  /**
   * Constructor.
   */
  public EdmProviderManager() {
    super();
    initialize("abbreviations/providers-v1.csv", true);
  }
}
