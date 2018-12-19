package de.gwdg.europeanaqa.api.model;

import de.gwdg.metadataqa.api.json.JsonBranch;
import de.gwdg.metadataqa.api.schema.Schema;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Proxies contains schema information about the two EDM proxies: provider and Europeana proxy.
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class Proxies implements Serializable {

  private static final Logger LOGGER = Logger.getLogger(
    Proxies.class.getCanonicalName()
  );

  private JsonBranch providerProxy;
  private JsonBranch europeanaProxy = null;

  /**
   * Creates a new object, and generates the two proxies as member variables.
   *
   * @param schema The metadata schema.
   */
  public Proxies(Schema schema) {
    providerProxy = schema.getPathByLabel("Proxy");
    try {
      europeanaProxy = (JsonBranch) providerProxy.clone();
      europeanaProxy.setJsonPath(
        providerProxy.getJsonPath().replace("false", "true"));
    } catch (CloneNotSupportedException ex) {
      LOGGER.severe(ex.getMessage());
    }
  }

  /**
   * Retrieves the provider proxy.
   * @return The provider proxy
   */
  public JsonBranch getProviderProxy() {
    return providerProxy;
  }

  /**
   * Retrieves the Europeana proxy.
   * @return The Europeana proxy
   */
  public JsonBranch getEuropeanaProxy() {
    return europeanaProxy;
  }

  /**
   * Get proxy by its type.
   * @param type The type of the proxy.
   * @return The proxy object.
   */
  public JsonBranch getByType(ProxyType type) {
    if (type.equals(ProxyType.PROVIDER)) {
      return getProviderProxy();
    } else{
      return getEuropeanaProxy();
    }
  }

}
