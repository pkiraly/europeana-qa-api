package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.europeanaqa.api.model.EdmStructure;
import de.gwdg.europeanaqa.api.model.EdmStructureBuilder;
import de.gwdg.europeanaqa.api.model.EntityAddressPatternBuilder;
import de.gwdg.europeanaqa.api.model.EntityType;
import de.gwdg.europeanaqa.api.model.Proxies;
import de.gwdg.europeanaqa.api.model.ProxyLink;
import de.gwdg.europeanaqa.api.model.ProxyType;
import de.gwdg.metadataqa.api.counter.FieldCounter;
import de.gwdg.metadataqa.api.interfaces.Calculator;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;
import de.gwdg.metadataqa.api.util.Converter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Proxy based iterator.
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class ProxyBasedCompletenessCalculator implements Calculator, Serializable {

  private static final Logger LOGGER = Logger.getLogger(
    ProxyBasedCompletenessCalculator.class.getCanonicalName()
  );

  /**
   * Name of the calculator.
   */
  public static final String CALCULATOR_NAME = "proxyBasedCompletenessCalculator";

  private final Schema schema;
  private final Proxies proxies;
  private final EntityAddressPatternBuilder entityAddressPatternBuilder;
  private FieldCounter<Integer> cardinalityCounter;

  /**
   * Constructs a new ProxyBasedIterator.
   *
   * @param schema The metadata schema.
   */
  public ProxyBasedCompletenessCalculator(Schema schema) {
    this.schema = schema;
    proxies = new Proxies(schema);
    entityAddressPatternBuilder = new EntityAddressPatternBuilder(schema);
  }

  public Proxies getProxies() {
    return proxies;
  }

  /**
   * Measure the object.
   * TODO this is a not finished function.
   * @param cache The cache object.
   */
  @Override
  public void measure(JsonPathCache cache) {
    cardinalityCounter = new FieldCounter<>();
    EdmStructureBuilder edmStructureBuilder = new EdmStructureBuilder(schema, proxies);

    EdmStructure edmStructure = edmStructureBuilder.build(cache);
    iterateOverProxyAndLinks(cache, edmStructure, ProxyType.PROVIDER);
    iterateOverProxyAndLinks(cache, edmStructure, ProxyType.EUROPEANA);
  }

  private void iterateOverProxyAndLinks(JsonPathCache cache, EdmStructure edmStructure, ProxyType proxyType) {
    iterateOverProxy(cache, proxyType);
    iterateOverLinkedContextualEntities(cache, edmStructure, proxyType);
  }

  private void iterateOverProxy(JsonPathCache cache, ProxyType proxyType) {
    JsonBranch proxy = proxies.getByType(proxyType);
    Object rawJsonFragment = cache.getFragment(proxy.getJsonPath());
    List<Object> entityFragments = Converter.jsonObjectToList(rawJsonFragment);
    if (!entityFragments.isEmpty()) {
      Object entityFragment = entityFragments.get(0);
      for (JsonBranch entityElement : proxy.getChildren()) {
        computeCardinality(cache, proxyType, 0, entityFragment, entityElement);
      }
    }
  }

  private void iterateOverLinkedContextualEntities(JsonPathCache cache, EdmStructure edmStructure, ProxyType proxyType) {
    List<ProxyLink> proxyLinks = edmStructure.getProxyLinks(proxyType);
    Map<EntityType, List<ProxyLink>> orderedProxyLinks = orderLinks(proxyLinks);
    int i = 0;
    for (EntityType entityType : EntityType.values()) {
      JsonBranch entitySchema = schema.getPathByLabel(entityType.getName());
      List<ProxyLink> entityLinks = orderedProxyLinks.get(entityType);
      if (entityLinks.isEmpty()) {
        for (JsonBranch entityElement : entitySchema.getChildren()) {
          cardinalityCounter.put(getKey(proxyType, entityElement), 0);
        }
      } else {
        for (ProxyLink proxyLink : entityLinks) {
          String link = proxyLink.getLink();
          Object rawJsonFragment = cache.getFragment(getPath(entityType, link));
          List<Object> entityFragments = Converter.jsonObjectToList(rawJsonFragment);
          if (entityFragments.isEmpty()) {
            continue;
          }
          for (Object entityFragment : entityFragments) {
            for (JsonBranch entityElement : entitySchema.getChildren()) {
              computeCardinality(cache, proxyType, i, entityFragment, entityElement);
            }
            i++;
          }
        }
      }
    }
  }

  private void computeCardinality(JsonPathCache cache,
                                  ProxyType proxyType,
                                  int i,
                                  Object entityFragment,
                                  JsonBranch entityElement) {
    List<Object> values = cache.get(
      entityElement.getAbsoluteJsonPath(i),
      entityElement.getJsonPath(),
      entityFragment
    );
    int cardinality = 0;
    if (values != null) {
      cardinality = values.size();
    }
    String key = getKey(proxyType, entityElement);
    if (cardinalityCounter.has(key)) {
      cardinality += cardinalityCounter.get(key);
    }
    cardinalityCounter.put(key, cardinality);
  }

  private String getKey(ProxyType proxyType, JsonBranch entityElement) {
    return proxyType.name() + ":" + entityElement.getLabel();
  }

  private Map<EntityType, List<ProxyLink>> orderLinks(List<ProxyLink> proxyLinks) {
    Map<EntityType, List<ProxyLink>> orderedLinks = new HashMap<>();
    for (EntityType entityType : EntityType.values()) {
      orderedLinks.put(entityType, new ArrayList<>());
    }
    for (ProxyLink proxyLink : proxyLinks) {
      orderedLinks.get(proxyLink.getTarget()).add(proxyLink);
    }
    return orderedLinks;
  }

  private String getPath(EntityType entityType, String link) {
    String pattern = entityAddressPatternBuilder.getOrCreate(entityType);
    String jsonPath = String.format(pattern, link.replace("'", "\\'"));
    return jsonPath;
  }

  @Override
  public Map<String, ? extends Object> getResultMap() {
    return cardinalityCounter.getMap();
  }

  @Override
  public Map<String, Map<String, ? extends Object>> getLabelledResultMap() {
    Map<String, Map<String, ? extends Object>> labelledResultMap = new LinkedHashMap<>();
    labelledResultMap.put(getCalculatorName(), cardinalityCounter.getMap());
    return labelledResultMap;
  }

  @Override
  public String getCsv(boolean withLabels, CompressionLevel compressionLevel) {
    return cardinalityCounter.getList(withLabels, compressionLevel);
  }

  @Override
  public List<String> getHeader() {
    List<String> header = new ArrayList<>();
    for (ProxyType proxyType : ProxyType.values()) {
      JsonBranch proxy = proxies.getByType(proxyType);
      for (JsonBranch child : proxy.getChildren()) {
        header.add(getKey(proxyType, child));
      }
      for (EntityType entityType : EntityType.values()) {
        JsonBranch entitySchema = schema.getPathByLabel(entityType.getName());
        for (JsonBranch child : entitySchema.getChildren()) {
          header.add(getKey(proxyType, child));
        }
      }
    }
    return header;
  }

  @Override
  public String getCalculatorName() {
    return CALCULATOR_NAME;
  }

}
