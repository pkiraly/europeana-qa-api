The common Java API for all Europeana metadata quality assurance related projects.

# Usage

		
		Counters counters = new Counters();
		CompletenessCalculator completenessCalculator = new CompletenessCalculator();
		completenessCalculator.setDataProviderManager(new DataProviderManager());
		completenessCalculator.setDatasetManager(new DatasetManager());
		
		boolean withLabels = false;
		for (String jsonRecord : jsonRecords) {
			JsonPathCache cache = new JsonPathCache(json);
			completenessCalculator.calculate(cache, counters);
			String csv = counters.getFullResults(withLabels);
			// store csv to somewhere
		}
