package de.gwdg.europeanaqa.api.calculator;

import com.jayway.jsonpath.InvalidJsonException;
import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.metadataqa.api.calculator.FieldExtractor;
import de.gwdg.metadataqa.api.model.pathcache.PathCache;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmFieldExtractor extends FieldExtractor {

  private static final Logger LOGGER = Logger.getLogger(
    EdmFieldExtractor.class.getCanonicalName());

  /**
   * Name of the calculator.
   */
  public static final String CALCULATOR_NAME = "edmFieldExtractor";

  private static final String ILLEGAL_ARGUMENT_TPL =
    "An EDM-based schema should define path for '%s' in the extractable fields.";

  private static final String DATA_PROVIDER = "dataProvider";
  private static final String DATASET = "dataset";
  private static final List<String> PATHS = Arrays.asList(
    "Aggregation/edm:dataProvider"
    // "Aggregation/edm:provider"
  );
  private static Map<String, String> pathCache = new HashMap<>();

  private EdmDataProviderManager dataProviderManager;
  private EdmDatasetManager datasetsManager;
  private Map<String, AbbreviationManager> abbreviationManagers;
  private boolean abbreviate;

  /**
   * Creates a new object.
   * @param schema The schema object
   */
  public EdmFieldExtractor(Schema schema) {
    super(schema);
    if (!schema.getExtractableFields().containsKey(DATASET)) {
      throw new IllegalArgumentException(
        String.format(ILLEGAL_ARGUMENT_TPL, DATASET)
      );
    }
    if (!schema.getExtractableFields().containsKey(DATA_PROVIDER)) {
      throw new IllegalArgumentException(
        String.format(ILLEGAL_ARGUMENT_TPL, DATA_PROVIDER)
      );
    }
    abbreviationManagers = new HashMap<>();
  }

  @Override
  public void measure(PathCache cache) throws InvalidJsonException {
    super.measure(cache);
    resultMap.put(super.FIELD_NAME,
      resultMap.get(super.FIELD_NAME).replace("http://data.europeana.eu/item", "")
    );

    String dataset  = extractValueByKey(cache, DATASET, null);
    String provider = extractValueByKey(cache, DATA_PROVIDER, null);
    if (provider == null) {
      for (String path : PATHS) {
        String jsonPath = getJsonPath(path);
        provider = extractValueByPath(cache, jsonPath, null);
        if (provider != null) {
          break;
        }
      }
    }
    if (dataset == null) {
      LOGGER.warning("Missing dataset! " + resultMap.get(super.FIELD_NAME)); // + "\n" + cache.getJsonString());
      dataset = "na";
    }
    if (provider == null) {
      // LOGGER.warning("Missing provider! " + resultMap.get(super.FIELD_NAME)); // + "\n" + cache.getJsonString());
      provider = "na";
    }
    if (abbreviate) {
      resultMap.put(DATASET, getDatasetCode(dataset));
      resultMap.put(DATA_PROVIDER, getDataProviderCode(provider));
    } else {
      resultMap.put(DATASET, dataset);
      resultMap.put(DATA_PROVIDER, provider);
    }

    for (Map.Entry<String, String> entry : schema.getExtractableFields().entrySet()) {
      String field = entry.getKey();
      if (field.equals(super.FIELD_NAME)
        || field.equals(DATASET)
        || field.equals(DATA_PROVIDER)) {
        continue;
      }
      String jsonPath = entry.getValue();
      String value = extractValueByPath(cache, jsonPath, "na");
      if (value.equals("na")) {
        LOGGER.info(String.format("no result for %s", entry.getKey()));
        // LOGGER.info(cache.getJsonString());
      }
      if (abbreviate) {
        value = abbreviationManagers.get(field).lookup(value).toString();
      }
      resultMap.put(field, value);
    }
  }

  private String getJsonPath(String path) {
    if (!pathCache.containsKey(path)) {
      JsonBranch branch = schema.getPathByLabel(path);
      pathCache.put(path, branch.getAbsoluteJsonPath(schema.getFormat()).replace("[*]", ""));
    }
    return pathCache.get(path);
  }

  private String extractValueByKey(PathCache cache, String key, String defaultValue) {
    if (!schema.getExtractableFields().containsKey(key)) {
      return defaultValue;
    }

    String jsonPath = schema.getExtractableFields().get(key);
    return extractValueByPath(cache, jsonPath, defaultValue);
  }

  private String extractValueByKey(PathCache cache, String key) {
    return extractValueByKey(cache, key, "na");
  }

  private String extractValueByPath(PathCache cache, String jsonPath) {
    return extractValueByPath(cache, jsonPath, "na");
  }

  private String extractValueByPath(PathCache cache, String jsonPath, String defaultValue) {
    List<EdmFieldInstance> instances = cache.get(jsonPath);
    String value = (instances != null && !instances.isEmpty())
      ? instances.get(0).getValue().trim()
      : defaultValue;
    return value;
  }

  @Override
  public String getIdPath() {
    return schema.getExtractableFields().get(super.FIELD_NAME);
  }

  /**
   * Gets abbreviated code.
   * @param value The value to abbreviate.
   * @param <T> An abbreviation manager.
   * @param manager An abbreviation manager.
   * @return The abbreviated code.
   */
  public <T extends AbbreviationManager> String getAbbreviatedCode(String value, T manager) {
    String code;
    if (value == null) {
      code = "0";
    } else if (manager != null) {
      code = String.valueOf(manager.lookup(value));
    } else {
      code = value;
    }
    return code;
  }

  /**
   * Gets the data provider abbreviation by its name.
   * @param dataProvider The data provider name
   * @return The abbreviation
   */
  public String getDataProviderCode(String dataProvider) {
    return getAbbreviatedCode(dataProvider, dataProviderManager);
  }

  /**
   * Gets the dataset abbreviation by its name.
   * @param dataset The dataset name
   * @return The abbreviation
   */
  public String getDatasetCode(String dataset) {
    return getAbbreviatedCode(dataset, datasetsManager);
  }

  /**
   * Sets data provider manager.
   * @param dataProviderManager data provider manager
   */
  public void setDataProviderManager(EdmDataProviderManager dataProviderManager) {
    this.dataProviderManager = dataProviderManager;
  }

  /**
   * Sets dataset manager.
   * @param datasetsManager dataset manager
   */
  public void setDatasetManager(EdmDatasetManager datasetsManager) {
    this.datasetsManager = datasetsManager;
  }

  /**
   * Adds an abbreviation manager.
   * @param field The field to use the manager for
   * @param manager The abbreviation manager
   */
  public void addAbbreviationManager(String field, AbbreviationManager manager) {
    abbreviationManagers.put(field, manager);
  }

  /**
   * Is the abbrevation feature enabled?
   * @return The abbreviation flag
   */
  public boolean abbreviate() {
    return abbreviate;
  }

  /**
   * Enable abbreviation.
   * @param abbreviate The abbreviation flag
   */
  public void abbreviate(boolean abbreviate) {
    this.abbreviate = abbreviate;
  }

  @Override
  public String getCsv(boolean withLabel, CompressionLevel compressionLevel) {
    return resultMap.getList(withLabel, CompressionLevel.ZERO);  // the extracted fields should never be compressed!
  }

  @Override
  public List<String> getHeader() {
    List<String> headers = new ArrayList<>();
    for (String field : schema.getExtractableFields().keySet()) {
      headers.add(field);
    }
    return headers;
  }
}
