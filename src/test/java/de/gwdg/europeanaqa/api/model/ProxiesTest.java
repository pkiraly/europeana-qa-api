package de.gwdg.europeanaqa.api.model;

import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.EdmOaiPmhXmlSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class ProxiesTest {
	private String jsonString;
	private Schema schema;

	@Before
	public void setUp() throws IOException, URISyntaxException {
		jsonString = FileUtils.readFirstLine("general/test.json");
	}

	@Test
	public void WhenFullBeanSchema_ThereAreTwoProxies() {
		schema = new EdmFullBeanSchema();
		Proxies proxies = new Proxies(schema);

		JsonBranch providerProxyBranch = proxies.getProviderProxy();
		assertEquals("$.['proxies'][?(@['europeanaProxy'] == false)]", providerProxyBranch.getJsonPath());
		assertEquals(56, providerProxyBranch.getChildren().size());
		assertEquals("$.['proxies'][?(@['europeanaProxy'] == false)][*]['about']",
			providerProxyBranch.getChildren().get(0).getAbsoluteJsonPath());

		JsonBranch europeanaProxyBranch = proxies.getEuropeanaProxy();
		assertEquals("$.['proxies'][?(@['europeanaProxy'] == true)]", europeanaProxyBranch.getJsonPath());
		assertEquals(56, europeanaProxyBranch.getChildren().size());
		assertEquals("$.['proxies'][?(@['europeanaProxy'] == true)]",
			europeanaProxyBranch.getChildren().get(0).getParent().getJsonPath());
		assertEquals("$.['proxies'][?(@['europeanaProxy'] == true)][*]['about']",
			europeanaProxyBranch.getChildren().get(0).getAbsoluteJsonPath());
	}

	@Test
	public void WhenOAISchema_ThereAreTwoProxies() {
		schema = new EdmOaiPmhXmlSchema();
		Proxies proxies = new Proxies(schema);

		JsonBranch providerProxyBranch = proxies.getProviderProxy();
		assertEquals("$.['ore:Proxy'][?(@['edm:europeanaProxy'][0] == 'false')]", providerProxyBranch.getJsonPath());
		assertEquals(56, providerProxyBranch.getChildren().size());
		assertEquals("$.['ore:Proxy'][?(@['edm:europeanaProxy'][0] == 'false')][*]['@about']",
			providerProxyBranch.getChildren().get(0).getAbsoluteJsonPath());

		JsonBranch europeanaProxyBranch = proxies.getEuropeanaProxy();
		assertEquals("$.['ore:Proxy'][?(@['edm:europeanaProxy'][0] == 'true')]", europeanaProxyBranch.getJsonPath());
		assertEquals(56, europeanaProxyBranch.getChildren().size());
		assertEquals("$.['ore:Proxy'][?(@['edm:europeanaProxy'][0] == 'true')]",
			europeanaProxyBranch.getChildren().get(0).getParent().getJsonPath());
		assertEquals("$.['ore:Proxy'][?(@['edm:europeanaProxy'][0] == 'true')][*]['@about']",
			europeanaProxyBranch.getChildren().get(0).getAbsoluteJsonPath());
	}

}
