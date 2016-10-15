package de.unidue.ltl.evalita;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.ml.report.BatchTrainTestReport;

import de.unidue.ltl.evalita.learningcurve.KnownUnknownWordAnalysisReport;
import de.unidue.ltl.evalita.util.TcPosTaggingWrapper;

public class TrainTestEvaluateIt
    implements Constants
{
    public static String trainFolder = null;
    public static String testFolder = null;
    public static String homeFolder = null;

    public static Integer charMinNgram = null;
    public static Integer charMaxNgram = null;
    public static Integer charTopNgram = null;

    public static String outputFolder = null;

    public static String languageCode = "it";
    public static Boolean useCoarse = false;
    private static String brownCluster;
    private static String posDict;
    private static String namelists;

    public static void main(String[] args)
        throws Exception
    {

        // FileInputStream input = new FileInputStream(args[0]);
        FileInputStream input = new FileInputStream("src/main/resources/eval.properties");
        Properties prop = new Properties();
        prop.load(input);

        trainFolder = prop.getProperty("trainData");
        testFolder = prop.getProperty("testData");

        outputFolder = prop.getProperty("outputFolder");
        homeFolder = prop.getProperty("homeFolder");

        brownCluster = prop.getProperty("brownCluster");
        posDict = prop.getProperty("posDict");
        namelists = prop.getProperty("namelists");

        System.setProperty("DKPRO_HOME", homeFolder);

        ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE, Constants.LM_SINGLE_LABEL);

        TrainTestEvaluateIt experiment = new TrainTestEvaluateIt();
        experiment.validation(pSpace);
        KnownUnknownWordAnalysisReport.writeUnknownKnownFine();
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(String featureMode, String learningMode)
        throws Exception
    {

        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        Dimension<TcFeatureSet> features = Dimension.create(DIM_FEATURE_SET, Features.getFeatures(
                brownCluster, posDict, namelists));

        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                asList(new String[] {
                        CRFSuiteAdapter.ALGORITHM_AVERAGED_PERCEPTRON, "-p", "feature.possible_states=1"
                        }));

        CollectionReaderDescription trainReader = CollectionReaderFactory.createReaderDescription(
                ItalianTokenTagReader.class, ItalianTokenTagReader.PARAM_LANGUAGE, languageCode,
                ItalianTokenTagReader.PARAM_SOURCE_LOCATION, trainFolder,
                ItalianTokenTagReader.PARAM_PATTERNS, "*.txt");
        dimReaders.put(DIM_READER_TRAIN, trainReader);

        CollectionReaderDescription testReader = CollectionReaderFactory.createReaderDescription(
                ItalianTokenTagReader.class, ItalianTokenTagReader.PARAM_LANGUAGE, languageCode,
                ItalianTokenTagReader.PARAM_SOURCE_LOCATION, testFolder,
                ItalianTokenTagReader.PARAM_PATTERNS, "*.txt");
        dimReaders.put(DIM_READER_TEST, testReader);

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, learningMode),
                Dimension.create(DIM_FEATURE_MODE, featureMode), features, dimClassificationArgs);

        return pSpace;
    }

    protected void validation(ParameterSpace pSpace)
        throws Exception
    {
        ExperimentTrainTest batch = new ExperimentTrainTest("evalita", CRFSuiteAdapter.class);
        batch.setPreprocessing(getPreprocessing());
        batch.addReport(BatchTrainTestReport.class);
        batch.addReport(KnownUnknownWordAnalysisReport.class);
        batch.setParameterSpace(pSpace);

        // Run
        Lab.getInstance().run(batch);
    }

    private AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(createEngineDescription(TcPosTaggingWrapper.class,
                TcPosTaggingWrapper.PARAM_USE_COARSE_GRAINED, false));
    }
}
