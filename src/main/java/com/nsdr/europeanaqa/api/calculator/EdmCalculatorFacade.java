package com.nsdr.europeanaqa.api.calculator;

import com.nsdr.europeanaqa.api.abbreviation.EdmDataProviderManager;
import com.nsdr.europeanaqa.api.abbreviation.EdmDatasetManager;
import com.nsdr.europeanaqa.api.problemcatalog.EmptyStrings;
import com.nsdr.europeanaqa.api.problemcatalog.LongSubject;
import com.nsdr.europeanaqa.api.problemcatalog.TitleAndDescriptionAreSame;
import com.nsdr.metadataqa.api.calculator.CalculatorFacade;
import com.nsdr.metadataqa.api.calculator.CompletenessCalculator;
import com.nsdr.metadataqa.api.calculator.TfIdfCalculator;
import com.nsdr.metadataqa.api.model.XmlFieldInstance;
import com.nsdr.metadataqa.api.problemcatalog.ProblemCatalog;
import com.nsdr.metadataqa.api.schema.EdmSchema;
import java.util.ArrayList;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmCalculatorFacade<T extends XmlFieldInstance> extends CalculatorFacade {

	EdmFieldExtractor fieldExtractor;

	public EdmCalculatorFacade(boolean doFieldExistence, boolean doFieldCardinality,
			  boolean doCompleteness, boolean doTfIdf, boolean doProblemCatalog) {
		super(doFieldExistence, doFieldCardinality, doCompleteness, doTfIdf, doProblemCatalog);
		setupCalculators();
	}

	private void setupCalculators() {
		calculators = new ArrayList<>();
		fieldExtractor = new EdmFieldExtractor();
		calculators.add(fieldExtractor);
		if (doAbbreviate) {
			fieldExtractor.setDataProviderManager(new EdmDataProviderManager());
			fieldExtractor.setDatasetManager(new EdmDatasetManager());
		}

		if (doCompleteness) {
			completenessCalculator = new CompletenessCalculator(new EdmSchema());
			calculators.add(completenessCalculator);
		}

		if (doTfIdf) {
			tfidfCalculator = new TfIdfCalculator(new EdmSchema());
			calculators.add(tfidfCalculator);
		}

		if (doProblemCatalog) {
			ProblemCatalog problemCatalog = new ProblemCatalog();
			LongSubject longSubject = new LongSubject(problemCatalog);
			TitleAndDescriptionAreSame titleAndDescriptionAreSame = new TitleAndDescriptionAreSame(problemCatalog);
			EmptyStrings emptyStrings = new EmptyStrings(problemCatalog);
			calculators.add(problemCatalog);
		}
	}

	@Override
	public void doAbbreviate(boolean doAbbreviate) {
		EdmDataProviderManager dataProviderManager = null;
		EdmDatasetManager datasetManager = null;
		if (doAbbreviate) {
			dataProviderManager = new EdmDataProviderManager();
			datasetManager = new EdmDatasetManager();
		}
		fieldExtractor.setDataProviderManager(dataProviderManager);
		fieldExtractor.setDatasetManager(datasetManager);
	}

}
