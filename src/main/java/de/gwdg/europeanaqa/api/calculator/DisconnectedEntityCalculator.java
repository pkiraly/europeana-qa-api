package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.europeanaqa.api.model.LinkRegister;
import de.gwdg.metadataqa.api.calculator.edm.EnhancementIdExtractor;
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
class DisconnectedEntityCalculator implements Calculator, Serializable {

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
	 * Entity types.
	 */
	public enum EntityType {
		/**
		 * Agent.
		 */
		AGENT,
		/**
		 * Concept.
		 */
		CONCEPT,
		/**
		 * Place.
		 */
		PLACE,
		/**
		 * Timespan.
		 */
		TIMESPAN
	}

	/**
	 * List of headers.
	 */
	private List<String> headers = Arrays.asList(
		"unlinkedEntities", "brokenProviderLinks",
		"brokenEuropeanaLinks", "contextualEntityCount",
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
		"Proxy/edm:isRelatedTo"
	);

	/**
	 * Maps of entity branch labels and entity types.
	 */
	private static final Map<String, EntityType> ENTITY_BRANCH_LABELS =
		new HashMap<>();
	static {
		ENTITY_BRANCH_LABELS.put("Agent/rdf:about", EntityType.AGENT);
		ENTITY_BRANCH_LABELS.put("Concept/rdf:about", EntityType.CONCEPT);
		ENTITY_BRANCH_LABELS.put("Place/rdf:about", EntityType.PLACE);
		ENTITY_BRANCH_LABELS.put("Timespan/rdf:about", EntityType.TIMESPAN);
	}

	/**
	 * Maps of entity types to labels.
	 */
	public static final Map<EntityType, String> ENTITY_TYPE_TO_BRANCH_LABELS =
		new HashMap<>();
	static {
		ENTITY_TYPE_TO_BRANCH_LABELS.put(EntityType.AGENT, "Agent");
		ENTITY_TYPE_TO_BRANCH_LABELS.put(EntityType.CONCEPT, "Concept");
		ENTITY_TYPE_TO_BRANCH_LABELS.put(EntityType.PLACE, "Place");
		ENTITY_TYPE_TO_BRANCH_LABELS.put(EntityType.TIMESPAN, "Timespan");
	}

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

	/**
	 * Contructor.
	 * @param schema Schema The schema to use by the functions.
	 */
	DisconnectedEntityCalculator(final Schema schema) {
		this.schema = schema;
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

		List<String> removables;
		List<String> providerProxyLinks = new ArrayList<>();
		List<String> providerProxyValues = new ArrayList<>();

		extractProviderProxyLinksAndValues(
			cache,
			providerProxyLinks,
			providerProxyValues
		);
		int providerProxyLinksCount = providerProxyLinks.size();
		int providerProxyValuesCount = providerProxyValues.size();
		List<String> europeanaProxyLinks = EnhancementIdExtractor
			.extractIds(cache);
		int europeanaProxyLinksCount = europeanaProxyLinks.size();

		checkContextualIDsInProviderProxy(
			contextualIds,
			providerProxyLinks,
			providerProxyValues
		);
		providerProxyLinksCount -= providerProxyLinks.size();
		providerProxyValuesCount -= providerProxyValues.size();
		checkContextualIDsInEuropeanaProxy(contextualIds, europeanaProxyLinks);
		europeanaProxyLinksCount -= europeanaProxyLinks.size();
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
		resultMap.put("brokenProviderLinks", providerProxyLinks.size());
		resultMap.put("brokenEuropeanaLinks", europeanaProxyLinks.size());
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
	 * @param providerProxyLinks The container of provider proxy links
	 * @param providerProxyValues The container of provider proxy values
	 */
	private void extractProviderProxyLinksAndValues(JsonPathCache cache,
																	List<String> providerProxyLinks,
																	List<String> providerProxyValues) {
		for (JsonBranch branch : schema.getPaths()) {
			if (branch.getLabel().equals("Proxy")) {
				Object rawProxy = cache.getFragment(branch.getJsonPath());
				List<Object> proxies = Converter.jsonObjectToList(rawProxy);
				for (JsonBranch child : branch.getChildren()) {
					if (!isEnrichableField(child)) {
						continue;
					}
					List<EdmFieldInstance> fieldInstances =
						cache.get(child.getJsonPath(), child.getJsonPath(), proxies.get(0));
					if (fieldInstances == null) {
						continue;
					}
					for (EdmFieldInstance fieldInstance : fieldInstances) {
						if (fieldInstance.isUrl()) {
							providerProxyLinks.add(fieldInstance.getUrl());
						} else if (fieldInstance.hasValue()) {
							providerProxyValues.add(fieldInstance.getValue());
						}
					}
				}
			}
		}
	}

	private static boolean isEnrichableField(JsonBranch child) {
		return ENRICHABLE_FIELDS.contains(child.getLabel());
	}

	public Map<String, EntityType> getContextualIds(JsonPathCache cache) {
		Map<String, EntityType> contextualIds = new HashMap<>();
		for (JsonBranch branch : schema.getPaths()) {
			if (ENTITY_BRANCH_LABELS.containsKey(branch.getLabel())) {
				Object rawJsonFragment = cache.getFragment(branch.getAbsoluteJsonPath());
				List<Object> jsonFragments = Converter.jsonObjectToList(rawJsonFragment);
				if (jsonFragments.size() > 0) {
					for (Object jsonFragment : jsonFragments) {
						if (jsonFragment != null) {
							if (jsonFragment instanceof String) {
								contextualIds.put(
									(String) jsonFragment,
									ENTITY_BRANCH_LABELS.get(branch.getLabel())
								);
							} else {
								LOGGER.info("jsonFragment is not String, but "
									+ jsonFragment.getClass().getCanonicalName());
							}
						}
					}
				}
			}
		}
		return contextualIds;
	}

	private void checkContextualIDsInProviderProxy(Map<String, EntityType> contextualIds,
			List<String> providerProxyLinks,
			List<String> providerProxyValues) {
		List<String> removable = new ArrayList<>();
		for (String id : contextualIds.keySet()) {
			if (providerProxyLinks.contains(id)) {
				removable.add(id);
				providerProxyLinks.remove(id);
			} else if (providerProxyValues.contains(id)) {
				removable.add(id);
				providerProxyValues.remove(id);
			}
		}
		for (String id : removable) {
			contextualIds.remove(id);
		}
	}

	private void checkContextualIDsInEuropeanaProxy(Map<String, EntityType> contextualIds,
			List<String> europeanaProxyLinks) {
		List<String> removable = new ArrayList<>();
		for (String id : contextualIds.keySet()) {
			if (europeanaProxyLinks.contains(id)) {
				removable.add(id);
				europeanaProxyLinks.remove(id);
			}
		}
		for (String id : removable) {
			contextualIds.remove(id);
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
}
