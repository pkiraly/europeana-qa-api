package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.europeanaqa.api.model.*;
import de.gwdg.metadataqa.api.counter.FieldCounter;
import de.gwdg.metadataqa.api.interfaces.Calculator;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.pathcache.PathCache;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;
import de.gwdg.metadataqa.api.util.Converter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class DisconnectedEntityCalculator implements Calculator, Serializable {

  /**
   * Logger.
   */
  private static final Logger LOGGER =
    Logger.getLogger(
      DisconnectedEntityCalculator.class.getCanonicalName());

  /**
   * The name of this calculator.
   */
  public static final String CALCULATOR_NAME = "disconnectedEntityCalculator";

    /**
   * List of headers.
   */
  private List<String> headers = Arrays.asList(
    "orphanedEntities",
    "selfLinkedEntities",
    "brokenProviderLinks",
    "brokenEuropeanaLinks",
    "contextualEntityCount",
    "providerProxyLinksCount",
    "europeanaProxyLinksCount",
    "contextualLinksCount"
  );

  /**
   * List of technical fields.
   */
  private static final List<String> TECHNICAL_FIELDS = Arrays.asList(
    "Proxy/ore:proxyIn", "Proxy/ore:proxyFor", "Proxy/rdf:about"
  );

  /**
   * The schema.
   */
  private Schema schema;
  private Proxies proxies;

  private EdmStructure edmStructure;
  private EdmStructureBuilder edmStructureBuilder;

  /**
   * Contructor.
   * @param schema Schema The schema to use by the functions.
   */
  public DisconnectedEntityCalculator(final Schema schema) {
    this.schema = schema;
    proxies = new Proxies(schema);
    edmStructureBuilder = new EdmStructureBuilder(schema, proxies);
  }

  /**
   * The result map.
   */
  private FieldCounter resultMap;

  @Override
  public void measure(final PathCache cache) {

    edmStructure = edmStructureBuilder.build(cache);

    List<ContextualId> orphanedEntities = edmStructure.getOrphanedEntities();
    if (!orphanedEntities.isEmpty()) {
      reportStrangeEntities(cache, orphanedEntities, "orphaned");
    }

    List<ContextualId> selfLinkedEntities = edmStructure.getSelfLinkedEntities();
    if (!selfLinkedEntities.isEmpty()) {
      reportStrangeEntities(cache, selfLinkedEntities, "self-linked");
    }

    resultMap = new FieldCounter<>();
    resultMap.put("orphanedEntities", orphanedEntities.size());
    resultMap.put("selfLinkedEntities", selfLinkedEntities.size());
    resultMap.put("brokenProviderLinks", edmStructure.getBrokenProviderProxyLinks().size());
    resultMap.put("brokenEuropeanaLinks", edmStructure.getBrokenEuropeanaProxyLinks().size());
    resultMap.put("contextualEntityCount", edmStructure.getContextualIds().size());
    resultMap.put("providerProxyLinksCount", edmStructure.getProviderProxyLinks().size());
    resultMap.put("europeanaProxyLinksCount", edmStructure.getEuropeanaProxyLinks().size());
    resultMap.put("contextualLinksCount", edmStructure.getInterLinkedEntities().size());
  }

  private void reportStrangeEntities(PathCache cache,
                                     List<ContextualId> contextualIds,
                                     String type) {
    List<String> entitiesList = new ArrayList<>();
    for (ContextualId contextualId : contextualIds) {
      String source = contextualId.getEntity().getName();
      if (StringUtils.isNotBlank(contextualId.getSourceField())) {
        source = contextualId.getSourceField();
      }
      entitiesList.add(String.format("%s (%s)", contextualId.getUri(), source));
    }
    String entities = StringUtils.join(entitiesList, ", ");
    LOGGER.warning(String.format("%s has %s entities: %s",
        getId(cache), type, entities));
  }

  /**
   * Get the contextual IDs from the cache.
   *
   * @param cache The cache object.
   * @return The map of contextual IDs, where the key are the IDs (URIs),
   *   values are entity types.
   */
  public Map<String, EntityType> getContextualIds(PathCache cache) {
    Map<String, EntityType> contextualIds = new HashMap<>();
    for (EntityType type : EntityType.values()) {
      JsonBranch branch = schema.getPathByLabel(type.getBranchId());
      Object rawJsonFragment = cache.getFragment(branch.getAbsoluteJsonPath(schema.getFormat()));
      List<Object> jsonFragments = Converter.jsonObjectToList(rawJsonFragment, schema);
      if (jsonFragments.isEmpty()) {
        continue;
      }
      for (Object jsonFragment : jsonFragments) {
        if (jsonFragment instanceof String) {
          contextualIds.put((String) jsonFragment, type);
        } else {
          LOGGER.info("jsonFragment is not String, but "
              + jsonFragment.getClass().getCanonicalName());
        }
      }
    }
    return contextualIds;
  }

  /**
   * Get the ID of the record.
   * @param cache The JSON path cache
   * @return The ID of the record
   */
  private String getId(PathCache cache) {
    String path = schema
      .getPathByLabel("ProvidedCHO/rdf:about")
      .getAbsoluteJsonPath(schema.getFormat())
      .replace("[*]", "");
    List<EdmFieldInstance> fieldInstances = cache.get(path);
    return fieldInstances
      .get(0)
      .getValue()
      .replace("http://data.europeana.eu/item/", "");
  }

  @Override
  public Map<String, ? extends Object> getResultMap() {
    return resultMap.getMap();
  }

  @Override
  public Map<String, Map<String, ? extends Object>> getLabelledResultMap() {
    Map<String, Map<String, ? extends Object>> labelledResultMap =
      new LinkedHashMap<>();
    labelledResultMap.put(getCalculatorName(), resultMap.getMap());
    return labelledResultMap;
  }

  @Override
  public String getCsv(final boolean withLabels,
                       final CompressionLevel compressionLevel) {
    return resultMap.getCsv(withLabels, compressionLevel);
  }

  @Override
  public List<String> getList(final boolean withLabels,
                       final CompressionLevel compressionLevel) {
    return resultMap.getList(withLabels, compressionLevel);
  }

  @Override
  public List<String> getHeader() {
    return headers;
  }

  @Override
  public String getCalculatorName() {
    return CALCULATOR_NAME;
  }

  public EdmStructure getEdmStructure() {
    return edmStructure;
  }
}
