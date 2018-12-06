package de.gwdg.europeanaqa.api.model;

/**
 * Proxy link object store information about a link inside a proxy to a contextual entity.
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class ProxyLink {
  private String link;
  private EntityType target = null;

  /**
   * Creates a new object.
   * @param link The link (URL).
   */
  public ProxyLink(String link) {
    this.link = link;
  }

  public void setTarget(EntityType target) {
    this.target = target;
  }

  public String getLink() {
    return link;
  }

  public EntityType getTarget() {
    return target;
  }

  @Override
  public String toString() {
    return "ProxyLink{"
      + "link='" + link + "'"
      + ", target=" + target
      + '}';
  }
}
