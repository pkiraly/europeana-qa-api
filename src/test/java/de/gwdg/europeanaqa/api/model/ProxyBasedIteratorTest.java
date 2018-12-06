package de.gwdg.europeanaqa.api.model;

import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.EdmFieldInstance;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;
import de.gwdg.metadataqa.api.util.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ProxyBasedIteratorTest {

  private String jsonString;
  private Schema schema;

  @Before
  public void setUp() throws IOException, URISyntaxException {
    jsonString = FileUtils.readFirstLine("general/test.json");
  }

  @Test
  public void WhenCreateNewWithOAI_ThereAreTwoProxies() {
    schema = new EdmOaiPmhXmlSchema();
    ProxyBasedIterator iterator = new ProxyBasedIterator(schema);
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
    ProxyBasedIterator iterator = new ProxyBasedIterator(schema);
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
  public void test2() {
    schema = new EdmFullBeanSchema();
    JsonPathCache<EdmFieldInstance> cache = new JsonPathCache<>(jsonString);
    ProxyBasedIterator iterator = new ProxyBasedIterator(schema);
    iterator.measure(cache);
    assertThat("proxyBasedIterator", is(iterator.getCalculatorName()));
  }

  @Test
  public void testNullReturns() {
    schema = new EdmFullBeanSchema();
    JsonPathCache<EdmFieldInstance> cache = new JsonPathCache<>(jsonString);
    ProxyBasedIterator iterator = new ProxyBasedIterator(schema);
    iterator.measure(cache);
    assertNull(iterator.getResultMap());
    assertNull(iterator.getLabelledResultMap());
    assertNull(iterator.getCsv(true, CompressionLevel.NORMAL));
    assertNull(iterator.getHeader());
  }
}
