

/* First created by JCasGen Thu Aug 25 10:53:34 CEST 2016 */
package de.unidue.ltl.evalita.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Aug 25 10:54:00 CEST 2016
 * XML source: /Users/toobee/Documents/Eclipse/lang-tech/projects/de.unidue.ltl.evalita/src/main/resources/desc/type/EvalitaId.xml
 * @generated */
public class EvalitaId extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(EvalitaId.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected EvalitaId() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public EvalitaId(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public EvalitaId(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public EvalitaId(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: tweetId

  /** getter for tweetId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTweetId() {
    if (EvalitaId_Type.featOkTst && ((EvalitaId_Type)jcasType).casFeat_tweetId == null)
      jcasType.jcas.throwFeatMissing("tweetId", "de.unidue.ltl.evalita.type.EvalitaId");
    return jcasType.ll_cas.ll_getStringValue(addr, ((EvalitaId_Type)jcasType).casFeatCode_tweetId);}
    
  /** setter for tweetId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTweetId(String v) {
    if (EvalitaId_Type.featOkTst && ((EvalitaId_Type)jcasType).casFeat_tweetId == null)
      jcasType.jcas.throwFeatMissing("tweetId", "de.unidue.ltl.evalita.type.EvalitaId");
    jcasType.ll_cas.ll_setStringValue(addr, ((EvalitaId_Type)jcasType).casFeatCode_tweetId, v);}    
  }

    