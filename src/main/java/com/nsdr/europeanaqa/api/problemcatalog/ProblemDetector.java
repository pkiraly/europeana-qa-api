package com.nsdr.europeanaqa.api.problemcatalog;

import com.nsdr.europeanaqa.api.model.JsonPathCache;
import java.util.Map;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public abstract class ProblemDetector {

	protected ProblemCatalog problemCatalog;

	public abstract void update(JsonPathCache cache, Map<String, Double> results);
}
