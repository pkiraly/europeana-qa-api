package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.calculator.CompletenessCalculator;
import de.gwdg.metadataqa.api.calculator.LanguageCalculator;
import de.gwdg.metadataqa.api.calculator.TfIdfCalculator;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.problemcatalog.EmptyStrings;
import de.gwdg.metadataqa.api.problemcatalog.LongSubject;
import de.gwdg.metadataqa.api.problemcatalog.ProblemCatalog;
import de.gwdg.metadataqa.api.problemcatalog.TitleAndDescriptionAreSame;
import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.schema.EdmSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmCalculatorFacade extends CalculatorFacade {

	private static final Logger logger = Logger.getLogger(EdmCalculatorFacade.class.getCanonicalName());

	public enum Formats {

		OAI_PMH_XML("xml"),
		FULLBEAN("fullbean");

		private final String name;

		private Formats(String name) {
			this.name = name;
		}
	};

	protected EdmFieldExtractor fieldExtractor;
	private EdmDataProviderManager dataProviderManager;
	private EdmDatasetManager datasetManager;
	protected boolean abbreviate = false;
	protected Formats format = Formats.OAI_PMH_XML;

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
		EdmSchema schema = getSchema();

		calculators = new ArrayList<>();
		fieldExtractor = new EdmFieldExtractor(schema);
		fieldExtractor.abbreviate(abbreviate);
		calculators.add(fieldExtractor);

		if (abbreviate) {
			this.dataProviderManager = new EdmDataProviderManager();
			this.datasetManager = new EdmDatasetManager();
			fieldExtractor.setDataProviderManager(dataProviderManager);
			fieldExtractor.setDatasetManager(datasetManager);
		}

		if (runCompleteness || runFieldExistence || runFieldCardinality) {
			completenessCalculator = new CompletenessCalculator(schema);
			completenessCalculator.setCompleteness(runCompleteness);
			completenessCalculator.setExistence(runFieldExistence);
			completenessCalculator.setCardinality(runFieldCardinality);
			completenessCalculator.collectFields(completenessCollectFields);
			calculators.add(completenessCalculator);
		}

		if (runTfIdf) {
			tfidfCalculator = new TfIdfCalculator(schema);
			tfidfCalculator.setDoCollectTerms(collectTfIdfTerms);
			calculators.add(tfidfCalculator);
		}

		if (runProblemCatalog) {
			ProblemCatalog problemCatalog = new ProblemCatalog(schema);
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

	public Formats getFormat() {
		return format;
	}

	public void setFormat(Formats format) {
		this.format = format;
	}

	private EdmSchema getSchema() {
		EdmSchema schema;
		if (format == null) {
			schema = new EdmOaiPmhXmlSchema();
		} else {
			switch(format) {
				case FULLBEAN:
					schema = new EdmFullBeanSchema(); break;
				case OAI_PMH_XML:
				default:
					schema = new EdmOaiPmhXmlSchema(); break;
			}
		}
		return schema;
	}

}
