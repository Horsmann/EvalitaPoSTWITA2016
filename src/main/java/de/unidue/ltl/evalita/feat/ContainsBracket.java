package de.unidue.ltl.evalita.feat;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class ContainsBracket
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{

    private final String FEATURE_NAME = "containsBracket";

    @Override
    public Set<Feature> extract(JCas view, TextClassificationTarget classificationUnit)
        throws TextClassificationException
    {

        String text = classificationUnit.getCoveredText();
        boolean b = contains(text);

        Set<Feature> features = new HashSet<Feature>();
        if (b) {
            features.add(new Feature(FEATURE_NAME, 1));
        }
        else {
            features.add(new Feature(FEATURE_NAME, 0, true));
        }

        return features;
    }

    public static boolean contains(String text)
    {
        return text.contains("(") || text.contains(")") || text.contains("{") || text.contains("}")
                || text.contains("[") || text.contains("]");
    }

}
