package de.gwdg.europeanaqa.api.abbreviation;

import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmLanguageManager extends AbbreviationManager {

  /**
   * Constructor.
   */
  public EdmLanguageManager() {
    super();
    initialize("abbreviations/languages-v2.csv", true);
  }
}
