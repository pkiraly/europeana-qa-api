package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;

import de.gwdg.europeanaqa.api.abbreviation.EdmCountryManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmLanguageManager;

import de.gwdg.metadataqa.api.calculator.CalculatorFacade;
import de.gwdg.metadataqa.api.calculator.CompletenessCalculator;
import de.gwdg.metadataqa.api.calculator.LanguageCalculator;
import de.gwdg.metadataqa.api.calculator.TfIdfCalculator;
import de.gwdg.metadataqa.api.calculator.UniquenessCalculator;

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

	private static final Logger LOGGER = Logger.getLogger(EdmCalculatorFacade.class.getCanonicalName());

	private EdmMultilingualitySaturationCalculator multilingualSaturationCalculator;

	/**
	 * Available input formats.
	 */
	public enum Formats {

		/**
		 * XML via OAI-PMH service.
		 */
		OAI_PMH_XML("xml"),
		/**
		 * FullBean JSON format via Record API and MongoDB export.
		 */
		FULLBEAN("fullbean");

		private final String name;

		Formats(String name) {
			this.name = name;
		}

		/**
		 * Gets format by format code.
		 * @param code Format code
		 * @return The format
		 */
		public static Formats byCode(String code) {
			for (Formats format : values()) {
				if (format.name.equals(code)) {
					return format;
				}
			}
			return null;
		}

	};

	private EdmFieldExtractor fieldExtractor;
	private EdmDataProviderManager dataProviderManager;
	private EdmDatasetManager datasetManager;
	private boolean abbreviate = false;
	private boolean disconnectedEntityMeasurementEnabled = false;
	private boolean uniquenessMeasurementEnabled = false;
	private boolean extendedFieldExtraction = false;
	private Formats format = Formats.OAI_PMH_XML;

	/**
	 * Creates an EdmCalculatorFacade object.
	 */
	public EdmCalculatorFacade() { }

	/**
	 * Creates an EdmCalculatorFacade object.
	 *
	 * @param enableFieldExistenceMeasurement Flag to enable field existence measurement
	 * @param enableFieldCardinalityMeasurement Flag to enable field cardinality measurement
	 * @param enableCompletenessMeasurement Flag to enable completeness measurement
	 * @param enableTfIdfMeasurement  Flag to enable TF-IDF measurement
	 * @param enableProblemCatalogMeasurement Flag to enable problem catalog measurement
	 */
	public EdmCalculatorFacade(boolean enableFieldExistenceMeasurement,
			boolean enableFieldCardinalityMeasurement,
			boolean enableCompletenessMeasurement,
			boolean enableTfIdfMeasurement,
			boolean enableProblemCatalogMeasurement) {
		super(
			enableFieldExistenceMeasurement, enableFieldCardinalityMeasurement,
			enableCompletenessMeasurement, enableTfIdfMeasurement,
			enableProblemCatalogMeasurement
		);
	}

	/**
	 * Creates an EdmCalculatorFacade object.
	 *
	 * @param enableFieldExistenceMeasurement Flag to enable field existence measurement
	 * @param enableFieldCardinalityMeasurement Flag to enable field cardinality measurement
	 * @param enableCompletenessMeasurement Flag to enable completeness measurement
	 * @param enableTfIdfMeasurement  Flag to enable TF-IDF measurement
	 * @param enableProblemCatalogMeasurement Flag to enable problem catalog measurement
	 * @param abbreviate Flag to abbreviate extracted fields
	 */
	public EdmCalculatorFacade(boolean enableFieldExistenceMeasurement,
			boolean enableFieldCardinalityMeasurement,
			boolean enableCompletenessMeasurement, boolean enableTfIdfMeasurement,
			boolean enableProblemCatalogMeasurement,
			boolean abbreviate) {
		super(
			enableFieldExistenceMeasurement, enableFieldCardinalityMeasurement,
			enableCompletenessMeasurement, enableTfIdfMeasurement,
			enableProblemCatalogMeasurement
		);
		this.abbreviate = abbreviate;
		changed();
	}

	@Override
	public void configure() {
		EdmSchema schema = (EdmSchema) getSchema();

		calculators = new ArrayList<>();
		fieldExtractor = new EdmFieldExtractor(schema);
		fieldExtractor.abbreviate(abbreviate);
		if (extendedFieldExtraction) {
			int index = (format == Formats.FULLBEAN)
				? -1
				: 0;
			schema.addExtractableField(
				"provider",
				getJsonPathForExtractor(schema, "Aggregation/edm:provider", index)
			);
			fieldExtractor.addAbbreviationManager("provider", new EdmProviderManager());
			schema.addExtractableField(
				"country",
				getJsonPathForExtractor(schema, "EuropeanaAggregation/edm:country", index)
			);
			fieldExtractor.addAbbreviationManager("country", new EdmCountryManager());
			schema.addExtractableField(
				"language",
				getJsonPathForExtractor(schema, "EuropeanaAggregation/edm:language", index)
			);
			fieldExtractor.addAbbreviationManager("language", new EdmLanguageManager());
		}

		calculators.add(fieldExtractor);

		if (abbreviate) {
			this.dataProviderManager = new EdmDataProviderManager();
			this.datasetManager = new EdmDatasetManager();
			fieldExtractor.setDataProviderManager(dataProviderManager);
			fieldExtractor.setDatasetManager(datasetManager);
		}

		if (completenessMeasurementEnabled
				|| fieldExistenceMeasurementEnabled
				|| fieldCardinalityMeasurementEnabled) {
			completenessCalculator = new CompletenessCalculator(schema);
			completenessCalculator.setCompleteness(completenessMeasurementEnabled);
			completenessCalculator.setExistence(fieldExistenceMeasurementEnabled);
			completenessCalculator.setCardinality(fieldCardinalityMeasurementEnabled);
			completenessCalculator.collectFields(completenessCollectFields);
			if (checkSkippableCollections) {
				completenessCalculator.setSkippedEntryChecker(
					new EdmSkippedEntryChecker()
				);
			}
			calculators.add(completenessCalculator);
		}

		if (tfIdfMeasurementEnabled) {
			tfidfCalculator = new TfIdfCalculator(schema);
			tfidfCalculator.setDoCollectTerms(collectTfIdfTerms);
			calculators.add(tfidfCalculator);
		}

		if (problemCatalogMeasurementEnabled) {
			ProblemCatalog problemCatalog = new ProblemCatalog(schema);
			LongSubject longSubject = new LongSubject(problemCatalog);
			TitleAndDescriptionAreSame titleAndDescriptionAreSame = new TitleAndDescriptionAreSame(problemCatalog);
			EmptyStrings emptyStrings = new EmptyStrings(problemCatalog);
			calculators.add(problemCatalog);
		}

		if (languageMeasurementEnabled) {
			languageCalculator = new LanguageCalculator(schema);
			calculators.add(languageCalculator);
		}

		if (multilingualSaturationMeasurementEnabled) {
			multilingualSaturationCalculator = new EdmMultilingualitySaturationCalculator(schema);
			/*
			if (saturationExtendedResult) {
				// multilingualSaturationCalculator.setResultType(MultilingualitySaturationCalculator.ResultTypes.EXTENDED);
			}
			*/
			if (checkSkippableCollections) {
				multilingualSaturationCalculator.setSkippedEntryChecker(
					new EdmSkippedEntryChecker()
				);
			}
			calculators.add(multilingualSaturationCalculator);
		}

		if (disconnectedEntityMeasurementEnabled) {
			DisconnectedEntityCalculator disconnectedEntityCalculator = new DisconnectedEntityCalculator(schema);
			calculators.add(disconnectedEntityCalculator);
		}

		if (uniquenessMeasurementEnabled) {
			UniquenessCalculator uniquenessCalculator = new UniquenessCalculator(schema);
			calculators.add(uniquenessCalculator);
		}
	}

	private String getJsonPathForExtractor(EdmSchema schema, String label, int index) {
		return schema.getPathByLabel(label).getAbsoluteJsonPath(index) + "[0]";
	}

	@Override
	public String measure(String jsonRecord) throws InvalidJsonException {
		return this.<EdmFieldInstance>measureWithGenerics(jsonRecord);
	}

	/**
	 * Set abbreviation for extracted fields.
	 * @param abbreviate Flag to enable abbreviation
	 */
	public void abbreviate(boolean abbreviate) {
		this.abbreviate = abbreviate;
	}

	/**
	 * Gets abbreviation setting.
	 * @return The abbreviation flag
	 */
	public boolean abbreviate() {
		return abbreviate;
	}

	/**
	 * Is disconnected entity feature enabled?
	 * @return disconnected entity flag.
	 */
	public boolean isDisconnectedEntityEnabled() {
		return disconnectedEntityMeasurementEnabled;
	}

	/**
	 * Flag to enable disconnected entity feature.
	 * @param disconnectedEntityMeasurementEnabled disconnected entity flag
	 */
	public void enableDisconnectedEntityMeasurement(boolean disconnectedEntityMeasurementEnabled) {
		this.disconnectedEntityMeasurementEnabled = disconnectedEntityMeasurementEnabled;
	}

	/**
	 * Is uniqueness measurement enabled?
	 * @return uniqueness measurement flag
	 */
	public boolean isUniquenessMeasurementEnabled() {
		return uniquenessMeasurementEnabled;
	}

	/**
	 * Flag to enable uniqueness measurement.
	 * @param uniquenessMeasurementEnabled The flag
	 */
	public void enableUniquenessMeasurementEnabled(boolean uniquenessMeasurementEnabled) {
		this.uniquenessMeasurementEnabled = uniquenessMeasurementEnabled;
	}

	/**
	 * Saves data providers to a file.
	 * @param fileName The target filename
	 * @throws FileNotFoundException If file is not found
	 * @throws UnsupportedEncodingException If encoding is unsupported
	 */
	public void saveDataProviders(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
		if (dataProviderManager != null) {
			dataProviderManager.save(fileName);
		}
	}

	/**
	 * Saves datasets to a file.
	 * @param fileName Target file name.
	 * @throws FileNotFoundException If file is not found
	 * @throws UnsupportedEncodingException If encoding is unsupported
	 */
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

	public boolean isExtendedFieldExtraction() {
		return extendedFieldExtraction;
	}

	public void setExtendedFieldExtraction(boolean extendedFieldExtraction) {
		this.extendedFieldExtraction = extendedFieldExtraction;
	}

	@Override
	public Schema getSchema() {
		EdmSchema schema;
		if (format == null) {
			schema = new EdmOaiPmhXmlSchema();
		} else {
			switch (format) {
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
