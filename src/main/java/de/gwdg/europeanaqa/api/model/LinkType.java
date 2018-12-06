package de.gwdg.europeanaqa.api.model;

/**
 * Link types.
 */
public enum LinkType {
  /**
   * None.
   */
  NONE,
  /**
   * Link from an entity to the same entity.
   */
  SELF,
  /**
   * Contextual entity.
   */
  CONTEXTUAL_ENTITY,
  /**
   * Provider proxy.
   */
  PROVIDER_PROXY,
  /**
   * Europeana proxy.
   */
  EUROPEANA_PROXY
}
