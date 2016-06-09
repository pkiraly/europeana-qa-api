# Europeana Metadata Quality Assurance API

The common Java API for all Europeana metadata quality assurance related projects. This API is based on the general [Metadata Quality Assurance API project](https://github.com/pkiraly/metadata-qa-api/). All the Java classes of this project are extend or implement a type in the API.

## Usage

The central class (and usually this is the only one you have to use) is the EdmCalculatorFacade (in the de.gwdg.europeanaqa.api.calculator namespace). You have to do two things:

1. configure the object - i.e. deciding which metrics you want to run on the records
2. run the meausements, and save the result

The result will contain a bunch of information depending on the configuration. The information blocks are:

1. Extracted fields from the JSON record: identifier, dataset and data provider
2. Existence of fields (if a field exists it gets 1, otherwise 0)
3. Cardinality of fields (how many times a field exists in a record)
4. Uniqueness values of (at the time of writing) three fields: dc:title, dcterms:alterative, dc:description. Each values appear in two flavors: an aggregated one and an average
5. Problem catalog. You can check existence of known metadata problems in a record. The score gives you the number of times they appear in the record. Right now there are 3 problems defined: „long subject”, „ientical title and description”, and „empty strings”.
6. Encountering languages. It gives you a different return value - the codes used in the language attribute of a string value. Since it requires a different post-processing method it might worth to run in a separate batch process.
 
The parameters you can set:

1. `runFieldExistence(boolean)` configure to run the existence measurement (see #2 in the above list)
2. `runFieldCardinality(boolean)` configure to run the cardinality measurement (see #3 in the above list)
3. `runCompleteness(boolean)`
4. `runTfIdf(boolean)` configure to run the uniqueness measurement (see #4 in the above list)
5. `runProblemCatalog(boolean)` configure to run the problem catalog measurement (see #5 in the above list)
6. `runLanguage(boolean)` configure to run the language measurement (see #6 in the above list)

For the first step the class provides a number of configuration options.

```java
import de.gwdg.europeanaqa.api.calculator.EdmCalculatorFacade;

...

EdmCalculatorFacade calculator = new EdmCalculatorFacade();
calculator.doAbbreviate(true);
calculator.runCompleteness(true);
calculator.runFieldCardinality(true);
calculator.runFieldExistence(true);
calculator.runTfIdf(false);
calculator.runProblemCatalog(true);
calculator.configure();

List<String> jsonRecords = ... // read JSON records from file/database
List<String> metrics = new ArrayList<>();
for (String jsonRecord : jsonRecords) {
	try {
		metrics.add(calculator.measure(jsonString));
	} catch (InvalidJsonException e) {
		logger.severe(String.format("Invalid JSON in %s: %s. Error message: %s.",
			inputFileName, jsonString, e.getLocalizedMessage()));
	}
}
```

