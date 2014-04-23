package com.asus.ctc.ie.nlptransformations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asus.ctc.ie.config.GlobalProperties;
import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.datastructures.interfaces.Paragraph;
import com.asus.ctc.ie.nlptransformations.interfaces.SentenceSimplification;
import com.asus.ctc.ie.tagger.CoreferenceResolutionUsingArkRef;
import com.asus.ctc.ie.tagger.interfaces.CoReferenceResolution;
import com.asus.ctc.ie.utilities.AnalysisUtilities;

import edu.stanford.nlp.trees.Tree;

/**
 * @author Sanjay_Meena
 * 
 */
public class SentenceSimplificationUsingSyntacticRules implements
	SentenceSimplification {
    private final static Logger log = LoggerFactory
	    .getLogger(SentenceSimplificationUsingSyntacticRules.class);

    // private static SentenceSimplificationUsingSyntacticRules instance;
    private NLPTransformations simplifier;
    private CoReferenceResolution coref;

    private boolean doSentenceSimplification;
    private boolean doPronounNPCFromSimplifiedSentences;
    private boolean doNonPronounNPCFromSimplifiedSentences;
    private boolean fixCapitalization;
    private boolean extractFromVerbComplements;
    private boolean breakNounPhrase;

    /**
	 * 
	 */
    public SentenceSimplificationUsingSyntacticRules() {

	initialize();

	simplifier = new NLPTransformations();
	simplifier.setExtractFromVerbComplements(extractFromVerbComplements);
	simplifier.setBreakNPs(breakNounPhrase);

	coref = new CoreferenceResolutionUsingArkRef();
    }

    private void initialize() {
	// TODO Auto-generated method stub

	// read IE processing configuration
	Properties sentenceSimplificationProperties = GlobalProperties
		.getProperties(2);

	doSentenceSimplification = Boolean
		.parseBoolean(sentenceSimplificationProperties
			.getProperty("doSentenceSimplification"));
	doPronounNPCFromSimplifiedSentences = Boolean
		.parseBoolean(sentenceSimplificationProperties
			.getProperty("doPronounNPCFromSimplifiedSentences"));
	doNonPronounNPCFromSimplifiedSentences = Boolean
		.parseBoolean(sentenceSimplificationProperties
			.getProperty("doNonPronounNPCFromSimplifiedSentences"));
	fixCapitalization = Boolean
		.parseBoolean(sentenceSimplificationProperties
			.getProperty("fixCapitalization"));
	extractFromVerbComplements = Boolean
		.parseBoolean(sentenceSimplificationProperties
			.getProperty("extractFromVerbComplements"));
	breakNounPhrase = Boolean.parseBoolean(sentenceSimplificationProperties
		.getProperty("breakNounPhrase"));

    }

    /**
     * 
     * 
     * This method breaks down the complexity of the Sentences by performing
     * following tasks
     * 
     * 1)Sentence Simplification to produce simpler atomic sentence 2) Noun
     * Phrase Resolution of Simplified Sentences;
     * 
     * 
     * @param textNode
     */
    public void simplifySentences(TextNode textNode) {
	StopWatch sw = new StopWatch();
	sw.start();
	if (doSentenceSimplification) {
	    // Number to start with for simplified sentences
	    // int simplifiedSentenceNumber = textNode.getAllSentences().size();
	    // InitialParagraphTransformation transform = new
	    // InitialParagraphTransformation();
	    Map<Integer, Paragraph> paragraphMap = textNode
		    .getParagraphMap();

	    // int t = textNode.getAllSentences().size();

	    Iterator<Entry<Integer, Paragraph>> iterator = paragraphMap
		    .entrySet().iterator();

	    while (iterator.hasNext()) {
		Entry<Integer, Paragraph> entry = iterator.next();

		Paragraph para = entry.getValue();

		// Simplify sentences in paragraph
		performSentenceSimplificationOnParagraph(para);

		// Map<Integer, SentenceNode> extractedSentenceMap = new
		// HashMap<Integer, SentenceNode>();

		// Create extracted sentences map for the paragraph

		/*
		 * for (SentenceNode sentenceNode : simplifiedlist) {
		 * 
		 * simplifiedSentenceNumber++;
		 * sentenceNode.setSentenceNumber(simplifiedSentenceNumber);
		 * extractedSentenceMap .put(simplifiedSentenceNumber,
		 * sentenceNode);
		 * 
		 * }
		 * 
		 * para.setExtractedSentenceMap(extractedSentenceMap);
		 * 
		 * }
		 */
	    }
	} else {
	    log.warn("Sentece Simplification is set to false. Please change in the resources/core_ie_resources/ie_data/ie_processing_configuration.properties");
	}
	
	sw.stop();

	log.info("Total Time taken for Sentence Simplification:  " + sw.toString());
    }

    /**
     * 
     * Simplify each sentence in the paragraph
     * 
     * @param sentences
     * @param paragraphNumber
     * @return
     * @return
     */
     void performSentenceSimplificationOnParagraph(Paragraph paraNode) {

	// resolvePronounCoreference(paraNode);

	// List<SentenceNode> treess = new ArrayList<SentenceNode>();

	Collection<SentenceNode> tmpSets;

	for (SentenceNode sentence : paraNode.getSourceSentenceMap().values()) {
	    List<SentenceNode> simplfiedSents = new ArrayList<SentenceNode>();
	    Tree tree = sentence.getTree();

	  //  log.info("Processing sentence: "+ sentence.getParagraphNumber()+ ":"+sentence.getSentenceNumber() +" = " + sentence.getString());
	    tmpSets = simplifier.simplifySentence(tree, fixCapitalization);

	    // if tmpSets is >0 , make simplifiedSentence to true
	    if (tmpSets != null && tmpSets.size() > 1) {
		sentence.setPerformedSimplification(true);

		// Used for coreference resolution;
		List<Tree> simplifiedTrees = new ArrayList<Tree>();

		for (SentenceNode q : tmpSets) {
		    q.setSourceSentenceNumber(sentence.getSentenceNumber());
		    simplifiedTrees.add(q.getTree());
		}

		// treess.addAll(tmpSets);
		simplfiedSents.addAll(tmpSets);

		// add new sentences with clarified/resolved NPs
		if (doPronounNPCFromSimplifiedSentences
			&& doNonPronounNPCFromSimplifiedSentences) {

		    /*
		     * Create the arkref document for mentions;
		     */
		    coref.resolveCoreference(simplifiedTrees);
		    // Clarify Noun phrases;

		    simplfiedSents = coref.clarifyNounPhrases(simplfiedSents,
			    doPronounNPCFromSimplifiedSentences,
			    doNonPronounNPCFromSimplifiedSentences);

		}

		// add some information to simplified sentences;
		addInformationToSentenceNode(simplfiedSents);
		addSimplifiedSentencesToSourceSentence(sentence, simplfiedSents);
	    }
	}

	// simplifiedSentences = sentenceAdaptar(trees);
	// addInformationToSentenceNode(treess);

	// upcase the first tokens of all output trees.

	return;

    }

    /**
     * This method adds simplified sentences to the original sentences
     * 
     * @param sentence
     * @param simplfiedSents
     */
    private void addSimplifiedSentencesToSourceSentence(SentenceNode sentence,
	    List<SentenceNode> simplfiedSents) {
	// TODO Auto-generated method stub

	int para = sentence.getParagraphNumber();
	int source = sentence.getSourceSentenceNumber();

	// set sentence numbers:
	for (int i = 0; i < simplfiedSents.size(); i++) {

	    SentenceNode simplified = simplfiedSents.get(i);
	    int sno = i + 1;

	    simplified.setSentenceNumber(sno);
	    simplified.setParagraphNumber(para);
	    simplified.setSourceSentenceNumber(source);
	}

	sentence.setSimplifiedSentences(simplfiedSents);

	//int t = 10;
    }

    private void addInformationToSentenceNode(List<SentenceNode> treess) {
	for (SentenceNode q : treess) {

	    String sent = AnalysisUtilities.getInstance().treeToString(
		    q.getTree());
	    String origsent = AnalysisUtilities.getInstance().treeToString(
		    q.getSourceTree());
	    q.setString(sent);
	    q.setOriginal_sentence(origsent);
	    AnalysisUtilities.getInstance().upcaseFirstToken(q.getTree());

	}

    }

    public NLPTransformations getSimplifier() {
	return simplifier;
    }

    public void setSimplifier(NLPTransformations simplifier) {
	this.simplifier = simplifier;
    }

    public boolean isExtractFromVerbComplements() {
	return extractFromVerbComplements;
    }

    public void setExtractFromVerbComplements(boolean extractFromVerbComplements) {
	this.extractFromVerbComplements = extractFromVerbComplements;
    }

    public boolean isBreakNounPhrase() {
	return breakNounPhrase;
    }

    public void setBreakNounPhrase(boolean breakNounPhrase) {
	this.breakNounPhrase = breakNounPhrase;
    }

    public boolean isDoPronounNPCFromSimplifiedSentences() {
	return doPronounNPCFromSimplifiedSentences;
    }

    public void setDoPronounNPCFromSimplifiedSentences(
	    boolean doPronounNPCFromSimplifiedSentences) {
	this.doPronounNPCFromSimplifiedSentences = doPronounNPCFromSimplifiedSentences;
    }

    public boolean isDoNonPronounNPCFromSimplifiedSentences() {
	return doNonPronounNPCFromSimplifiedSentences;
    }

    public void setDoNonPronounNPCFromSimplifiedSentences(
	    boolean doNonPronounNPCFromSimplifiedSentences) {
	this.doNonPronounNPCFromSimplifiedSentences = doNonPronounNPCFromSimplifiedSentences;
    }

    public boolean isFixCapitalization() {
	return fixCapitalization;
    }

    public void setFixCapitalization(boolean fixCapitalization) {
	this.fixCapitalization = fixCapitalization;
    }

}
