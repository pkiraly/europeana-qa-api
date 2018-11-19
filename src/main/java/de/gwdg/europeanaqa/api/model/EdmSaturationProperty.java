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

	private int taggedLiteralsCount = 0;
	private LanguageSaturationType type;
	private Set<String> distinctLanguages;
	private int distinctLanguageCount = 0;

	public final double getNumberOfLiteralsPerLanguage() {
		if (distinctLanguageCount == 0) {
			return 0.0;
		}
		return (double) taggedLiteralsCount / (double) distinctLanguageCount;
	}

	public final int getTaggedLiteralsCount() {
		return taggedLiteralsCount;
	}

	public final void setTaggedLiteralsCount(final int pTaggedLiteralsCount) {
		this.taggedLiteralsCount = pTaggedLiteralsCount;
	}

	public final LanguageSaturationType getType() {
		return type;
	}

	public final void setType(final LanguageSaturationType pType) {
		this.type = pType;
	}

	public final Set<String> getDistinctLanguages() {
		return distinctLanguages;
	}

	public final void setDistinctLanguages(final Set<String> pDistinctLanguages) {
		this.distinctLanguages = pDistinctLanguages;
		distinctLanguageCount = distinctLanguages.size();
	}

	public final FieldCounter<Double> getCsv(final String propertyName) {
		FieldCounter<Double> values = new FieldCounter<>();
		if (getType().isTaggedLiteral()) {
			values.put(
				propertyName + "/taggedLiterals",
				(double) getTaggedLiteralsCount()
			);
			values.put(
				propertyName + "/languages",
				(double) getDistinctLanguages().size()
			);
			values.put(
				propertyName + "/literalsPerLanguage",
				(double) getNumberOfLiteralsPerLanguage()
			);
		} else {
			values.put(propertyName + "/taggedLiterals", (double) getType().value());
			values.put(propertyName + "/languages", 0.0);
			values.put(propertyName + "/literalsPerLanguage", 0.0);
		}
		return values;
	}

	static List<String> getHeaders(final String propertyName) {
		return Arrays.asList(
			propertyName + "/taggedLiterals",
			propertyName + "/languages",
			propertyName + "/literalsPerLanguage"
		);
	}

	@Override
	public final String toString() {
		return "{"
		        + "taggedLiteralsCount=" + taggedLiteralsCount
		        + ", type=" + type
		        + ", distinctLanguages=" + distinctLanguages
		        + ", distinctLanguageCount=" + distinctLanguageCount
		        + ", literalsPerLanguage=" + getNumberOfLiteralsPerLanguage()
		        + '}';
	}

	public final void setTypedCount(final LanguageSaturationType pType,
											  final int taggedStringCount) {
		setType(pType);
		if (pType.equals(LanguageSaturationType.NA)) {
			this.taggedLiteralsCount = 0;
		} else {
			this.taggedLiteralsCount = taggedStringCount;
		}
	}
}
