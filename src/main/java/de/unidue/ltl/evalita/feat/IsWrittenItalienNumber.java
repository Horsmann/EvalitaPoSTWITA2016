/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.unidue.ltl.evalita.feat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class IsWrittenItalienNumber
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{

    public final String FEATURE_NAME = "isWrittenItalienNum";
    
    private Set<String> italienWords = new HashSet<>();
    
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        italienWords.add("zero");
        italienWords.add("uno");
        italienWords.add("due");
        italienWords.add("tre");
        italienWords.add("quattro");
        italienWords.add("cinque");
        italienWords.add("sei");
        italienWords.add("sette");
        italienWords.add("otto");
        italienWords.add("nove");
        italienWords.add("dieci");
        italienWords.add("undici");
        italienWords.add("dodici");
        italienWords.add("tredici");
        italienWords.add("quattordici");
        italienWords.add("quindici");
        italienWords.add("sedici");
        italienWords.add("diciassette");
        italienWords.add("diciotto");
        italienWords.add("diciannove");
        italienWords.add("venti");

        italienWords.add("diecimila");
        italienWords.add("mille");
        italienWords.add("milioni");
        italienWords.add("miliardario");
        return true;
    }

    public Set<Feature> extract(JCas aView, TextClassificationTarget aClassificationUnit)
        throws TextClassificationException
    {
        String token = aClassificationUnit.getCoveredText();
        Feature feature;
        boolean is = isNum(token.toLowerCase());
        if (is) {
            feature = new Feature(FEATURE_NAME, 1);
        }
        else {
            feature = new Feature(FEATURE_NAME, 0, true);
        }

        Set<Feature> features = new HashSet<Feature>();
        features.add(feature);
        return features;
    }

      boolean isNum(String aCoveredText)
    {
        if (aCoveredText.isEmpty()) {
            return false;
        }

        return italienWords.contains(aCoveredText);
    }

}
