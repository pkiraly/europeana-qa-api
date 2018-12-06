package de.gwdg.europeanaqa.api.model;

/**
 * Available input formats.
 */
public enum Format {
  /**
   * XML via OAI-PMH service.
   */
  OAI_PMH_XML("xml"),

  /**
   * FullBean JSON format via Record API and MongoDB export.
   */
  FULLBEAN("fullbean");

  private final String name;

  Format(String name) {
    this.name = name;
  }

  /**
   * Gets format by format code.
   * @param code Format code
   * @return The format
   */
  public static Format byCode(String code) {
    for (Format format : values()) {
      if (format.name.equals(code)) {
        return format;
      }
    }
    return null;
  }
}
