package de.gwdg.europeanaqa.api.model;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
class EdmSaturationPropertyContainer {
	
	public static final int PROVIDER_PROXY = 0;
	public static final int EUROPEANA_PROXY = 1;

	String propertyName;
	EdmSaturationProperty providerProxy;
	EdmSaturationProperty europeanaProxy;

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
		return "{" + "propertyName=" + propertyName + ", providerProxy=" + providerProxy + ", europeanaProxy=" + europeanaProxy + '}';
	}
}
