package com.nsdr.europeanaqa.api.calculator;

import com.nsdr.europeanaqa.api.abbreviation.DataProviderManager;
import com.nsdr.europeanaqa.api.abbreviation.DatasetManager;
import com.nsdr.europeanaqa.api.counter.Counters;
import com.nsdr.europeanaqa.api.interfaces.Calculator;
import com.nsdr.europeanaqa.api.problemcatalog.EmptyStrings;
import com.nsdr.europeanaqa.api.problemcatalog.LongSubject;
import com.nsdr.europeanaqa.api.problemcatalog.ProblemCatalog;
import com.nsdr.europeanaqa.api.problemcatalog.TitleAndDescriptionAreSame;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class CalculatorFacade {

	private final boolean doFieldExistence;
	private final boolean doFieldCardinality;
	private final boolean doCompleteness;
	private final boolean doTfIdf;
	private final boolean doProblemCatalog;
	private boolean doAbbreviate = false;
	private List<Calculator> calculators;
	CompletenessCalculator completenessCalculator;
	TfIdfCalculator tfidfCalculator;
	ProblemCatalog problemCatalog;

	public CalculatorFacade(boolean doFieldExistence, boolean doFieldCardinality, 
			  boolean doCompleteness, boolean doTfIdf, boolean doProblemCatalog) {
		this.doFieldExistence = doFieldExistence;
		this.doFieldCardinality = doFieldCardinality;
		this.doCompleteness = doCompleteness;
		this.doTfIdf = doTfIdf;
		this.doProblemCatalog = doProblemCatalog;
		setupCalculators();
	}

	private void setupCalculators() {
		calculators = new ArrayList<>();
		if (doCompleteness) {
			completenessCalculator = new CompletenessCalculator();
			calculators.add(completenessCalculator);
			if (doAbbreviate) {
				completenessCalculator.setDataProviderManager(new DataProviderManager());
				completenessCalculator.setDatasetManager(new DatasetManager());
			}
		}

		if (doTfIdf) {
			tfidfCalculator = new TfIdfCalculator();
			calculators.add(tfidfCalculator);
		}

		if (doProblemCatalog) {
			problemCatalog = new ProblemCatalog();
			LongSubject longSubject = new LongSubject(problemCatalog);
			TitleAndDescriptionAreSame titleAndDescriptionAreSame = new TitleAndDescriptionAreSame(problemCatalog);
			EmptyStrings emptyStrings = new EmptyStrings(problemCatalog);
			calculators.add(problemCatalog);
		}
	}
	
	public void configureCounter(Counters counters) {
		counters.doReturnFieldExistenceList(doFieldExistence);
		counters.doReturnFieldInstanceList(doFieldCardinality);
		counters.doReturnTfIdfList(doTfIdf);
		counters.doReturnProblemList(doProblemCatalog);
	}

	public boolean isDoFieldExistence() {
		return doFieldExistence;
	}

	public boolean isDoFieldCardinality() {
		return doFieldCardinality;
	}

	public boolean isDoCompleteness() {
		return doCompleteness;
	}

	public boolean isDoTfIdf() {
		return doTfIdf;
	}

	public boolean isDoProblemCatalog() {
		return doProblemCatalog;
	}

	public List<Calculator> getCalculators() {
		return calculators;
	}

	public void doAbbreviate(boolean doAbbreviate) {
		DataProviderManager dataProviderManager = null;
		DatasetManager datasetManager = null;
		if (doAbbreviate) {
			dataProviderManager = new DataProviderManager();
			datasetManager = new DatasetManager();
		}
		completenessCalculator.setDataProviderManager(dataProviderManager);
		completenessCalculator.setDatasetManager(datasetManager);
	}

}
