package com.asus.ctc.ie.tagger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arkref.analysis.ARKref;
import arkref.analysis.FindMentions;
import arkref.analysis.RefsToEntities;
import arkref.analysis.Resolve;
import arkref.analysis.Types;
import arkref.data.Document;
import arkref.data.Mention;
import arkref.parsestuff.TregexPatternFactory;

import com.asus.ctc.ie.config.GlobalProperties;
import com.asus.ctc.ie.datastructures.ParagraphNode;
import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.datastructures.interfaces.Paragraph;
import com.asus.ctc.ie.datastructures.senserelation.SuperSenseWord;
import com.asus.ctc.ie.tagger.interfaces.CoReferenceResolution;
import com.asus.ctc.ie.utilities.AnalysisUtilities;
import com.asus.ctc.ie.utilities.servers.SuperSenseWrapper;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

public class CoreferenceResolutionUsingArkRef implements CoReferenceResolution {
    private final static Logger log = LoggerFactory
	    .getLogger(CoreferenceResolutionUsingArkRef.class);

    private Document document;

    boolean clarifyPronouns;
    boolean clarifyNonPronouns;

    public CoreferenceResolutionUsingArkRef() {
	ARKref.Opts.propertiesFile = GlobalProperties.getProperties(1)
		.getProperty("propertiesFilePath");
	// read IE processing configuration
	Properties coreferenceProperties = GlobalProperties.getProperties(2);

	clarifyPronouns = Boolean.parseBoolean(coreferenceProperties
		.getProperty("doPronounNPC"));
	clarifyNonPronouns = Boolean.parseBoolean(coreferenceProperties
		.getProperty("doNonPronounNPC"));
    }

    /**
     * This method will resolve coreferences present in each document;
     * 
     * @param textNode
     */
    public void resolveCoreferences(TextNode textNode) {

	StopWatch sw = new StopWatch();
	sw.start();

	// TODO Auto-generated method stub
	for (Paragraph paragraph : textNode.getParagraphMap().values()) {
	    // CoreferenceResolutionUsingArkRef coref=new
	    // CoreferenceResolutionUsingArkRef();
	    /*
	     * Create the arkref document for mentions;
	     */
	    resolveCoreference(paragraph);

	    // Clarify Noun phrases;
	    clarifyNPs(paragraph);
	}
	sw.stop();

	log.info("Total Time taken for Coreference resolution:  "
		+ sw.toString());
    }

    public Document resolveCoreference(Paragraph paragraph) {
	List<Tree> trees = new ArrayList<Tree>();
	List<String> entityStrings = new ArrayList<String>();

	for (SentenceNode sentence : paragraph.getSourceSentenceMap().values()) {
	    Tree t = sentence.getSourceTree();
	    Document.addNPsAbovePossessivePronouns(t);
	    Document.addInternalNPStructureForRoleAppositives(t);
	    trees.add(t);

	    entityStrings.add(convertSuperSensePOSTOEntityString(sentence
		    .getStanfordSupersenseTaggedSentence()));

	}

	document = new Document(trees, entityStrings);

	FindMentions.go(document);
	Resolve.go(document);
	RefsToEntities.go(document);

	paragraph.setDocument(document);
	return document;
    }

    private String convertSuperSensePOSTOEntityString(
	    List<SuperSenseWord> stanfordSupersenseTaggedSentence) {

	String res = "";

	for (int i = 0; i < stanfordSupersenseTaggedSentence.size(); i++) {
	    SuperSenseWord sw = stanfordSupersenseTaggedSentence.get(i);

	    res += sw.getToken() + "/" + sw.getSstag();
	    res += " ";
	}

	return res;
    }

    public void resolveCoreference(List<Tree> sentences) {
	List<Tree> trees = new ArrayList<Tree>();
	List<String> entityStrings = new ArrayList<String>();

	for (Tree t : sentences) {
	    Document.addNPsAbovePossessivePronouns(t);
	    Document.addInternalNPStructureForRoleAppositives(t);
	    trees.add(t);
	    entityStrings.add(convertSupersensesToEntityString(t,
		    SuperSenseWrapper.getInstance()
			    .annotateSentenceWithSupersenses(t)));
	}

	document = new Document(trees, entityStrings);

	FindMentions.go(document);
	Resolve.go(document);
	RefsToEntities.go(document);

	return;
    }

    private String convertSupersensesToEntityString(Tree t,
	    List<String> supersenses) {
	String res = "";

	List<String> converted = new ArrayList<String>();
	for (int i = 0; i < supersenses.size(); i++) {
	    if (supersenses.get(i).endsWith("noun.person")) {
		converted.add("PERSON");
	    } else {
		converted.add(supersenses.get(i));
	    }
	}

	List<Tree> leaves = t.getLeaves();
	while (leaves.size() > converted.size())
	    converted.add("0");
	for (int i = 0; i < leaves.size(); i++) {
	    if (i > 0)
		res += " ";
	    res += leaves.get(i) + "/" + converted.get(i);
	}

	return res;
    }

    // Method for viewing supersense tagger output.
    public List<String> getSuperSenseTag(List<Tree> origdoc) {
	List<Tree> trees = new ArrayList<Tree>();
	List<String> entityStrings = new ArrayList<String>();

	for (Tree t : origdoc) {
	    Document.addNPsAbovePossessivePronouns(t);
	    Document.addInternalNPStructureForRoleAppositives(t);
	    trees.add(t);
	    entityStrings.add(convertSupersensesToEntityString(t,
		    SuperSenseWrapper.getInstance()
			    .annotateSentenceWithSupersenses(t)));
	}

	return entityStrings;
    }

    public static boolean hasPronoun(Tree t) {
	TregexPattern pat = TregexPatternFactory.getPattern("/^PRP/=pronoun");
	TregexMatcher matcher = pat.matcher(t);
	return matcher.find();
    }

    public static boolean isPronoun(Tree t) {
	TregexPattern pat = TregexPatternFactory
		.getPattern("NP !>> __ <<# /^PRP/=pronoun");
	TregexMatcher matcher = pat.matcher(t);
	return matcher.find();
    }

    /***
     * Method for Co-reference resolution using ArkRef
     * 
     * @param treeSet
     * @param clarifyPronouns
     * @param clarifyNonPronouns
     * @return
     */
    public List<SentenceNode> clarifyNPs(Paragraph paragraph) {

	List<SentenceNode> newSentences = new ArrayList<SentenceNode>();
	// arrays for return values from findReplacement
	List<Boolean> retModified = new ArrayList<Boolean>();
	List<Boolean> retResolvedPronounsIfNecessary = new ArrayList<Boolean>();
	List<Boolean> retHadPronouns = new ArrayList<Boolean>();
	Tree replacement;

	for (Iterator<SentenceNode> itr = paragraph.getSourceSentenceMap()
		.values().iterator(); itr.hasNext();) {

	    boolean modified = false;
	    boolean resolvedPronounsIfNecessary = true;
	    boolean hadPronouns = false;

	    SentenceNode sentence = itr.next();
	    SentenceNode sent = sentence.deepCopy();
	    Tree qRoot = sent.getTree();

	    List<Tree> replacedMentionTrees = new ArrayList<Tree>();
	    List<Tree> replacementMentionTrees = new ArrayList<Tree>();

	    if (GlobalProperties.getDebug())
		System.err.println("NPClarification processing: "
			+ AnalysisUtilities.getInstance().treeToString(qRoot));

	    // iterate over mentions in the input tree
	    List<Tree> sentenceMentionNodes = FindMentions
		    .findMentionNodes(qRoot);
	    Set<Tree> alreadySeenNodes = new HashSet<Tree>();
	    for (Tree qMentionNode : sentenceMentionNodes) {

		if (isPronoun(qMentionNode) && !clarifyPronouns)
		    continue;
		if (!isPronoun(qMentionNode) && !clarifyNonPronouns)
		    continue;

		// if the input contains multiple instances of the same node,
		// only replace the first one (e.g., as in
		// "He thought he could win.")
		// this works in conjunction with the later call to
		// isFirstMentionOfEntityInSentence,
		// which handles cases like "John thought he could win" (where
		// he = John).
		if (alreadySeenNodes.contains(qMentionNode))
		    continue;
		alreadySeenNodes.add(qMentionNode);

		replacement = findAndReplace(qMentionNode,
			sentenceMentionNodes, qRoot, sent.getSentenceNumber(),
			clarifyNonPronouns, retHadPronouns,
			retResolvedPronounsIfNecessary, retModified);

		if (replacement != null) {
		    replacedMentionTrees.add(qMentionNode);
		    replacementMentionTrees.add(replacement);
		}

		modified |= retModified.get(0);
		resolvedPronounsIfNecessary &= retResolvedPronounsIfNecessary
			.get(0);
		hadPronouns |= retHadPronouns.get(0);
	    }

	    if (modified
		    && (!hadPronouns || (hadPronouns && resolvedPronounsIfNecessary))) {
		if (GlobalProperties.getDebug())
		    System.err.println("NPClarification added: "
			    + AnalysisUtilities.getInstance().treeToString(
				    sent.getTree()));

		sent.setString(AnalysisUtilities.getInstance().treeToString(
			sent.getTree()));

		newSentences.add(sent);

		itr.remove();

	    }

	    if (!modified && resolvedPronounsIfNecessary && hadPronouns) {

		if (GlobalProperties.getDebug())
		    System.err.println("NPClarification resolved pronouns in: "
			    + sent.getString());

	    }

	}

	for (SentenceNode sent : newSentences) {
	    paragraph.getSourceSentenceMap()
		    .put(sent.getSentenceNumber(), sent);
	}

	// treeSet.addAll(newTrees);
	// add the newly created trees to the input set
	return newSentences;
    }

    /**
     * Look in the entity graph of the document to find a replacement for
     * mentionNode (a copy of a node from the original document). Checks
     * sentenceMentionNodes to make sure that the mention is the first in the
     * sentence. Uses sentenceRoot (a copy of the original sentence root) to
     * find out if head's match.
     * 
     * @param mentionNode
     * @param sentenceMentionNodes
     * @param sentenceRoot
     * @param qSentNumber
     * @return
     */
    private Tree findAndReplace(Tree mentionNode,
	    List<Tree> sentenceMentionNodes, Tree sentenceRoot,
	    int qSentNumber, boolean clarifyNonPronouns,
	    List<Boolean> retHadPronouns,
	    List<Boolean> retResolvedPronounsIfNecessary,
	    List<Boolean> retModified) {
	boolean modified = false;
	boolean resolvedPronounsIfNecessary = true;
	boolean hadPronouns = false;
	Tree replacementCopy = null;

	// iterate over document mentions to find a match
	for (int i = 0; i < document.mentions().size(); i++) {
	    replacementCopy = null;
	    Mention m = document.mentions().get(i);
	    int mentionSentenceNum = m.getSentence().ID();

	    // skip the tree if its not the same sentence as the mention
	    if (mentionSentenceNum != qSentNumber) {
		continue;
	    }

	    // Skip if the original mention doesn't match the input mention.
	    // We use case-insensitive match so that "he" equals "He", which
	    // might happen due to other transformations...
	    // Of course, using the case insensitive matching like this is a bit
	    // of a hack,
	    // but it's probably simpler than making sure all the input is
	    // properly cased.
	    if (!caseInsensitiveNodeMatch(m.node(), mentionNode)) {
		// if(!m.node().equals(mentionNode)){
		continue;
	    }

	    if (Types.isPronominal(m)) {
		hadPronouns = true;
		resolvedPronounsIfNecessary = false;
	    }

	    // with multiple instances of the same word (e.g., "he said he did")
	    // skip if the original mention has a different parent head word
	    // if(!parentsHaveSameHeadWords(m.node(),
	    // m.getSentence().rootNode(), mentionNode, sentenceRoot)){
	    // continue;
	    // }

	    // find best mention to replace this with
	    Mention replacement = findReplacementByFirstMention(m);
	    // don't replace if the replacement is identical
	    if (AnalysisUtilities
		    .getInstance()
		    .treeToString(replacement.node())
		    .equalsIgnoreCase(
			    AnalysisUtilities.getInstance().treeToString(
				    mentionNode))) {
		continue;
	    }

	    // skip it if the best is a pronoun itself
	    if (hasPronoun(replacement.node())) {
		continue;
	    } else if (hadPronouns) {
		resolvedPronounsIfNecessary = true;
	    }

	    // only consider replacements for the first mention in the sentence
	    // of each entity
	    if (!isFirstMentionOfEntityInSentence(m, mentionNode,
		    sentenceMentionNodes)) {
		continue;
	    }

	    if (replacement.ID() != m.ID()
		    && !replacement.node().dominates(m.node())) {
		// make copy with nested mentions replaced
		// create a copy of the node that will be used to replace
		// the node in the input tree.
		if (clarifyNonPronouns) {
		    replacementCopy = createCopyWithNestedMentionsClarified(
			    replacement, sentenceMentionNodes);
		} else {
		    replacementCopy = replacement.node().deepCopy();
		}
		replacementCopy = simplifyMentionTree(replacementCopy);

		if (isPossessiveNP(m.node())
			&& !isPossessiveNP(replacement.node())) {
		    replacementCopy.addChild(AnalysisUtilities.getInstance()
			    .readTreeFromString("(POS 's)"));
		}
		if (!isPossessiveNP(m.node())
			&& isPossessiveNP(replacement.node())) {
		    // remove the POS node
		    List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		    List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		    TregexPattern matchPattern = TregexPatternFactory
			    .getPattern("POS=pos");
		    ps.add(Tsurgeon.parseOperation("prune pos"));
		    TsurgeonPattern p = Tsurgeon.collectOperations(ps);
		    ops.add(new Pair<TregexPattern, TsurgeonPattern>(
			    matchPattern, p));
		    Tsurgeon.processPatternsOnTree(ops, replacementCopy);

		}
		AnalysisUtilities.getInstance().downcaseFirstToken(
			replacementCopy);

		// insert the copy into the input tree
		Tree qParent = mentionNode.parent(sentenceRoot);
		// if we already replaced a parent node, skip
		if (qParent == null) {
		    continue;
		}

		qParent.setChild(qParent.objectIndexOf(mentionNode),
			replacementCopy);
		modified = true;

		// don't need to consider other mentions
		break;
	    }
	}

	retModified.clear();
	retModified.add(modified);
	retResolvedPronounsIfNecessary.clear();
	retResolvedPronounsIfNecessary.add(resolvedPronounsIfNecessary);
	retHadPronouns.clear();
	retHadPronouns.add(hadPronouns);

	return replacementCopy;
    }

    /**
     * Creates a copy of the node for replacement that has any nested mentions
     * clarified. Uses sentenceMentionNodes to make sure any nested mentions
     * will not be replaced if they are the second mention in the sentence.
     * 
     * 
     * @param replacement
     *            (from the original document object)
     * @param sentenceMentionNodes
     * @param sentenceRoot
     * @param qSentNumber
     * @return
     */
    private Tree createCopyWithNestedMentionsClarified(Mention replacement,
	    List<Tree> sentenceMentionNodes) {
	Tree copy = replacement.node().deepCopy();

	// consider all nodes dominated by the replacement
	for (Mention other : document.mentions()) {
	    if (!replacement.node().dominates(other.node())) {
		continue;
	    }

	    for (Tree t : copy) {
		if (t.equals(other.node())) {
		    findAndReplace(t, sentenceMentionNodes, copy, other
			    .getSentence().ID(), true,
			    new ArrayList<Boolean>(), new ArrayList<Boolean>(),
			    new ArrayList<Boolean>());
		}
	    }
	}

	return copy;
    }

    /**
     * return true if one of the other mentions in the sentence (qMentionNodes)
     * is in the list of linked mentions for the given mention m AND if this
     * other mention comes before m. Else, return false.
     * 
     * @param mentionInOriginalDocument
     * @param mentionNodesInCurrentSentence
     * @return
     */
    private boolean isFirstMentionOfEntityInSentence(
	    Mention mentionInOriginalDocument,
	    Tree mentionNodeInCurrentSentence,
	    List<Tree> mentionNodesInCurrentSentence) {
	int mentionIndex = mentionNodesInCurrentSentence
		.indexOf(mentionNodeInCurrentSentence);

	// iterate over mentions that are linked with the given mention m
	for (Mention linkedMention : document.entGraph().getLinkedMentions(
		mentionInOriginalDocument)) {

	    // skip the linked mention if it is not in the same sentence
	    if (linkedMention.getSentence().ID() != mentionInOriginalDocument
		    .getSentence().ID()) {
		continue;
	    }

	    // iterate through all the mentions before the given
	    // mentionNode in the current (possibly transformed) sentence.
	    // if the linked mention matches a tree object,
	    // then return false to indicate that the given mention node is
	    // not the first mention of its entity (because linkedmention is).
	    Tree linkedMentionHead = linkedMention.getHeadNode();
	    for (int i = 0; i < mentionIndex; i++) {
		if (caseInsensitiveNodeMatch(
			linkedMentionHead,
			mentionNodesInCurrentSentence.get(i)
				.headTerminal(
					AnalysisUtilities.getInstance()
						.getHeadFinder()))) {
		    return false;
		}
	    }

	}

	return true;
    }

    private Mention findReplacementByFirstMention(Mention m) {
	Mention res = m;
	int minID = m.ID();
	for (Mention other : document.entGraph().getLinkedMentions(m)) {
	    if (other.ID() < minID) {
		res = other;
		minID = other.ID();
	    }
	}

	return res;
    }

    public Document getDocument() {
	return document;
    }

    public static boolean isPossessiveNP(Tree tree) {
	String patS = "NP=parentnp [ < /^PRP\\$/ | < POS ] !> __";
	TregexPattern pat = TregexPatternFactory.getPattern(patS);
	TregexMatcher matcher = pat.matcher(tree);
	return matcher.find();
    }

    /**
     * remove non-restrictive appositives and non-restrictive relative clauses
     * 
     * @param input
     * @return
     */
    public Tree simplifyMentionTree(Tree input) {
	String tregexOpStr;
	TregexPattern matchPattern;
	Tree res = input;

	// if the head is a proper noun, return the NP subtree dominating the
	// head
	Tree newHead = input.headPreTerminal(AnalysisUtilities.getInstance()
		.getHeadFinder());

	boolean hasCommaSubtree = false;
	boolean hasParenthesesSubtree = false;
	for (Tree subtree : input.getChildrenAsList()) {
	    if (subtree.label().value().equals(",")) {
		hasCommaSubtree = true;
	    }
	}

	List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
	tregexOpStr = "NP < (PRN=paren $ __)";
	matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
	if (matchPattern.matcher(input).find()) {
	    hasParenthesesSubtree = true;

	    List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
	    TsurgeonPattern p;
	    ps.add(Tsurgeon.parseOperation("prune paren"));
	    p = Tsurgeon.collectOperations(ps);
	    ops.add(new Pair<TregexPattern, TsurgeonPattern>(matchPattern, p));
	    Tsurgeon.processPatternsOnTree(ops, input);

	}

	// remove parenthesis
	tregexOpStr = "__=parenthetical $, /-LRB-/=leadingpunc $. /-RRB-/=trailingpunc";
	matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
	if (matchPattern.matcher(input).find()) {
	    hasParenthesesSubtree = true;
	    List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
	    ps = new ArrayList<TsurgeonPattern>();
	    ps.add(Tsurgeon.parseOperation("prune leadingpunc"));
	    ps.add(Tsurgeon.parseOperation("prune parenthetical"));
	    ps.add(Tsurgeon.parseOperation("prune trailingpunc"));
	    TsurgeonPattern p = Tsurgeon.collectOperations(ps);
	    ops.add(new Pair<TregexPattern, TsurgeonPattern>(matchPattern, p));

	    Tsurgeon.processPatternsOnTree(ops, input);
	}

	if (!hasCommaSubtree && !hasParenthesesSubtree) {
	    return input;
	}

	// if there is a comma, choose the subtree that has the proper noun as
	// the head
	if (!newHead.label().value().equals("NNP")
		&& !newHead.label().value().equals("NNPS")) {

	    tregexOpStr = "__=mention !> __ < /,/=comma << NP=np";

	    matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
	    TregexMatcher m = matchPattern.matcher(input);
	    Tree np;
	    while (m.find()) {
		np = m.getNode("np");
		Tree subtreeHead = np.headPreTerminal(AnalysisUtilities
			.getInstance().getHeadFinder());
		if (subtreeHead.label().value().equals("NNP")
			|| subtreeHead.label().value().equals("NNPS")) {
		    newHead = subtreeHead;
		    break;
		}
	    }
	}

	for (Tree subtree : input.getChildrenAsList()) {
	    if (subtree.dominates(newHead)) {
		return subtree;
	    }
	}

	return res;
    }

    public static boolean caseInsensitiveNodeMatch(Tree n1, Tree n2) {
	return n1.toString().equalsIgnoreCase(n2.toString());
    }

    /**
     * code to clarify Noun Phrases;
     * 
     * @param treeSet
     * @param clarifyPronouns
     * @param clarifyNonPronouns
     * @return
     */
    public List<SentenceNode> clarifyNounPhrases(List<SentenceNode> treeSet,
	    boolean clarifyPronouns, boolean clarifyNonPronouns) {

	List<SentenceNode> newTreess = new ArrayList<SentenceNode>();

	// arrays for return values from findReplacement
	List<Boolean> retModified = new ArrayList<Boolean>();
	List<Boolean> retResolvedPronounsIfNecessary = new ArrayList<Boolean>();
	List<Boolean> retHadPronouns = new ArrayList<Boolean>();
	Tree replacement;

	for (Iterator<SentenceNode> itr = treeSet.iterator(); itr.hasNext();) {
	    SentenceNode q = itr.next();
	    boolean modified = false;
	    boolean resolvedPronounsIfNecessary = true;
	    boolean hadPronouns = false;

	    SentenceNode qCopy = q.deepCopy();
	    Tree qRoot = qCopy.getTree();

	    List<Tree> replacedMentionTrees = new ArrayList<Tree>();
	    List<Tree> replacementMentionTrees = new ArrayList<Tree>();

	    if (GlobalProperties.getDebug())
		System.err.println("NPClarification processing: "
			+ AnalysisUtilities.getInstance().treeToString(qRoot));

	    // iterate over mentions in the input tree
	    List<Tree> sentenceMentionNodes = FindMentions
		    .findMentionNodes(qRoot);
	    Set<Tree> alreadySeenNodes = new HashSet<Tree>();
	    for (Tree qMentionNode : sentenceMentionNodes) {

		if (isPronoun(qMentionNode) && !clarifyPronouns)
		    continue;
		if (!isPronoun(qMentionNode) && !clarifyNonPronouns)
		    continue;

		// if the input contains multiple instances of the same node,
		// only replace the first one (e.g., as in
		// "He thought he could win.")
		// this works in conjunction with the later call to
		// isFirstMentionOfEntityInSentence,
		// which handles cases like "John thought he could win" (where
		// he = John).
		if (alreadySeenNodes.contains(qMentionNode))
		    continue;
		alreadySeenNodes.add(qMentionNode);

		replacement = findAndReplace(qMentionNode,
			sentenceMentionNodes, qRoot,
			qCopy.getSourceSentenceNumber(), clarifyNonPronouns,
			retHadPronouns, retResolvedPronounsIfNecessary,
			retModified);

		if (replacement != null) {
		    replacedMentionTrees.add(qMentionNode);
		    replacementMentionTrees.add(replacement);
		}

		modified |= retModified.get(0);
		resolvedPronounsIfNecessary &= retResolvedPronounsIfNecessary
			.get(0);
		hadPronouns |= retHadPronouns.get(0);
	    }

	    if (modified
		    && (!hadPronouns || (hadPronouns && resolvedPronounsIfNecessary))) {
		if (GlobalProperties.getDebug())
		    System.err.println("NPClarification added: "
			    + AnalysisUtilities.getInstance().treeToString(
				    qCopy.getTree()));

		newTreess.add(qCopy);
		// extractClarificationFeatures(qCopy, replacedMentionTrees,
		// replacementMentionTrees);
		itr.remove();

	    }

	    if (!modified && resolvedPronounsIfNecessary && hadPronouns) {
		if (GlobalProperties.getDebug())
		    System.err.println("NPClarification resolved pronouns in: "
			    + AnalysisUtilities.getInstance().treeToString(
				    q.getTree()));
		// set the NPC feature for the ORIGINAL tree (we don't need to
		// add it), not the copy
		q.setFeatureValue("performedNPClarification", 1.0);
	    }

	}
	// treeSet.addAll(newTrees);

	treeSet.addAll(newTreess);

	// add the newly created trees to the input set
	return treeSet;
    }

    /*
     * private void extractClarificationFeatures(SentenceNode qCopy, List<Tree>
     * replacedMentionTrees, List<Tree> replacementMentionTrees) { // TODO
     * Auto-generated method stub
     * qCopy.setFeatureValue("performedNPClarification", 1.0); String treeName;
     * List<Tree> treeList;
     * 
     * for (int i = 0; i < 2; i++) { if (i == 0) { treeList =
     * replacedMentionTrees; treeName = "ReplacedMentions"; } else { treeList =
     * replacementMentionTrees; treeName = "ReplacementMentions"; }
     * 
     * double numVagueNPs = 0.0; double length = 0.0; double numNPs = 0.0;
     * double numProperNouns = 0.0; double numQuantities = 0.0; double
     * numAdjectives = 0.0; double numAdverbs = 0.0; double numPPs = 0.0; double
     * numSubordinateClauses = 0.0; double numConjunctions = 0.0; double
     * numPronouns = 0.0;
     * 
     * for (Tree tree : treeList) {
     * SpecificityAnalyzer.getInstance().analyze(tree); numVagueNPs +=
     * SpecificityAnalyzer.getInstance() .getNumVagueNPs(); length +=
     * AnalysisUtilities.getInstance().sentenceLength(tree); numNPs +=
     * AnalysisUtilities.getInstance().getNumberOfMatchesInTree( "NP !> NP",
     * tree); numProperNouns +=
     * AnalysisUtilities.getInstance().getNumberOfMatchesInTree( "/^NNP/",
     * tree); numQuantities +=
     * AnalysisUtilities.getInstance().getNumberOfMatchesInTree( "CD|QP", tree);
     * numAdjectives +=
     * AnalysisUtilities.getInstance().getNumberOfMatchesInTree( "/^JJ/", tree);
     * numAdverbs += AnalysisUtilities.getInstance().getNumberOfMatchesInTree(
     * "/^RB/", tree); numPPs += AnalysisUtilities.getInstance()
     * .getNumberOfMatchesInTree("PP", tree); numSubordinateClauses +=
     * AnalysisUtilities.getInstance() .getNumberOfMatchesInTree("SBAR", tree);
     * numConjunctions +=
     * AnalysisUtilities.getInstance().getNumberOfMatchesInTree( "CC", tree);
     * numPronouns += AnalysisUtilities.getInstance().getNumberOfMatchesInTree(
     * "/^PRP/", tree);
     * 
     * } }
     * 
     * 
     * }
     */

    /**
     * @param args
     */
    public static void main(String[] args) {
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    System.in));

	    GlobalProperties.setDebug(true);
	    // ARKref.Opts.debug = true;

	    if (GlobalProperties.getDebug())
		System.err.println("\nInput Text:");
	    String doc;

	    while (true) {
		doc = "";
		String buf = "";

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

		List<String> sentences = AnalysisUtilities.getInstance()
			.getSentences(doc);

		List<Tree> trees = new ArrayList<Tree>();
		HashMap<Integer, SentenceNode> sourceSentenceMap = new HashMap<Integer, SentenceNode>();

		int sentenceNum = 0;
		for (String s : sentences) {
		    Tree t = AnalysisUtilities.getInstance().parseSentence(s).parse;
		    trees.add(t);

		    SentenceNode sentence = new SentenceNode();
		    sentence.setOriginal_sentence(s);
		    sentence.setString(s);
		    sentence.setTree(t);
		    sentence.setSentenceNumber(sentenceNum);
		    sourceSentenceMap.put(sentenceNum, sentence);

		    sentenceNum++;

		    // sentenceList.add(sentence);
		}

		ParagraphNode para = new ParagraphNode();
		para.setSourceSentenceMap(sourceSentenceMap);

		CoreferenceResolutionUsingArkRef npc = new CoreferenceResolutionUsingArkRef();
		npc.resolveCoreference(para);

		// Normally, we would perform some transformations right here,
		// but for this class we just print out clarified versions of
		// the original sentences.

		List<SentenceNode> newTrees = npc.clarifyNPs(para);

		for (SentenceNode q : newTrees) {
		    System.out.println(AnalysisUtilities.getInstance()
			    .treeToString(q.getTree()));
		}

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void setDocument(Document document) {
	this.document = document;
    }

}
