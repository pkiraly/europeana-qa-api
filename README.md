The common Java API for all Europeana metadata quality assurance related projects.

# Usage

		Counters counters = new Counters();
		CompletenessCalculator completenessCalculator = new CompletenessCalculator();
		completenessCalculator.setDataProviderManager(new DataProviderManager());
		completenessCalculator.setDatasetManager(new DatasetManager());
		
		JsonPathCache cache = new JsonPathCache(readFirstLine("general/test.json"));
		completenessCalculator.calculate(cache, counters);
