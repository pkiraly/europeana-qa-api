# Europeana Metadata Quality Assurance API

The common Java API for all Europeana metadata quality assurance related projects. This API is based on the general [Metadata Quality Assurance API project](https://github.com/pkiraly/metadata-qa-api/). All the Java classes of this project are extend or implement a type in the API.

## Usage

The central class (and usually this is the only one you have to use) is the EdmCalculatorFacade (in the de.gwdg.europeanaqa.api.calculator namespace). You have to do two things:

1. configure the object - i.e. deciding which metrics you want to run on the records
2. run the meausements
3. retrieve the results

### 1. Configuration

The result will contain a bunch of information depending on the configuration. The information blocks are:

1. Extracted fields from the JSON record: identifier, dataset and data provider
2. Completeness
    1. Existence of fields (if a field exists it gets 1, otherwise 0)
    2. Cardinality of fields (how many times a field exists in a record)
4. Uniqueness values of (at the time of writing) three fields: dc:title, dcterms:alterative, dc:description. Each values appear in two flavors: an aggregated one and an average
5. Problem catalog. You can check existence of known metadata problems in a record. The score gives you the number of times they appear in the record. Right now there are 3 problems defined: „long subject”, „ientical title and description”, and „empty strings”.
6. Encountering languages. It gives you a different return value - the codes used in the language attribute of a string value. Since it requires a different post-processing method it might worth to run in a separate batch process.
 
The parameters you can set:

1. `runFieldExistence(boolean)` configure to run the existence measurement (see #2.i in the above list)
2. `runFieldCardinality(boolean)` configure to run the cardinality measurement (see #2.ii in the above list)
3. `runCompleteness(boolean)` configure to run the completeness measurement (see #2 in the above list)
4. `runTfIdf(boolean)` configure to run the uniqueness measurement (see #4 in the above list)
5. `runProblemCatalog(boolean)` configure to run the problem catalog measurement (see #5 in the above list)
6. `runLanguage(boolean)` configure to run the language measurement (see #6 in the above list)

Other options:

* `verbose(boolean)` The completeness calculation will collect empty, existent and missing fields
* `abbreviate(boolean)` The field extractor will use a predefined dictionary to translate dataset and data provider names to numbers (which makes the output CSV smaller)
* `collectTfIdfTerms(boolean)` If it is set, the measurement will collect each individual terms with their Term Ferquency and Invers Document Frequency scores.

When you set the values, you have to issue

     calculator.configure();

to make everything prepared.

### 2. Run measurement

To run the measurement you have to call `measure(String)` method with the JSON string as the parameter. It parses JSON and if it finds invalid throws `com.jayway.jsonpath.InvalidJsonException` error. The method return the result as CSV (Comma Separated Values) string.

```java
try {
    String csv = calculator.measure(jsonRecord);
} catch (InvalidJsonException e) {
    // handle exception
}
```

### 3. Retrieve the results

The `measure()` method already returns a CSV string, but you might want more.

* `Map<String, Double> getResults()` returns the raw scores in a Map
* `List<String> getExistingFields()` returns the list of existing fields
* `List<String> getEmptyFields()` returns the list of empty fields
* `List<String> getMissingFields()` returns the list of missing fields
* `Map<String, List<TfIdf>> getTermsCollection()` returns the TF-IDF term list

## Examples

### Most general usage: measuring scores

The first step the class provides a number of configuration options.

```java
import de.gwdg.europeanaqa.api.calculator.EdmCalculatorFacade;
import com.jayway.jsonpath.InvalidJsonException;

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
		metrics.add(calculator.measure(jsonRecord));
	} catch (InvalidJsonException e) {
		logger.severe(String.format("Invalid JSON in %s: %s. Error message: %s.",
			inputFileName, jsonRecord, e.getLocalizedMessage()));
	}
}
```

