package com.nsdr.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import com.nsdr.metadataqa.api.model.EdmFieldInstance;
import com.nsdr.europeanaqa.api.abbreviation.EdmDataProviderManager;
import com.nsdr.europeanaqa.api.abbreviation.EdmDatasetManager;
import com.nsdr.metadataqa.api.calculator.FieldExtractor;
import com.nsdr.metadataqa.api.counter.Counters;
import com.nsdr.metadataqa.api.model.JsonPathCache;
import java.util.List;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmFieldExtractor extends FieldExtractor {

	private static final String ID_PATH = "$.identifier";
	private static final String DATA_PROVIDER_PATH = "$.['ore:Aggregation'][0]['edm:dataProvider'][0]";
	private static final String DATASET_PATH = "$.sets[0]";

	private EdmDataProviderManager dataProviderManager;
	private EdmDatasetManager datasetsManager;

	@Override
	public void calculate(JsonPathCache cache, Counters counters) throws InvalidJsonException {
		super.calculate(cache, counters);

		List<EdmFieldInstance> providers = cache.get(DATA_PROVIDER_PATH);
		List<EdmFieldInstance> datasets = cache.get(DATASET_PATH);
		counters.setField("dataProvider", providers.get(0).getValue());
		counters.setField("dataProviderCode", getDataProviderCode(providers.get(0).getValue()));
		counters.setField("dataset", datasets.get(0).getValue());
		counters.setField("datasetCode", getDatasetCode(datasets.get(0).getValue()));
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

}
