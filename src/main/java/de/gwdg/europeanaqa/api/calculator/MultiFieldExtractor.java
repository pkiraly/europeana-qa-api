package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.metadataqa.api.counter.FieldCounter;
import de.gwdg.metadataqa.api.interfaces.Calculator;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class MultiFieldExtractor implements Calculator, Serializable {

	private static final Logger LOGGER = Logger.getLogger(
		MultiFieldExtractor.class.getCanonicalName());

	/**
	 * Name of the calculator.
	 */
	public static final String CALCULATOR_NAME = "edmFieldExtractor";

	private static final String ILLEGAL_ARGUMENT_TPL =
		"An EDM-based schema should define path for '%' in the extractable fields.";

	private static final String FIELD_NAME = "recordId";
	private static final String DATA_PROVIDER = "dataProvider";
	private static final String DATASET = "dataset";

	private EdmDataProviderManager dataProviderManager;
	private EdmDatasetManager datasetsManager;
	private boolean abbreviate;
	private FieldCounter<List<String>> resultMap;
	private Schema schema;

	/**
	 * Creates a new object.
	 * @param schema The schema
	 */
	public MultiFieldExtractor(Schema schema) {
		this.schema = schema;
	}

	@Override
	public void measure(JsonPathCache cache) throws InvalidJsonException {
		resultMap = new FieldCounter<>();

		for (Map.Entry<String, String> entry : schema.getExtractableFields().entrySet()) {
			String key = entry.getKey();
			String path = entry.getValue();
			List<EdmFieldInstance> edmValues = cache.get(path);
			List<String> values = new ArrayList<>();
			if (edmValues != null) {
				for (EdmFieldInstance edmValue : edmValues) {
					values.add(edmValue.getValue());
				}
			}
			resultMap.put(key, values);
		}
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

	@Override
	public String getCalculatorName() {
		return null;
	}
}
