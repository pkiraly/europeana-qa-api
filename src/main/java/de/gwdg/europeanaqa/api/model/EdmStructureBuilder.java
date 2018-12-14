package de.gwdg.europeanaqa.api.model;

import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class EdmStructureBuilder {

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(
      EdmStructureBuilder.class.getCanonicalName()
  );

  /**
   * List of enrichable fields.
   */
  private static final List<String> ENRICHABLE_FIELDS = Arrays.asList(
      "Proxy/dc:contributor", "Proxy/dc:publisher",
      "Proxy/dc:creator", "Proxy/dc:subject", "Proxy/dc:type",
      "Proxy/edm:hasType", "Proxy/dc:coverage",
      "Proxy/dcterms:spatial", "Proxy/edm:currentLocation",
      "Proxy/dc:date", "Proxy/dcterms:created",
      "Proxy/dcterms:issued", "Proxy/dcterms:temporal",
      "Proxy/edm:hasMet", "Proxy/dc:format",
      "Proxy/dcterms:conformsTo", "Proxy/dcterms:medium",
      "Proxy/edm:isRelatedTo",
      "Proxy/edm:year"
  );

  private Schema schema;
  private Proxies proxies;

  public EdmStructureBuilder(Schema schema, Proxies proxies) {
    this.schema = schema;
    this.proxies = proxies;
  }

  public EdmStructure build(JsonPathCache cache) {
    EdmStructure edmStructure = new EdmStructure();
    extractProxyLinksAndValues(cache, edmStructure, ProxyType.PROVIDER);
    extractProxyLinksAndValues(cache, edmStructure, ProxyType.EUROPEANA);
    extractsContextualIds(cache, edmStructure);
    checkContextualIDsInProxies(edmStructure);
    checkContextualIDsInEntities(cache, edmStructure);
    return edmStructure;
  }

  private void extractProxyLinksAndValues(JsonPathCache cache,
                                          EdmStructure edmStructure,
                                          ProxyType type) {
    JsonBranch proxySchema = proxies.getByType(type);
    Object rawProxy = cache.getFragment(proxySchema.getJsonPath());
    Object jsonFragment = Converter.jsonObjectToList(rawProxy).get(0);
    for (JsonBranch child : proxySchema.getChildren()) {
      if (!isEnrichableField(child)) {
        continue;
      }
      String address = proxySchema.getJsonPath() + "/" + child.getJsonPath();
      List<EdmFieldInstance> fieldInstances = cache.get(address, child.getJsonPath(), jsonFragment);
      if (fieldInstances == null) {
        continue;
      }
      for (EdmFieldInstance fieldInstance : fieldInstances) {
        if (fieldInstance.isUrl()) {
          edmStructure.addProxyLink(type, fieldInstance.getUrl());
        } else if (fieldInstance.hasValue()) {
          edmStructure.addProxyValue(type, fieldInstance.getValue());
        }
      }
    }
  }

  private static boolean isEnrichableField(JsonBranch child) {
    return ENRICHABLE_FIELDS.contains(child.getLabel());
  }

  /**
   * Get the contextual IDs from the cache.
   *
   * @param cache The cache object.
   * @return The map of contextual IDs, where the key are the IDs (URIs),
   *   values are entity types.
   */
  public void extractsContextualIds(JsonPathCache cache,
                                    EdmStructure edmStructure) {
    for (EntityType type : EntityType.values()) {
      JsonBranch branch = schema.getPathByLabel(type.getBranchId());
      Object rawJsonFragment = cache.getFragment(branch.getAbsoluteJsonPath());
      List<Object> jsonFragments = Converter.jsonObjectToList(rawJsonFragment);
      if (jsonFragments.isEmpty()) {
        continue;
      }
      for (Object jsonFragment : jsonFragments) {
        if (jsonFragment instanceof String) {
          String url = (String) jsonFragment;
          edmStructure.addContextualId(url, type, LinkType.NONE);
        } else {
          LOGGER.info("jsonFragment is not String, but "
              + jsonFragment.getClass().getCanonicalName());
        }
      }
    }
  }

  private void checkContextualIDsInProxies(EdmStructure edmStructure) {
    for (String uri : edmStructure.getContextualIds().keySet()) {
      ContextualId id = edmStructure.getContextualIds().get(uri);
      if (edmStructure.containsProviderLink(uri)) {
        edmStructure.setProviderLinkTarget(uri, id.getEntity());
        id.setSource(LinkType.PROVIDER_PROXY);
      } else if (edmStructure.containsEuropeanaLink(uri)) {
        edmStructure.setEuropeanaLinkTarget(uri, id.getEntity());
        id.setSource(LinkType.EUROPEANA_PROXY);
      }
    }
  }

  /**
   * Check the the contextual IDs in the entities.
   * @param cache
   */
  private void checkContextualIDsInEntities(JsonPathCache cache,
                                            EdmStructure edmStructure) {
    if (edmStructure.getContextualIds().size() > 0) {
      for (String uri : edmStructure.getContextualIds().keySet()) {
        ContextualId id = edmStructure.getContextualIds().get(uri);
        if (id.getSource().equals(LinkType.NONE)) {
          checkInternalEntityLinks(cache, id);
        }
      }
    }
  }

  /**
   * Checks the internal proxy links.
   * @param cache The JSON cache.
   * @param contextualId The contextual id object.
   */
  private void checkInternalEntityLinks(JsonPathCache cache,
                                        ContextualId contextualId) {
    EntityType entityType = contextualId.getEntity();
    JsonBranch entityPath = schema.getPathByLabel(entityType.getName());
    Object rawJsonFragment = cache.getFragment(entityPath.getJsonPath());
    List<Object> jsonFragments = Converter.jsonObjectToList(rawJsonFragment);
    if (!jsonFragments.isEmpty()) {
      JsonBranch idPath = schema.getPathByLabel(entityType.getBranchId());
      int i = 0;
      for (Object jsonFragment : jsonFragments) {
        String addressPrefix = entityPath.getJsonPath() + "/" + i++;
        String address = addressPrefix + "/" + idPath.getJsonPath();
        EdmFieldInstance entityId = (EdmFieldInstance) cache.get(address, idPath.getJsonPath(), jsonFragment).get(0);
        for (String entityField : entityType.getLinkableFields()) {
          JsonBranch fieldPath = schema.getPathByLabel(entityField);
          address = addressPrefix + "/" + fieldPath.getJsonPath();
          List<EdmFieldInstance> fieldInstances = cache.get(address, fieldPath.getJsonPath(), jsonFragment);
          if (fieldInstances != null && !fieldInstances.isEmpty()) {
            for (EdmFieldInstance fieldInstance : fieldInstances) {
              if (fieldInstance.isUrl()) {
                if (fieldInstance.getUrl().equals(contextualId.getUri())) {
                  if (fieldInstance.getUrl().equals(entityId.getUrl())) {
                    contextualId.setSource(LinkType.SELF);
                    contextualId.setSourceField(entityField);
                  } else {
                    contextualId.setSource(LinkType.CONTEXTUAL_ENTITY);
                  }
                  break;
                }
              }
            }
          }
        }
      }
    }
  }
}