package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.metadataqa.api.calculator.FieldExtractor;
import de.gwdg.metadataqa.api.counter.FieldCounter;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class MultiFieldExtractor extends FieldExtractor {

	public static final String CALCULATOR_NAME = "edmFieldExtractor";

	private final static String ILLEGAL_ARGUMENT_TPL = "An EDM-based schema should define path for '%' in the extractable fields.";

	private static final String DATA_PROVIDER = "dataProvider";
	private static final String DATASET = "dataset";

	private EdmDataProviderManager dataProviderManager;
	private EdmDatasetManager datasetsManager;
	private boolean abbreviate;
	protected FieldCounter<List<String>> resultMap;

	public MultiFieldExtractor(Schema schema) {
		super(schema);
	}

	@Override
	public void measure(JsonPathCache cache) throws InvalidJsonException {
		super.measure(cache);

		for (Map.Entry<String, String> entry : schema.getExtractableFields().entrySet()) {
			System.err.printf("%s -> %s\n", entry.getKey(), entry.getValue());
			List<EdmFieldInstance> edmValues = cache.get(entry.getValue());
			List<String> values = new ArrayList<>();
			for (EdmFieldInstance edmValue : edmValues) {
				values.add(edmValue.getValue());
			}
			resultMap.put(entry.getKey(), values);
		}
	}

	@Override
	public String getIdPath() {
		return schema.getExtractableFields().get(super.FIELD_NAME);
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
