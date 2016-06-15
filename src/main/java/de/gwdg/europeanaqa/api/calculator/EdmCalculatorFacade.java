package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.europeanaqa.api.problemcatalog.EmptyStrings;
import de.gwdg.europeanaqa.api.problemcatalog.LongSubject;
import de.gwdg.europeanaqa.api.problemcatalog.TitleAndDescriptionAreSame;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.calculator.CompletenessCalculator;
import de.gwdg.metadataqa.api.calculator.LanguageCalculator;
import de.gwdg.metadataqa.api.calculator.TfIdfCalculator;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.problemcatalog.ProblemCatalog;
import de.gwdg.metadataqa.api.schema.EdmSchema;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmCalculatorFacade extends CalculatorFacade {

	protected EdmFieldExtractor fieldExtractor;
	private EdmDataProviderManager dataProviderManager;
	private EdmDatasetManager datasetManager;
	protected boolean abbreviate = false;

	public EdmCalculatorFacade() {}

	public EdmCalculatorFacade(boolean runFieldExistence, boolean runFieldCardinality,
			boolean runCompleteness, boolean runTfIdf, boolean runProblemCatalog) {
		super(runFieldExistence, runFieldCardinality, runCompleteness, runTfIdf,
			runProblemCatalog);
	}

	public EdmCalculatorFacade(boolean runFieldExistence, boolean runFieldCardinality,
			boolean runCompleteness, boolean runTfIdf, boolean runProblemCatalog, 
			boolean abbreviate) {
		super(runFieldExistence, runFieldCardinality, runCompleteness, runTfIdf, runProblemCatalog);
		this.abbreviate = abbreviate;
		changed();
	}

	@Override
	public void configure() {
		calculators = new ArrayList<>();
		fieldExtractor = new EdmFieldExtractor();
		fieldExtractor.abbreviate(abbreviate);
		calculators.add(fieldExtractor);

		EdmSchema schema = new EdmSchema();

		if (abbreviate) {
			this.dataProviderManager = new EdmDataProviderManager();
			this.datasetManager = new EdmDatasetManager();
			fieldExtractor.setDataProviderManager(dataProviderManager);
			fieldExtractor.setDatasetManager(datasetManager);
		}

		if (runCompleteness) {
			completenessCalculator = new CompletenessCalculator(schema);
			completenessCalculator.collectFields(completenessCollectFields);
			calculators.add(completenessCalculator);
		}

		if (runTfIdf) {
			tfidfCalculator = new TfIdfCalculator(schema);
			tfidfCalculator.setDoCollectTerms(collectTfIdfTerms);
			calculators.add(tfidfCalculator);
		}

		if (runProblemCatalog) {
			ProblemCatalog problemCatalog = new ProblemCatalog();
			LongSubject longSubject = new LongSubject(problemCatalog);
			TitleAndDescriptionAreSame titleAndDescriptionAreSame = new TitleAndDescriptionAreSame(problemCatalog);
			EmptyStrings emptyStrings = new EmptyStrings(problemCatalog);
			calculators.add(problemCatalog);
		}

		if (runLanguage) {
			languageCalculator = new LanguageCalculator(schema);
			calculators.add(languageCalculator);
		}
	}

	@Override
	public String measure(String jsonRecord) throws InvalidJsonException {
		return this.<EdmFieldInstance>measureWithGenerics(jsonRecord);
	}

	public void abbreviate(boolean abbreviate) {
		this.abbreviate = abbreviate;
	}

	public boolean abbreviate() {
		return abbreviate;
	}

	public void saveDataProviders(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
		if (dataProviderManager != null) {
			dataProviderManager.save(fileName);
		}
	}

	public void saveDatasets(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
		if (datasetManager != null) {
			datasetManager.save(fileName);
		}
	}
}
