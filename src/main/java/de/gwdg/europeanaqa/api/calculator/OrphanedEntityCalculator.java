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
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
class OrphanedEntityCalculator implements Calculator, Serializable {

	public static final String CALCULATOR_NAME = "orphanedEntityCalculator";

	public enum EntityType {
		AGENT, CONCEPT, PLACE, TIMESPAN
	}

	private List<String> headers = Arrays.asList("unlinkedEntities", "brokenProviderLinks", "brokenEuropeanaLinks");

	private static final List<String> technicalFields = Arrays.asList(
		"Proxy/ore:proxyIn", "Proxy/ore:proxyFor", "Proxy/rdf:about"
	);

	private static final List<String> enrichableFields = Arrays.asList(
		"Proxy/dc:contributor", "Proxy/dc:publisher", "Proxy/dc:creator",
		"Proxy/dc:subject", "Proxy/dc:type", "Proxy/edm:hasType", "Proxy/dc:coverage",
		"Proxy/dcterms:spatial", "Proxy/edm:currentLocation", "Proxy/dc:date",
		"Proxy/dcterms:created", "Proxy/dcterms:issued", "Proxy/dcterms:temporal",
		"Proxy/edm:hasMet",
		"Proxy/dc:format", "Proxy/dcterms:conformsTo",
		"Proxy/dcterms:medium", "Proxy/edm:isRelatedTo"
	);

	private static final Map<String, EntityType> entityBranchLabels = new HashMap<>();
	static {
		entityBranchLabels.put("Agent/rdf:about", EntityType.AGENT);
		entityBranchLabels.put("Concept/rdf:about", EntityType.CONCEPT);
		entityBranchLabels.put("Place/rdf:about", EntityType.PLACE);
		entityBranchLabels.put("Timespan/rdf:about", EntityType.TIMESPAN);
	}

	private static final Map<EntityType, List<String>> contextualLinkFields = new HashMap<>();
	static {
		contextualLinkFields.put(EntityType.AGENT, Arrays.asList(
			"Agent/edm:hasMet", "Agent/edm:isRelatedTo", "Agent/owl:sameAs"
		));
		contextualLinkFields.put(EntityType.CONCEPT, Arrays.asList(
			"Concept/skos:broader", "Concept/skos:narrower", "Concept/skos:related",
			"Concept/skos:broadMatch", "Concept/skos:narrowMatch", "Concept/skos:relatedMatch",
			"Concept/skos:exactMatch", "Concept/skos:closeMatch"
		));
		contextualLinkFields.put(EntityType.PLACE, Arrays.asList(
			"Place/dcterms:isPartOf", "Place/dcterms:hasPart", "Place/owl:sameAs"
		));
		contextualLinkFields.put(EntityType.TIMESPAN, Arrays.asList(
			"Timespan/dcterms:isPartOf", "Timespan/dcterms:hasPart",
			"Timespan/edm:isNextInSequence", "Timespan/owl:sameAs"
		));
	}

	protected Schema schema;

	public OrphanedEntityCalculator(Schema schema) {
		this.schema = schema;
	}

	private FieldCounter resultMap;

	@Override
	public void measure(JsonPathCache cache) {
		resultMap = new FieldCounter<>();

		LinkRegister register = new LinkRegister();
		// "Proxy",
		Map<String, EntityType> contextualIds = getContextualIds(cache);
		register.putAll(new ArrayList(contextualIds.keySet()), LinkRegister.LinkingType.NONE);
		// System.err.println("contextualIds: " + StringUtils.join(contextualIds, ", "));

		List<String> removables;

		List<String> providerProxyLinks = getProxyLinks(cache);
		System.err.println("providerProxyLinks: " + StringUtils.join(providerProxyLinks, ", "));
		removables = new ArrayList<>();
		for (String uri : providerProxyLinks) {
			if (register.exists(uri)) {
				register.put(uri, LinkRegister.LinkingType.PROVIDER_PROXY);
				removables.add(uri);
			} else {
				// System.err.println(String.format("URI %s is not a valid entity's URI", uri));
			}
		}
		providerProxyLinks.removeAll(removables);

		List<String> europeanaProxyLinks = EnhancementIdExtractor.extractIds(cache);
		// System.err.println("europeanaProxyLinks: " + StringUtils.join(europeanaProxyLinks, ", "));
		removables = new ArrayList<>();
		for (String uri : europeanaProxyLinks) {
			if (register.exists(uri)) {
				register.put(uri, LinkRegister.LinkingType.EUROPEANA_PROXY);
				removables.add(uri);
			} else {
				// System.err.println(String.format("URI %s is not a valid entity's URI", uri));
			}
		}
		europeanaProxyLinks.removeAll(removables);

		if (register.getUnlinkedEntities().size() > 0) {
			for (String uri : register.getUnlinkedEntities()) {
				checkInternalProxyLinks(cache, register, uri, contextualIds.get(uri));
			}
		}
		if (register.getUnlinkedEntities().size() > 0) {
			System.err.println("orphaned entities: " + StringUtils.join(register.getUnlinkedEntities(), ", "));
		}
		resultMap.put("orphanedEntities", register.getUnlinkedEntities().size());
		resultMap.put("brokenProviderLinks", providerProxyLinks.size());
		resultMap.put("brokenEuropeanaLinks", europeanaProxyLinks.size());
	}

	private void checkInternalProxyLinks(JsonPathCache cache, LinkRegister register, String uri, EntityType type) {
		List<String> paths = contextualLinkFields.get(type);
		for (String field : paths) {
			JsonBranch branch = schema.getPathByLabel(field);
			List<EdmFieldInstance> fieldInstances = cache.get(branch.getAbsoluteJsonPath());
			if (fieldInstances != null) {
				for (EdmFieldInstance fieldInstance : fieldInstances) {
					if (fieldInstance.isUrl() && fieldInstance.getUrl().equals(uri)) {
						register.put(uri, LinkRegister.LinkingType.CONTEXTUAL_ENTITY);
						System.err.println(String.format("%s (%s) is found as %s", uri, type, field));
						break;
					}
				}
			}
		}
	}

	private List<String> getProxyLinks(JsonPathCache cache) {
		List<String> proxyLinks = new ArrayList<>();
		for (JsonBranch branch : schema.getPaths()) {
			if (branch.getLabel().equals("Proxy")) {
				Object rawProxy = cache.getFragment(branch.getJsonPath());
				List<Object> proxies = Converter.jsonObjectToList(rawProxy);
				// System.err.println("proxy: " + proxy.getClass());
				for (JsonBranch child : branch.getChildren()) {
					List<EdmFieldInstance> fieldInstances = cache.get(child.getJsonPath(), child.getJsonPath(), proxies.get(0));
					if (fieldInstances != null 
					    && enrichableFields.contains(child.getLabel())) {
						for (EdmFieldInstance fieldInstance : fieldInstances) {
							if (fieldInstance.isUrl()) {
								proxyLinks.add(fieldInstance.getUrl());
								System.err.println(child.getLabel() 
									  + ": " 
									  + fieldInstance.toString());
							}
						}
					}
				}
			}
		}
		return proxyLinks;
	}

	private Map<String, EntityType> getContextualIds(JsonPathCache cache) {
		Map<String, EntityType> contextualIds = new HashMap<>();
		for (JsonBranch branch : schema.getPaths()) {
			if (entityBranchLabels.containsKey(branch.getLabel())) {
				Object rawJsonFragment = cache.getFragment(branch.getAbsoluteJsonPath());
				List<Object> jsonFragments = Converter.jsonObjectToList(rawJsonFragment);
				if (jsonFragments.size() > 0) {
					for(Object jsonFragment : jsonFragments) {
						if (jsonFragment != null) {
							if (jsonFragment instanceof String) {
								contextualIds.put((String)jsonFragment, entityBranchLabels.get(branch.getLabel()));
							} else {
								System.err.println(jsonFragment.getClass().getCanonicalName());
							}
						}
					}
				}
			}
		}
		return contextualIds;
	}

	@Override
	public Map<String, ? extends Object> getResultMap() {
		return resultMap.getMap();
	}

	@Override
	public Map<String, Map<String, ? extends Object>> getLabelledResultMap() {
		Map<String, Map<String, ? extends Object>> labelledResultMap = new LinkedHashMap<>();
		labelledResultMap.put(getCalculatorName(), resultMap.getMap());
		return labelledResultMap;
	}

	@Override
	public String getCsv(boolean withLabels, CompressionLevel compressionLevel) {
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
