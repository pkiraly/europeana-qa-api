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

	public enum LinkingType {
		NONE, CONTEXTUAL_ENTITY, PROVIDER_PROXY, EUROPEANA_PROXY
	}

	private Map<String, LinkingType> register = new HashMap<>();

	public void put(String uri, LinkingType type) {
		register.put(uri, type);
	}

	public void putAll(List<String> uris, LinkingType type) {
		for (String uri : uris)
			put(uri, type);
	}

	public boolean exists(String uri) {
		return register.containsKey(uri);
	}

	public LinkingType get(String uri) {
		return register.get(uri);
	}


	public List<String> getUnlinkedEntities() {
		List<String> unlinkedEntities = new ArrayList<>();
		for (String uri : register.keySet()) {
			if (register.get(uri).equals(LinkingType.NONE))
				unlinkedEntities.add(uri);
		}
		return unlinkedEntities;
	}
}
