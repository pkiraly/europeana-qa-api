package de.gwdg.europeanaqa.api.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Structural information of an EDM record.
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmStructure {

  private final Map<String, ContextualId> contextualIds;
  private final List<ProxyLink> providerProxyLinks;
  private final List<String> providerProxyValues;
  private final List<ProxyLink> europeanaProxyLinks;
  private final List<String> europeanaProxyValues;

  /**
   * Creates an empty Edm record structure.
   */
  public EdmStructure() {
    contextualIds = new HashMap<>();
    providerProxyLinks = new ArrayList<>();
    providerProxyValues = new ArrayList<>();
    europeanaProxyLinks = new ArrayList<>();
    europeanaProxyValues = new ArrayList<>();
  }

  /**
   * Add a proxy link.
   * @param type The type of the proxy (provider or Europeana)
   * @param url The URL of the link.
   */
  public void addProxyLink(ProxyType type, String url) {
    if (type.equals(ProxyType.PROVIDER)) {
      addProviderProxyLink(url);
    } else {
      addEuropeanaProxyLink(url);
    }
  }

  /**
   * Add a (non URL) proxy value.
   * @param type The type of the proxy (provider or Europeana).
   * @param value The string value.
   */
  public void addProxyValue(ProxyType type, String value) {
    if (type.equals(ProxyType.PROVIDER)) {
      addProviderProxyValue(value);
    } else {
      addEuropeanaProxyValue(value);
    }
  }

  /**
   * Add a provider proxy link.
   * @param url The URL of the link.
   */
  public void addProviderProxyLink(String url) {
    providerProxyLinks.add(new ProxyLink(url));
  }

  /**
   * Add a provider proxy value.
   * @param value The property value.
   */
  public void addProviderProxyValue(String value) {
    providerProxyValues.add(value);
  }

  /**
   * Add Europeana proxy link.
   * @param url The URL of the link.
   */
  public void addEuropeanaProxyLink(String url) {
    europeanaProxyLinks.add(new ProxyLink(url));
  }

  /**
   * Edd Europeana proxy value.
   * @param value The property value.
   */
  public void addEuropeanaProxyValue(String value) {
    europeanaProxyValues.add(value);
  }

  /**
   * Get provider proxy links.
   * @return The proxy links.
   */
  public List<ProxyLink> getProviderProxyLinks() {
    return providerProxyLinks;
  }

  /**
   * Get provider proxy values.
   * @return The proxy values.
   */
  public List<String> getProviderProxyValues() {
    return providerProxyValues;
  }

  /**
   * Get Europeana proxy links.
   * @return The proxy links.
   */
  public List<ProxyLink> getEuropeanaProxyLinks() {
    return europeanaProxyLinks;
  }

  /**
   * Check if the object contains a provider proxy link.
   * @param url The URL to check.
   * @return True if the URL is registered, otherwise false.
   */
  public boolean containsProviderLink(String url) {
    return containsLink(url, providerProxyLinks);
  }

  /**
   * Checks if the object contains the URL among the Europeana proxy links.
   * @param url The URL to check.
   * @return True if the URL is registered, otherwise false.
   */
  public boolean containsEuropeanaLink(String url) {
    return containsLink(url, europeanaProxyLinks);
  }

  /**
   * Checks if the object contains the URL among the Europeana proxy links.
   * @param url The URL to check.
   * @param links The links to check against.
   * @return True if the URL is registered, otherwise false.
   */
  private boolean containsLink(String url, List<ProxyLink> links) {
    boolean found = false;
    for (ProxyLink link : links) {
      if (link.getLink().equals(url)) {
        found = true;
        break;
      }
    }
    return found;
  }

  /**
   * Set a provider link target (contextual entity type).
   * @param url The URL.
   * @param entityType The target's entity type.
   */
  public void setProviderLinkTarget(String url, EntityType entityType) {
    setLinkTarget(url, entityType, providerProxyLinks);
  }

  /**
   * Set a URL link target.
   * @param url The URL.
   * @param entityType The target's entity type.
   */
  public void setEuropeanaLinkTarget(String url, EntityType entityType) {
    setLinkTarget(url, entityType, europeanaProxyLinks);
  }

  private void setLinkTarget(String url, EntityType entityType, List<ProxyLink> europeanaProxyLinks) {
    for (ProxyLink link : europeanaProxyLinks) {
      if (link.getLink().equals(url)) {
        link.setTarget(entityType);
      }
    }
  }

  /**
   * Gets the values of Europeana proxy.
   * @return The values of Europeana proxy.
   */
  public List<String> getEuropeanaProxyValues() {
    return europeanaProxyValues;
  }

  public void addContextualId(String url, EntityType type, LinkType linkType) {
    contextualIds.put(url, new ContextualId(url, type, linkType));
  }

  /**
   * Format the counts of collections inside the object.
   * @return The counts as formatted string.
   */
  public String formatCounts() {
    return "{"
      + "provider links: " + providerProxyLinks.size()
      + ", provider value: " + providerProxyValues.size()
      + ", europeana links: " + europeanaProxyLinks.size()
      + ", europeana values: " + europeanaProxyValues.size()
      + "}";
  }

  public List<String> getBrokenProviderProxyLinks() {
    return getBrokenLinks(ProxyType.PROVIDER);
  }

  public List<String> getBrokenEuropeanaProxyLinks() {
    return getBrokenLinks(ProxyType.EUROPEANA);
  }

  public Map<String, ContextualId> getContextualIds() {
    return contextualIds;
  }

  /**
   * Get the broken links of a given proxy type. Broken links don't have target
   * contextual entities.
   *
   * @param type The proxy type.
   * @return The list of broken links.
   */
  public List<String> getBrokenLinks(ProxyType type) {
    List<String> links = new ArrayList<>();
    List<ProxyLink> proxyLinks = type.equals(ProxyType.PROVIDER)
      ? providerProxyLinks
      : europeanaProxyLinks;
    for (ProxyLink link : proxyLinks) {
      if (link.getTarget() == null) {
        links.add(link.getLink());
      }
    }
    return links;
  }

  public List<ContextualId> getOrphanedEntities() {
    return getContextualIdsByType(LinkType.NONE);
  }

  public List<ContextualId> getSelfLinkedEntities() {
    return getContextualIdsByType(LinkType.SELF);
  }

  public List<ContextualId> getInterLinkedEntities() {
    return getContextualIdsByType(LinkType.CONTEXTUAL_ENTITY);
  }

  @NotNull
  private List<ContextualId> getContextualIdsByType(LinkType linkType) {
    List<ContextualId> selectedEntities = new ArrayList<>();
    for (ContextualId contextualId : contextualIds.values()) {
      if (contextualId.getSource().equals(linkType)) {
        selectedEntities.add(contextualId);
      }
    }
    return selectedEntities;
  }
}
