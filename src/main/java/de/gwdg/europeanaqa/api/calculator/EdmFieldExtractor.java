package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.metadataqa.api.calculator.FieldExtractor;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;
import java.util.ArrayList;
import java.util.List;
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


	private EdmDataProviderManager dataProviderManager;
	private EdmDatasetManager datasetsManager;
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
	}

	@Override
	public void measure(JsonPathCache cache) throws InvalidJsonException {
		super.measure(cache);

		List<EdmFieldInstance> datasets = cache.get(schema.getExtractableFields().get(DATASET));
		List<EdmFieldInstance> providers = cache.get(schema.getExtractableFields().get(DATA_PROVIDER));
		if (abbreviate) {
			if (datasets == null) {
				logger.warning("Missing dataset!" + resultMap.get(super.FIELD_NAME));
				resultMap.put(DATASET, "null");
			} else {
				String dataset = datasets.get(0).getValue();
				resultMap.put(DATASET, getDatasetCode(dataset));
			}
			if (providers == null || providers.isEmpty()) {
				resultMap.put(DATA_PROVIDER, "null");
			} else {
				resultMap.put(DATA_PROVIDER, getDataProviderCode(providers.get(0).getValue()));
			}
		} else {
			resultMap.put(DATASET, datasets.get(0).getValue());
			if (providers == null || providers.isEmpty()) {
				resultMap.put(DATA_PROVIDER, "null");
			} else {
				resultMap.put(DATA_PROVIDER, providers.get(0).getValue());
			}
		}
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
