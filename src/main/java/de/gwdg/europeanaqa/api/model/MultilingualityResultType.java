package de.gwdg.europeanaqa.api.model;

/**
 * The result's types.
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public enum MultilingualityResultType {

  /**
   * Normal type.
   */
  NORMAL(0),

  /**
   * Extended type containing average and normalized scores.
   */
  EXTENDED(1);

  private final int value;

  MultilingualityResultType(int value) {
    this.value = value;
  }

  /**
   * Gets the value.
   * @return The value.
   */
  public int value() {
    return value;
  }
}
