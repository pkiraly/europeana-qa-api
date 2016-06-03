package com.nsdr.europeanaqa.api.interfaces;

import com.nsdr.europeanaqa.api.counter.Counters;
import com.nsdr.europeanaqa.api.model.JsonPathCache;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public interface Calculator {

	void calculate(JsonPathCache cache, Counters counters);
}
