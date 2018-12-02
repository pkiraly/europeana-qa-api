package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.europeanaqa.api.model.EdmStructure;
import de.gwdg.europeanaqa.api.model.EntityType;
import de.gwdg.europeanaqa.api.model.LinkRegister;
import de.gwdg.europeanaqa.api.model.Proxies;
import de.gwdg.europeanaqa.api.model.ProxyType;
import de.gwdg.metadataqa.api.counter.FieldCounter;
import de.gwdg.metadataqa.api.interfaces.Calculator;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
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
	public static final String CALCULATOR_NAME = "orphanedEntityCalculator";

		/**
	 * List of headers.
	 */
	private List<String> headers = Arrays.asList(
		"unlinkedEntities",
		"brokenProviderLinks", "brokenEuropeanaLinks",
		"contextualEntityCount",
		"providerProxyLinksCount", "providerProxyValuesCount",
		"europeanaProxyLinksCount", "contextualLinksCount"
	);

	/**
	 * List of technical fields.
	 */
	private static final List<String> TECHNICAL_FIELDS = Arrays.asList(
		"Proxy/ore:proxyIn", "Proxy/ore:proxyFor", "Proxy/rdf:about"
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

	/**
	 * Register the fields belong to the contextual entities.
	 */
	private static final Map<EntityType, List<String>> CONTEXTUAL_LINK_FIELDS =
		new HashMap<>();
	static {
		CONTEXTUAL_LINK_FIELDS.put(EntityType.AGENT, Arrays.asList(
			"Agent/edm:hasMet", "Agent/edm:isRelatedTo",
			"Agent/owl:sameAs"
		));
		CONTEXTUAL_LINK_FIELDS.put(EntityType.CONCEPT, Arrays.asList(
			"Concept/skos:broader", "Concept/skos:narrower",
			"Concept/skos:related", "Concept/skos:broadMatch",
			"Concept/skos:narrowMatch", "Concept/skos:relatedMatch",
			"Concept/skos:exactMatch", "Concept/skos:closeMatch"
		));
		CONTEXTUAL_LINK_FIELDS.put(EntityType.PLACE, Arrays.asList(
			"Place/dcterms:isPartOf", "Place/dcterms:hasPart",
			"Place/owl:sameAs"
		));
		CONTEXTUAL_LINK_FIELDS.put(EntityType.TIMESPAN, Arrays.asList(
			"Timespan/dcterms:isPartOf", "Timespan/dcterms:hasPart",
			"Timespan/edm:isNextInSequence", "Timespan/owl:sameAs"
		));
	}

	/**
	 * The schema.
	 */
	private Schema schema;
	private Proxies proxies;

	private EdmStructure edmStructure;

	/**
	 * Contructor.
	 * @param schema Schema The schema to use by the functions.
	 */
	public DisconnectedEntityCalculator(final Schema schema) {
		this.schema = schema;
		proxies = new Proxies(schema);
	}

	/**
	 * The result map.
	 */
	private FieldCounter resultMap;

	@Override
	public void measure(final JsonPathCache cache) {
		resultMap = new FieldCounter<>();

		LinkRegister register = new LinkRegister();
		Map<String, EntityType> contextualIds = getContextualIds(cache);
		int contextualEntityCount = contextualIds.size();
		register.putAll(
			new ArrayList(contextualIds.keySet()),
			LinkRegister.LinkingType.NONE
		);

		edmStructure = new EdmStructure();

		// List<String> removables;

		extractProxyLinksAndValues(cache, edmStructure, ProxyType.PROVIDER);
		extractProxyLinksAndValues(cache, edmStructure, ProxyType.EUROPEANA);
		int providerProxyLinksCount = edmStructure.getProviderProxyLinks().size();
		int providerProxyValuesCount = edmStructure.getProviderProxyValues().size();
		// List<String> europeanaProxyLinks = EnhancementIdExtractor.extractIds(cache);
		int europeanaProxyLinksCount = edmStructure.getEuropeanaProxyLinks().size();

		checkContextualIDsInProviderProxy(contextualIds, edmStructure);
		// providerProxyLinksCount -= edmStructure.getProviderProxyLinks().size();
		providerProxyValuesCount -= edmStructure.getProviderProxyValues().size();
		checkContextualIDsInProxies(contextualIds);
		// europeanaProxyLinksCount -= edmStructure.getEuropeanaProxyLinks().size();
		int contextualLinksCount = contextualIds.size();
		checkContextualIDsInEntities(contextualIds, cache, register);
		contextualLinksCount -= contextualIds.size();

		if (contextualIds.size() > 0) {
			String id = getId(cache);
			List<String> entities = new ArrayList<>();
			for (String uri : contextualIds.keySet()) {
				entities.add(String.format(
					"%s (%s)",
					uri, contextualIds.get(uri)
				));
			}
			LOGGER.warning(String.format(
				"%s has orphaned entities: %s",
				id,
				StringUtils.join(entities, ", "))
			);
		}

		resultMap.put("orphanedEntities", contextualIds.size());
		resultMap.put("brokenProviderLinks", edmStructure.getBrokenProviderProxyLinks().size());
		resultMap.put("brokenEuropeanaLinks", edmStructure.getBrokenEuropeanaProxyLinks().size());
		resultMap.put("contextualEntityCount", contextualEntityCount);
		resultMap.put("providerProxyLinksCount", providerProxyLinksCount);
		resultMap.put("providerProxyValuesCount", providerProxyValuesCount);
		resultMap.put("europeanaProxyLinksCount", europeanaProxyLinksCount);
		resultMap.put("contextualLinksCount", contextualLinksCount);
	}

	/**
	 * Checks the internal proxy links.
	 * @param cache
	 * @param register
	 * @param uri
	 * @param type
	 * @return
	 */
	private boolean checkInternalProxyLinks(JsonPathCache cache,
														 LinkRegister register,
														 final String uri,
														 final EntityType type) {
		boolean found = false;
		List<String> paths = CONTEXTUAL_LINK_FIELDS.get(type);
		for (String field : paths) {
			JsonBranch branch = schema.getPathByLabel(field);
			List<EdmFieldInstance> fieldInstances = cache.get(branch.getAbsoluteJsonPath());
			if (fieldInstances != null) {
				for (EdmFieldInstance fieldInstance : fieldInstances) {
					if (fieldInstance.isUrl()) {
						if (fieldInstance.getUrl().equals(uri)) {
							register.put(uri, LinkRegister.LinkingType.CONTEXTUAL_ENTITY);
							found = true;
							break;
						}
					}
				}
			}
		}
		return found;
	}

	/**
	 * Extracts Provider Proxy links and values.
	 * @param cache
	 * @param edmStructure
	 */
	private void extractProxyLinksAndValues(JsonPathCache cache,
														 EdmStructure edmStructure,
														 ProxyType type) {
		JsonBranch branch = type.equals(ProxyType.PROVIDER)
			? proxies.getProviderProxy()
			: proxies.getEuropeanaProxy();
		Object rawProxy = cache.getFragment(branch.getJsonPath());
		List<Object> proxies = Converter.jsonObjectToList(rawProxy);
		for (JsonBranch child : branch.getChildren()) {
			if (!isEnrichableField(child)) {
				continue;
			}
			String address = branch.getJsonPath() + "/" + child.getJsonPath();
			List<EdmFieldInstance> fieldInstances =
				cache.get(address, child.getJsonPath(), proxies.get(0));
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
	public Map<String, EntityType> getContextualIds(JsonPathCache cache) {
		Map<String, EntityType> contextualIds = new HashMap<>();
		for (EntityType type : EntityType.values()) {
			JsonBranch branch = schema.getPathByLabel(type.getBranchId());
			Object rawJsonFragment = cache.getFragment(branch.getAbsoluteJsonPath());
			List<Object> jsonFragments = Converter.jsonObjectToList(rawJsonFragment);
			if (jsonFragments.size() <= 0) {
				continue;
			}
			for (Object jsonFragment : jsonFragments) {
				if (jsonFragment != null) {
					if (jsonFragment instanceof String) {
						contextualIds.put((String) jsonFragment, type);
					} else {
						LOGGER.info("jsonFragment is not String, but "
							+ jsonFragment.getClass().getCanonicalName());
					}
				}
			}
		}
		return contextualIds;
	}

	private void checkContextualIDsInProviderProxy(Map<String, EntityType> contextualIds,
																  EdmStructure edmStructure) {
		List<String> removable = new ArrayList<>();
		for (String id : contextualIds.keySet()) {
			if (edmStructure.getProviderProxyLinks().contains(id)) {
				removable.add(id);
				edmStructure.getProviderProxyLinks().remove(id);
			} else if (edmStructure.getProviderProxyValues().contains(id)) {
				removable.add(id);
				edmStructure.getProviderProxyValues().remove(id);
			}
		}
		for (String id : removable) {
			contextualIds.remove(id);
		}
	}

	private void checkContextualIDsInProxies(Map<String, EntityType> contextualIds) {
		List<String> removable = new ArrayList<>();
		for (String url : contextualIds.keySet()) {
			if (edmStructure.containsProviderLink(url)) {
				removable.add(url);
				edmStructure.setProviderLinkTarget(url, contextualIds.get(url));
			}
			if (edmStructure.containsEuropeanaLink(url)) {
				removable.add(url);
				edmStructure.setEuropeanaLinkTarget(url, contextualIds.get(url));
			}
		}
		for (String id : removable) {
			if (contextualIds.containsKey(id)) {
				contextualIds.remove(id);
			}
		}
	}

	/**
	 * Check the the contextual IDs in the entities.
	 * @param contextualIds
	 * @param cache
	 * @param register
	 */
	private void checkContextualIDsInEntities(Map<String, EntityType> contextualIds,
															JsonPathCache cache,
															LinkRegister register) {
		if (contextualIds.size() > 0) {
			List<String> removableURIs = new ArrayList<>();
			for (String uri : contextualIds.keySet()) {
				if (checkInternalProxyLinks(cache, register, uri, contextualIds.get(uri))) {
					removableURIs.add(uri);
				}
			}
			for (String removable : removableURIs) {
				contextualIds.remove(removable);
			}
		}
	}

	/**
	 * Get the ID of the record.
	 * @param cache The JSON path cache
	 * @return The ID of the record
	 */
	private String getId(JsonPathCache cache) {
		String path = schema
			.getPathByLabel("ProvidedCHO/rdf:about")
			.getAbsoluteJsonPath()
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
