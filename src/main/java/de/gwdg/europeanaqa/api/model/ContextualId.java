package de.gwdg.europeanaqa.api.model;

/**
 * Value object for a contextual id.
 *
 * A container for an uri, the type of contextual entity it belongs to
 * the source of the link (which points to it) and the source field.
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class ContextualId {
  private String uri;
  private EntityType entity;
  private LinkType source;
  private String sourceField;

  public ContextualId(String uri, EntityType entity, LinkType source) {
    this.uri = uri;
    this.entity = entity;
    this.source = source;
  }

  public String getUri() {
    return uri;
  }

  public EntityType getEntity() {
    return entity;
  }

  public LinkType getSource() {
    return source;
  }

  public void setSource(LinkType source) {
    this.source = source;
  }

  public String getSourceField() {
    return sourceField;
  }

  public void setSourceField(String sourceField) {
    this.sourceField = sourceField;
  }
}
