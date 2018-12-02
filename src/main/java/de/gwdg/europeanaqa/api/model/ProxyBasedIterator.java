package de.gwdg.europeanaqa.api.model;

import de.gwdg.europeanaqa.api.calculator.DisconnectedEntityCalculator;
import de.gwdg.metadataqa.api.interfaces.Calculator;
import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.model.JsonPathCache;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.CompressionLevel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Proxy based iterator.
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class ProxyBasedIterator implements Calculator, Serializable {

	private static final Logger LOGGER = Logger.getLogger(
		ProxyBasedIterator.class.getCanonicalName()
	);

	private final Schema schema;
	private final List<JsonBranch> proxies;

	/**
	 * Constructs a new ProxyBasedIterator.
	 *
	 * @param schema The metadata schema.
	 */
	public ProxyBasedIterator(Schema schema) {
		this.schema = schema;

		JsonBranch providerProxy = schema.getPathByLabel("Proxy");
		JsonBranch europeanaProxy = null;
		try {
			europeanaProxy = (JsonBranch) providerProxy.clone();
			europeanaProxy.setJsonPath(
				providerProxy.getJsonPath().replace("false", "true"));
		} catch (CloneNotSupportedException ex) {
			LOGGER.severe(ex.getMessage());
		}
		proxies = Arrays.asList(providerProxy, europeanaProxy);
	}

	public List<JsonBranch> getProxies() {
		return proxies;
	}

	/**
	 * Measure the object.
	 * TODO this is non finished function.
	 * @param cache The cache object.
	 */
	@Override
	public void measure(JsonPathCache cache) {
		DisconnectedEntityCalculator calculator = new DisconnectedEntityCalculator(schema);
		Map<String, EntityType> contextualIds = calculator.getContextualIds(cache);
		/*
		for (Map.Entry<String, EntityType> entry : contextualIds.entrySet()) {
			//TODO this function is a trunk, it should write properly
		}
		*/
	}

	@Override
	public Map<String, ? extends Object> getResultMap() {
		return null;
	}

	@Override
	public Map<String, Map<String, ? extends Object>> getLabelledResultMap() {
		return null;
	}

	@Override
	public String getCsv(boolean withLabels, CompressionLevel compressionLevel) {
		return null;
	}

	@Override
	public List<String> getHeader() {
		return null;
	}

	@Override
	public String getCalculatorName() {
		return null;
	}
}
