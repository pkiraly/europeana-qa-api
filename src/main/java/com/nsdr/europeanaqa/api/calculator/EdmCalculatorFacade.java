package com.nsdr.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import com.nsdr.europeanaqa.api.abbreviation.EdmDataProviderManager;
import com.nsdr.europeanaqa.api.abbreviation.EdmDatasetManager;
import com.nsdr.europeanaqa.api.problemcatalog.EmptyStrings;
import com.nsdr.europeanaqa.api.problemcatalog.LongSubject;
import com.nsdr.europeanaqa.api.problemcatalog.TitleAndDescriptionAreSame;
import com.nsdr.metadataqa.api.calculator.CalculatorFacade;
import com.nsdr.metadataqa.api.calculator.CompletenessCalculator;
import com.nsdr.metadataqa.api.calculator.TfIdfCalculator;
import com.nsdr.metadataqa.api.model.EdmFieldInstance;
import com.nsdr.metadataqa.api.problemcatalog.ProblemCatalog;
import com.nsdr.metadataqa.api.schema.EdmSchema;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmCalculatorFacade extends CalculatorFacade {

	protected EdmFieldExtractor fieldExtractor;
	protected boolean abbreviate = false;
	private EdmDataProviderManager dataProviderManager;
	private EdmDatasetManager datasetManager;

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
		calculators.add(fieldExtractor);
		if (abbreviate) {
			this.dataProviderManager = new EdmDataProviderManager();
			this.datasetManager = new EdmDatasetManager();
			fieldExtractor.setDataProviderManager(dataProviderManager);
			fieldExtractor.setDatasetManager(datasetManager);
		}

		if (runCompleteness) {
			completenessCalculator = new CompletenessCalculator(new EdmSchema());
			calculators.add(completenessCalculator);
		}

		if (runTfIdf) {
			tfidfCalculator = new TfIdfCalculator(new EdmSchema());
			calculators.add(tfidfCalculator);
		}

		if (runProblemCatalog) {
			ProblemCatalog problemCatalog = new ProblemCatalog();
			LongSubject longSubject = new LongSubject(problemCatalog);
			TitleAndDescriptionAreSame titleAndDescriptionAreSame = new TitleAndDescriptionAreSame(problemCatalog);
			EmptyStrings emptyStrings = new EmptyStrings(problemCatalog);
			calculators.add(problemCatalog);
		}
	}

	@Override
	public String measure(String jsonRecord) throws InvalidJsonException {
		return this.<EdmFieldInstance>measureWithGenerics(jsonRecord);
	}

	public void doAbbreviate(boolean abbreviate) {
		this.abbreviate = abbreviate;
	}

	public boolean doAbbreviate() {
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
