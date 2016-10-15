package de.unidue.ltl.evalita.learningcurve;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;

public class AccuracyPerWordClass
    extends BatchReportBase
    implements Constants
{

    static String FINE_OUTPUT_FILE = "fineWordClassPerformance.txt";
    static String COARSE_OUTPUT_FILE = "coarseWordClassPerformance.txt";
    public static String mappingFile;

    public void execute()
        throws Exception
    {
        StorageService sv = getContext().getStorageService();
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().contains("TestTask")) {
                File prediction = sv.locateKey(subcontext.getId(), new CRFSuiteAdapter().getFrameworkFilename(
                        AdapterNameEntries.predictionsFile));
                StorageService storageService = getContext().getStorageService();

                String reportFine = generateFineWordClassReport(prediction);
                String reportCoarse = generateCoarseWordClassReport(prediction);

                File fineTargetFile = storageService.locateKey(getContext().getId(),
                        FINE_OUTPUT_FILE);
                FileUtils.writeStringToFile(fineTargetFile, reportFine);

                File coarseTargetFile = storageService.locateKey(getContext().getId(),
                        COARSE_OUTPUT_FILE);
                FileUtils.writeStringToFile(coarseTargetFile, reportCoarse);
            }
        }
    }

    private String generateCoarseWordClassReport(File locateKey)
        throws UIMAException, IOException
    {
        Map<String, WordClass> wcp = getCoarseWcPerformances(locateKey);

        StringBuilder sb = new StringBuilder();
        List<String> keySet = new ArrayList<String>(wcp.keySet());
        Collections.sort(keySet);

        sb.append(String.format("#%10s\t%5s\t%5s\n", "Class", "Occr", "Accr"));
        for (String k : keySet) {
            WordClass wc = wcp.get(k);
            double accuracy = wc.getCorrect() / wc.getN() * 100;

            sb.append(String.format("%10s", k) + "\t" + String.format("%5d", wc.getN().intValue())
                    + "\t" + String.format("%5.1f", accuracy));
            sb.append("\n");
        }

        return sb.toString();
    }

    private Map<String, WordClass> getCoarseWcPerformances(File locateKey)
        throws UIMAException, IOException
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

        Map<String, WordClass> wcp = new HashMap<>();

        List<String> lines = FileUtils.readLines(locateKey);

        for (String l : lines) {
            if (l.startsWith("#") || l.isEmpty()) {
                continue;
            }
            String[] split = l.split("\t");

            String prediction = getCoarse(jcas, posMappingProvider, split[0]);
            String gold = getCoarse(jcas, posMappingProvider, split[1]);

            WordClass wordClass = wcp.get(gold);
            if (wordClass == null) {
                wordClass = new WordClass();
            }
            if (gold.equals(prediction)) {
                wordClass.correct();
            }
            else {
                wordClass.incorrect();
            }
            wcp.put(gold, wordClass);
        }
        return wcp;
    }

    private static String getCoarse(JCas jcas, MappingProvider posMappingProvider, String fineGold)
    {
        Type posTag = posMappingProvider.getTagType(fineGold);
        POS pos = (POS) jcas.getCas().createAnnotation(posTag, 0, 1);
        pos.setPosValue(fineGold);

        return pos.getClass().getSimpleName();
    }

    private String generateFineWordClassReport(File locateKey)
        throws IOException
    {
        Map<String, WordClass> wcp = getFineWcPerformances(locateKey);

        StringBuilder sb = new StringBuilder();
        List<String> keySet = new ArrayList<String>(wcp.keySet());
        Collections.sort(keySet);

        sb.append(String.format("#%10s\t%5s\t%5s\n", "Class", "Occr", "Accr"));
        for (String k : keySet) {
            WordClass wc = wcp.get(k);
            double accuracy = wc.getCorrect() / wc.getN() * 100;

            sb.append(String.format("%10s", k) + "\t" + String.format("%5d", wc.getN().intValue())
                    + "\t" + String.format("%5.1f", accuracy));
            sb.append("\n");
        }

        return sb.toString();

    }

    private Map<String, WordClass> getFineWcPerformances(File locateKey)
        throws IOException
    {
        Map<String, WordClass> wcp = new HashMap<>();

        List<String> lines = FileUtils.readLines(locateKey);

        for (String l : lines) {
            if (l.startsWith("#") || l.isEmpty()) {
                continue;
            }
            String[] split = l.split("\t");

            String prediction = split[0];
            String gold = split[1];

            WordClass wordClass = wcp.get(gold);
            if (wordClass == null) {
                wordClass = new WordClass();
            }
            if (gold.equals(prediction)) {
                wordClass.correct();
            }
            else {
                wordClass.incorrect();
            }
            wcp.put(gold, wordClass);
        }
        return wcp;
    }

    class WordClass
    {
        double correct = 0;
        double incorrect = 0;

        public Double getN()
        {
            return correct + incorrect;
        }

        public Double getCorrect()
        {
            return correct;
        }

        public void correct()
        {
            correct++;
        }

        public void incorrect()
        {
            incorrect++;
        }
    }

}
