package de.gwdg.europeanaqa.api.model;

import de.gwdg.metadataqa.api.counter.FieldCounter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmSaturationMap {

  private List<EdmSaturationPropertyContainer> properties;
  private Map<String, Integer> index;
  private int taggedLiteralsInProviderProxy;
  private int taggedLiteralsInEuropeanaProxy;
  private int taggedPropertiesInProviderProxy;
  private int taggedPropertiesInEuropeanaProxy;
  private int taggedLiteralsInObject;
  private int taggedPropertiesInObject;
  private int languagesInProviderProxy;
  private int languagesInEuropeanaProxy;
  private Set<String> distinctLanguagesInProviderProxy;
  private Set<String> distinctLanguagesInEuropeanaProxy;
  private Set<String> distinctLanguagesInObject;

  /**
   * Contructs an EdmSaturationMap object.
   */
  public EdmSaturationMap() {
    properties = new ArrayList<>();
    index = new HashMap<>();
  }

  /**
   * Calculate scores.
   */
  public final void calculate() {
    taggedPropertiesInProviderProxy = 0;
    taggedPropertiesInEuropeanaProxy = 0;
    taggedLiteralsInProviderProxy = 0;
    taggedLiteralsInEuropeanaProxy = 0;
    languagesInProviderProxy = 0;
    languagesInEuropeanaProxy = 0;
    distinctLanguagesInProviderProxy = new HashSet<>();
    distinctLanguagesInEuropeanaProxy = new HashSet<>();
    EdmSaturationProperty property;
    for (EdmSaturationPropertyContainer container : properties) {
      property = container.getProviderProxy();
      if (property.getType().isTaggedLiteral()) {
        taggedPropertiesInProviderProxy++;
        taggedLiteralsInProviderProxy += property.getTaggedLiteralsCount();
        languagesInProviderProxy += property.getDistinctLanguages().size();
        distinctLanguagesInProviderProxy.addAll(property.getDistinctLanguages());
      }

      property = container.getEuropeanaProxy();
      if (property.getType().isTaggedLiteral()) {
        taggedPropertiesInEuropeanaProxy++;
        taggedLiteralsInEuropeanaProxy += property.getTaggedLiteralsCount();
        languagesInEuropeanaProxy += property.getDistinctLanguages().size();
        distinctLanguagesInEuropeanaProxy.addAll(property.getDistinctLanguages());
      }
    }
    taggedLiteralsInObject =
      taggedLiteralsInProviderProxy + taggedLiteralsInEuropeanaProxy;
    taggedPropertiesInObject =
      taggedPropertiesInProviderProxy + taggedPropertiesInEuropeanaProxy;

    distinctLanguagesInObject = new HashSet<>();
    distinctLanguagesInObject.addAll(distinctLanguagesInEuropeanaProxy);
    distinctLanguagesInObject.addAll(distinctLanguagesInProviderProxy);
  }

  /**
   * Gets number of distinct languages in an EDM object.
   * @return number of distinct languages
   */
  public int getDistinctLanguagesInObject() {
    return distinctLanguagesInObject.size();
  }

  /**
   * Gets the number of languages per property by a proxy.
   * @param proxyType The ID of the proxy
   * @return number of languages per property
   */
  public double getNumberOfLanguagesPerPropertyPerProxy(ProxyType proxyType) {
    if (proxyType.equals(ProxyType.PROVIDER)) {
      return (double) languagesInProviderProxy
        / (double) taggedPropertiesInProviderProxy;
    }
    return (double) languagesInEuropeanaProxy
      / (double) taggedPropertiesInEuropeanaProxy;
  }

  private double getNumberOfLanguagesPerPropertyInProviderProxy() {
    if (taggedPropertiesInProviderProxy == 0) {
      return 0.0;
    }
    return (double) languagesInProviderProxy
           / (double) taggedPropertiesInProviderProxy;
  }

  private double getNumberOfLanguagesPerPropertyInEuropeanaProxy() {
    if (taggedPropertiesInEuropeanaProxy == 0) {
      return 0.0;
    }
    return (double) languagesInEuropeanaProxy
           / (double) taggedPropertiesInEuropeanaProxy;
  }

  /**
   * Gets the number of languages per property in the object.
   * @return number of languages per property
   */
  public double getNumberOfLanguagesPerPropertyInObject() {
    int taggedPropertiesCount =
      taggedPropertiesInProviderProxy + taggedPropertiesInEuropeanaProxy;
    if (taggedPropertiesCount == 0) {
      return 0.0;
    }
    int languageCount = languagesInProviderProxy + languagesInEuropeanaProxy;
    return (double) languageCount / (double) taggedPropertiesCount;
  }

  /**
   * Gets a property.
   * @param fieldName The field name
   * @param proxyType The provider
   * @return The property
   */
  public EdmSaturationProperty createOrGetProperty(String fieldName,
                                                   ProxyType proxyType) {
    EdmSaturationPropertyContainer container;
    if (index.containsKey(fieldName)) {
      container = properties.get(index.get(fieldName));
    } else {
      container = new EdmSaturationPropertyContainer(fieldName);
      properties.add(container);
      index.put(fieldName, properties.size() - 1);
    }
    if (proxyType == ProxyType.PROVIDER) {
      return container.getProviderProxy();
    } else {
      return container.getEuropeanaProxy();
    }
  }

  public List<EdmSaturationPropertyContainer> getProperties() {
    return properties;
  }

  public int getTaggedLiteralsInProviderProxy() {
    return taggedLiteralsInProviderProxy;
  }

  public int getTaggedLiteralsInEuropeanaProxy() {
    return taggedLiteralsInEuropeanaProxy;
  }

  public int getTaggedLiteralsInObject() {
    return taggedLiteralsInObject;
  }

  public int getLanguagesInProviderProxy() {
    return languagesInProviderProxy;
  }

  public int getLanguagesInEuropeanaProxy() {
    return languagesInEuropeanaProxy;
  }

  public Set<String> getDistinctLanguagesInProviderProxy() {
    return distinctLanguagesInProviderProxy;
  }

  public int getDistinctLanguageCountInProviderProxy() {
    return distinctLanguagesInProviderProxy.size();
  }

  public Set<String> getDistinctLanguagesInEuropeanaProxy() {
    return distinctLanguagesInEuropeanaProxy;
  }

  public int getDistinctLanguageCountInEuropeanaProxy() {
    return distinctLanguagesInEuropeanaProxy.size();
  }

  public Set<String> getDistinctLanguageSetInObject() {
    return distinctLanguagesInObject;
  }

  /**
   * Gets the CSV value container.
   * @return The CSV value container
   */
  public FieldCounter<Double> getCsv() {
    calculate();
    FieldCounter<Double> saturationResult = new FieldCounter<>();
    saturationResult.putAll(getPropertiesCsv());
    saturationResult.putAll(getGeneralCsv());
    return saturationResult;
  }

  /**
   * Gets the headers.
   * @param properties The list of properties.
   * @return The headers
   */
  public static List<String> getHeader(List<String> properties) {
    List<String> headers = new ArrayList<>();
    for (String property : properties) {
      headers.addAll(EdmSaturationProperty.getHeaders("provider" + property));
      headers.addAll(EdmSaturationProperty.getHeaders("europeana" + property));
    }
    headers.addAll(getGeneralHeaders());
    return headers;
  }

  private FieldCounter<Double> getPropertiesCsv() {
    FieldCounter<Double> saturationResult = new FieldCounter<>();
    EdmSaturationProperty property;
    for (EdmSaturationPropertyContainer container : properties) {
      property = container.getProviderProxy();
      saturationResult.putAll(
        property.getCsv("provider" + container.getPropertyName()));

      property = container.getEuropeanaProxy();
      saturationResult.putAll(
        property.getCsv("europeana" + container.getPropertyName()));
    }
    return saturationResult;
  }

  private FieldCounter<Double> getGeneralCsv() {
    FieldCounter<Double> saturationResult = new FieldCounter<>();
    saturationResult.put("NumberOfLanguagesPerPropertyInProviderProxy",
      (double) getNumberOfLanguagesPerPropertyInProviderProxy());
    saturationResult.put("NumberOfLanguagesPerPropertyInEuropeanaProxy",
      (double) getNumberOfLanguagesPerPropertyInEuropeanaProxy());
    saturationResult.put("NumberOfLanguagesPerPropertyInObject",
      (double) getNumberOfLanguagesPerPropertyInObject());
    saturationResult.put("TaggedLiteralsInProviderProxy",
      (double) getTaggedLiteralsInProviderProxy());
    saturationResult.put("TaggedLiteralsInEuropeanaProxy",
      (double) getTaggedLiteralsInEuropeanaProxy());
    saturationResult.put("DistinctLanguageCountInProviderProxy",
      (double) getDistinctLanguageCountInProviderProxy());
    saturationResult.put("DistinctLanguageCountInEuropeanaProxy",
      (double) getDistinctLanguageCountInEuropeanaProxy());
    saturationResult.put("TaggedLiteralsInObject",
      (double) getTaggedLiteralsInObject());
    saturationResult.put("DistinctLanguagesInObject",
      (double) getDistinctLanguagesInObject());
    saturationResult.put("TaggedLiteralsPerLanguageInProviderProxy",
      (double) getTaggedLiteralsPerLanguageInProviderProxy());
    saturationResult.put("TaggedLiteralsPerLanguageInEuropeanaProxy",
      (double) getTaggedLiteralsPerLanguageInEuropeanaProxy());
    saturationResult.put("TaggedLiteralsPerLanguageInObject",
      (double) getTaggedLiteralsPerLanguageInObject());
    return saturationResult;
  }

  private static List<String> getGeneralHeaders() {
    return Arrays.asList(
      "NumberOfLanguagesPerPropertyInProviderProxy",
      "NumberOfLanguagesPerPropertyInEuropeanaProxy",
      "NumberOfLanguagesPerPropertyInObject",
      "TaggedLiteralsInProviderProxy",
      "TaggedLiteralsInEuropeanaProxy",
      "DistinctLanguageCountInProviderProxy",
      "DistinctLanguageCountInEuropeanaProxy",
      "TaggedLiteralsInObject",
      "DistinctLanguagesInObject",
      "TaggedLiteralsPerLanguageInProviderProxy",
      "TaggedLiteralsPerLanguageInEuropeanaProxy",
      "TaggedLiteralsPerLanguageInObject"
    );
  }

  @Override
  public String toString() {
    calculate();
    return "EdmSaturationMap{"
      + "properties=" + properties
      + ", numberOfLanguagesPerPropertyInProviderProxy="
        + getNumberOfLanguagesPerPropertyInProviderProxy()
      + ", numberOfLanguagesPerPropertyInEuropeanaProxy="
        + getNumberOfLanguagesPerPropertyInEuropeanaProxy()
      + ", numberOfLanguagesPerPropertyInObject="
        + getNumberOfLanguagesPerPropertyInObject()
      + ", taggedLiteralsInProviderProxy=" + getTaggedLiteralsInProviderProxy()
      + ", taggedLiteralsInEuropeanaProxy=" + getTaggedLiteralsInEuropeanaProxy()
      + ", languagesInProviderProxy=" + getDistinctLanguageCountInProviderProxy()
      + ", languagesInEuropeanaProxy=" + getDistinctLanguageCountInEuropeanaProxy()
      + ", taggedLiteralsInObject=" + getTaggedLiteralsInObject()
      + ", languagesInObject=" + getDistinctLanguagesInObject()
      + ", taggedLiteralsPerLanguageInProviderProxy="
          + getTaggedLiteralsPerLanguageInProviderProxy()
      + ", taggedLiteralsPerLanguageInEuropeanaProxy="
          + getTaggedLiteralsPerLanguageInEuropeanaProxy()
      + ", taggedLiteralsPerLanguageInObject="
          + getTaggedLiteralsPerLanguageInObject()
      + '}';
  }

  /**
   * Gets number of tagged literals per langauge in the Provider proxy.
   * @return number of tagged literals per langauge.
   */
  public double getTaggedLiteralsPerLanguageInProviderProxy() {
    if (getDistinctLanguageCountInProviderProxy() == 0) {
      return 0.0;
    }
    return (double) taggedLiteralsInProviderProxy
            / getDistinctLanguageCountInProviderProxy();
  }

  /**
   * Gets number of tagged literals per langauge in the Europeana proxy.
   * @return number of tagged literals per langauge.
   */
  public double getTaggedLiteralsPerLanguageInEuropeanaProxy() {
    if (getDistinctLanguageCountInEuropeanaProxy() == 0) {
      return 0.0;
    }
    return (double) taggedLiteralsInEuropeanaProxy
            / getDistinctLanguageCountInEuropeanaProxy();
  }

  /**
   * Gets number of tagged literals per langauge in the whole object.
   * @return number of tagged literals per langauge.
   */
  public double getTaggedLiteralsPerLanguageInObject() {
    if (getDistinctLanguagesInObject() == 0) {
      return 0.0;
    }
    return (double) taggedLiteralsInObject / getDistinctLanguagesInObject();
  }
}
