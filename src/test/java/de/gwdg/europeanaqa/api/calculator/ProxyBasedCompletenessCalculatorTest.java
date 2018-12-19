package de.gwdg.europeanaqa.api.calculator;

import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;
import de.gwdg.metadataqa.api.util.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ProxyBasedCompletenessCalculatorTest {

  private String jsonString;
  private Schema schema;

  @Before
  public void setUp() throws IOException, URISyntaxException {
    jsonString = FileUtils.readFirstLine("general/test.json");
  }

  @Test
  public void WhenCreateNewWithOAI_ThereAreTwoProxies() {
    schema = new EdmOaiPmhXmlSchema();
    ProxyBasedCompletenessCalculator iterator = new ProxyBasedCompletenessCalculator(schema);
    assertNotNull(iterator.getProxies());
    assertNotNull(iterator.getProxies().getProviderProxy());
    assertNotNull(iterator.getProxies().getEuropeanaProxy());

    JsonBranch providerProxyBranch = iterator.getProxies().getProviderProxy();
    assertEquals(
      "$.['ore:Proxy'][?(@['edm:europeanaProxy'][0] == 'false')]",
      providerProxyBranch.getJsonPath()
    );
    assertEquals(56, providerProxyBranch.getChildren().size());
    assertEquals(
      "$.['ore:Proxy'][?(@['edm:europeanaProxy'][0] == 'false')][*]['@about']",
      providerProxyBranch.getChildren().get(0).getAbsoluteJsonPath()
    );

    JsonBranch europeanaProxyBranch = iterator.getProxies().getEuropeanaProxy();
    assertEquals(
      "$.['ore:Proxy'][?(@['edm:europeanaProxy'][0] == 'true')]",
      europeanaProxyBranch.getJsonPath()
    );
    assertEquals(56, europeanaProxyBranch.getChildren().size());
    assertEquals(
      "$.['ore:Proxy'][?(@['edm:europeanaProxy'][0] == 'true')]",
      europeanaProxyBranch.getChildren().get(0).getParent().getJsonPath()
    );
    assertEquals(
      "$.['ore:Proxy'][?(@['edm:europeanaProxy'][0] == 'true')][*]['@about']",
      europeanaProxyBranch.getChildren().get(0).getAbsoluteJsonPath()
    );
  }

  @Test
  public void WhenCreateNewWithFullBean_ThereAreTwoProxies() {
    schema = new EdmFullBeanSchema();
    ProxyBasedCompletenessCalculator iterator = new ProxyBasedCompletenessCalculator(schema);
    assertNotNull(iterator.getProxies());
    assertNotNull(iterator.getProxies().getProviderProxy());
    assertNotNull(iterator.getProxies().getEuropeanaProxy());

    JsonBranch providerProxyBranch = iterator.getProxies().getProviderProxy();
    assertEquals(
      "$.['proxies'][?(@['europeanaProxy'] == false)]",
      providerProxyBranch.getJsonPath());
    assertEquals(56, providerProxyBranch.getChildren().size());
    assertEquals(
      "$.['proxies'][?(@['europeanaProxy'] == false)][*]['about']",
      providerProxyBranch.getChildren().get(0).getAbsoluteJsonPath()
    );

    JsonBranch europeanaProxyBranch = iterator.getProxies().getEuropeanaProxy();
    assertEquals(
      "$.['proxies'][?(@['europeanaProxy'] == true)]",
      europeanaProxyBranch.getJsonPath());
    assertEquals(56, europeanaProxyBranch.getChildren().size());
    assertEquals(
      "$.['proxies'][?(@['europeanaProxy'] == true)]",
      europeanaProxyBranch.getChildren().get(0).getParent().getJsonPath()
    );
    assertEquals(
      "$.['proxies'][?(@['europeanaProxy'] == true)][*]['about']",
      europeanaProxyBranch.getChildren().get(0).getAbsoluteJsonPath()
    );
  }

  @Test
  public void testMeasure() {
    schema = new EdmOaiPmhXmlSchema();
    JsonPathCache<EdmFieldInstance> cache = new JsonPathCache<>(jsonString);
    ProxyBasedCompletenessCalculator iterator = new ProxyBasedCompletenessCalculator(schema);
    iterator.measure(cache);

    assertThat("proxyBasedCompletenessCalculator", is(iterator.getCalculatorName()));

    Map<String, Integer> results = (Map<String, Integer>) iterator.getResultMap();
    assertEquals(224, results.size());

    assertEquals(1, (int) results.get("PROVIDER:Proxy/rdf:about"));
    results.remove("PROVIDER:Proxy/rdf:about");
    assertEquals(1, (int) results.get("PROVIDER:Proxy/dc:title"));
    results.remove("PROVIDER:Proxy/dc:title");
    assertEquals(1, (int) results.get("PROVIDER:Proxy/dc:type"));
    results.remove("PROVIDER:Proxy/dc:type");
    assertEquals(1, (int) results.get("PROVIDER:Proxy/dc:identifier"));
    results.remove("PROVIDER:Proxy/dc:identifier");
    assertEquals(5, (int) results.get("PROVIDER:Proxy/dc:subject"));
    results.remove("PROVIDER:Proxy/dc:subject");
    assertEquals(1, (int) results.get("PROVIDER:Proxy/dcterms:isPartOf"));
    results.remove("PROVIDER:Proxy/dcterms:isPartOf");
    assertEquals(1, (int) results.get("PROVIDER:Proxy/dc:rights"));
    results.remove("PROVIDER:Proxy/dc:rights");
    assertEquals(1, (int) results.get("PROVIDER:Proxy/edm:type"));
    results.remove("PROVIDER:Proxy/edm:type");
    assertEquals(1, (int) results.get("PROVIDER:Proxy/edm:europeanaProxy"));
    results.remove("PROVIDER:Proxy/edm:europeanaProxy");
    assertEquals(1, (int) results.get("PROVIDER:Proxy/ore:proxyIn"));
    results.remove("PROVIDER:Proxy/ore:proxyIn");
    assertEquals(1, (int) results.get("PROVIDER:Proxy/ore:proxyFor"));
    results.remove("PROVIDER:Proxy/ore:proxyFor");
    assertEquals(1, (int) results.get("PROVIDER:Proxy/dcterms:hasFormat"));
    results.remove("PROVIDER:Proxy/dcterms:hasFormat");
    assertEquals(1, (int) results.get("EUROPEANA:Proxy/rdf:about"));
    results.remove("EUROPEANA:Proxy/rdf:about");
    assertEquals(1, (int) results.get("EUROPEANA:Proxy/dc:subject"));
    results.remove("EUROPEANA:Proxy/dc:subject");
    assertEquals(1, (int) results.get("EUROPEANA:Proxy/edm:type"));
    results.remove("EUROPEANA:Proxy/edm:type");
    assertEquals(1, (int) results.get("EUROPEANA:Proxy/edm:europeanaProxy"));
    results.remove("EUROPEANA:Proxy/edm:europeanaProxy");
    assertEquals(1, (int) results.get("EUROPEANA:Proxy/ore:proxyIn"));
    results.remove("EUROPEANA:Proxy/ore:proxyIn");
    assertEquals(1, (int) results.get("EUROPEANA:Proxy/ore:proxyFor"));
    results.remove("EUROPEANA:Proxy/ore:proxyFor");
    assertEquals(1, (int) results.get("EUROPEANA:Concept/rdf:about"));
    results.remove("EUROPEANA:Concept/rdf:about");
    assertEquals(12, (int) results.get("EUROPEANA:Concept/skos:prefLabel"));
    results.remove("EUROPEANA:Concept/skos:prefLabel");

    assertEquals(204, results.size());
    for (String key : results.keySet()) {
      assertEquals(0, (int) results.get(key));
    }
  }

  @Test
  public void testLabelledResult() {
    schema = new EdmOaiPmhXmlSchema();
    JsonPathCache<EdmFieldInstance> cache = new JsonPathCache<>(jsonString);
    ProxyBasedCompletenessCalculator iterator = new ProxyBasedCompletenessCalculator(schema);
    iterator.measure(cache);

    assertEquals(1, iterator.getLabelledResultMap().size());
    assertEquals(224, iterator.getLabelledResultMap().get("proxyBasedCompletenessCalculator").size());
    assertEquals(
      "1,1,0,0,0,0,0,1,1,0,0,0,0,5,0,0,0,0,0,0,0,1,0,0,1,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0," +
      "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0," +
      "0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0," +
      "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,12,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0," +
      "0,0,0,0,0,0",
      iterator.getCsv(false, CompressionLevel.NORMAL)
    );
  }

  @Test
  public void testHeader() {
    schema = new EdmOaiPmhXmlSchema();
    ProxyBasedCompletenessCalculator iterator = new ProxyBasedCompletenessCalculator(schema);
    List<String> header = iterator.getHeader();
    assertEquals(
      "PROVIDER:Proxy/rdf:about, PROVIDER:Proxy/dc:title, PROVIDER:Proxy/dcterms:alternative, PROVIDER:Proxy/dc:description, " +
      "PROVIDER:Proxy/dc:creator, PROVIDER:Proxy/dc:publisher, PROVIDER:Proxy/dc:contributor, PROVIDER:Proxy/dc:type, " +
      "PROVIDER:Proxy/dc:identifier, PROVIDER:Proxy/dc:language, PROVIDER:Proxy/dc:coverage, " +
      "PROVIDER:Proxy/dcterms:temporal, PROVIDER:Proxy/dcterms:spatial, PROVIDER:Proxy/dc:subject, PROVIDER:Proxy/dc:date, " +
      "PROVIDER:Proxy/dcterms:created, PROVIDER:Proxy/dcterms:issued, PROVIDER:Proxy/dcterms:extent, " +
      "PROVIDER:Proxy/dcterms:medium, PROVIDER:Proxy/dcterms:provenance, PROVIDER:Proxy/dcterms:hasPart, " +
      "PROVIDER:Proxy/dcterms:isPartOf, PROVIDER:Proxy/dc:format, PROVIDER:Proxy/dc:source, PROVIDER:Proxy/dc:rights, " +
      "PROVIDER:Proxy/dc:relation, PROVIDER:Proxy/edm:isNextInSequence, PROVIDER:Proxy/edm:type, " +
      "PROVIDER:Proxy/edm:europeanaProxy, PROVIDER:Proxy/edm:year, PROVIDER:Proxy/edm:userTag, PROVIDER:Proxy/ore:proxyIn, " +
      "PROVIDER:Proxy/ore:proxyFor, PROVIDER:Proxy/dcterms:conformsTo, PROVIDER:Proxy/dcterms:hasFormat, " +
      "PROVIDER:Proxy/dcterms:hasVersion, PROVIDER:Proxy/dcterms:isFormatOf, PROVIDER:Proxy/dcterms:isReferencedBy, " +
      "PROVIDER:Proxy/dcterms:isReplacedBy, PROVIDER:Proxy/dcterms:isRequiredBy, PROVIDER:Proxy/dcterms:isVersionOf, " +
      "PROVIDER:Proxy/dcterms:references, PROVIDER:Proxy/dcterms:replaces, PROVIDER:Proxy/dcterms:requires, " +
      "PROVIDER:Proxy/dcterms:tableOfContents, PROVIDER:Proxy/edm:currentLocation, PROVIDER:Proxy/edm:hasMet, " +
      "PROVIDER:Proxy/edm:hasType, PROVIDER:Proxy/edm:incorporates, PROVIDER:Proxy/edm:isDerivativeOf, " +
      "PROVIDER:Proxy/edm:isRelatedTo, PROVIDER:Proxy/edm:isRepresentationOf, PROVIDER:Proxy/edm:isSimilarTo, " +
      "PROVIDER:Proxy/edm:isSuccessorOf, PROVIDER:Proxy/edm:realizes, PROVIDER:Proxy/edm:wasPresentAt, " +
      "PROVIDER:Agent/rdf:about, PROVIDER:Agent/edm:begin, PROVIDER:Agent/edm:end, PROVIDER:Agent/edm:hasMet, " +
      "PROVIDER:Agent/edm:isRelatedTo, PROVIDER:Agent/owl:sameAs, PROVIDER:Agent/foaf:name, PROVIDER:Agent/dc:date, " +
      "PROVIDER:Agent/dc:identifier, PROVIDER:Agent/rdaGr2:dateOfBirth, PROVIDER:Agent/rdaGr2:placeOfBirth, " +
      "PROVIDER:Agent/rdaGr2:dateOfDeath, PROVIDER:Agent/rdaGr2:placeOfDeath, PROVIDER:Agent/rdaGr2:dateOfEstablishment, " +
      "PROVIDER:Agent/rdaGr2:dateOfTermination, PROVIDER:Agent/rdaGr2:gender, PROVIDER:Agent/rdaGr2:professionOrOccupation, " +
      "PROVIDER:Agent/rdaGr2:biographicalInformation, PROVIDER:Agent/skos:prefLabel, PROVIDER:Agent/skos:altLabel, " +
      "PROVIDER:Agent/skos:note, PROVIDER:Concept/rdf:about, PROVIDER:Concept/skos:broader, PROVIDER:Concept/skos:narrower, " +
      "PROVIDER:Concept/skos:related, PROVIDER:Concept/skos:broadMatch, PROVIDER:Concept/skos:narrowMatch, " +
      "PROVIDER:Concept/skos:relatedMatch, PROVIDER:Concept/skos:exactMatch, PROVIDER:Concept/skos:closeMatch, " +
      "PROVIDER:Concept/skos:notation, PROVIDER:Concept/skos:inScheme, PROVIDER:Concept/skos:prefLabel, " +
      "PROVIDER:Concept/skos:altLabel, PROVIDER:Concept/skos:note, PROVIDER:Place/rdf:about, PROVIDER:Place/wgs84:lat, " +
      "PROVIDER:Place/wgs84:long, PROVIDER:Place/wgs84:alt, PROVIDER:Place/dcterms:isPartOf, PROVIDER:Place/wgs84_pos:lat_long, " +
      "PROVIDER:Place/dcterms:hasPart, PROVIDER:Place/owl:sameAs, PROVIDER:Place/skos:prefLabel, PROVIDER:Place/skos:altLabel, " +
      "PROVIDER:Place/skos:note, PROVIDER:Timespan/rdf:about, PROVIDER:Timespan/edm:begin, PROVIDER:Timespan/edm:end, " +
      "PROVIDER:Timespan/dcterms:isPartOf, PROVIDER:Timespan/dcterms:hasPart, PROVIDER:Timespan/edm:isNextInSequence, " +
      "PROVIDER:Timespan/owl:sameAs, PROVIDER:Timespan/skos:prefLabel, PROVIDER:Timespan/skos:altLabel, " +
      "PROVIDER:Timespan/skos:note, EUROPEANA:Proxy/rdf:about, EUROPEANA:Proxy/dc:title, EUROPEANA:Proxy/dcterms:alternative, " +
      "EUROPEANA:Proxy/dc:description, EUROPEANA:Proxy/dc:creator, EUROPEANA:Proxy/dc:publisher, EUROPEANA:Proxy/dc:contributor, " +
      "EUROPEANA:Proxy/dc:type, EUROPEANA:Proxy/dc:identifier, EUROPEANA:Proxy/dc:language, EUROPEANA:Proxy/dc:coverage, " +
      "EUROPEANA:Proxy/dcterms:temporal, EUROPEANA:Proxy/dcterms:spatial, EUROPEANA:Proxy/dc:subject, EUROPEANA:Proxy/dc:date, " +
      "EUROPEANA:Proxy/dcterms:created, EUROPEANA:Proxy/dcterms:issued, EUROPEANA:Proxy/dcterms:extent, " +
      "EUROPEANA:Proxy/dcterms:medium, EUROPEANA:Proxy/dcterms:provenance, EUROPEANA:Proxy/dcterms:hasPart, " +
      "EUROPEANA:Proxy/dcterms:isPartOf, EUROPEANA:Proxy/dc:format, EUROPEANA:Proxy/dc:source, EUROPEANA:Proxy/dc:rights, " +
      "EUROPEANA:Proxy/dc:relation, EUROPEANA:Proxy/edm:isNextInSequence, EUROPEANA:Proxy/edm:type, " +
      "EUROPEANA:Proxy/edm:europeanaProxy, EUROPEANA:Proxy/edm:year, EUROPEANA:Proxy/edm:userTag, " +
      "EUROPEANA:Proxy/ore:proxyIn, EUROPEANA:Proxy/ore:proxyFor, EUROPEANA:Proxy/dcterms:conformsTo, " +
      "EUROPEANA:Proxy/dcterms:hasFormat, EUROPEANA:Proxy/dcterms:hasVersion, EUROPEANA:Proxy/dcterms:isFormatOf, " +
      "EUROPEANA:Proxy/dcterms:isReferencedBy, EUROPEANA:Proxy/dcterms:isReplacedBy, EUROPEANA:Proxy/dcterms:isRequiredBy, " +
      "EUROPEANA:Proxy/dcterms:isVersionOf, EUROPEANA:Proxy/dcterms:references, EUROPEANA:Proxy/dcterms:replaces, " +
      "EUROPEANA:Proxy/dcterms:requires, EUROPEANA:Proxy/dcterms:tableOfContents, EUROPEANA:Proxy/edm:currentLocation, " +
      "EUROPEANA:Proxy/edm:hasMet, EUROPEANA:Proxy/edm:hasType, EUROPEANA:Proxy/edm:incorporates, " +
      "EUROPEANA:Proxy/edm:isDerivativeOf, EUROPEANA:Proxy/edm:isRelatedTo, EUROPEANA:Proxy/edm:isRepresentationOf, " +
      "EUROPEANA:Proxy/edm:isSimilarTo, EUROPEANA:Proxy/edm:isSuccessorOf, EUROPEANA:Proxy/edm:realizes, " +
      "EUROPEANA:Proxy/edm:wasPresentAt, EUROPEANA:Agent/rdf:about, EUROPEANA:Agent/edm:begin, EUROPEANA:Agent/edm:end, " +
      "EUROPEANA:Agent/edm:hasMet, EUROPEANA:Agent/edm:isRelatedTo, EUROPEANA:Agent/owl:sameAs, EUROPEANA:Agent/foaf:name, " +
      "EUROPEANA:Agent/dc:date, EUROPEANA:Agent/dc:identifier, EUROPEANA:Agent/rdaGr2:dateOfBirth, " +
      "EUROPEANA:Agent/rdaGr2:placeOfBirth, EUROPEANA:Agent/rdaGr2:dateOfDeath, EUROPEANA:Agent/rdaGr2:placeOfDeath, " +
      "EUROPEANA:Agent/rdaGr2:dateOfEstablishment, EUROPEANA:Agent/rdaGr2:dateOfTermination, EUROPEANA:Agent/rdaGr2:gender, " +
      "EUROPEANA:Agent/rdaGr2:professionOrOccupation, EUROPEANA:Agent/rdaGr2:biographicalInformation, " +
      "EUROPEANA:Agent/skos:prefLabel, EUROPEANA:Agent/skos:altLabel, EUROPEANA:Agent/skos:note, " +
      "EUROPEANA:Concept/rdf:about, EUROPEANA:Concept/skos:broader, EUROPEANA:Concept/skos:narrower, " +
      "EUROPEANA:Concept/skos:related, EUROPEANA:Concept/skos:broadMatch, EUROPEANA:Concept/skos:narrowMatch, " +
      "EUROPEANA:Concept/skos:relatedMatch, EUROPEANA:Concept/skos:exactMatch, EUROPEANA:Concept/skos:closeMatch, " +
      "EUROPEANA:Concept/skos:notation, EUROPEANA:Concept/skos:inScheme, EUROPEANA:Concept/skos:prefLabel, " +
      "EUROPEANA:Concept/skos:altLabel, EUROPEANA:Concept/skos:note, EUROPEANA:Place/rdf:about, EUROPEANA:Place/wgs84:lat, " +
      "EUROPEANA:Place/wgs84:long, EUROPEANA:Place/wgs84:alt, EUROPEANA:Place/dcterms:isPartOf, " +
      "EUROPEANA:Place/wgs84_pos:lat_long, EUROPEANA:Place/dcterms:hasPart, EUROPEANA:Place/owl:sameAs, " +
      "EUROPEANA:Place/skos:prefLabel, EUROPEANA:Place/skos:altLabel, EUROPEANA:Place/skos:note, EUROPEANA:Timespan/rdf:about, " +
      "EUROPEANA:Timespan/edm:begin, EUROPEANA:Timespan/edm:end, EUROPEANA:Timespan/dcterms:isPartOf, " +
      "EUROPEANA:Timespan/dcterms:hasPart, EUROPEANA:Timespan/edm:isNextInSequence, EUROPEANA:Timespan/owl:sameAs, " +
      "EUROPEANA:Timespan/skos:prefLabel, EUROPEANA:Timespan/skos:altLabel, EUROPEANA:Timespan/skos:note",
      StringUtils.join(header, ", ")
    );
  }

}
