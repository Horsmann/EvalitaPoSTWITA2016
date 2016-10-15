package de.unidue.ltl.evalita.feat;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class IsCamelCase extends FeatureExtractorResource_ImplBase implements
		FeatureExtractor {

	public final String FEATURE_NAME = "isCamelCase";

	@Override
	public Set<Feature> extract(JCas view,
			TextClassificationTarget classificationUnit)
			throws TextClassificationException {

		String text = classificationUnit.getCoveredText();

		boolean isCamelCase = isCamelCase(text);

		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(FEATURE_NAME, isCamelCase ? 1 : 0));
		return features;
	}

	static boolean isCamelCase(String text) {
		return Pattern.matches("[A-ZÄÖÜ]?[a-zäöü]+[A-ZÄÖÜ][a-zäöü]+", text);
	}

}
