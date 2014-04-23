package com.asus.ctc.ie.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import com.asus.ctc.ie.config.GlobalProperties;
import com.asus.ctc.ie.datastructures.entities.Concept;
import com.asus.ctc.ie.datastructures.interfaces.Sentence;
import com.asus.ctc.ie.datastructures.senserelation.SenseRelation;
import com.asus.ctc.ie.datastructures.senserelation.SuperSenseWord;

import edu.stanford.nlp.trees.Tree;

public class SentenceNode implements Sentence,Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4303613612108392261L;
	private int paragraphNumber;
    private int sentenceNumber;
    private int sourceSentenceNumber;
    private List<SuperSenseWord> stanfordSupersenseTaggedSentence;
    private List<Concept> conceptsList;
    private String string;
    private String original_sentence;
    private Tree tree;
    private Tree sourceTree;
    private List<SentenceNode> simplifiedSentences;
    private SenseRelation supersenseRelation;
    private Map<String, Double> featureMap;
    private List<Double> featureValueList;
    private static List<String> featureNames;
    private boolean performedSimplification;

    public SentenceNode() {
	conceptsList = new ArrayList<Concept>();
	featureMap = new HashMap<String, Double>();
	performedSimplification = false;
    }

    public SentenceNode deepCopy() {

	SentenceNode sentence = new SentenceNode();
	sentence.setOriginal_sentence(original_sentence);
	sentence.setString(string);
	sentence.setParagraphNumber(paragraphNumber);
	sentence.setSentenceNumber(sentenceNumber);
	sentence.setTree(tree.deepCopy());
	sentence.setSourceTree(sourceTree.deepCopy());
	sentence.copyFeatures(featureMap);
	return sentence;

    }

    public int getParagraphNumber() {
	return paragraphNumber;
    }

    public void setParagraphNumber(int paragraphNumber) {
	this.paragraphNumber = paragraphNumber;
    }

    public int getSentenceNumber() {
	return sentenceNumber;
    }

    public void setSentenceNumber(int sentenceNumber) {
	this.sentenceNumber = sentenceNumber;
    }

    public String getString() {
	return string;
    }

    public void setString(String string) {
	this.string = string;
    }

    public Tree getTree() {
	return tree;
    }

    public void setTree(Tree tree) {
	this.tree = tree;
    }

    public int getSourceSentenceNumber() {
	return sourceSentenceNumber;
    }

    public void setSourceSentenceNumber(int sourceSentenceNumber) {
	this.sourceSentenceNumber = sourceSentenceNumber;
    }

    public List<SuperSenseWord> getStanfordSupersenseTaggedSentence() {
	return stanfordSupersenseTaggedSentence;
    }

    public void setStanfordSupersenseTaggedSentence(
	    List<SuperSenseWord> stanfordSupersenseTaggedSentence) {
	this.stanfordSupersenseTaggedSentence = stanfordSupersenseTaggedSentence;
    }

    public String getOriginal_sentence() {
	return original_sentence;
    }

    public void setOriginal_sentence(String original_sentence) {
	this.original_sentence = original_sentence;
    }

    @Override
    public String toString() {
	StringBuffer display = new StringBuffer();
	String delimiter_dash = "------------------------------------------------------------------------------------------------------------------\n";

	display.append("#Para:Sentence = " + paragraphNumber + ":"
		+ sourceSentenceNumber + "\n");
	display.append("#Sentence: " + string + "\n");
	
	if(stanfordSupersenseTaggedSentence!=null && stanfordSupersenseTaggedSentence.size()>0)
	{display.append("#Tags: " + stanfordSupersenseTaggedSentence + "\n");}
	
	
	display.append("#Tree: " + tree + "\n");
	if (!isPerformedSimplification())
	    display.append("#SourceSentence: " + original_sentence + "\n");

	if (conceptsList.size() > 0) {
	    display.append("#Entity List:  ");
	    display.append(conceptsList.toString() + "\n");
	}
	if (isPerformedSimplification()) {
	    display.append("\n#Sentence Simplification :  "
		    + isPerformedSimplification() + "\n");
	    display.append("\n#Simplified Sentences :" + "\n");
	    display.append(delimiter_dash);
	    for (SentenceNode simplified : simplifiedSentences) {
		display.append(printSimplifiedSentence(simplified));
	    }
	    display.append(delimiter_dash);
	} else {
	    display.append("#Sentence Simplification:  "
		    + isPerformedSimplification() + "\n");

	    if (supersenseRelation != null) {
		display.append("#Relation Data:  ");

		display.append(supersenseRelation.toString() + "\n");
	    }

	}

	return display.toString();
    }

    private String printSimplifiedSentence(SentenceNode simplified) {
	StringBuffer display = new StringBuffer();

	display.append("#Para:Sentence:Simplified =");
	display.append(simplified.getParagraphNumber() + ":"
		+ simplified.getSourceSentenceNumber() + ":"
		+ simplified.getSentenceNumber() + "\n");
	display.append("#Sentence: " + simplified.getString() + "\n");
	display.append("#SourceSentence: " + original_sentence + "\n");
	display.append("#ExtractionFeatures: "
		+ simplified.getFeatureMap().toString() + "\n");
	display.append("#Tags: "
		+ simplified.getStanfordSupersenseTaggedSentence() + "\n");
	display.append("#Tree: " + simplified.getTree() + "\n");
	//
	if (simplified.getConceptsList().size() > 0) {
	    display.append("#Entity List:  ");
	    display.append(simplified.getConceptsList().toString() + "\n");
	}

	SenseRelation relation = simplified.getSupersenseRelation();
	if (relation != null) {
	    display.append("#Relation Data:  \n");

	    display.append(relation.toString() + "\n");
	}

	return display.toString();
    }

    public List<SentenceNode> getSimplifiedSentences() {
	return simplifiedSentences;
    }

    public void setSimplifiedSentences(List<SentenceNode> simplifiedSentences) {
	this.simplifiedSentences = simplifiedSentences;
    }

    public List<Concept> getConceptsList() {
	return conceptsList;
    }

    public void setConceptsList(List<Concept> conceptsList) {
	this.conceptsList = conceptsList;
    }

    public Tree getSourceTree() {
	return sourceTree;
    }

    public void setSourceTree(Tree sourceTree) {
	this.sourceTree = sourceTree;
    }

    public Map<String, Double> getFeatureMap() {
	return featureMap;
    }

    public void setFeatureMap(Map<String, Double> featureMap) {
	this.featureMap = featureMap;
    }

    /**
     * @param key
     * @param value
     */
    public void setFeatureValue(String key, Double value) {
	featureMap.put(key, value);
    }

    /**
     * @param features
     */
    public void copyFeatures(Map<String, Double> features) {
	this.featureMap.putAll(features);
    }

    /**
     * @param featureValueList
     */
    public void setFeatureValues(List<Double> featureValueList) {
	this.featureValueList = featureValueList;
	Double value;
	List<String> names = getFeatureNames();

	// only populate the feature map if it looks like we are still using the
	// same feature set
	if (names.size() != featureValueList.size()) {
	    return;
	}

	for (int i = 0; i < featureValueList.size(); i++) {
	    value = featureValueList.get(i);
	    featureMap.put(names.get(i), value);
	}
    }

    protected static List<Double> createFeatureValueList(
	    Map<String, Double> featureNameToValueMap) {
	List<Double> res = new ArrayList<Double>();
	Double val;

	for (String name : getFeatureNames()) {
	    val = featureNameToValueMap.get(name);
	    if (val == null)
		val = 0.0;
	    res.add(val);
	}

	return res;
    }

    /**
     * returns the index into the featureNames list of the given name (mainly
     * for testing purposes)
     * 
     * @param featurename
     * @return featurevalue
     */
    public static int getFeatureValueIndex(String featurename) {
	return getFeatureNames().indexOf(featurename);
    }

    /**
     * @return List<String>
     */
    public static List<String> getFeatureNames() {
	if (featureNames == null) {
	    featureNames = new ArrayList<String>();

	    String defaultFeatureNames = "performedNPClarification;questionLength;sourceLength;answerPhraseLength;negation;whQuestion;whQuestionPrep;whQuestionWho;whQuestionWhat;whQuestionWhere;whQuestionWhen;whQuestionWhose;whQuestionHowMuch;whQuestionHowMany;isSubjectMovement;removedLeadConjunctions;removedAsides;removedLeadModifyingPhrases;extractedFromAppositive;extractedFromFiniteClause;extractedFromParticipial;extractedFromRelativeClause;mainVerbPast;mainVerbPresent;mainVerbFuture;mainVerbCopula;meanWordFreqSource;meanWordFreqAnswer;numNPsQuestion;numProperNounsQuestion;numQuantitiesQuestion;numAdjectivesQuestion;numAdverbsQuestion;numPPsQuestion;numSubordinateClausesQuestion;numConjunctionsQuestion;numPronounsQuestion;numNPsAnswer;numProperNounsAnswer;numQuantitiesAnswer;numAdjectivesAnswer;numAdverbsAnswer;numPPsAnswer;numSubordinateClausesAnswer;numConjunctionsAnswer;numPronounsAnswer;numVagueNPsSource;numVagueNPsQuestion;numVagueNPsAnswer;numLeadingModifiersQuestion";
	    String[] names = GlobalProperties.getProperties(1)
		    .getProperty("featureNames", defaultFeatureNames)
		    .split(";");

	    boolean includeGreaterThanFeatures = new Boolean(GlobalProperties
		    .getProperties(1).getProperty("includeGreaterThanFeatures",
			    "true"));

	    Arrays.sort(names);

	    for (int i = 0; i < names.length; i++) {
		featureNames.add(names[i]);
		if (includeGreaterThanFeatures && names[i].matches("num.+")) {
		    for (int j = 0; j < 5; j++) {
			featureNames.add(names[i] + "GreaterThan" + j);
		    }
		} else if (includeGreaterThanFeatures
			&& names[i].matches("length.+")) {
		    for (int j = 0; j < 32; j += 4) {
			featureNames.add(names[i] + "GreaterThan" + j);
		    }
		}
	    }

	    Collections.sort(featureNames);

	}

	return featureNames;
    }

    /**
     * @param featureName
     * @return
     */
    public double getFeatureValue(String featureName) {
	Double val = featureMap.get(featureName);
	if (val == null) {
	    val = 0.0;
	}
	return val.doubleValue();
    }

    public SenseRelation getSupersenseRelation() {
	return supersenseRelation;
    }

    public void setSupersenseRelation(SenseRelation supersenseRelation) {
	this.supersenseRelation = supersenseRelation;
    }

    public boolean isPerformedSimplification() {
	return performedSimplification;
    }

    public void setPerformedSimplification(boolean performedSimplification) {
	this.performedSimplification = performedSimplification;
    }

    @Override
    public boolean equals(Object obj) {
	SentenceNode sn = (SentenceNode) obj;

	if (tree.equals(sn.getTree())) {
	    return true;
	}

	return false;
    }

}
