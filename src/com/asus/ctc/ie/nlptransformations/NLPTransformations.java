package com.asus.ctc.ie.nlptransformations;

import static com.asus.ctc.ie.config.GlobalProperties.FactualStatementExtractor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.asus.ctc.ie.config.GlobalProperties;

import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.utilities.AnalysisUtilities;

import edu.stanford.nlp.trees.Tree;

/**
 * Class for extracting simplified factual statements from complex sentences.
 * 
 * This class constitutes "stage 1" in the framework for Question Generation.
 * When creating the output, the system makes various calls to
 * Question.setFeatureValue(). These calls track what operations were performed
 * in creating the sentence, and could be used in the later stages of an NLP/NLG
 * system.
 * 
 * 
 * @author sanjay meena (sanjay_meena@asus.com).
 * 
 */
public class NLPTransformations {

    TranformationFunctions nlptransformations;
    TransformationsChecks nlpChecks;
    TransformationUtilities utils;

    public NLPTransformations() {
	nlptransformations = new TranformationFunctions();
	nlpChecks = new TransformationsChecks();
	utils = new TransformationUtilities();
    }

    /**
     * Primary method for simplifying sentences. Takes an input sentence in the
     * form of a tree and returns a list of Question objects, which help to
     * track what operations were performed.
     * 
     * @param sentence
     * @param fixCapitalization
     * @return
     */
    public List<SentenceNode> simplifySentence(Tree sentence,
	    boolean fixCapitalization) {

	List<SentenceNode> treeLists = new ArrayList<SentenceNode>();

	numSimplifyHelperCalls = 0;
	if (GlobalProperties.getDebug())
	    System.err.println("simplify input:" + sentence);
	// add original tree

	//
	SentenceNode origs = new SentenceNode();
	origs.setTree(sentence.deepCopy());
	origs.setSourceTree(sentence);

	// if the input contains any UCP or other odd nodes, then just return
	// the original sentence
	// such nodes indicate that the parse failed, or at least that our
	// system will likely produce bad output
	if (nlpChecks.uglyParse(sentence)) {

	    treeLists.add(origs);

	    return treeLists;
	}

	//
	AnalysisUtilities.getInstance().downcaseFirstToken(origs.getTree());

	SentenceNode currents = origs.deepCopy();

	//
	List<SentenceNode> extracteds = new ArrayList<SentenceNode>();

	// for each nested element in the INPUT... (nested elements include
	// finite verbs, non-restrictive relative clauses, appositives,
	// conjunction of VPs, conjunction of clauses, participial phrases)
	// transform the nested element into a declarative sentence (preserving
	// tense), removing conjunctions, etc.

	extracteds.add(currents);

	nlptransformations.extractSubordinateClauses(extracteds, origs);

	nlptransformations.extractNounParticipialModifiers(extracteds, origs);

	nlptransformations.extractNonRestrictiveRelativeClauses(extracteds,
		origs);

	nlptransformations.extractAppositives(extracteds, origs);

	nlptransformations.extractVerbParticipialModifiers(extracteds, origs);

	// extractWITHPartcipialPhrases(extracted, orig); //too rare to worry
	// about
	if (extractFromVerbComplements) {

	    nlptransformations.extractComplementClauses(extracteds, origs);
	}

	for (SentenceNode q : extracteds) {
	    addAllIfNovels(treeLists, simplifyHelper(q));
	}

	if (treeLists.size() == 0) {
	    addIfNovel(treeLists, currents);
	}

	if (fixCapitalization) {
	    // upcase the first tokens of all output trees.

	    // upcase the first tokens of all output trees.
	    for (SentenceNode q : treeLists) {
		AnalysisUtilities.getInstance().upcaseFirstToken(q.getTree());
	    }

	}

	// clean up the output
	for (SentenceNode q : treeLists) {
	    AnalysisUtilities.getInstance().removeExtraQuotes(q.getTree());
	}

	if (GlobalProperties.getComputeFeatures()) {

	    SentenceNode t1 = treeLists.get(0);
	    boolean fromMainClause = true;
	    String[] nestedExtractionFeatureNames = {
		    "extractedFromParticipial", "extractedFromVerbParticipial",
		    "extractedFromFiniteClause",
		    "extractedFromSubordinateClause",
		    "extractedFromComplementClause", "extractedFromAppositive",
		    "extractedFromRelativeClause", "extractedFromParticipial",
		    "extractedFromNounParticipial" };

	    for (String name : nestedExtractionFeatureNames) {
		if (t1.getFeatureValue(name) != 0) {
		    fromMainClause = false;
		    break;
		}

		if (fromMainClause)
		    t1.setFeatureValue("extractedFromLeftMostMainClause", 1.0);
		// t.setFeatureValue("extractedFromLeftMostMainClause", 1.0);
	    }

	}

	if (GlobalProperties.getDebug())
	    System.err.println("simplifyHelperCalls:\t"
		    + numSimplifyHelperCalls);

	if (mainClauseOnly) {
	    SentenceNode tmp = treeLists.get(0);
	    treeLists.clear();
	    if (tmp.getFeatureValue("extractedFromLeftMostMainClause") == 1.0)
		treeLists.add(tmp);
	}

	// check whether simplification has been performed

	if (treeLists.size() == 1) {
	    SentenceNode t = treeLists.get(0);
	    if (origs.equals(t)) {
		//no simplification performed
		treeLists=null;
	    }

	}

	return treeLists;
    }

    /**
     * @param input
     * @return List<Question>
     */
    public List<SentenceNode> simplifyHelper(SentenceNode input) {
	if (GlobalProperties.getDebug())
	    System.err.println("simplifyHelper: " + input.getTree());
	numSimplifyHelperCalls++;
	List<SentenceNode> treeCollection = new ArrayList<SentenceNode>();

	// move fronted PPs to the end of the sentence
	// if modified, add new tree to results and make a new copy
	nlptransformations.moveLeadingPPsAndQuotes(input);

	// remove connecting words, subordinating conjunctions, and adverbs.
	// e.g., "but", "however," "while," etc.
	// also remove all nested elements (e.g., appositives, participials,
	// relative clauses (?), etc.)
	removeNestedElements(input);

	// add the original input as the canonical sentence if none of the above
	// transformations
	// produced a simpler form
	if (!nlpChecks.hasBreakableConjunction(input)
		&& nlpChecks.hasSubjectAndFiniteMainVerb(input)
		&& nlpChecks.mainVerbOK(input)) {
	    addIfNovel(treeCollection, input);
	} else {

	    List<SentenceNode> extracted = new ArrayList<SentenceNode>();

	    // if there is a conjunction of NPs within this small chunk, also
	    // extract separate sentences using each conjunct NP.
	    if (breakNPs)
		nlptransformations.extractConjoinedNPs(extracted, input);

	    nlptransformations.extractConjoinedPhrases(extracted, input);

	    for (SentenceNode e : extracted) {
		// recur
		addAllIfNovels(treeCollection, simplifyHelper(e));
	    }

	}

	return treeCollection;
    }

    private void removeNestedElements(SentenceNode input) {
	nlptransformations.removeAppositives(input);
	nlptransformations.removeVerbalModifiersAfterCommas(input);
	nlptransformations.removeClauseLevelModifiers(input);
	nlptransformations.removeNonRestrRelClausesAndParticipials(input);
	nlptransformations.removeParentheticals(input);

	if (GlobalProperties.getComputeFeatures())
	    input.setFeatureValue("removedNestedElements", 1.0); // old feature
								 // name
    }

    public List<SentenceNode> simplify(Tree sentence) {
	return simplifySentence(sentence, true);
    }

    /**
     * Method to add a new Question object q to a given set if the collection
     * treeSet does not already include q.
     * 
     * @param treeSet
     * @param q
     */
    private void addIfNovel(Collection<SentenceNode> treeSet, SentenceNode q) {
	boolean exists = false;
	for (SentenceNode old : treeSet) {
	    if (old.getTree().equals(q.getTree())) {
		exists = true;
		break;
	    }
	}
	if (!exists) {
	    treeSet.add(q);
	}
    }

    /**
     * Adds new trees that do not already exist in the treeSet. We don't use
     * addAll because there may be multiple TreeWithFeatures objects with the
     * same tree but different features.
     * 
     * @param treeSet
     * @param extracted
     */
    private void addAllIfNovels(Collection<SentenceNode> treeSet,
	    Collection<SentenceNode> extracted) {
	for (SentenceNode q : extracted) {
	    this.addIfNovel(treeSet, q);
	}
    }

    public long getNumSimplifyHelperCalls() {
	return numSimplifyHelperCalls;
    }

    public void setNumSimplifyHelperCalls(long numSimplifyHelperCalls) {
	this.numSimplifyHelperCalls = numSimplifyHelperCalls;
    }

    public void setBreakNPs(boolean breakNPs1) {
	breakNPs = breakNPs1;
    }

    public boolean getBreakNPs() {
	return breakNPs;
    }

    public void setExtractFromVerbComplements(boolean extractFromVerbComplements) {
	this.extractFromVerbComplements = extractFromVerbComplements;
    }

    public boolean getExtractFromVerbComplements() {
	return extractFromVerbComplements;
    }

    @SuppressWarnings("unused")
    private static String simplificationFeatureString(SentenceNode q) {
	String res = "";

	res += q.getFeatureValue("extractedFromAppositive");
	res += "\t" + q.getFeatureValue("extractedFromComplementClause");
	res += "\t" + q.getFeatureValue("extractedFromConjoined");
	res += "\t" + q.getFeatureValue("extractedFromConjoinedNPs");
	res += "\t" + q.getFeatureValue("extractedFromNounParticipial");
	res += "\t" + q.getFeatureValue("extractedFromRelativeClause");
	res += "\t" + q.getFeatureValue("extractedFromSubordinateClause");
	res += "\t" + q.getFeatureValue("extractedFromVerbParticipial");
	// res += "\t" + q.getFeatureValue("extractedFromWithParticipial");
	res += "\t" + q.getFeatureValue("movedLeadingPPs");
	res += "\t" + q.getFeatureValue("removedAppositives");
	res += "\t" + q.getFeatureValue("removedClauseLevelModifiers");
	res += "\t"
		+ q.getFeatureValue("removedNonRestrRelClausesAndParticipials");
	res += "\t" + q.getFeatureValue("removedParentheticals");
	res += "\t" + q.getFeatureValue("removedVerbalModifiersAfterCommas");
	res += "\t" + q.getFeatureValue("extractedFromLeftMostMainClause");

	return res;
    }

    private static String simplificationFeatureType(SentenceNode q) {
	String res = "";

	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("extractedFromAppositive"),
		"extractedFromAppositive");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("extractedFromComplementClause"),
		"extractedFromComplementClause");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("extractedFromConjoined"),
		"extractedFromConjoined");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("extractedFromConjoinedNPs"),
		"extractedFromConjoinedNPs");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("extractedFromNounParticipial"),
		"extractedFromNounParticipial");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("extractedFromRelativeClause"),
		"extractedFromRelativeClause");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("extractedFromSubordinateClause"),
		"extractedFromSubordinateClause");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("extractedFromVerbParticipial"),
		"extractedFromVerbParticipial");
	// res +=
	// simplificationFeatureTypeHelper(q.getFeatureValue("extractedFromWithParticipial"),
	// );
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("movedLeadingPPs"), "movedLeadingPPs");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("removedAppositives"), "removedAppositives");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("removedClauseLevelModifiers"),
		"removedClauseLevelModifiers");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("removedNonRestrRelClausesAndParticipials"),
		"removedNonRestrRelClausesAndParticipials");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("removedParentheticals"),
		"removedParentheticals");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("removedVerbalModifiersAfterCommas"),
		"removedVerbalModifiersAfterCommas");
	res += simplificationFeatureTypeHelper(
		q.getFeatureValue("extractedFromLeftMostMainClause"),
		"extractedFromLeftMostMainClause");

	return res;
    }

    private static String simplificationFeatureTypeHelper(double value,
	    String featureName) {
	String res = "";

	if (value == 1.0) {
	    res = featureName + " |";
	}

	return res;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	NLPTransformations ss = new NLPTransformations();
	boolean doMobyDickParse = false;
	boolean treeInput = false;
	boolean verbose = false;
	String propertiesFile = FactualStatementExtractor;

	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("--debug")) {
		GlobalProperties.setDebug(true);
	    } else if (args[i].equals("--properties")) {
		propertiesFile = args[i + 1];
	    } else if (args[i].equals("--moby")) {
		doMobyDickParse = true;
	    } else if (args[i].equals("--break-nps")) {
		ss.setBreakNPs(true);
	    } else if (args[i].equals("--comp")) {
		ss.setExtractFromVerbComplements(true);
	    } else if (args[i].equals("--tree-input")) {
		treeInput = true;
	    } else if (args[i].equals("--verbose")) {
		verbose = true;
		GlobalProperties.setComputeFeatures(true);
	    } else if (args[i].equals("--help") || args[i].equals("-help")
		    || args[i].equals("-h")) {
		String helpStr = "A program for extracting simplified factual statements"
			+ " from complex sentences.\n"
			+ "Written by Sanjay Meena.\n"
			+ "The program takes plain text on standard input and prints output"
			+ " on standard output.  The following options are available:\n\n"
			+ " --debug\t\tprint debugging output to standard error.\n"
			+ " --properties PATH\tload settings from the properties file at PATH\n"
			+ " --break-nps\t\tsplit conjunctions of noun phrases (disabled by default)\n"
			+ " --comp\t\t\textract from verb complements (disabled by default)\n\n";
		System.err.println(helpStr);
		System.exit(0);
	    }
	}

	GlobalProperties.loadProperties(1, propertiesFile);

	if (doMobyDickParse) {
	    String parseStr = "(ROOT (S (SBAR (IN As) (S (NP (PRP they)) (VP (VBD narrated) (PP (TO to) (NP (DT each) (NN other))) (NP (NP (PRP$ their) (JJ unholy) (NNS adventures)) (, ,) (NP (NP (PRP$ their) (NNS tales)) (PP (IN of) (NP (NN terror))) (VP (VBN told) (PP (IN in) (NP (NNS words) (PP (IN of) (NP (NN mirth))))))))))) (: ;) (SBAR (IN as) (S (NP (PRP$ their) (JJ uncivilized) (NN laughter)) (VP (VBD forked) (ADVP (RB upwards)) (PP (IN out) (IN of) (NP (PRP them))) (, ,) (PP (IN like) (NP (NP (DT the) (NNS flames)) (PP (IN from) (NP (DT the) (NN furnace)))))))) (: ;) (SBAR (IN as) (S (ADVP (TO to) (CC and) (RB fro)) (, ,) (PP (IN in) (NP (PRP$ their) (NN front))) (, ,) (NP (DT the) (NNS harpooneers)) (VP (ADVP (RB wildly)) (VBD gesticulated) (PP (IN with) (NP (PRP$ their) (JJ huge) (NP (JJ pronged) (NNS forks)) (CC and) (NNS dippers)))))) (: ;) (SBAR (IN as) (S (S (NP (DT the) (NN wind)) (VP (VBD howled) (PRT (RP on)))) (, ,) (CC and) (S (NP (DT the) (NN sea)) (VP (VBD leaped))) (, ,) (CC and) (S (NP (DT the) (NN ship)) (VP (VP (VBD groaned) (CC and) (VBD dived)) (, ,) (CC and) (RB yet) (VP (VP (ADVP (RB steadfastly)) (VBD shot) (NP (PRP$ her) (JJ red) (NN hell)) (ADVP (RBR further) (CC and) (RBR further)) (PP (IN into) (NP (NP (DT the) (NN blackness)) (PP (IN of) (NP (NP (DT the) (NN sea)) (CC and) (NP (DT the) (NN night))))))) (, ,) (CC and) (VP (ADVP (RB scornfully)) (VBD champed) (NP (NP (DT the) (JJ white) (NN bone)) (PP (IN in) (PRP$ her) (NP (NN mouth))))) (, ,) (CC and) (VP (ADVP (RB viciously)) (VBD spat) (PP (IN round) (NP (PRP her))) (PP (IN on) (NP (DT all) (NNS sides))))))))) (: ;) (ADVP (RB then)) (NP (NP (DT the) (JJ rushing) (NNP Pequod)) (, ,) (VP (VP (VBN freighted) (PP (IN with) (NP (NN savages)))) (, ,) (CC and) (VP (VBN laden) (PP (IN with) (NP (NN fire)))) (, ,) (CC and) (VP (VBG burning) (NP (DT a) (NN corpse))) (, ,) (CC and) (VP (VBG plunging) (PP (IN into) (NP (NP (DT that) (NN blackness)) (PP (IN of) (NP (NN darkness))))))) (, ,)) (VP (VBD seemed) (NP (NP (DT the) (JJ material) (NN counterpart)) (PP (IN of) (NP (NP (PRP$ her) (JJ monomaniac) (NN commander) (POS 's)) (NN soul))))) (. .)))";
	    Tree mobyParse = AnalysisUtilities.getInstance()
		    .readTreeFromString(parseStr);
	    Collection<SentenceNode> output = ss.simplify(mobyParse);
	    for (SentenceNode q : output) {
		System.out.println(AnalysisUtilities.getInstance().getCleanedUpYield(q
			.getTree()));
		// System.out.println(q.findLogicalWordsAboveIntermediateTree());
	    }
	    System.exit(0);
	}

	String buf, doc;
	Tree parsed;

	// pre-load
	if (GlobalProperties.getDebug())
	    System.err.println("Enter sentence: ");
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    System.in));

	    if (treeInput) {
		doc = "";
		buf = br.readLine();
		doc += buf + " ";
		while (br.ready()) {
		    buf = br.readLine();
		    if (buf == null)
			break;
		    doc += buf;
		}
		Tree t = AnalysisUtilities.getInstance()
			.readTreeFromString(doc);
		for (SentenceNode q : ss.simplify(t)) {
		    System.out.println(AnalysisUtilities.getInstance().getCleanedUpYield(q
			    .getTree()));
		}
		System.exit(0);
	    }

	    while (true) {
		doc = "";
		buf = "";

		buf = br.readLine();
		if (buf == null) {
		    break;
		}
		doc += buf;

		while (br.ready()) {
		    buf = br.readLine();
		    if (buf == null) {
			break;
		    }
		    doc += buf + " ";
		}
		if (doc.length() == 0) {
		    break;
		}

		long startTime = System.currentTimeMillis();

		List<String> sentences = AnalysisUtilities.getInstance()
			.getSentences(doc);

		// iterate over each segmented sentence and generate questions
		List<SentenceNode> output = new ArrayList<SentenceNode>();

		for (String sentence : sentences) {
		    parsed = AnalysisUtilities.getInstance().parseSentence(
			    sentence).parse;
		    if (GlobalProperties.getDebug())
			System.err.println("input: "
				+ AnalysisUtilities.getInstance().treeToString(parsed));
		    if (GlobalProperties.getDebug())
			System.err.println("parse: " + sentence.toString());
		    // if no parse, print the original sentence
		    if (AnalysisUtilities.getInstance().treeToString(parsed).equals(".")) {
			System.out.print(sentence);
			if (verbose)
			    System.out.print("\t" + sentence);
			System.out.println();
			continue;
		    }

		    output.clear();
		    output.addAll(ss.simplify(parsed));

		    printSimplifiedSentence(verbose, output);
		    /*
		     * for(Question q: output){
		     * 
		     * 
		     * System.out.print(AnalysisUtilities.getCleanedUpYield(q.
		     * getIntermediateTree())); if(verbose)
		     * System.out.print("\t"
		     * +AnalysisUtilities.getCleanedUpYield(q.getSourceTree()));
		     * if(verbose)
		     * System.out.print("\t"+simplificationFeatureString(q));
		     * //System
		     * .out.println(q.findLogicalWordsAboveIntermediateTree());
		     * System.out.println();
		     * 
		     * 
		     * 
		     * }
		     */
		}

		System.err.println("Seconds Elapsed:\t"
			+ ((System.currentTimeMillis() - startTime) / 1000.0));
		// prompt for another piece of input text
		if (GlobalProperties.getDebug())
		    System.err.println("\nInput Text:");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private static void printSimplifiedSentence(boolean verbose,
	    List<SentenceNode> output) {

	int currSenNum = 0;
	int prevSenNum = -1;
	int temp = -1;
	for (SentenceNode q : output) {

	    temp++;
	    currSenNum = q.getSourceSentenceNumber();
	    if (temp == 0) {

		if (verbose)
		    System.err.println(AnalysisUtilities.getInstance().getCleanedUpYield(q
			    .getSourceTree()));
		System.out.print(AnalysisUtilities.getInstance().getCleanedUpYield(q
			.getTree()));
		if (verbose)
		    System.out.print("\t" + simplificationFeatureType(q));

		// System.out.println(q.findLogicalWordsAboveIntermediateTree());
		System.out.println();

	    }

	    else {
		prevSenNum = output.get(temp - 1).getSourceSentenceNumber();
		if (prevSenNum == currSenNum)// same sentence
		{
		    System.out.print(AnalysisUtilities.getInstance().getCleanedUpYield(q
			    .getTree()));
		    if (verbose)
			System.out.print("\t" + simplificationFeatureType(q));

		    // System.out.println(q.findLogicalWordsAboveIntermediateTree());
		    System.out.println();
		} else // Different EmailSentence
		{
		    System.out.println();
		    if (verbose)
			System.err.println(AnalysisUtilities.getInstance()
				.getCleanedUpYield(q.getSourceTree()));
		    System.out.print(AnalysisUtilities.getInstance().getCleanedUpYield(q
			    .getTree()));
		    if (verbose)
			System.out.print("\t" + simplificationFeatureType(q));

		    // System.out.println(q.findLogicalWordsAboveIntermediateTree());
		    System.out.println();
		}

	    }

	}

    }

    public void setMainClauseOnly(boolean b) {
	mainClauseOnly = b;
    }

    private boolean mainClauseOnly = false;
    private long numSimplifyHelperCalls; // for debugging, counts the number of
					 // call to simplifyHelper to check
					 // that duplicate derivations are
					 // avoided

    public static boolean breakNPs = false; // whether to break conjunctions of
					    // noun phrases (e.g., John and I
					    // are friends.)
    private boolean extractFromVerbComplements = false; // whether to extract
							// from complements
							// (e.g., John thought
							// that I studied -> I
							// studied)

}
