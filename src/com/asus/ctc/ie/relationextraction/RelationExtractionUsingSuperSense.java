package com.asus.ctc.ie.relationextraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asus.ctc.ie.config.GlobalProperties;
import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.datastructures.entities.Concept;
import com.asus.ctc.ie.datastructures.senserelation.SenseRelation;
import com.asus.ctc.ie.datastructures.senserelation.SuperSenseWord;
import com.asus.ctc.ie.entityextractor.EntityExtractionUsingSuperSense;
import com.asus.ctc.ie.relationextraction.interfaces.RelationExtractionFromText;
import com.asus.ctc.ie.utilities.AnalysisUtilities;

public class RelationExtractionUsingSuperSense implements RelationExtractionFromText {
    private final static Logger log = LoggerFactory
	    .getLogger(EntityExtractionUsingSuperSense.class);

    // lemmatize the verb in the predicate phrase
    boolean lemmatize;

    public RelationExtractionUsingSuperSense() {
	// read IE processing configuration
	Properties rem = GlobalProperties.getProperties(2);

	lemmatize = Boolean.parseBoolean(rem.getProperty("lemmatize"));
    }

    @Override
    public void extractRelations(TextNode textNode) {
	// TODO Auto-generated method stub
	performRelationExtraction(textNode);
    }

    /**
     * @param TextNode
     */
    public void performRelationExtraction(TextNode textnode) {
	StopWatch sw = new StopWatch();
	sw.start();

	List<SentenceNode> sentences = textnode.getAllSentences();

	extractRelationsUsingSenseTagsMethod(sentences);
	sw.stop();

	log.info("Total Time taken for Relation Extraction:  " + sw.toString());
    }

    /**
     * Relation extraction method using sense tags
     * 
     * @param sentences
     */
    private void extractRelationsUsingSenseTagsMethod(
	    List<SentenceNode> sentences) {

	for (SentenceNode sentenceNode : sentences) {

	    // if sentence does not have simplified sentences, perform relation
	    // extraction on original sentence
	    if (!sentenceNode.isPerformedSimplification()) {
		RelationExtraction(sentenceNode);
	    }

	    else {
		// perform relation extraction for each simplified sentence;
		List<SentenceNode> simplifiedList = sentenceNode
			.getSimplifiedSentences();
		for (SentenceNode sentenceNode2 : simplifiedList) {
		    RelationExtraction(sentenceNode2);
		}
	    }

	}

    }

    /**
     * Perform relation extraction using super sense tagged information
     * 
     * @param sentenceNode
     */
    private void RelationExtraction(SentenceNode sentenceNode) {
	// TODO Auto-generated method stub

	SenseRelation sRelation = new SenseRelation();

	/**
	 * Extract the Predicate from the EmailSentence.
	 */
	extractPredicate(sRelation, sentenceNode);

	/*
	 * The subject/subjects are from index 0 to
	 */

	setConceptsListForSubjectObject(sRelation, sentenceNode);

	extractSubjectAndObject(sentenceNode, sRelation);

	lemmatize(sRelation);

	sentenceNode.setSupersenseRelation(sRelation);

    }

    private void extractSubjectAndObject(SentenceNode sentenceNode,
	    SenseRelation sRelation) {
	// TODO Auto-generated method stub
	List<SuperSenseWord> sentenceSuperSenseList = sentenceNode
		.getStanfordSupersenseTaggedSentence();

	int subjectPhraseIndex = sRelation.getSubjectPhraseIndex();
	int objectPhraseIndex = sRelation.getObjectPhraseIndex();

	List<SuperSenseWord> subjectPhraseList = new ArrayList<SuperSenseWord>();
	List<SuperSenseWord> objectPhraseList = new ArrayList<SuperSenseWord>();
	List<SuperSenseWord> verbPhraseList = new ArrayList<SuperSenseWord>();

	for (int i = 0; i <= subjectPhraseIndex; i++) {
	    SuperSenseWord sw = sentenceSuperSenseList.get(i);
	    subjectPhraseList.add(sw);
	}

	if (objectPhraseIndex != -1) {
	    for (int i = objectPhraseIndex; i < sentenceSuperSenseList.size(); i++) {
		SuperSenseWord sw = sentenceSuperSenseList.get(i);
		objectPhraseList.add(sw);
	    }

	    for (int i = subjectPhraseIndex + 1; i < objectPhraseIndex; i++) {
		SuperSenseWord sw = sentenceSuperSenseList.get(i);
		verbPhraseList.add(sw);
	    }

	}

	else {
	    for (int i = subjectPhraseIndex + 1; i < sentenceSuperSenseList
		    .size(); i++) {
		SuperSenseWord sw = sentenceSuperSenseList.get(i);
		if (!sw.getPosTag().equals("."))
		    verbPhraseList.add(sw);
	    }

	}

	String subjectPhrase = REUtilities.getInstance()
		.createStringFromStanfordSuperSenseWordList(subjectPhraseList)
		.trim();

	String objectPhrase = REUtilities.getInstance()
		.createStringFromStanfordSuperSenseWordList(objectPhraseList)
		.trim();
	if (objectPhrase.endsWith(".")) {
	    objectPhrase = objectPhrase.substring(0, objectPhrase.length() - 1)
		    .trim();

	}

	String predicatePhrase = REUtilities.getInstance()
		.createStringFromStanfordSuperSenseWordList(verbPhraseList)
		.trim();
	if (predicatePhrase.endsWith(".")) {
	    predicatePhrase = predicatePhrase.substring(0,
		    predicatePhrase.length() - 1).trim();

	}

	sRelation.setSubjectSenseList(subjectPhraseList);
	sRelation.setObjectSenseList(objectPhraseList);
	sRelation.setPredicateSenseList(verbPhraseList);

	sRelation.setSubjectPhrase(subjectPhrase);
	sRelation.setObjectPhrase(objectPhrase);
	sRelation.setPredicatePhrase(predicatePhrase);

    }

    private void extractPredicate(SenseRelation sRelation,
	    SentenceNode sentenceNode) {
	// TODO Auto-generated method stub
	boolean mainVerbFound = false;
	boolean supersenseVerb = false;

	int objectPhraseIndex = -1;
	int subjectPhraseIndex = -1;
	int verbIndex = -1;

	String relation = "";
	List<SuperSenseWord> list = sentenceNode
		.getStanfordSupersenseTaggedSentence();

	for (int i = 0; i < list.size(); i++) {
	    SuperSenseWord word = list.get(i);

	    if (word.getSstag().startsWith("B-verb")) {
		supersenseVerb = true;
		verbIndex = i;
		// sRelation.setVerbIndex(verbIndex);
		subjectPhraseIndex = verbIndex - 1;
		break;

	    }

	}

	// If the tagged verb is present

	if (supersenseVerb) {

	    int lastNounttagIndexBeforeVerb = 0;

	    for (int i = 0; i < list.size() && !mainVerbFound; i++) {

		SuperSenseWord word = list.get(i);

		// Get the last count of noun tag before the verb is found
		if (word.getSstag().startsWith("B-noun.")
			|| word.getSstag().equals("PERSON")) {
		    lastNounttagIndexBeforeVerb = i;

		}

		// Extract Predicate First
		if (word.getSstag().startsWith("B-verb")) {
		    mainVerbFound = true;
		    // verbIndex = i;
		    String rel = word.getToken();

		    // PreModifiers:

		    // find the first instance of B-noun.*
		    for (int z = verbIndex - 1; z > lastNounttagIndexBeforeVerb; z--) {
			SuperSenseWord word1 = list.get(z);

			String posTag1 = word1.getPosTag();

			// Scope for improvement here.

			if (posTag1.startsWith("V")) {
			    // This should be the index for the verb now.
			    // sRelation.setVerbIndex(z);
			    subjectPhraseIndex = z - 1;
			    rel = word1.getToken() + " " + rel;
			}

			if (posTag1.equals("MD") || posTag1.equals("RB")) {
			    subjectPhraseIndex = z - 1;
			    rel = word1.getToken() + " " + rel;
			}

		    }

		    // Now find the 1st occurent of B-noun tag on the right side
		    // of main verb.

		    // boolean foundObjectOnRightSideOfVerb = false;

		    for (int j = verbIndex + 1; j < list.size(); j++) {
			SuperSenseWord word1 = list.get(j);
			String ssTag = word1.getSstag();
			String posTag = word1.getPosTag();
			/*
			 * If 1)SStag is not noun 2)POSTag is IN
			 */
			/*
			 * if (!(ssTag.startsWith("B-noun.")) ) { rel += " " +
			 * list.get(j).getToken(); }
			 */

			/**
			 * Lets first check if there is something present on the
			 * right side of the main verb. E.g John could lose.
			 * Verb here is lose, but there is no object so no point
			 * looking on the right hand side.
			 */

			if (posTag.equals(".") && (j == list.size() - 1)) {
			    relation = rel;
			    sRelation.setSubjectPhraseIndex(subjectPhraseIndex);

			    if (objectPhraseIndex == -1) {
				objectPhraseIndex = verbIndex + 1;
			    }

			    sRelation.setObjectPhraseIndex(objectPhraseIndex);
			    sRelation.setPredicatePhrase(relation);
			    sRelation.setPredicateType(word.getSstag());
			    break;
			}

			if (posTag.equals("IN")
				|| ssTag.equals("B-noun.relation")
				|| ssTag.equals("B-noun.possession")
				|| posTag.equals("MD")
				|| posTag.startsWith("V")
				|| posTag.startsWith("DT")
				|| (posTag.equals("NN") && (!ssTag
					.startsWith("B-noun.") || !ssTag
					.equals("PERSON")))
				|| posTag.equals("TO")) {
			    rel += " " + list.get(j).getToken();

			}

			if ((ssTag.startsWith("B-noun.") || ssTag
				.equals("PERSON"))
				&& !ssTag.equals("B-noun.relation")
				&& !ssTag.equals("B-noun.possession")) {

			    for (int k = j - 1; k > verbIndex; k--) {
				SuperSenseWord word2 = list.get(k);
				String ssTag2 = word2.getSstag();
				String posTag2 = word2.getPosTag();

				if (posTag2.equals("IN")
					|| ssTag2.equals("B-noun.relation")
					|| posTag2.equals("MD")
					|| posTag2.startsWith("V")) {
				    objectPhraseIndex = k + 1;

				    break;
				}
			    }

			    relation = rel;
			    sRelation.setSubjectPhraseIndex(subjectPhraseIndex);

			    if (objectPhraseIndex == -1) {
				objectPhraseIndex = verbIndex + 1;
			    }
			    sRelation.setObjectPhraseIndex(objectPhraseIndex);
			    sRelation.setPredicatePhrase(relation);
			    sRelation.setPredicateType(word.getSstag());
			    break;

			}

		    }
		}
	    }

	}

	else {
	    for (int i = 0; i < list.size() && !mainVerbFound; i++) {

		int lastNounttagIndexBeforeVerb = 0;

		SuperSenseWord word = list.get(i);
		String verbPos = word.getPosTag();

		// Extract Predicate First
		if (verbPos.startsWith("V")) {
		    mainVerbFound = true;
		    verbIndex = i;
		    sRelation.setVerbIndex(verbIndex);
		    sRelation.setSubjectPhraseIndex(verbIndex - 1);

		    String rel = word.getToken();

		    // Get the last count of noun tag before the verb is found
		    if (word.getSstag().startsWith("B-noun.")
			    || word.getSstag().equals("PERSON")) {
			lastNounttagIndexBeforeVerb = i;

		    }

		    // PreModifiers:

		    // find the first instance of B-noun.*
		    for (int z = verbIndex - 1; z > lastNounttagIndexBeforeVerb; z--) {
			SuperSenseWord word1 = list.get(z);

			String posTag1 = word1.getPosTag();

			// Scope for improvement here.
			if (posTag1.equals("MD") || posTag1.startsWith("V")
				|| posTag1.equals("RB")) {
			    rel = word1.getToken() + " " + rel;
			}

		    }

		    // look for words after verb.
		    for (int j = verbIndex + 1; j < list.size(); j++) {
			SuperSenseWord word1 = list.get(j);
			String ssTag = word1.getSstag();
			String posTag = word1.getPosTag();

			/*
			 * If 1)SStag is not noun 2)POSTag is IN
			 */
			/*
			 * if (!(ssTag.startsWith("B-noun.")) ) { rel += " " +
			 * list.get(j).getToken(); }
			 */
			if (posTag.equals("IN")
				|| ssTag.equals("B-noun.relation")
				|| posTag.equals("MD")
				|| posTag.startsWith("V")) {
			    rel += " " + list.get(j).getToken();

			}

			if ((ssTag.startsWith("B-noun.") && !ssTag
				.equals("B-noun.relation"))
				|| ssTag.equals("PERSON")) {
			    relation = rel;

			    for (int k = j - 1; k > verbIndex; k--) {
				SuperSenseWord word2 = list.get(k);
				String ssTag2 = word2.getSstag();
				String posTag2 = word2.getPosTag();

				if (posTag2.equals("IN")
					|| ssTag2.equals("B-noun.relation")
					|| posTag2.equals("MD")
					|| posTag.startsWith("V")) {
				    objectPhraseIndex = k + 1;

				    break;
				}
			    }
			    if (objectPhraseIndex == -1) {
				objectPhraseIndex = verbIndex + 1;
			    }
			    sRelation.setObjectPhraseIndex(objectPhraseIndex);
			    sRelation.setPredicatePhrase(relation);
			    ;
			    sRelation.setPredicateType(word.getSstag());
			    break;

			}

		    }
		}
	    }

	}

	String prd = sRelation.getPredicateType();
	prd = prd.substring(prd.indexOf(".") + 1, prd.length());
	sRelation.setPredicateType(prd);
	sRelation.setVerbIndex(verbIndex);
    }

    /*
     * This function will produce the lemmatized form of predicate relation
     */
    private void lemmatize(SenseRelation sRelation) {
	// TODO Auto-generated method stub

	if (lemmatize) {
	    String predicatePhrase = "";

	    List<SuperSenseWord> predicatePhraseList = sRelation
		    .getPredicateSenseList();
	    for (int i = 0; i < predicatePhraseList.size(); i++) {
		SuperSenseWord w = predicatePhraseList.get(i);
		String lemma = AnalysisUtilities.getInstance().getLemma(
			w.getToken(), w.getPosTag());
		predicatePhrase += lemma + " ";

	    }
	    predicatePhrase = predicatePhrase.trim();
	    sRelation.setPredicatePhrase(predicatePhrase);
	}
    }

    private void setConceptsListForSubjectObject(SenseRelation sRelation,
	    SentenceNode sentenceNode) {
	List<Concept> conceptsInSubjectPhrase = new ArrayList<Concept>();
	List<Concept> conceptsInObjectPhrase = new ArrayList<Concept>();

	List<SuperSenseWord> list = sentenceNode
		.getStanfordSupersenseTaggedSentence();

	int verbIndex = sRelation.getVerbIndex();
	List<Concept> conceptList = sentenceNode.getConceptsList();
	for (int i = 0; i < conceptList.size(); i++) {
	    Concept concept = conceptList.get(i);

	    boolean conceptFound = REUtilities.getInstance()
		    .findConceptInSupersenseWordList(concept, list, verbIndex,
			    1);

	    if (conceptFound) {

		conceptsInSubjectPhrase.add(concept);

	    }

	    else {
		conceptFound = REUtilities.getInstance()
			.findConceptInSupersenseWordList(concept, list,
				verbIndex, 2);
		if (conceptFound) {

		    conceptsInObjectPhrase.add(concept);

		}
	    }

	}

	sRelation.setConceptsInObject(conceptsInObjectPhrase);
	sRelation.setConceptsInSubject(conceptsInSubjectPhrase);

	// println("subjectCount" + sRelation.getConceptsInObject().size() +
	// " predicatePhrase: " + sRelation.getPredicate() +
	// "   predicateType: " +
	// sRelation.getPredicateType());
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub

    }

    @Override
    public void extractRelations(String document) {
	// TODO Auto-generated method stub

    }

}
