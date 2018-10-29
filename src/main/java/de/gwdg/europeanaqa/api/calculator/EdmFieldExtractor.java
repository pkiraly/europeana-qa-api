package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.metadataqa.api.calculator.FieldExtractor;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;

import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmFieldExtractor extends FieldExtractor {

	private static final Logger logger = Logger.getLogger(EdmFieldExtractor.class.getCanonicalName());

	public static final String CALCULATOR_NAME = "edmFieldExtractor";

	private final static String ILLEGAL_ARGUMENT_TPL = "An EDM-based schema should define path for '%' in the extractable fields.";

	private static final String DATA_PROVIDER = "dataProvider";
	private static final String DATASET = "dataset";
	private static final List<String> paths = Arrays.asList(
		"Aggregation/edm:dataProvider",
		"Aggregation/edm:provider"
	);

	private EdmDataProviderManager dataProviderManager;
	private EdmDatasetManager datasetsManager;
	private Map<String, AbbreviationManager> abbreviationManagers;
	private boolean abbreviate;

	public EdmFieldExtractor(Schema schema) {
		super(schema);
		if (!schema.getExtractableFields().containsKey(super.FIELD_NAME)) {
			throw new IllegalArgumentException(String.format(ILLEGAL_ARGUMENT_TPL, super.FIELD_NAME));
		}
		if (!schema.getExtractableFields().containsKey(DATASET)) {
			throw new IllegalArgumentException(String.format(ILLEGAL_ARGUMENT_TPL, DATASET));
		}
		if (!schema.getExtractableFields().containsKey(DATA_PROVIDER)) {
			throw new IllegalArgumentException(String.format(ILLEGAL_ARGUMENT_TPL, DATA_PROVIDER));
		}
		abbreviationManagers = new HashMap<>();
	}

	@Override
	public void measure(JsonPathCache cache) throws InvalidJsonException {
		super.measure(cache);

		String dataset  = extractValueByKey(cache, DATASET, null);
		String provider = extractValueByKey(cache, DATA_PROVIDER, null);
		if (provider == null) {
			for (String path : paths) {
				JsonBranch branch = schema.getPathByLabel(path);
				List<EdmFieldInstance> providers = cache.get(branch.getAbsoluteJsonPath().replace("[*]", ""));
				provider = (providers != null && !providers.isEmpty()) ? providers.get(0).getValue() : null;
				if (provider != null)
					break;
			}
		}
		if (dataset == null) {
			logger.warning("Missing dataset! " + resultMap.get(super.FIELD_NAME) + "\n" + cache.getJsonString());
		}
		if (provider == null) {
			logger.warning("Missing provider! " + resultMap.get(super.FIELD_NAME) + "\n" + cache.getJsonString());
		}
		if (abbreviate) {
			resultMap.put(DATASET, (dataset == null) ? "null" : getDatasetCode(dataset));
			resultMap.put(DATA_PROVIDER, (provider == null) ? "null" : getDataProviderCode(provider));
		} else {
			resultMap.put(DATASET, dataset);
			resultMap.put(DATA_PROVIDER, (provider == null) ? "null" : provider);
		}

		for (Map.Entry<String, String> entry : schema.getExtractableFields().entrySet()) {
			String field = entry.getKey();
			if (field.equals(super.FIELD_NAME)
			   || field.equals(DATASET)
				|| field.equals(DATA_PROVIDER))
				continue;
			String value = extractValueByPath(cache, entry.getValue());
			if (abbreviate) {
				value = abbreviationManagers.get(field).lookup(value).toString();
			}
			resultMap.put(field, value);
		}
	}

	private String extractValueByKey(JsonPathCache cache, String key, String defaultValue) {
		if (!schema.getExtractableFields().containsKey(key))
			return defaultValue;

		String jsonPath = schema.getExtractableFields().get(key);
		return extractValueByPath(cache, jsonPath, defaultValue);
	}

	private String extractValueByKey(JsonPathCache cache, String key) {
		return extractValueByKey(cache, key, "na");
	}

	private String extractValueByPath(JsonPathCache cache, String jsonPath) {
		return extractValueByPath(cache, jsonPath, "na");
	}

	private String extractValueByPath(JsonPathCache cache, String jsonPath, String defaultValue) {
		List<EdmFieldInstance> instances = cache.get(jsonPath);
		String value = (instances != null && !instances.isEmpty()) ? instances.get(0).getValue() : defaultValue;
		return value;
	}

	@Override
	public String getIdPath() {
		return schema.getExtractableFields().get(super.FIELD_NAME);
	}

	public String getDataProviderCode(String dataProvider) {
		String dataProviderCode;
		if (dataProvider == null) {
			dataProviderCode = "0";
		} else if (dataProviderManager != null) {
			dataProviderCode = String.valueOf(dataProviderManager.lookup(dataProvider));
		} else {
			dataProviderCode = dataProvider;
		}
		return dataProviderCode;
	}

	public String getDatasetCode(String dataset) {
		String datasetCode;
		if (dataset == null) {
			datasetCode = "0";
		} else if (datasetsManager != null) {
			datasetCode = String.valueOf(datasetsManager.lookup(dataset));
		} else {
			datasetCode = dataset;
		}
		return datasetCode;
	}

	public void setDataProviderManager(EdmDataProviderManager dataProviderManager) {
		this.dataProviderManager = dataProviderManager;
	}

	public void setDatasetManager(EdmDatasetManager datasetsManager) {
		this.datasetsManager = datasetsManager;
	}

	public void addAbbreviationManager(String field, AbbreviationManager manager) {
		abbreviationManagers.put(field, manager);
	}

	public boolean abbreviate() {
		return abbreviate;
	}

	public void abbreviate(boolean abbreviate) {
		this.abbreviate = abbreviate;
	}

	@Override
	public String getCsv(boolean withLabel, CompressionLevel compressionLevel) {
		return resultMap.getList(withLabel, CompressionLevel.ZERO);  // the extracted fields should never be compressed!
	}

	@Override
	public List<String> getHeader() {
		List<String> headers = new ArrayList<>();
		for (String field : schema.getExtractableFields().keySet()) {
			headers.add(field);
		}
		return headers;
	}
}
