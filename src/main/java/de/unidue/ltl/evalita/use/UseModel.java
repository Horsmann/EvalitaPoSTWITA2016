package de.unidue.ltl.evalita.use;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.ltl.evalita.ItalianTokenTagReader;

public class UseModel
{

    public static void main(String[] args)
        throws Exception
    {
        // war dann wohl ein average perceptron model
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                ItalianTokenTagReader.class, ItalianTokenTagReader.PARAM_LANGUAGE, "it",
                ItalianTokenTagReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/data/test/goldTESTset-2016_09_12.txt",
                ItalianTokenTagReader.PARAM_SEQUENCES_PER_CAS, 100);

        AnalysisEngineDescription tag = AnalysisEngineFactory.createEngineDescription(
                FlexTagPosTagger.class, FlexTagPosTagger.PARAM_MODEL_LOCATION,
                System.getProperty("user.home") + "/Desktop/evalitaModel");

        AnalysisEngineDescription wrt = AnalysisEngineFactory.createEngineDescription(
                EvalitaFormatWriter.class, EvalitaFormatWriter.PARAM_TARGET_FILE,
                System.getProperty("user.home") + "/Desktop/predictions.txt");

        SimplePipeline.runPipeline(reader, tag, wrt);

    }

}
