# Europeana Metadata Quality Assurance API

The common Java API for all Europeana metadata quality assurance related projects. This API is based on the general (Metadata Quality Assurance API project)[https://github.com/pkiraly/metadata-qa-api/]

## Usage

```java
CompletenessCalculator completenessCalculator = new CompletenessCalculator();
completenessCalculator.setDataProviderManager(new DataProviderManager());
completenessCalculator.setDatasetManager(new DatasetManager());

TfIdfCalculator tfidfCalculator = new TfIdfCalculator();

ProblemCatalog problemCatalog = new ProblemCatalog();
new LongSubject(problemCatalog);
new TitleAndDescriptionAreSame(problemCatalog);
new EmptyStrings(problemCatalog);

boolean withLabels = false;
for (String jsonRecord : jsonRecords) {
	try {
		Counters counters = new Counters();
		// set what to measure and return
		counters.doReturnFieldExistenceList(true);
		counters.doReturnFieldInstanceList(true);
		counters.doReturnTfIdfList(false);
		counters.doReturnProblemList(true);

		// read and process JSON
		JsonPathCache cache = new JsonPathCache(json);

		// run calculations
		completenessCalculator.calculate(cache, counters);
		tfidfCalculator.calculate(jsonString, counters);
		problemCatalog.calculate(cache, counters);

		// return the result of calculations
		String csv = counters.getFullResults(withLabels);
		// store csv to somewhere
	} catch (InvalidJsonException e) {
		// log problem
	}
}
```
