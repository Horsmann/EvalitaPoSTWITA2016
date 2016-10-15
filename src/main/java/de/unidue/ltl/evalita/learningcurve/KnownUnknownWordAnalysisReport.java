package de.unidue.ltl.evalita.learningcurve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;

public class KnownUnknownWordAnalysisReport
    extends BatchReportBase
    implements Constants
{

    public static List<Double> in_fine = new ArrayList<>();
    public static List<Double> out_fine = new ArrayList<>();
    public static List<Double> in_coarse = new ArrayList<>();
    public static List<Double> out_coarse = new ArrayList<>();

    static String featureFile = null;
    static String predictionFile = null;
    public static String mappingFile=null;

    static File lastCrf = null;

    {
        TCMachineLearningAdapter adapter = CRFSuiteAdapter.getInstance();
        featureFile = adapter.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        predictionFile = adapter.getFrameworkFilename(AdapterNameEntries.predictionsFile);
    }

    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        String trainContextId = null;
        String testContextId = null;
        String predictionContextId = null;
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getId().contains("TestTask")) {
                predictionContextId = subcontext.getId();
            }
            if (subcontext.getId().contains("ExtractFeaturesTask-Test")) {
                testContextId = subcontext.getId();
            }
            if (subcontext.getId().contains("ExtractFeaturesTask-Train")) {
                trainContextId = subcontext.getId();
            }
        }

        File train = buildFileLocation(store, trainContextId,
                TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        Set<String> trainVocab = extractVocab(train);

        File test = buildFileLocation(store, testContextId,
                TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        List<String> testVocab = readTest(test);

        File p = buildFileLocation(store, predictionContextId, predictionFile);
        lastCrf = p.getParentFile();
        List<String> pred = readPredictions(p);
        evaluate(trainVocab, testVocab, pred, in_fine, out_fine);
        evaluate(trainVocab, map(testVocab), mapPred(pred), in_coarse, out_coarse);
    }

    private List<String> map(List<String> data) throws UIMAException
    {
        MappingProvider posMappingProvider = new MappingProvider();
        posMappingProvider.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/"
                        + "core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
        posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        posMappingProvider.setOverride(MappingProvider.LOCATION, mappingFile);
        posMappingProvider.setOverride(MappingProvider.LANGUAGE, "en");
        posMappingProvider.setOverride("tagger.tagset", "abc");

        JCas jcas = JCasFactory.createJCas();

        posMappingProvider.configure(jcas.getCas());
        
        List<String> out = new ArrayList<>();
        for(String p : data){
            String[] split = p.split("\t");
            out.add(split[0]+"\t"+getCoarse(jcas, posMappingProvider, split[1]));
        }
        
        return out;
    }

    private List<String> mapPred(List<String> data) throws UIMAException
    {
        MappingProvider posMappingProvider = new MappingProvider();
        posMappingProvider.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/"
                        + "core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
        posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        posMappingProvider.setOverride(MappingProvider.LOCATION, mappingFile);
        posMappingProvider.setOverride(MappingProvider.LANGUAGE, "en");
        posMappingProvider.setOverride("tagger.tagset", "abc");

        JCas jcas = JCasFactory.createJCas();

        posMappingProvider.configure(jcas.getCas());
        
        List<String> out = new ArrayList<>();
        for(String p : data){
            out.add(getCoarse(jcas, posMappingProvider, p));
        }
        
        return out;
    }

    private String getCoarse(JCas jcas, MappingProvider posMappingProvider, String string)
    {
        Type posTag = posMappingProvider.getTagType(string);
        POS pos = (POS) jcas.getCas().createAnnotation(posTag, 0, 1);
        pos.setPosValue(string);

        return pos.getClass().getSimpleName();
    }

    private List<String> readPredictions(File p)
        throws IOException
    {
        List<String> pre = new ArrayList<>();
        List<String> readLines = FileUtils.readLines(p);
        int i=0;
        for (String r : readLines) {
            if (r.isEmpty()) {
                continue;
            }
            if (r.startsWith("#") && i==0) {
                i++;
                continue;
            }
            pre.add(r.split("\t")[1]);
        }

        return pre;
    }

    private void evaluate(Set<String> trainVocab, List<String> testVocab, List<String> pred,
            List<Double> in, List<Double> out)
    {
        double correct_in = 0;
        double incorrect_in = 0;
        double correct_out = 0;
        double incorrect_out = 0;
        
        for (int i = 0; i < testVocab.size(); i++) {
            String string = testVocab.get(i);
            
            String[] split = string.split("\t");

            if (trainVocab.contains(split[0])) {
                if (pred.get(i).equals(split[1])) {
                    correct_in++;
                }
                else {
                    incorrect_in++;
                }
            }
            else {
                if (pred.get(i).equals(split[1])) {
                    correct_out++;
                }
                else {
                    incorrect_out++;
                }
            }

        }
        in.add(correct_in / (correct_in + incorrect_in));
        out.add(correct_out / (correct_out + incorrect_out));
    }

    private List<String> readTest(File test)
    {
        List<String> lines = new ArrayList<>();
        try {
            InputStreamReader streamReader = new InputStreamReader(new FileInputStream(test),
                    "UTF-8");
            BufferedReader br = new BufferedReader(streamReader);

            String next = null;
            while ((next = br.readLine()) != null) {

                if (next.isEmpty()) {
                    continue;
                }

                String word = extractUnit(next);
                String tag = extractTag(next);
                lines.add(word + "\t" + tag);
            }

            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return lines;
    }

    private String extractTag(String next)
    {
        String[] split = next.split("\t");
        return split[0];
    }

    private File buildFileLocation(StorageService store, String context, String fileName)
    {
        return store.locateKey(context, fileName);
    }

    private Set<String> extractVocab(File train)
    {
        Set<String> training = new HashSet<String>();
        try {
            InputStreamReader streamReader = new InputStreamReader(new FileInputStream(train),
                    "UTF-8");
            BufferedReader br = new BufferedReader(streamReader);

            String next = null;
            while ((next = br.readLine()) != null) {

                if (next.isEmpty()) {
                    continue;
                }
                String word = extractUnit(next);
                training.add(word);
            }

            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return training;
    }

    private String extractUnit(String next)
    {
        int start = next.indexOf(ID_FEATURE_NAME);
        int end = next.indexOf("\t", start);
        if (end == -1) {
            end = next.length();
        }
        start = next.lastIndexOf("_", end);

        String word = next.substring(start + 1, end);

        return word;
    }

    public final static String UNKNOWN_WORDS_FINE = "unknown_words_fine.txt";
    public final static String KNOWN_WORDS_FINE = "known_words_fine.txt";

    public static void writeUnknownKnownFine()
        throws IOException
    {

        Double avg_in = 0.0;
        for (Double i : in_fine) {
            avg_in += i;
        }
        avg_in /= in_fine.size();

        Double avg_out = 0.0;
        for (Double i : out_fine) {
            avg_out += i;
        }
        avg_out /= out_fine.size();

        FileUtils.write(new File(lastCrf, UNKNOWN_WORDS_FINE), String.format("%.1f", avg_out * 100));
        FileUtils.write(new File(lastCrf, KNOWN_WORDS_FINE), String.format("%.1f", avg_in * 100));
    }
    
    public final static String UNKNOWN_WORDS_COARSE = "unknown_words_coarse.txt";
    public final static String KNOWN_WORDS_COARSE = "known_words_coarse.txt";
    public static void writeUnknownKnownCoarse()
            throws IOException
        {

            Double avg_in = 0.0;
            for (Double i : in_coarse) {
                avg_in += i;
            }
            avg_in /= in_coarse.size();

            Double avg_out = 0.0;
            for (Double i : out_coarse) {
                avg_out += i;
            }
            avg_out /= out_coarse.size();

            FileUtils.write(new File(lastCrf, UNKNOWN_WORDS_COARSE), String.format("%.1f", avg_out * 100));
            FileUtils.write(new File(lastCrf, KNOWN_WORDS_COARSE), String.format("%.1f", avg_in * 100));
        }
}
