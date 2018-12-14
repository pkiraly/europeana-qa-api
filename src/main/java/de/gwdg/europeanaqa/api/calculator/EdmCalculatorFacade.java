package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;

import de.gwdg.europeanaqa.api.abbreviation.EdmCountryManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmLanguageManager;

import de.gwdg.europeanaqa.api.model.Format;
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
import de.gwdg.metadataqa.api.uniqueness.DefaultSolrClient;
import de.gwdg.metadataqa.api.uniqueness.SolrClient;
import org.jetbrains.annotations.NotNull;

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

  private EdmFieldExtractor fieldExtractor;
  private EdmDataProviderManager dataProviderManager;
  private EdmDatasetManager datasetManager;
  private SolrClient solrClient;
  private boolean abbreviate = false;
  private boolean disconnectedEntityMeasurementEnabled = false;
  private boolean uniquenessMeasurementEnabled = false;
  private boolean extendedFieldExtraction = false;
  private Format format = Format.OAI_PMH_XML;
  private EdmSchema schema = null;

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
                             boolean enableCompletenessMeasurement,
                             boolean enableTfIdfMeasurement,
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
    schema = createSchema();

    calculators = new ArrayList<>();
    buildEdmFieldExtractor(schema);
    calculators.add(fieldExtractor);

    if (completenessMeasurementEnabled
        || fieldExistenceMeasurementEnabled
        || fieldCardinalityMeasurementEnabled) {
      buildCompletenessCalculator(schema);
      calculators.add(completenessCalculator);
    }

    if (tfIdfMeasurementEnabled) {
      buildTfIdfCalculator(schema);
      calculators.add(tfidfCalculator);
    }

    if (problemCatalogMeasurementEnabled) {
      calculators.add(buildProblemCatalog(schema));
    }

    if (languageMeasurementEnabled) {
      calculators.add(new LanguageCalculator(schema));
    }

    if (multilingualSaturationMeasurementEnabled) {
      buildMultilingualSaturationCalculator(schema);
      calculators.add(multilingualSaturationCalculator);
    }

    if (disconnectedEntityMeasurementEnabled) {
      calculators.add(new DisconnectedEntityCalculator(schema));
    }

    if (uniquenessMeasurementEnabled) {
      if (solrClient == null && solrConfiguration == null) {
        throw new IllegalArgumentException(
          "If Uniqueness measurement is enabled, Solr configuration should not be null."
        );
      }
      if (solrClient == null) {
        solrClient = new DefaultSolrClient(solrConfiguration);
      }
      calculators.add(new UniquenessCalculator(solrClient, schema));
    }
  }

  private void buildEdmFieldExtractor(EdmSchema schema) {
    fieldExtractor = new EdmFieldExtractor(schema);
    fieldExtractor.abbreviate(abbreviate);
    if (extendedFieldExtraction) {
      extendsFieldExtraction(schema);
    }
    if (abbreviate) {
      this.dataProviderManager = new EdmDataProviderManager();
      this.datasetManager = new EdmDatasetManager();
      fieldExtractor.setDataProviderManager(dataProviderManager);
      fieldExtractor.setDatasetManager(datasetManager);
    }
  }

  private void buildTfIdfCalculator(EdmSchema schema) {
    tfidfCalculator = new TfIdfCalculator(schema);
    tfidfCalculator.enableTermCollection(collectTfIdfTerms);
  }

  private void buildMultilingualSaturationCalculator(EdmSchema schema) {
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
  }

  @NotNull
  private ProblemCatalog buildProblemCatalog(EdmSchema schema) {
    ProblemCatalog problemCatalog = new ProblemCatalog(schema);
    LongSubject longSubject = new LongSubject(problemCatalog);
    TitleAndDescriptionAreSame titleAndDescriptionAreSame =
      new TitleAndDescriptionAreSame(problemCatalog);
    EmptyStrings emptyStrings = new EmptyStrings(problemCatalog);
    return problemCatalog;
  }

  private void buildCompletenessCalculator(EdmSchema schema) {
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
  }

  private void extendsFieldExtraction(EdmSchema schema) {
    int index = (format == Format.FULLBEAN)
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
  public void saveDataProviders(String fileName)
      throws FileNotFoundException, UnsupportedEncodingException {
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
  public void saveDatasets(String fileName)
      throws FileNotFoundException, UnsupportedEncodingException {
    if (datasetManager != null) {
      datasetManager.save(fileName);
    }
  }

  public Format getFormat() {
    return format;
  }

  public void setFormat(Format format) {
    this.format = format;
  }

  public boolean isExtendedFieldExtraction() {
    return extendedFieldExtraction;
  }

  public void setExtendedFieldExtraction(boolean extendedFieldExtraction) {
    this.extendedFieldExtraction = extendedFieldExtraction;
  }

  /**
   * Create a schema based on the format.
   * @return The format dependant EDM schema.
   */
  public EdmSchema createSchema() {
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

  @Override
  public Schema getSchema() {
    if (schema == null) {
      schema = createSchema();
    }
    return schema;
  }

  public void setSolrClient(SolrClient solrClient) {
    this.solrClient = solrClient;
  }
}
