package de.unidue.ltl.evalita.use;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.evalita.type.EvalitaId;

public class EvalitaFormatWriter
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_TARGET_FILE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_TARGET_FILE, mandatory = true)
    private File target;

    BufferedWriter out;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), "UTF-8"));
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas arg0)
        throws AnalysisEngineProcessException
    {
        for (Sentence s : JCasUtil.select(arg0, Sentence.class)) {
            EvalitaId evalita = JCasUtil.selectCovered(arg0, EvalitaId.class, s.getBegin(), s.getEnd()).get(0);
            
            try {
                out.write(evalita.getTweetId()+"\n");
                
                for(Token t : JCasUtil.selectCovered(arg0, Token.class, s.getBegin(), s.getEnd())){
                    out.write(t.getCoveredText() + "\t" + t.getPos().getPosValue()+"\n");
                }
                out.write("\n");
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException();
            }
        }

    }
    
    @Override
    public void collectionProcessComplete(){
        IOUtils.closeQuietly(out);
    }

}
