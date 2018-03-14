package de.gwdg.europeanaqa.api.model;

import de.gwdg.metadataqa.api.counter.FieldCounter;
import de.gwdg.metadataqa.api.model.LanguageSaturationType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Péter Király <peter.kiraly at gwdg.de>
 */
public class EdmSaturationProperty {

	int taggedLiteralsCount = 0;
	LanguageSaturationType type;
	Set<String> distinctLanguages;
	int distinctLanguageCount = 0;

	public double getNumberOfLiteralsPerLanguage() {
		if (distinctLanguageCount == 0)
			return 0.0;
		return (double)taggedLiteralsCount / (double)distinctLanguageCount;
	}

	public int getTaggedLiteralsCount() {
		return taggedLiteralsCount;
	}

	public void setTaggedLiteralsCount(int taggedLiteralsCount) {
		this.taggedLiteralsCount = taggedLiteralsCount;
	}

	public LanguageSaturationType getType() {
		return type;
	}

	public void setType(LanguageSaturationType type) {
		this.type = type;
	}

	public Set<String> getDistinctLanguages() {
		return distinctLanguages;
	}

	public void setDistinctLanguages(Set<String> distinctLanguages) {
		this.distinctLanguages = distinctLanguages;
		distinctLanguageCount = distinctLanguages.size();
	}

	public FieldCounter<Double> getCsv(String propertyName) {
		FieldCounter<Double> values = new FieldCounter<>();
		if (getType().isTaggedLiteral()) {
			values.put(propertyName + "/taggedLiterals",
			          (double)getTaggedLiteralsCount());
			values.put(propertyName + "/languages",
			          (double)getDistinctLanguages().size());
			values.put(propertyName + "/literalsPerLanguage",
			           (double)getNumberOfLiteralsPerLanguage());
		} else {
			values.put(propertyName + "/taggedLiterals", (double)getType().value());
			values.put(propertyName + "/languages", 0.0);
			values.put(propertyName + "/literalsPerLanguage", 0.0);
		}
		return values;
	}

	static List<String> getHeaders(String propertyName) {
		return Arrays.asList(
			propertyName + "/taggedLiterals",
			propertyName + "/languages",
			propertyName + "/literalsPerLanguage"
		);
	}

	@Override
	public String toString() {
		return "{"
		        + "taggedLiteralsCount=" + taggedLiteralsCount
		        + ", type=" + type
		        + ", distinctLanguages=" + distinctLanguages
		        + ", distinctLanguageCount=" + distinctLanguageCount
		        + ", literalsPerLanguage=" + getNumberOfLiteralsPerLanguage()
		        + '}';
	}

	public void setTypedCount(LanguageSaturationType type, int taggedStringCount) {
		setType(type);
		if (type.equals(LanguageSaturationType.NA)) {
			this.taggedLiteralsCount = 0;
		} else {
			this.taggedLiteralsCount = taggedStringCount;
		}
	}
}
