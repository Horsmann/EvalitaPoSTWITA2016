package de.unidue.ltl.evalita.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

public class SplitUtil
{

    public static List<String> splitIntoNFilesIntoTemporaryFolder(String corpusFolder,
            String fileEnding, boolean shuffle, int split)
                throws Exception
    {
        List<String> outfiles = new ArrayList<String>();

        List<String> readLines = loadFileContents(corpusFolder, fileEnding, shuffle);
        int splitPoint = readLines.size() / split;

        String tempDir = System.getProperty("java.io.tmpdir");
        String currentRunId = "tmpFolder_" + System.currentTimeMillis();
        String outPath = tempDir + "/" + currentRunId;

        File out = new File(outPath);
        out.mkdir();
        out.deleteOnExit();

        int j = 0;
        List<String> buffer = new ArrayList<String>();
        for (int i = 0; i < split; i++) {

            int nrToken = 0;
            boolean doBreak = false;
            for (; j < readLines.size(); j++) {
                String string = readLines.get(j);

                if (string.isEmpty() && doBreak) {
                    j++;
                    break;
                }

                buffer.add(string);
                nrToken++;

                if (nrToken >= splitPoint) {
                    doBreak = true;
                }
            }

            String filePath = outPath + "/" + i + ".data";

            if (buffer.isEmpty()) {
                throw new IllegalStateException("In iteration [" + i
                        + "] no data was left (e.g. number of splits to high, keeping sequences together might leave some folds with no data)");
            }

            FileUtils.writeLines(new File(filePath), buffer);

            outfiles.add(filePath);
            buffer = new ArrayList<String>();
        }

        return outfiles;
    }

    private static List<String> loadFileContents(String corpusFolder, String fileEnding,
            boolean shuffle)
                throws IOException
    {
        List<List<String>> all = new ArrayList<List<String>>();

        for (File f : new File(corpusFolder).listFiles()) {
            if (f.isDirectory()) {
                continue;
            }
            if (f.isHidden()) {
                continue;
            }
            if (!f.getAbsolutePath().endsWith(fileEnding)) {
                continue;
            }
            List<String> readLines = FileUtils.readLines(f, "utf-8");

            List<String> sequence = new ArrayList<String>();
            for (String s : readLines) {
                if (s.isEmpty()) {
                    all.add(sequence);
                    sequence = new ArrayList<String>();
                    continue;
                }
                sequence.add(s);
            }

            if (!sequence.isEmpty()) {
                all.add(sequence);
            }
        }

        if (shuffle) {
            Collections.shuffle(all, new Random(System.nanoTime()));
        }

        List<String> out = new ArrayList<String>();

        for (int i = 0; i < all.size(); i++) {
            List<String> list = all.get(i);
            for (String e : list) {
                out.add(e);
            }
            if (i + 1 < all.size()) {
                out.add("");
            }
        }

        return out;
    }

    public static List<List<String>> createLearningCurvesplits(List<String> files)
        throws Exception
    {

        List<List<String>> allSplits = new ArrayList<List<String>>();

        for (int i = 0; i < files.size(); i++) {
            int testIdx = i;
            String tempDir = System.getProperty("java.io.tmpdir");
            String currentRunId = "learningCurve_" + testIdx + "_" + System.currentTimeMillis();
            String outPath = tempDir + "/" + currentRunId;

            String trainFolderPath = outPath + "/train";
            File trainFolder = new File(trainFolderPath);
            trainFolder.mkdirs();
            trainFolder.deleteOnExit();

            String testFolderPath = outPath + "/test";
            File testFolder = new File(testFolderPath);
            testFolder.mkdirs();
            testFolder.deleteOnExit();

            // copy test file outside of loop
            File f = new File(files.get(testIdx));
            File testFile = new File(testFolder.getAbsolutePath() + "/" + f.getName());
            Files.copy(f, testFile);

            List<String> trainFolders = new ArrayList<String>();
            for (int j = 0; j < files.size(); j++) {
                String subTrain = trainFolderPath + "/" + j;
                new File(subTrain).mkdirs();
                trainFolders.add(subTrain);
            }

            int j = 0;
            for (int k = 0; k < files.size(); k++) {
                j = k;
                String currFile = files.get(k);
                for (; j < trainFolders.size(); j++) {
                    String folder = trainFolders.get(j);
                    if (j == testIdx) {
                        continue;
                    }
                    String targetFile = folder + "/" + k + ".data";
                    Files.copy(new File(currFile), new File(targetFile));
                }
            }
            List<String> split = new ArrayList<String>();
            split.add(testFolder.getAbsolutePath());

            // throw out the file/folders that are not needed
            for (String fold : trainFolders) {
                new File(fold + "/" + testIdx + ".data").delete();
            }
            new File(trainFolders.get(testIdx)).delete();
            trainFolders.remove(testIdx);

            split.addAll(trainFolders);

            allSplits.add(split);
        }

        return allSplits;
    }

    /**
     * Creates N splits into train test of the provided data. The length of the list and the number
     * of folds must be equal.
     */
    public static List<String[]> createCrossValidationSplits(String nameRoot, List<String> split,
            int numFolds)
                throws IOException
    {

        if (numFolds != split.size()) {
            throw new IllegalArgumentException(
                    "Number of folds must be equal to the size of the list");
        }

        String tempDir = System.getProperty("java.io.tmpdir");

        List<String[]> out = new ArrayList<>();
        // build folder structure
        for (int i = 0; i < numFolds; i++) {
            File root = new File(tempDir + "/" + nameRoot + System.nanoTime());
            root.mkdirs();
            File train = new File(root, "train");
            train.mkdirs();
            File test = new File(root, "test");
            test.mkdirs();

            for (int j = 0; j < numFolds; j++) {

                String name = new File(split.get(j)).getName();
                int idx = name.indexOf(".");
                if (idx > 0) {
                    name = name.substring(0, idx);
                }

                if (i == j) {
                    Files.copy(new File(split.get(j)), new File(test, name + ".txt"));
                }
                else {
                    Files.copy(new File(split.get(j)), new File(train, name + ".txt"));
                }
            }
            out.add(new String[] { train.getAbsolutePath(), test.getAbsolutePath() });
        }

        return out;
    }

}
