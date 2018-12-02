package de.gwdg.europeanaqa.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class LinkRegister {

	/**
	 * The URI register. It contains pairs of URIs and its type.
	 */
	private Map<String, LinkType> register = new HashMap<>();

	/**
	 * Put a URI and type pair to register.
	 * @param uri String The URI
	 * @param type LinkingType The type of the URI
	 */
	public final void put(final String uri, final LinkType type) {
		register.put(uri, type);
	}

	/**
	 * Put a list of URIs with the same type to register.
	 * @param uris String The URI
	 * @param type LinkingType The type of the URI
	 */
	public final void putAll(final List<String> uris,
									 final LinkType type) {
		for (String uri : uris) {
			put(uri, type);
		}
	}

	/**
	 * Does URI exist in the register?
	 * @param uri String The URI
	 * @return boolean True if the URI is already registered, otherwise false.
	 */
	public final boolean exists(final String uri) {
		return register.containsKey(uri);
	}

	/**
	 * Returns the link type of a registered URI.
	 * @param uri String The URI
	 * @return LinkingType The type of the URI
	 */
	public final LinkType get(final String uri) {
		return register.get(uri);
	}

	/**
	 * Get the URIs for which the type is NONE.
	 * @return List<String> The list of unlinked URIs
	 */
	public final List<String> getUnlinkedEntities() {
		List<String> unlinkedEntities = new ArrayList<>();
		for (String uri : register.keySet()) {
			if (register.get(uri).equals(LinkType.NONE)) {
				unlinkedEntities.add(uri);
			}
		}
		return unlinkedEntities;
	}
}
