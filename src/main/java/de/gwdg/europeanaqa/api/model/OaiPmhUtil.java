package de.gwdg.europeanaqa.api.model;

public class OaiPmhUtil {
  private final static String baseUrl = "https://oai.europeana.eu/oai/record?verb=GetRecord&metadataPrefix=edm&identifier=http://data.europeana.eu/item";

  public static String getRecordUrl(String id) {
    if (id.startsWith("/"))
      return baseUrl + id;
    return baseUrl + "/" + id;
  }
}
