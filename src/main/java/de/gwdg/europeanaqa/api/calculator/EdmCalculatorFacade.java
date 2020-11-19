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

import de.gwdg.metadataqa.api.calculator.output.OutputCollector;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;

import de.gwdg.metadataqa.api.problemcatalog.EmptyStrings;
import de.gwdg.metadataqa.api.problemcatalog.LongSubject;
import de.gwdg.metadataqa.api.problemcatalog.ProblemCatalog;
import de.gwdg.metadataqa.api.problemcatalog.TitleAndDescriptionAreSame;

import de.gwdg.metadataqa.api.rule.RuleCatalog;
import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhJsonSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.schema.EdmSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.uniqueness.DefaultSolrClient;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
  private boolean abbreviate = false;
  private boolean disconnectedEntityMeasurementEnabled = false;
  private boolean proxyBasedCompletenessEnabled = false;
  private boolean extendedFieldExtraction = false;
  private Format format = Format.OAI_PMH_JSON;
  // private EdmSchema schema = null;

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
    conditionalConfiguration();
  }

  @Override
  public void configure() {
    if (super.schema == null) {
      super.schema = createSchema();
    }
    setSchema(schema);

    calculators = new ArrayList<>();
    buildEdmFieldExtractor((EdmSchema) super.schema);
    calculators.add(fieldExtractor);

    if (proxyBasedCompletenessEnabled) {
      calculators.add(new ProxyBasedCompletenessCalculator(schema));
    } else if (completenessMeasurementEnabled
        || fieldExistenceMeasurementEnabled
        || fieldCardinalityMeasurementEnabled) {
      buildCompletenessCalculator((EdmSchema) super.schema);
      calculators.add(completenessCalculator);
    }

    if (tfIdfMeasurementEnabled) {
      buildTfIdfCalculator((EdmSchema) super.schema);
      calculators.add(tfidfCalculator);
    }

    if (problemCatalogMeasurementEnabled) {
      calculators.add(buildProblemCatalog((EdmSchema) super.schema));
    }

    if (languageMeasurementEnabled) {
      calculators.add(new LanguageCalculator(schema));
    }

    if (multilingualSaturationMeasurementEnabled) {
      buildMultilingualSaturationCalculator((EdmSchema) super.schema);
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

    if (ruleCatalogMeasurementEnabled) {
      calculators.add(new RuleCatalog(schema));
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
        new EdmSkippedEntryChecker(schema)
      );
    }
  }

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
        new EdmSkippedEntryChecker(schema)
      );
    }
  }

  private void extendsFieldExtraction(EdmSchema schema) {
    schema.addExtractableField(
      "provider",
      getJsonPathForExtractor(schema, "Aggregation/edm:provider")
    );
    fieldExtractor.addAbbreviationManager("provider", new EdmProviderManager());

    schema.addExtractableField(
      "country",
      getJsonPathForExtractor(schema, "EuropeanaAggregation/edm:country")
    );
    fieldExtractor.addAbbreviationManager("country", new EdmCountryManager());

    schema.addExtractableField(
      "language",
      getJsonPathForExtractor(schema, "EuropeanaAggregation/edm:language")
    );
    fieldExtractor.addAbbreviationManager("language", new EdmLanguageManager());
  }

  private String getJsonPathForExtractor(EdmSchema schema, String label) {
    int index = (format == Format.FULLBEAN)
        ? -1
        : 0;
    return schema.getPathByLabel(label).getAbsoluteJsonPath(index) + "[0]";
  }

  @Override
  public String measure(String jsonRecord) throws InvalidJsonException {
    return (String) this.<EdmFieldInstance>measureWithGenerics(jsonRecord);
  }

  public List<String> measureAsList(String jsonRecord) throws InvalidJsonException {
    return (List<String>) this.<EdmFieldInstance>measureWithGenerics(
      jsonRecord, OutputCollector.TYPE.STRING_LIST);
  }

  public Map<String, Object> measureAsMap(String jsonRecord) throws InvalidJsonException {
    return (Map<String, Object>) this.<EdmFieldInstance>measureWithGenerics(
      jsonRecord, OutputCollector.TYPE.MAP);
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

  public EdmCalculatorFacade enableDisconnectedEntityMeasurement() {
    return enableDisconnectedEntityMeasurement(true);
  }

  public EdmCalculatorFacade disableDisconnectedEntityMeasurement() {
    return enableDisconnectedEntityMeasurement(false);
  }

  /**
   * Flag to enable disconnected entity feature.
   * @param disconnectedEntityMeasurementEnabled disconnected entity flag
   * @return
   */
  public EdmCalculatorFacade enableDisconnectedEntityMeasurement(boolean disconnectedEntityMeasurementEnabled) {
    this.disconnectedEntityMeasurementEnabled = disconnectedEntityMeasurementEnabled;
    return this;
  }


  /**
   * Is disconnected entity feature enabled?
   * @return disconnected entity flag.
   */
  public boolean isProxyBasedCompletenessEnabled() {
    return proxyBasedCompletenessEnabled;
  }

  public EdmCalculatorFacade enableProxyBasedCompleteness() {
    return enableProxyBasedCompleteness(true);
  }

  public EdmCalculatorFacade disableProxyBasedCompleteness() {
    return enableProxyBasedCompleteness(false);
  }

  /**
   * Flag to enable disconnected entity feature.
   * @param proxyBasedCompletenessEnabled disconnected entity flag
   * @return
   */
  public EdmCalculatorFacade enableProxyBasedCompleteness(boolean proxyBasedCompletenessEnabled) {
    this.proxyBasedCompletenessEnabled = proxyBasedCompletenessEnabled;
    return this;
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
    LOGGER.info("createSchema()");
    EdmSchema schema;
    if (format == null) {
      schema = new EdmOaiPmhJsonSchema();
    } else {
      switch (format) {
        case FULLBEAN:
          schema = new EdmFullBeanSchema(); break;
        case OAI_PMH_XML:
          schema = new EdmOaiPmhXmlSchema(); break;
        case OAI_PMH_JSON:
        default:
          schema = new EdmOaiPmhJsonSchema(); break;
      }
    }
    return schema;
  }

  @Override
  public Schema getSchema() {
    // LOGGER.info("getSchema()");
    // LOGGER.info("schema: " + schema);
    // LOGGER.info("this.schema: " + this.schema);
    // LOGGER.info("super.schema: " + super.schema);
    if (super.schema == null) {
      super.schema = createSchema();
    }
    return super.schema;
  }
}
