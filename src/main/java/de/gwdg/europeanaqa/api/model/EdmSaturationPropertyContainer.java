package de.gwdg.europeanaqa.api.model;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
class EdmSaturationPropertyContainer {

  private String propertyName;
  private EdmSaturationProperty providerProxy;
  private EdmSaturationProperty europeanaProxy;

  EdmSaturationPropertyContainer(String propertyName) {
    this.propertyName = propertyName;
    this.providerProxy = new EdmSaturationProperty();
    this.europeanaProxy = new EdmSaturationProperty();
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public EdmSaturationProperty getProviderProxy() {
    return providerProxy;
  }

  public void setProviderProxy(EdmSaturationProperty providerProxy) {
    this.providerProxy = providerProxy;
  }

  public EdmSaturationProperty getEuropeanaProxy() {
    return europeanaProxy;
  }

  public void setEuropeanaProxy(EdmSaturationProperty europeanaProxy) {
    this.europeanaProxy = europeanaProxy;
  }

  @Override
  public String toString() {
    return "{"
      + "propertyName=" + propertyName
      + ", providerProxy=" + providerProxy
      + ", europeanaProxy=" + europeanaProxy
      + '}';
  }
}
