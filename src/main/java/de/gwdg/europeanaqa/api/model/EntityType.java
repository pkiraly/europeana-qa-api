package de.gwdg.europeanaqa.api.model;

/**
 * Entity types.
 */
public enum EntityType {

	/**
	 * Agent.
	 */
	AGENT("Agent", "Agent/rdf:about"),

	/**
	 * Concept.
	 */
	CONCEPT("Concept", "Concept/rdf:about"),

	/**
	 * Place.
	 */
	PLACE("Place", "Place/rdf:about"),

	/**
	 * Timespan.
	 */
	TIMESPAN("Timespan", "Timespan/rdf:about");

	private String name;
	private String branchId;

	EntityType(String name, String branchId) {
		this.name = name;
		this.branchId = branchId;
	}

	public String getName() {
		return name;
	}

	public String getBranchId() {
		return branchId;
	}
}
