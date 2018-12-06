package de.gwdg.europeanaqa.api.model;

import de.gwdg.metadataqa.api.counter.FieldCounter;
import de.gwdg.metadataqa.api.model.LanguageSaturationType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Container of EDM multilingual saturation properties.
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmSaturationProperty {

  private int taggedLiteralsCount = 0;
  private LanguageSaturationType type;
  private Set<String> distinctLanguages;
  private int distinctLanguageCount = 0;

  /**
   * Gets the number of literals per language.
   * @return number of literals per language
   */
  public final double getNumberOfLiteralsPerLanguage() {
    if (distinctLanguageCount == 0) {
      return 0.0;
    }
    return (double) taggedLiteralsCount / (double) distinctLanguageCount;
  }

  /**
   * Gets the number of tagged literals.
   * @return number of tagged literals
   */
  public final int getTaggedLiteralsCount() {
    return taggedLiteralsCount;
  }

  /**
   * Sets the number of tagged literals.
   * @param pTaggedLiteralsCount number of tagged literals
   */
  public final void setTaggedLiteralsCount(final int pTaggedLiteralsCount) {
    this.taggedLiteralsCount = pTaggedLiteralsCount;
  }

  /**
   * Gets the language saturation type.
   * @return language saturation type
   */
  public final LanguageSaturationType getType() {
    return type;
  }

  /**
   * Sets the language saturation type.
   * @param pType language saturation type
   */
  public final void setType(final LanguageSaturationType pType) {
    this.type = pType;
  }

  /**
   * Gets the distinct languages.
   * @return Set of distinct languages
   */
  public final Set<String> getDistinctLanguages() {
    return distinctLanguages;
  }

  /**
   * Sets the distinct languages.
   * @param pDistinctLanguages distinct languages
   */
  public final void setDistinctLanguages(final Set<String> pDistinctLanguages) {
    this.distinctLanguages = pDistinctLanguages;
    distinctLanguageCount = distinctLanguages.size();
  }

  /**
   * Get the multilinguality values for a property.
   * @param propertyName The property name.
   * @return multilinguality values.
   */
  public final FieldCounter<Double> getCsv(final String propertyName) {
    FieldCounter<Double> values = new FieldCounter<>();
    if (getType().isTaggedLiteral()) {
      values.put(
        propertyName + "/taggedLiterals",
        (double) getTaggedLiteralsCount()
      );
      values.put(
        propertyName + "/languages",
        (double) getDistinctLanguages().size()
      );
      values.put(
        propertyName + "/literalsPerLanguage",
        (double) getNumberOfLiteralsPerLanguage()
      );
    } else {
      values.put(propertyName + "/taggedLiterals", (double) getType().value());
      values.put(propertyName + "/languages", 0.0);
      values.put(propertyName + "/literalsPerLanguage", 0.0);
    }
    return values;
  }

  static List<String> getHeaders(final String propertyName) {
    return Arrays.asList(
      propertyName + "/taggedLiterals",
      propertyName + "/languages",
      propertyName + "/literalsPerLanguage"
    );
  }

  @Override
  public final String toString() {
    return "{"
            + "taggedLiteralsCount=" + taggedLiteralsCount
            + ", type=" + type
            + ", distinctLanguages=" + distinctLanguages
            + ", distinctLanguageCount=" + distinctLanguageCount
            + ", literalsPerLanguage=" + getNumberOfLiteralsPerLanguage()
            + '}';
  }

  /**
   * Sets language saturation type and tagged literals count.
   * @param pType language saturation type
   * @param pTaggedLiteralsCount tagged literals count
   */
  public final void setTypedCount(final LanguageSaturationType pType,
                        final int pTaggedLiteralsCount) {
    setType(pType);
    if (pType.equals(LanguageSaturationType.NA)) {
      this.taggedLiteralsCount = 0;
    } else {
      this.taggedLiteralsCount = pTaggedLiteralsCount;
    }
  }
}
