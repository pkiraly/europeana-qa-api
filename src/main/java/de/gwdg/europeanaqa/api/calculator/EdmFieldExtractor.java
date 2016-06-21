package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.metadataqa.api.calculator.FieldExtractor;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmFieldExtractor extends FieldExtractor {

	private static final String ID_PATH = "$.identifier";
	// private static final String ID_PATH = "$.['edm:ProvidedCHO'][0]['@about']";

	private static final String DATA_PROVIDER_PATH = "$.['ore:Aggregation'][0]['edm:dataProvider'][0]";
	private static final String DATASET_PATH = "$.sets[0]";
	private static final String DATA_PROVIDER = "dataProvider";
	private static final String DATASET = "dataset";

	private EdmDataProviderManager dataProviderManager;
	private EdmDatasetManager datasetsManager;
	private boolean abbreviate;

	@Override
	public void measure(JsonPathCache cache) throws InvalidJsonException {
		super.measure(cache);

		List<EdmFieldInstance> datasets = cache.get(DATASET_PATH);
		List<EdmFieldInstance> providers = cache.get(DATA_PROVIDER_PATH);
		if (abbreviate) {
			resultMap.put(DATASET, getDatasetCode(datasets.get(0).getValue()));
			if (providers == null || providers.size() == 0) {
				resultMap.put(DATA_PROVIDER, "null");
			} else {
				resultMap.put(DATA_PROVIDER, getDataProviderCode(providers.get(0).getValue()));
			}
		} else {
			resultMap.put(DATASET, datasets.get(0).getValue());
			if (providers == null || providers.size() == 0) {
				resultMap.put(DATA_PROVIDER, "null");
			} else {
				resultMap.put(DATA_PROVIDER, providers.get(0).getValue());
			}
		}
	}

	@Override
	public String getIdPath() {
		return ID_PATH;
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
	public String getCsv(boolean withLabel, boolean compressed) {
		return resultMap.getList(withLabel, compressed);
	}

	@Override
	public List<String> getHeader() {
		List<String> headers = new ArrayList<>();
		headers.add(super.FIELD_NAME);
		headers.add(DATASET);
		headers.add(DATA_PROVIDER);
		return headers;
	}

}
