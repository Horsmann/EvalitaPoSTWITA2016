package de.unidue.ltl.evalita.feat;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class IsNumber
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    private final String FEATURE_NAME = "isNum";

    public Set<Feature> extract(JCas aView, TextClassificationTarget aClassificationUnit)
        throws TextClassificationException
    {

        boolean isNum = is(aClassificationUnit.getCoveredText());
        Feature feature;

        if (isNum) {
            feature = new Feature(FEATURE_NAME, 1);
        }
        else {
            feature = new Feature(FEATURE_NAME, 0, true);
        }
        Set<Feature> features = new HashSet<Feature>();
        features.add(feature);
        return features;
    }

    static boolean is(String coveredText)
    {
        return isPure(coveredText) || isTime(coveredText) || isDotCommaSeparatedNum(coveredText)
                || isNumberWithUnitOrMiscSymbols(coveredText);
    }

    private static boolean isNumberWithUnitOrMiscSymbols(String coveredText)
    {
        return isPure(coveredText.replaceAll("[\\.,\\-%////\\€¥\\$]+", ""));
    }

    static boolean isDotCommaSeparatedNum(String coveredText)
    {

        String value = coveredText.replaceAll(",", "").replaceAll("\\.", "");
        return isPure(value) && !value.isEmpty();
    }

    static boolean isTime(String coveredText)
    {
        return coveredText.matches("[0-9]+:[0-9]+");
    }

    static boolean isPure(String coveredText)
    {
        Set<Character> chars = new HashSet<>();
        for (char c : coveredText.toCharArray()) {
            chars.add(c);
        }

        for (char c : chars) {
            if (!(c >= '0' && c <= '9')) {
                return false;
            }
        }

        return true;
    }
}
