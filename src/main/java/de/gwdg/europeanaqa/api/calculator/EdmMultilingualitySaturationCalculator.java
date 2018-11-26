package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.europeanaqa.api.model.EdmSaturationMap;
import de.gwdg.europeanaqa.api.model.EdmSaturationProperty;
import de.gwdg.metadataqa.api.calculator.SkippedEntryChecker;
import de.gwdg.metadataqa.api.counter.BasicCounter;
import de.gwdg.metadataqa.api.counter.FieldCounter;
import de.gwdg.metadataqa.api.interfaces.Calculator;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.model.LanguageSaturationType;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;
import de.gwdg.metadataqa.api.util.Converter;
import de.gwdg.metadataqa.api.util.SkippedEntitySelector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmMultilingualitySaturationCalculator implements Calculator, Serializable {

	private static final Logger LOGGER = Logger.getLogger(
		EdmMultilingualitySaturationCalculator.class.getCanonicalName());

	/**
	 * Name of the calculator.
	 */
	public static final String CALCULATOR_NAME = "edmMultilingualitySaturation";

	private static final String NA = "n.a.";

	private static final String FULLBEAN_SCHEMA_PREF_LABEL_SELECTOR =
		"%s[?(@['about'] == '%s')]['prefLabel']";
	private static final String OAIXML_SCHEMA_PREF_LABEL_SELECTOR =
		"%s[?(@['@about'] == '%s')]['skos:prefLabel']";
	private boolean isEdmFullBeanSchema = false;

	/**
	 * The result's types.
	 */
	public enum ResultTypes {

		/**
		 * Normal type.
		 */
		NORMAL(0),
		/**
		 * Extended type containing average and normalized scores.
		 */
		EXTENDED(1);

		private final int value;

		ResultTypes(int value) {
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

	private ResultTypes resultType = ResultTypes.NORMAL;
	private String inputFileName;
	private FieldCounter<Double> saturationMap;
	private Map<String, Map<String, Double>> rawScoreMap = new LinkedHashMap<>();
	private Map<String, List<SortedMap<LanguageSaturationType, Double>>> rawLanguageSaturationMap;

	private Schema schema;
	private List<JsonBranch> providers;
	private SkippedEntryChecker skippedEntryChecker = null;
	private SkippedEntitySelector skippedEntitySelector = new SkippedEntitySelector();
	private Map<String, DisconnectedEntityCalculator.EntityType> contextualIds;
	private EdmSaturationMap edmSaturationMap;
	private String recordId;

	/**
	 * Creates an object.
	 */
	public EdmMultilingualitySaturationCalculator() {
		// this.recordID = null;
	}

	/**
	 * Constructor.
	 * @param schema The schema object.
	 */
	public EdmMultilingualitySaturationCalculator(Schema schema) {
		this.schema = schema;
		isEdmFullBeanSchema = schema.getClass().getSimpleName().equals("EdmFullBeanSchema");
		JsonBranch providerProxy = schema.getPathByLabel("Proxy");
		JsonBranch europeanaProxy = null;
		try {
			europeanaProxy = (JsonBranch) providerProxy.clone();
			europeanaProxy.setJsonPath(
				providerProxy.getJsonPath().replace("false", "true"));
		} catch (CloneNotSupportedException ex) {
			LOGGER.severe(ex.getMessage());
		}
		providers = Arrays.asList(providerProxy, europeanaProxy);
	}

	@Override
	public String getCalculatorName() {
		return CALCULATOR_NAME;
	}

	@Override
	public List<String> getHeader() {

		List<String> properties = new ArrayList<>();
		for (JsonBranch jsonBranch : providers.get(0).getChildren()) {
			if (!schema.getNoLanguageFields().contains(jsonBranch.getLabel())) {
				properties.add(jsonBranch.getLabel());
			}
		}
		return EdmSaturationMap.getHeader(properties);
	}

	@Override
	public void measure(JsonPathCache cache)
			throws InvalidJsonException {

		edmSaturationMap = new EdmSaturationMap();
		DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
		recordId = cache.getRecordId();
		contextualIds = calculator.getContextualIds(cache);
		rawLanguageSaturationMap = new LinkedHashMap<>();
		measureHierarchicalSchema(cache);
		// saturationMap = calculateScore(rawLanguageSaturationMap);
	}

	private void measureHierarchicalSchema(JsonPathCache cache) {
		List<String> skippableIds = getSkippableIds(cache);
		for (int i = 0; i < providers.size(); i++) {
			JsonBranch collection = providers.get(i);
			Object rawJsonFragment = cache.getFragment(collection.getJsonPath());
			if (rawJsonFragment == null) {
				measureMissingCollection(collection);
			} else {
				measureExistingCollection(rawJsonFragment, collection, cache, skippableIds, i);
			}
		}
	}

	private List<String> getSkippableIds(JsonPathCache cache) {
		return skippedEntryChecker != null
				  ? skippedEntryChecker.getSkippableCollectionIds(cache)
				  : new ArrayList<>();
	}

	private void measureMissingCollection(JsonBranch collection) {
		for (JsonBranch child : collection.getChildren()) {
			if (!schema.getNoLanguageFields().contains(child.getLabel())) {
				Map<LanguageSaturationType, BasicCounter> languages = new TreeMap<>();
				increase(languages, LanguageSaturationType.NA);
				updateMaps(child.getLabel(), transformLanguages(languages, 0));
			}
		}
	}

	private void measureExistingCollection(Object rawJsonFragment,
		  JsonBranch collection,
		  JsonPathCache cache,
		  List<String> skippableIds,
		  int providerNr) {
		List<Object> jsonFragments = Converter.jsonObjectToList(rawJsonFragment);
		if (jsonFragments.isEmpty()) {
			measureMissingCollection(collection);
		} else {
			for (int i = 0, len = jsonFragments.size(); i < len; i++) {
				Object jsonFragment = jsonFragments.get(i);
				if (skippedEntitySelector.isCollectionSkippable(skippableIds,
						  collection, i, cache, jsonFragment)) {
					LOGGER.info(String.format(
						"skip %s (%s)",
						collection.getLabel(),
						((LinkedHashMap) jsonFragment).get("@about")
					));
					measureMissingCollection(collection);
					// TODO???
				} else {
					for (JsonBranch child : collection.getChildren()) {
						if (!schema.getNoLanguageFields().contains(child.getLabel())) {
							String address = String.format(
								"%s/%d/%s",
								 collection.getJsonPath(), i, child.getJsonPath()
							);
							extractLanguageTags(jsonFragment, child, address, cache, rawLanguageSaturationMap, providerNr);
						}
					}
				}
			}
		}
	}

	private void extractLanguageTags(
			Object jsonFragment,
			JsonBranch field,
			String address,
			JsonPathCache cache,
			Map<String, List<SortedMap<LanguageSaturationType, Double>>> rawLanguageMap,
			int providerNr
	) {
		List<EdmFieldInstance> values = cache.get(address, field.getJsonPath(), jsonFragment);
		Map<LanguageSaturationType, BasicCounter> languages = new TreeMap<>();
		Set<String> individualLanguages = new HashSet<>();
		if (values != null && !values.isEmpty()) {
			for (EdmFieldInstance fieldInstance : values) {
				if (isLinkToEntity(fieldInstance)) {
					followEntityLink(cache, field, fieldInstance, individualLanguages, languages);
				} else {
					handleFieldValueInstance(fieldInstance, individualLanguages, languages);
				}
			}
		} else {
			increase(languages, LanguageSaturationType.NA);
		}

		EdmSaturationProperty property = edmSaturationMap.createOrGetProperty(
			field.getLabel(), providerNr
		);
		property.setDistinctLanguages(individualLanguages);
		SortedMap<LanguageSaturationType, Double> best = transformLanguages(
			languages, individualLanguages.size()
		);
		if (best.size() == 0) {
			LOGGER.severe(String.format(
				"NULL in %s",
				address
			));
			property.setTypedCount(LanguageSaturationType.NA, 0);
		} else {
			LanguageSaturationType type = best.firstKey();
			int count = (type == LanguageSaturationType.TRANSLATION
			            || type == LanguageSaturationType.LANGUAGE)
				? ((Double) languages.get(LanguageSaturationType.LANGUAGE).getTotal()).intValue()
				: best.get(type).intValue();
			property.setTypedCount(type, count);
		}

		updateMaps(
			field.getLabel() + "#" + providerNr,
			transformLanguages(languages, individualLanguages.size())
		);
	}

	private void handleFieldValueInstance(EdmFieldInstance field,
		  Set<String> individualLanguages,
		  Map<LanguageSaturationType, BasicCounter> languages) {
		if (field.hasValue()) {
			if (field.hasLanguage()) {
				individualLanguages.add(field.getLanguage());
				increase(languages, LanguageSaturationType.LANGUAGE);
			} else {
				increase(languages, LanguageSaturationType.STRING);
			}
		} else {
			increase(languages, LanguageSaturationType.STRING);
		}
	}

	private void followEntityLink(
		  JsonPathCache cache,
		  JsonBranch field,
		  EdmFieldInstance fieldInstance,
		  Set<String> individualLanguages,
		  Map<LanguageSaturationType, BasicCounter> languages) {
		String url = fieldInstance.hasResource()
		           ? fieldInstance.getResource()
		           : fieldInstance.getValue();
		DisconnectedEntityCalculator.EntityType type = contextualIds.get(url);
		String label = DisconnectedEntityCalculator.ENTITY_TYPE_TO_BRANCH_LABELS.get(type);
		JsonBranch entityBranch = schema.getPathByLabel(label);
		String jsonPath = selectEntityById(entityBranch.getJsonPath(), url);
		List<EdmFieldInstance> values = cache.get(jsonPath);
		if (values != null) {
			for (EdmFieldInstance referencedFieldInstance : values) {
				handleFieldValueInstance(
					referencedFieldInstance, individualLanguages, languages);
			}
		} else {
			increase(languages, LanguageSaturationType.STRING);
			String message = String.format(
				"The %s: %s does not have prefLabels (link from record %s, field %s)",
				type.name(), url, recordId, field.getLabel());
			LOGGER.warning(message);
		}
	}

	private void updateMaps(String label,
		  SortedMap<LanguageSaturationType, Double> instance) {
		if (!rawLanguageSaturationMap.containsKey(label)) {
			rawLanguageSaturationMap.put(label, new ArrayList<>());
		}
		rawLanguageSaturationMap.get(label).add(instance);
	}

	private void increase(Map<LanguageSaturationType, BasicCounter> languages,
		  LanguageSaturationType key) {
		if (!languages.containsKey(key)) {
			languages.put(key, new BasicCounter(1));
		} else {
			languages.get(key).increaseTotal();
		}
	}

	private String extractLanguagesFromRaw(Map<String, Integer> languages) {
		String result = "";
		for (String lang : languages.keySet()) {
			if (result.length() > 0) {
				result += ";";
			}
			result += lang + ":" + languages.get(lang);
		}
		return result;
	}

	private String extractLanguages(Map<String, BasicCounter> languages) {
		String result = "";
		for (String lang : languages.keySet()) {
			if (result.length() > 0) {
				result += ";";
			}
			result += lang + ":" + languages.get(lang).getTotalAsInt();
		}
		return result;
	}

	private SortedMap<LanguageSaturationType, Double> transformLanguages(
			Map<LanguageSaturationType, BasicCounter> languages,
			int languageCount) {
		SortedMap<LanguageSaturationType, Double> result = new TreeMap<>();
		for (LanguageSaturationType lang : languages.keySet()) {
			result.put(lang, languages.get(lang).getTotal());
		}

		if (result.containsKey(LanguageSaturationType.LANGUAGE)
				&& result.get(LanguageSaturationType.LANGUAGE) > 1
				&& languageCount > 1) {
			Double count = result.remove(LanguageSaturationType.LANGUAGE);
			result.put(
				LanguageSaturationType.TRANSLATION,
				(double) languageCount
				// normalizeTranslationCount(languageCount)
			);
		}

		if (languageCount > 1) {
			result = keepOnlyTheBest(result);
		}
		return result;
	}

	public Map<String, Double> getSaturationMap() {
		return edmSaturationMap.getCsv().getMap();
	}

	@Override
	public Map<String, Map<String, ? extends Object>> getLabelledResultMap() {
		Map<String, Map<String, ? extends Object>> labelledResultMap = new LinkedHashMap<>();
		labelledResultMap.put(getCalculatorName(), edmSaturationMap.getCsv().getMap());
		return labelledResultMap;
	}

	private Map<String, Map<String, Object>> mergeMaps() {
		Map<String, Map<String, Object>> map = new LinkedHashMap<>();
		for (String key : rawLanguageSaturationMap.keySet()) {
			Map<String, Object> entry = new LinkedHashMap<>();
			List<Object> list = new ArrayList<>();
			entry.put("instances", normalizeRawValue(rawLanguageSaturationMap.get(key)));
			entry.put("score", rawScoreMap.get(key));
			map.put(key, entry);
		}
		return map;
	}

	@Override
	public Map<String, ? extends Object> getResultMap() {
		return edmSaturationMap.getCsv().getMap();
	}

	@Override
	public String getCsv(boolean withLabel, CompressionLevel compressionLevel) {
		return edmSaturationMap.getCsv().getList(withLabel, compressionLevel);
	}

	private SortedMap<LanguageSaturationType, Double> keepOnlyTheBest(SortedMap<LanguageSaturationType, Double> result) {
		if (result.size() > 1) {
			LanguageSaturationType best = LanguageSaturationType.NA;
			for (LanguageSaturationType key : result.keySet()) {
				if (key.value() > best.value()) {
					best = key;
				}
			}

			if (best != LanguageSaturationType.NA) {
				double modifier = 0.0;
				if (best == LanguageSaturationType.TRANSLATION
					&& result.containsKey(LanguageSaturationType.STRING)) {
					modifier = -0.2;
				}
				SortedMap<LanguageSaturationType, Double> replacement = new TreeMap<>();
				replacement.put(best, result.get(best) + modifier);
				result = replacement;
			}
		}
		return result;
	}

	private FieldCounter<Double> calculateScore(Map<String,
			List<SortedMap<LanguageSaturationType, Double>>> rawLanguageMap) {
		double sum, average, normalized;
		List<Double> sums = new ArrayList<>();
		FieldCounter<Double> languageMap = new FieldCounter<>();
		boolean countWithWeight = false;
		for (String field : rawLanguageMap.keySet()) {
			Map<String, Double> fieldMap = new LinkedHashMap<>();
			List<SortedMap<LanguageSaturationType, Double>> values = rawLanguageMap.get(field);
			sum = 0.0;
			boolean isSet = false;
			for (SortedMap<LanguageSaturationType, Double> value : values) {
				double saturation = value.firstKey().value();
				if (saturation == -1.0) {
					continue;
				}
				if (countWithWeight) {
					double weight = value.get(value.firstKey());
					if (value.firstKey() == LanguageSaturationType.TRANSLATION) {
						saturation += weight;
					}
				}
				sum += saturation;
				isSet = true;
			}
			if (!isSet) {
				sum = LanguageSaturationType.NA.value();
				average = LanguageSaturationType.NA.value();
				normalized = LanguageSaturationType.NA.value();
			} else {
				average = sum / (double) values.size();
				normalized = normalize(average);
				sums.add(sum);
			}

			fieldMap.put("average", average);
			fieldMap.put("normalized", normalized);
			fieldMap.put("sum", sum);
			rawScoreMap.put(field, fieldMap);

			if (resultType.equals(ResultTypes.NORMAL)) {
				languageMap.put(field, sum);
			} else {
				languageMap.put(field + ":average", average);
				languageMap.put(field + ":normalized", normalized);
				languageMap.put(field + ":sum", sum);
			}
		}
		sum = summarize(sums);
		average = sum / (double) sums.size();
		normalized = normalize(average);
		if (resultType.equals(ResultTypes.EXTENDED)) {
			languageMap.put(CALCULATOR_NAME + ":average", average);
			languageMap.put(CALCULATOR_NAME + ":normalized", normalized);
		}
		languageMap.put(CALCULATOR_NAME + ":sum", sum);

		return languageMap;
	}

	private double summarize(List<Double> sums) {
		double sum;
		sum = 0.0;
		for (Double item : sums) {
			sum += item;
		}
		return sum;
	}

	private static double normalize(double average) {
		return 1.0 - (1.0 / (average + 1.0));
	}

	private Object normalizeRawValue(List<SortedMap<LanguageSaturationType, Double>> values) {
		List<SortedMap<LanguageSaturationType, Double>> normalized = new LinkedList<>();
		for (SortedMap<LanguageSaturationType, Double> value : values) {
			SortedMap<LanguageSaturationType, Double> norm = new TreeMap<>();
			double saturation = value.firstKey().value();
			double weight = value.get(value.firstKey());
			if (value.firstKey() == LanguageSaturationType.TRANSLATION) {
				saturation += weight;
			}
			norm.put(value.firstKey(), saturation);
			normalized.add(norm);
		}
		return normalized;
	}

	public ResultTypes getResultType() {
		return resultType;
	}

	public void setResultType(ResultTypes resultType) {
		this.resultType = resultType;
	}

	public SkippedEntryChecker getSkippedEntryChecker() {
		return skippedEntryChecker;
	}

	/**
	 * Sets a skipped entity checker.
	 * @param skippedEntryChecker a skipped entity checker
	 */
	public void setSkippedEntryChecker(SkippedEntryChecker skippedEntryChecker) {
		this.skippedEntryChecker = skippedEntryChecker;
		skippedEntitySelector.setSkippedEntryChecker(skippedEntryChecker);
	}

	private boolean isLinkToEntity(EdmFieldInstance field) {
		if (field.hasResource() && contextualIds.keySet().contains(field.getResource())) {
			return true;
		}
		return contextualIds.keySet().contains(field.getValue());
	}

	/**
	 * Creates a JSON path selector for prefLabel of a contextual entity identified by an URI.
	 * @param jsonPath The JSON path of the entity
	 * @param url The URI identifier
	 * @return a JSON path selector of the prefLabel
	 */
	public String selectEntityById(String jsonPath, String url) {
		String pattern = isEdmFullBeanSchema
			? FULLBEAN_SCHEMA_PREF_LABEL_SELECTOR
			: OAIXML_SCHEMA_PREF_LABEL_SELECTOR;

		return String.format(pattern, jsonPath, url.replace("'", "\\'"));
	}
}
