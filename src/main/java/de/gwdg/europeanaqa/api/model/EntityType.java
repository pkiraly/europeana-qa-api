package de.gwdg.europeanaqa.api.model;

import java.util.Arrays;
import java.util.List;

/**
 * Entity types.
 */
public enum EntityType {

  /**
   * Agent.
   */
  AGENT("Agent", "Agent/rdf:about",
    "Agent/edm:hasMet", "Agent/edm:isRelatedTo", "Agent/owl:sameAs"),

  /**
   * Concept.
   */
  CONCEPT("Concept", "Concept/rdf:about",
    "Concept/skos:broader", "Concept/skos:narrower",
    "Concept/skos:related", "Concept/skos:broadMatch",
    "Concept/skos:narrowMatch", "Concept/skos:relatedMatch",
    "Concept/skos:exactMatch", "Concept/skos:closeMatch"),

  /**
   * Place.
   */
  PLACE("Place", "Place/rdf:about",
    "Place/dcterms:isPartOf", "Place/dcterms:hasPart",
    "Place/owl:sameAs"),

  /**
   * Timespan.
   */
  TIMESPAN("Timespan", "Timespan/rdf:about",
    "Timespan/dcterms:isPartOf", "Timespan/dcterms:hasPart",
    "Timespan/edm:isNextInSequence", "Timespan/owl:sameAs");

  private String name;
  private String branchId;
  private List<String> linkableFields;

  EntityType(String name, String branchId, String... linkableFields) {
    this.name = name;
    this.branchId = branchId;
    this.linkableFields = Arrays.asList(linkableFields);
  }

  public String getName() {
    return name;
  }

  public String getBranchId() {
    return branchId;
  }

  public List<String> getLinkableFields() {
    return linkableFields;
  }
}
