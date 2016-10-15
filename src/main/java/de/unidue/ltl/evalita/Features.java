package de.unidue.ltl.evalita;

import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.length.NrOfChars;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;
import org.dkpro.tc.features.tcu.TargetSurfaceFormContextFeature;

import de.unidue.ltl.evalita.feat.BrownClusterNormalizedLowerCaseFeature;
import de.unidue.ltl.evalita.feat.ContainsBracket;
import de.unidue.ltl.evalita.feat.ContainsCapitalLetter;
import de.unidue.ltl.evalita.feat.ContainsComma;
import de.unidue.ltl.evalita.feat.ContainsDot;
import de.unidue.ltl.evalita.feat.ContainsHyphen;
import de.unidue.ltl.evalita.feat.ContainsNumber;
import de.unidue.ltl.evalita.feat.ContainsUnderScore;
import de.unidue.ltl.evalita.feat.DictionaryTagFeature;
import de.unidue.ltl.evalita.feat.IsAllCapitalized;
import de.unidue.ltl.evalita.feat.IsAllNonPunctuationSpecialChars;
import de.unidue.ltl.evalita.feat.IsAllSpecialCharacters;
import de.unidue.ltl.evalita.feat.IsCamelCase;
import de.unidue.ltl.evalita.feat.IsHashtag;
import de.unidue.ltl.evalita.feat.IsName;
import de.unidue.ltl.evalita.feat.IsNumber;
import de.unidue.ltl.evalita.feat.IsRetweet;
import de.unidue.ltl.evalita.feat.IsURL;
import de.unidue.ltl.evalita.feat.IsUserMention;
import de.unidue.ltl.evalita.feat.IsWrittenItalienNumber;

public class Features
    implements Constants
{

    public static TcFeatureSet getFeatures(String brownCluster, String posDict, String namelist)
    {
        return new TcFeatureSet(
                TcFeatureFactory.create(TargetSurfaceFormContextFeature.class, TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, -1),
                TcFeatureFactory.create(TargetSurfaceFormContextFeature.class, TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, 0),
                TcFeatureFactory.create(TargetSurfaceFormContextFeature.class, TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, +1),
                TcFeatureFactory.create(ContainsCapitalLetter.class),
                TcFeatureFactory.create(ContainsComma.class),
                TcFeatureFactory.create(ContainsHyphen.class),
                TcFeatureFactory.create(ContainsDot.class),
                TcFeatureFactory.create(ContainsUnderScore.class),
                TcFeatureFactory.create(ContainsBracket.class),
                TcFeatureFactory.create(ContainsNumber.class),
                TcFeatureFactory.create(IsAllCapitalized.class),
                TcFeatureFactory.create(IsAllNonPunctuationSpecialChars.class),
                TcFeatureFactory.create(IsAllSpecialCharacters.class),
                TcFeatureFactory.create(IsCamelCase.class),
                TcFeatureFactory.create(LuceneCharacterNGram.class,
                        LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 1,
                        LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 1,
                        LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 50),
                TcFeatureFactory.create(LuceneCharacterNGram.class,
                        LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 2,
                        LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 2,
                        LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 750),
                TcFeatureFactory.create(LuceneCharacterNGram.class,
                        LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 3,
                        LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 3,
                        LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 750),
                TcFeatureFactory.create(LuceneCharacterNGram.class,
                        LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 4,
                        LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 4,
                        LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 750),
                TcFeatureFactory.create(NrOfChars.class),
                TcFeatureFactory.create(IsURL.class),
                TcFeatureFactory.create(IsUserMention.class),
                TcFeatureFactory.create(IsHashtag.class),
                TcFeatureFactory.create(IsRetweet.class),
                TcFeatureFactory.create(IsNumber.class),
                TcFeatureFactory.create(IsWrittenItalienNumber.class),
                TcFeatureFactory.create(IsName.class, IsName.PARAM_NAMELIST_FOLDER, namelist,IsName.PARAM_LOWER_CASE, true),
                TcFeatureFactory.create(BrownClusterNormalizedLowerCaseFeature.class, BrownClusterNormalizedLowerCaseFeature.PARAM_BROWN_CLUSTERS_LOCATION, brownCluster),
                TcFeatureFactory.create(DictionaryTagFeature.class, DictionaryTagFeature.PARAM_DICTIONARY_LOCATION, posDict)
                );

    }

}
