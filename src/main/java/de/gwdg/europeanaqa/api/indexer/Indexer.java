package de.gwdg.europeanaqa.api.indexer;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JsonProvider;
import de.gwdg.europeanaqa.api.abbreviation.EdmCountryManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDataProviderManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmDatasetManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmLanguageManager;
import de.gwdg.europeanaqa.api.abbreviation.EdmProviderManager;
import de.gwdg.metadataqa.api.abbreviation.AbbreviationManager;
import de.gwdg.metadataqa.api.schema.Schema;
import net.minidev.json.JSONArray;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Indexer {

  private HttpSolrClient server;
  private Schema schema;
  private String solrUrl;

  private static final String DEFAULT_SOLR_URL = "http://localhost:8983/solr/europeana";
  private static final JsonProvider jsonProvider = Configuration.defaultConfiguration().jsonProvider();

  private List<FieldInfo> fields;

  public Indexer(Schema schema) {
    this.schema = schema;
    if (solrUrl == null)
      solrUrl = DEFAULT_SOLR_URL;
    server = new HttpSolrClient.Builder(solrUrl).build();
    initializeFields();
  }

  private void initializeFields() {
    fields = new ArrayList<FieldInfo>() {{
      add(new FieldInfo("id", "$.identifier"));
      add(new FieldInfo("dataProvider_i", getFirstJsonPath("Aggregation/edm:dataProvider")));
      add(new FieldInfo("provider_i", getFirstJsonPath("Aggregation/edm:provider")));
      add(new FieldInfo("collection_i", "$.sets[0]"));
      add(new FieldInfo("language_i", getFirstJsonPath("EuropeanaAggregation/edm:language")));
      add(new FieldInfo("country_i", getFirstJsonPath("EuropeanaAggregation/edm:country")));

      add(new FieldInfo(Arrays.asList("dc_title_txt", "dc_title_ss"),
        getJsonPath("Proxy/dc:title")));
      add(new FieldInfo(Arrays.asList("dc_description_txt", "dc_description_ss"),
        getJsonPath("Proxy/dc:description")));
      add(new FieldInfo(
        Arrays.asList("dcterms_alternative_txt", "dcterms_alternative_ss"),
        getJsonPath("Proxy/dcterms:alternative")));
    }};
  }

  private String getFirstJsonPath(String fieldName) {
    return getJsonPath(fieldName) + "[0]";
  }

  private String getJsonPath(String fieldName) {
    return schema.getPathByLabel(fieldName).getAbsoluteJsonPath(-1);
  }

  public SolrInputDocument processLine(String strLine) {
    Object jsonDoc = null;
    try {
      jsonDoc = jsonProvider.parse(strLine);
    } catch(InvalidJsonException e) {
      System.err.println("Invalid Json record: " + e.getLocalizedMessage());
      System.err.println(strLine);
    }

    SolrInputDocument solrDoc = new SolrInputDocument();
    for (FieldInfo field : fields) {
      try {
        Object value = JsonPath.read(jsonDoc, field.jsonPath);
        if (value != null) {
          if (field.abbreviationManager != null)
            value = field.abbreviationManager.getOrDefault((String) value, -1);
          else if (field.acceptListOfValues)
            value = jsonArrayToStringArray(value);

          if (value == null)
            continue;

          for (String solrField : field.solrFields)
            solrDoc.addField(solrField, value);
        }
      } catch (PathNotFoundException e) {
        System.err.println("PathNotFoundException: " + e.getLocalizedMessage());
      }
    }
    return solrDoc;
  }

  @Nullable
  private Object jsonArrayToStringArray(Object value) {
    List<String> valueList = new ArrayList<>();
    if (value instanceof String)
      valueList.add((String) value);
    else if (value instanceof JSONArray)
      valueList = toFlatList((JSONArray) value);
    else
      System.err.println("Unhandled type: " + value);
    value = (valueList.isEmpty())
      ? null
      : valueList.toArray();
    return value;
  }

  public String getSolrUrl() {
    return solrUrl;
  }

  private List<String> toFlatList(JSONArray array) {
    List<String> list = new ArrayList<>();
    for (Object item : array) {
      if (item instanceof String)
        list.add(item.toString());
      else if (item instanceof JSONArray)
        list.addAll(toFlatList((JSONArray) item));
      else
        System.err.println("Unhandled type: " + item.getClass());
    }
    return list;
  }

  public void setSolrUrl(String solrUrl) {
    this.solrUrl = solrUrl;
  }

  private class FieldInfo {
    List<String> solrFields;
    String jsonPath;
    AbbreviationManager abbreviationManager = null;
    boolean acceptListOfValues = false;

    public FieldInfo(List<String> solrFields, String jsonPath) {
      this.solrFields = solrFields;
      this.jsonPath = jsonPath;
      this.acceptListOfValues = true;
    }

    public FieldInfo(String solrField, String jsonPath) {
      this.solrFields = Arrays.asList(solrField);
      this.jsonPath = jsonPath;
      switch (solrField) {
        case "dataProvider_i": abbreviationManager = new EdmDataProviderManager(); break;
        case "provider_i": abbreviationManager = new EdmProviderManager(); break;
        case "collection_i": abbreviationManager = new EdmDatasetManager(); break;
        case "language_i": abbreviationManager = new EdmLanguageManager(); break;
        case "country_i": abbreviationManager = new EdmCountryManager(); break;
        default: break;
      }
    }

    public List<String> getSolrFields() {
      return solrFields;
    }

    public String getJsonPath() {
      return jsonPath;
    }

    public AbbreviationManager getAbbreviationManager() {
      return abbreviationManager;
    }
  }
}
