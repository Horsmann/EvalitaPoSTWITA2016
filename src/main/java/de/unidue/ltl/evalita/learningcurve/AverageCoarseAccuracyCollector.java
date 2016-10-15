package de.unidue.ltl.evalita.learningcurve;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class AverageCoarseAccuracyCollector
    extends BatchReportBase
    implements Constants
{

    public static final String SINGLE_RESULT_FINE = "totalFineAccuracy.txt";
    public static final String SINGLE_RESULT_COARSE = "totalCoarseAccuracy.txt";
    public static List<File> finePredictionFiles = new ArrayList<>();
    public static File lastFolder;

    public void execute()
        throws Exception
    {
        StorageService sv = getContext().getStorageService();
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getId().contains("TestTask")) {
                lastFolder = sv.locateKey(subcontext.getId(), "");
                File prediction = sv.locateKey(subcontext.getId(), new CRFSuiteAdapter()
                        .getFrameworkFilename(AdapterNameEntries.predictionsFile));
                finePredictionFiles.add(prediction);
            }
        }
    }

    public static void buildReport(List<File> fine, String mappingPath, File fineOutfile,
            File coarseOutfile)
                throws IOException, UIMAException
    {

        doFine(fine, fineOutfile);

        doCoarse(fine, mappingPath, coarseOutfile);
    }

    private static void doCoarse(List<File> fine, String mappingPath, File coarseOutfile)
        throws IOException, UIMAException
    {
        MappingProvider posMappingProvider = new MappingProvider();
        posMappingProvider.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/"
                        + "core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
        posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        posMappingProvider.setOverride(MappingProvider.LOCATION, mappingPath);
        posMappingProvider.setOverride(MappingProvider.LANGUAGE, "en");
        posMappingProvider.setOverride("tagger.tagset", "abc");

        JCas jcas = JCasFactory.createJCas();

        posMappingProvider.configure(jcas.getCas());

        List<Double> acc = new ArrayList<>();
        for (File f : fine) {
            List<String> readLines = FileUtils.readLines(f);
            Double correct = new Double(0);
            Double incorrect = new Double(0);
            for (String line : readLines) {
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.isEmpty()) {
                    continue;
                }
                String[] split = line.split("\t");
                String fineGold = split[0];
                String finePred = split[1];

                String coarseGold = getCoarse(jcas, posMappingProvider, fineGold);
                String coarsePred = getCoarse(jcas, posMappingProvider, finePred);

                if (coarseGold.equals(coarsePred)) {
                    correct++;
                }
                else {
                    incorrect++;
                }
            }
            double coarseAcc = correct / (correct + incorrect) * 100;
            acc.add(coarseAcc);
        }

        Double total = new Double(0);
        for (Double d : acc) {
            total += d;
        }
        total /= acc.size();

        StringBuilder sb = new StringBuilder();
        acc.forEach(x -> sb.append(String.format("%5.2f", x) + "\n"));
        sb.append("--------\n");
        sb.append(String.format("%5.2f", total) + "\n");

        FileUtils.writeStringToFile(coarseOutfile, sb.toString());
        FileUtils.write(new File(coarseOutfile.getParentFile(), SINGLE_RESULT_COARSE),
                String.format("%5.2f", total));
    }

    private static void doFine(List<File> fine, File fineOutfile)
        throws IOException
    {
        List<Double> acc = new ArrayList<>();
        for (File f : fine) {
            List<String> readLines = FileUtils.readLines(f);
            Double correct = new Double(0);
            Double incorrect = new Double(0);
            for (String line : readLines) {
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.isEmpty()) {
                    continue;
                }
                String[] split = line.split("\t");
                String fineGold = split[0];
                String finePred = split[1];

                if (fineGold.equals(finePred)) {
                    correct++;
                }
                else {
                    incorrect++;
                }
            }
            double a = correct / (correct + incorrect) * 100;
            acc.add(a);
        }

        Double total = new Double(0);
        for (Double d : acc) {
            total += d;
        }
        total /= acc.size();

        StringBuilder sb = new StringBuilder();
        acc.forEach(x -> sb.append(String.format("%5.2f", x) + "\n"));
        sb.append("--------\n");
        sb.append(String.format("%5.2f", total) + "\n");

        FileUtils.writeStringToFile(fineOutfile, sb.toString());

        FileUtils.write(new File(fineOutfile.getParentFile(), SINGLE_RESULT_FINE),
                String.format("%5.2f", total));
    }

    private static String getCoarse(JCas jcas, MappingProvider posMappingProvider, String fineGold)
    {
        Type posTag = posMappingProvider.getTagType(fineGold);
        POS pos = (POS) jcas.getCas().createAnnotation(posTag, 0, 1);
        pos.setPosValue(fineGold);

        return pos.getClass().getSimpleName();
    }

}
