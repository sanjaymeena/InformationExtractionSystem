package com.asus.ctc.ie.entityextractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javatools.parsers.NounGroup;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asus.ctc.ie.config.GlobalProperties;
import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.datastructures.entities.Concept;
import com.asus.ctc.ie.datastructures.senserelation.SuperSenseWord;
import com.asus.ctc.ie.entityextractor.interfaces.EntityExtractionFromText;
import com.asus.ctc.ie.utilities.AnalysisUtilities;
import com.asus.ctc.ie.utilities.EssentialEnums.NERNounTags;
import com.asus.ctc.ie.utilities.TregexPatternFactory;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class EntityExtractionUsingSuperSense implements
	EntityExtractionFromText {
    Map<NERNounTags, Map<Concept, List<Integer>>> AllTags;
    boolean includePluralNoun; // NNS
    boolean includeProperPluralNouns; // NNPS
    boolean includeCommonNoun; // NN
    boolean extractGenericTag;
    private final static Logger log = LoggerFactory
	    .getLogger(EntityExtractionUsingSuperSense.class);

    public EntityExtractionUsingSuperSense() {

	// read IE processing configuration
	Properties entityExtractorProps = GlobalProperties.getProperties(2);

	includePluralNoun = Boolean.parseBoolean(entityExtractorProps
		.getProperty("includePluralNoun"));
	includeProperPluralNouns = Boolean.parseBoolean(entityExtractorProps
		.getProperty("includeProperPluralNouns"));
	includeCommonNoun = Boolean.parseBoolean(entityExtractorProps
		.getProperty("includeCommonNoun"));
	extractGenericTag= Boolean.parseBoolean(entityExtractorProps
		.getProperty("extractGenericTag"));
    }

    /**
     * @Override
     * @param articleKnowledge
     */
    public void extractEntitiesFromText(TextNode textnode) {
	StopWatch sw = new StopWatch();
	sw.start();

	/**
	 * Create ner tags for the article.
	 */
	initializeEntityMap();

	/**
	 * Create the ConceptKnowledgeMap for the text
	 */
	extractEntities(textnode.getAllSentences());

	// set the map in textNode;
	textnode.setAllTags(AllTags);

	// add the entity information in simplified sentences;
	addEntitiesToSimplifiedSentences(textnode);

	sw.stop();

	log.info("Total Time taken for Entity Extraction:  " + sw.toString());
    }

    /**
     * Add the entity information to the simplified sentences;
     * 
     * @param textnode
     */
    private void addEntitiesToSimplifiedSentences(TextNode textnode) {
	// TODO Auto-generated method stub
	List<SentenceNode> allSentences = textnode.getAllSentences();
	for (SentenceNode sentenceNode : allSentences) {
	    if (sentenceNode.isPerformedSimplification()) {
		List<SentenceNode> simplifiedSentences = sentenceNode
			.getSimplifiedSentences();
		for (SentenceNode simplified : simplifiedSentences) {
		    addEntitiesToSimplifiedSentences_helper(sentenceNode,
			    simplified);
		}
	    }
	}
    }

    /**
     * 
     * @param sourceSentence
     * @param simplified
     */
    private void addEntitiesToSimplifiedSentences_helper(
	    SentenceNode sourceSentence, SentenceNode simplified) {
	List<Concept> conceptsList = sourceSentence.getConceptsList();
	List<Concept> simplifiedConceptsList = new ArrayList<Concept>();
	// currently a simple string matching method.

	String sent = simplified.getString().toLowerCase();
	for (Concept concept : conceptsList) {
	    String c = concept.getConcept().toLowerCase().trim();
	    if (sent.contains(c)) {
		simplifiedConceptsList.add(concept);
	    }
	}

	simplified.setConceptsList(simplifiedConceptsList);
    }

    /**
     * This function creates the Entity Knowledge Map. All the entities are put
     * in a data structure classifiable by entity type;
     * 
     * Entities are only extracted from the source sentence
     * 
     * @param sentenceTags
     */
    private void extractEntities(

    List<SentenceNode> sentenceTags) {

	for (SentenceNode sentenceTagInfo : sentenceTags) {

	    // For the tagged sentence
	    List<SuperSenseWord> nerTags = sentenceTagInfo
		    .getStanfordSupersenseTaggedSentence();

	    int t1 = 10;
	    if (nerTags != null) {
		for (int i = 0; i < nerTags.size(); i++) {
		    SuperSenseWord word = nerTags.get(i);
		    String w = word.getToken();
		    String t = word.getSstag();

		    if (t.contains("noun.") || t.contains("PERSON")) {

			String headWord = EntityExtractorUtilities
				.getInstance().NPTree(w,
					sentenceTagInfo.getTree());
			// println(headWord);

			/**
			 * One problem here: @date 22 nov 2012 Republic of China
			 * : Republic/NN/B-noun.location, of/IN/I-noun.location
			 */

			int newIndex = -1;
			if (headWord != null) {
			    for (int j = i; j < nerTags.size(); j++) {

				String Tagelement = nerTags.get(j).getToken();
				if (headWord.equals(Tagelement)) {
				    newIndex = j;
				    /**
				     * We want to handle the case of Republic of
				     * China: Republic/NN/B-noun.location,
				     * of/IN/I-noun.location,
				     * China/NNP/I-noun.location,
				     */
				    int possibleNewIndex = checkSpecialCasesForHeadword(
					    newIndex, sentenceTagInfo);
				    if (possibleNewIndex != -1) {
					newIndex = -1;
					i = possibleNewIndex;
				    }

				    break;
				}
			    }
			}

			if (newIndex != -1) {
			    // println(newIndex);
			    word = nerTags.get(newIndex);
			    w = word.getToken();
			    t = word.getSstag();

			    i = extractTagsFromTaggedSense(word,
				    AllTags.get(NERNounTags.location),
				    NERNounTags.location, sentenceTagInfo, i);
			}

		    }

		    // Cases when no tags are present
		    else {

			if (t.contains("0") && !w.equals(".")) {
			    // SuperSenseWord w1 = nerTags.get(i);

			    /**
			     * Special case for
			     * 
			     * Webber works in Asus. Penguins are found in
			     * Antartica. Here Asus, Antartica are not tagged.
			     */
			    List<SuperSenseWord> sensePOsList = sentenceTagInfo
				    .getStanfordSupersenseTaggedSentence();
			    SuperSenseWord nonTaggedSuperSense = sensePOsList
				    .get(i);
			    String pos = nonTaggedSuperSense.getPosTag();

			    if (pos.equals("NNP") && ((i - 1) > 0)) {

				SuperSenseWord swe = sensePOsList.get(i - 1);

				String pos1 = swe.getPosTag();
				// String token = swe.getToken();

				if (pos1.equals("IN")) {

				    SuperSenseWord newWord = nerTags.get(i);

				    String sstag = "B-noun.location";

				    newWord.setSstag(sstag);
				    nonTaggedSuperSense.setSstag(sstag);

				    sentenceTagInfo
					    .getStanfordSupersenseTaggedSentence()
					    .set(i, nonTaggedSuperSense);
				    nerTags.set(i, newWord);
				    i = i - 1;
				}
			    }
			}
		    }

		}

		// println(locationTags);
	    }
	}
    }

    /**
     * 
     * This is a helper function for extraction of entities with tagged sense
     * information
     * 
     * @param word
     * @param map
     * @param location
     * @param sentenceTagInfo
     * @param i
     * @return
     */
    int extractTagsFromTaggedSense(SuperSenseWord word,
	    Map<Concept, List<Integer>> map, NERNounTags location,
	    SentenceNode sentenceTagInfo, int i) {

	String w = word.getToken();
	String t = word.getSstag();

	if (t.contains("noun.location")) {
	    i = extractTags(AllTags.get(NERNounTags.location),
		    NERNounTags.location, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.person") || t.contains("PERSON")) {
	    i = extractTags(AllTags.get(NERNounTags.person),
		    NERNounTags.person, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.group")) {
	    i = extractTags(AllTags.get(NERNounTags.group), NERNounTags.group,
		    w, sentenceTagInfo, i);
	} else if (t.contains("noun.time")) {
	    i = extractTags(AllTags.get(NERNounTags.time), NERNounTags.time, w,
		    sentenceTagInfo, i);
	} else if (t.contains("noun.act")) {
	    i = extractTags(AllTags.get(NERNounTags.act), NERNounTags.act, w,
		    sentenceTagInfo, i);
	}

	else if (t.contains("noun.object")) {
	    i = extractTags(AllTags.get(NERNounTags.object),
		    NERNounTags.object, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.process")) {
	    i = extractTags(AllTags.get(NERNounTags.process),
		    NERNounTags.process, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.event")) {
	    i = extractTags(AllTags.get(NERNounTags.event), NERNounTags.event,
		    w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.communication")) {
	    i = extractTags(AllTags.get(NERNounTags.communication),
		    NERNounTags.communication, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.animal")) {
	    i = extractTags(AllTags.get(NERNounTags.animal),
		    NERNounTags.animal, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.substance")) {
	    i = extractTags(AllTags.get(NERNounTags.substance),
		    NERNounTags.substance, w, sentenceTagInfo, i);
	} else if (t.contains("noun.artifact")) {
	    i = extractTags(AllTags.get(NERNounTags.artifact),
		    NERNounTags.artifact, w, sentenceTagInfo, i);
	} else if (t.contains("noun.body")) {
	    i = extractTags(AllTags.get(NERNounTags.body), NERNounTags.body, w,
		    sentenceTagInfo, i);
	}

	else if (t.contains("noun.food")) {
	    i = extractTags(AllTags.get(NERNounTags.body), NERNounTags.body, w,
		    sentenceTagInfo, i);
	} else if (t.contains("noun.other")) {
	    i = extractTags(AllTags.get(NERNounTags.other), NERNounTags.other,
		    w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.cognition")) {
	    i = extractTags(AllTags.get(NERNounTags.cognition),
		    NERNounTags.cognition, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.Tops")) {
	    i = extractTags(AllTags.get(NERNounTags.Tops), NERNounTags.Tops, w,
		    sentenceTagInfo, i);
	}

	else if (t.contains("noun.attribute")) {
	    i = extractTags(AllTags.get(NERNounTags.attribute),
		    NERNounTags.attribute, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.motive")) {
	    i = extractTags(AllTags.get(NERNounTags.motive),
		    NERNounTags.motive, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.phenomenon")) {
	    i = extractTags(AllTags.get(NERNounTags.phenomenon),
		    NERNounTags.phenomenon, w, sentenceTagInfo, i);
	} else if (t.contains("noun.possession")) {
	    i = extractTags(AllTags.get(NERNounTags.possession),
		    NERNounTags.possession, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.plant")) {
	    i = extractTags(AllTags.get(NERNounTags.plant), NERNounTags.plant,
		    w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.relation")) {
	    i = extractTags(AllTags.get(NERNounTags.relation),
		    NERNounTags.relation, w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.shape")) {
	    i = extractTags(AllTags.get(NERNounTags.shape), NERNounTags.shape,
		    w, sentenceTagInfo, i);
	}

	else if (t.contains("noun.state")) {
	    i = extractTags(AllTags.get(NERNounTags.state), NERNounTags.state,
		    w, sentenceTagInfo, i);
	} else if (t.contains("noun.feeling")) {
	    i = extractTags(AllTags.get(NERNounTags.state), NERNounTags.state,
		    w, sentenceTagInfo, i);
	} else if (t.contains("noun.quantity")) {
	    i = extractTags(AllTags.get(NERNounTags.state), NERNounTags.state,
		    w, sentenceTagInfo, i);
	}

	else {

	}

	return i;
    }

    private int extractTags(Map<Concept, List<Integer>> map, NERNounTags tag,
	    String w, SentenceNode sentenceTagInfo, int index) {

	Tree concept = extractTagsHelper(tag, w, sentenceTagInfo, index);
	int newIndex = -1;
	newIndex = getNewIndex(index, concept,
		sentenceTagInfo.getStanfordSupersenseTaggedSentence());

	if (concept != null) {

	    addTags(map, concept, sentenceTagInfo, tag);
	    if (newIndex != -1) {
		return newIndex;
	    }

	}

	return index;
    }

    private int getNewIndex(int index, Tree concept, List<SuperSenseWord> nerTag) {

	int newIndex = -1;

	if (concept != null) {
	    List<Tree> conceptLeaves = concept.getLeaves();
	    Tree lastElm = conceptLeaves.get(conceptLeaves.size() - 1);

	    String lastLeave = AnalysisUtilities.getInstance().treeToString(
		    lastElm);
	    // println(lastLeave);

	    for (int i = index; i < nerTag.size(); i++) {
		String Tagelement = nerTag.get(i).getToken();
		if (lastLeave.equals(Tagelement)) {
		    newIndex = i;
		    break;
		}
	    }

	    // println(newIndex);
	}
	return newIndex;
    }

    private void addTags(Map<Concept, List<Integer>> tags, Tree conceptTree,
	    SentenceNode sentenceTagInfo, NERNounTags tag) {

	Concept concepts = new Concept();

	String concept = AnalysisUtilities.getInstance().treeToString(
		conceptTree);

	concepts.setConcept(concept);
	concepts.setConcepTree(conceptTree);
	concepts.setTag(tag);

	boolean addConcept;

	/**
	 * If the tag is not animal, person, location . Let's let is pass NER
	 * inclusion test.
	 */

	if (!tag.equals(NERNounTags.person)
		&& !tag.equals(NERNounTags.location)
		&& !tag.equals(NERNounTags.date)) {
	    addConcept = true;
	}

	else {
	    addConcept = checkIfConceptbeAdded(concepts);
	}

	if (addConcept) {
	    addModifierDetailsToConcept(concepts,
		    sentenceTagInfo.getStanfordSupersenseTaggedSentence());

	    if (tags.size() > 0 && tags.containsKey(concepts)) {
		tags.get(concepts).add(
			sentenceTagInfo.getSourceSentenceNumber());
		sentenceTagInfo.getConceptsList().add(concepts);
		
		
		
	    }

	    else {
		List<Integer> sentenceNumber = new ArrayList<Integer>();
		sentenceNumber.add(sentenceTagInfo.getSourceSentenceNumber());
		tags.put(concepts, sentenceNumber);
		sentenceTagInfo.getConceptsList().add(concepts);
	    }
	}

    }

    /**
     * 
     * @param concept
     * @return
     */
    private boolean checkIfConceptbeAdded(Concept concept) {

	boolean addConcept = false;

	Tree parse = concept.getConcepTree();
	String conceptPhrase = concept.getConcept();

	NounGroup ng = new NounGroup(conceptPhrase);
	String word = ng.head();

	// String head = null;

	String tregex1;
	TregexPattern matchPattern1;
	TregexMatcher matcher1;

	tregex1 = getProperTregexForConceptAddition(word);

	// tregex1 = "NP=parent < (NNP|NNS|NNPS=word < " + word + ")";
	matchPattern1 = TregexPatternFactory.getPattern(tregex1);
	matcher1 = matchPattern1.matcher(parse);

	if (matcher1.find()) {
	    addConcept = true;

	}

	return addConcept;
	// TODO Auto-generated method stub

    }

    /**
     * Function to provide tregex for filter of noun types. E.g. Whether to
     * include NNS, NNPS or not
     * 
     * @param word
     */
    private String getProperTregexForConceptAddition(String word) {
	// TODO Auto-generated method stub
	String tregex = "";
	String base_tregex = "NP=parent < (NNP";

	tregex = base_tregex;
	if (includePluralNoun) {
	    tregex += "|NNS";
	}

	if (includeProperPluralNouns) {
	    tregex += "|NNPS";
	}
	if (includeCommonNoun) {
	    tregex += "|NN";
	}

	tregex += "=word < " + word + ")";

	return tregex;
    }

    private Tree extractTagsHelper(NERNounTags tag, String word,
	    SentenceNode sentenceTagInfo, int index) {

	Tree concept = null;
	// Tree sentenceParse;

	switch (tag) {
	case location:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case object:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case person:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case act:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case animal:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case group:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case process:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case event:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case communication:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case substance:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case artifact:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case body:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case other:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case cognition:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case Tops:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case attribute:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case motive:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case phenomenon:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case possession:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case plant:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case relation:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	case shape:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case state:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case feeling:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;
	case quantity:
	    concept = extractGenericTag(word, sentenceTagInfo, index);
	    break;

	default:
	    break;
	}
	return concept;
    }

    /**
     * @param word
     * @param sentenceTagInfo
     * @param currIndex
     * @return the concept Tree extracted from Tree
     */
    public Tree extractGenericTag(String word, SentenceNode sentenceTagInfo,
	    int currIndex) {

	Tree concept = null;
	Tree sentenceTree = sentenceTagInfo.getTree();
	String tregex1;
	TregexPattern matchPattern1;
	TregexMatcher matcher1;
	
	if(extractGenericTag){
	    tregex1 = "NP=parent < (NN|NNP|NNS|NNPS=word < " + word + ")";
	}
	else{
	    tregex1=  getProperTregexForConceptAddition(word);
	}
	
	matchPattern1 = TregexPatternFactory.getPattern(tregex1);
	matcher1 = matchPattern1.matcher(sentenceTree);

	while (matcher1.find()) {

	    Tree parent = matcher1.getNode("parent");
	    Tree parentCopy = parent.deepCopy();

	    parentCopy = EntityExtractorUtilities.getInstance()
		    .checkConjunctionPatternCases(parentCopy, word);

	    int conceptIndex = EntityExtractorUtilities.getInstance()
		    .getLastIndexOfConcept(parentCopy, sentenceTagInfo);
	    if (conceptIndex >= currIndex) {
		EntityExtractorUtilities.getInstance().genericPrune(parentCopy);
		concept = parentCopy;
		break;
	    }
	}

	return concept;

    }

    /**
     * Initialize the EntityMap to store different entities
     * 
     * @param AllTags
     */
    private void initializeEntityMap() {
	AllTags = new HashMap<NERNounTags, Map<Concept, List<Integer>>>();
	Map<Concept, List<Integer>> locationTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> personTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> objectTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> groupTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> actTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> processTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> eventTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> communicationTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> animalTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> substanceTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> artifactTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> bodyTags = new HashMap<Concept, List<Integer>>();

	Map<Concept, List<Integer>> foodTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> otherTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> cognitionTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> TopsTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> attributeTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> motiveTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> phenomenonTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> possessionTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> plantTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> relationTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> shapeTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> stateTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> feelingTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> quantityTags = new HashMap<Concept, List<Integer>>();
	Map<Concept, List<Integer>> timeTags = new HashMap<Concept, List<Integer>>();

	AllTags.put(NERNounTags.body, bodyTags);
	AllTags.put(NERNounTags.location, locationTags);
	AllTags.put(NERNounTags.person, personTags);
	AllTags.put(NERNounTags.object, objectTags);
	AllTags.put(NERNounTags.group, groupTags);
	AllTags.put(NERNounTags.act, actTags);
	AllTags.put(NERNounTags.process, processTags);
	AllTags.put(NERNounTags.event, eventTags);
	AllTags.put(NERNounTags.communication, communicationTags);
	AllTags.put(NERNounTags.animal, animalTags);
	AllTags.put(NERNounTags.substance, substanceTags);
	AllTags.put(NERNounTags.artifact, artifactTags);
	AllTags.put(NERNounTags.food, foodTags);
	AllTags.put(NERNounTags.other, otherTags);
	AllTags.put(NERNounTags.cognition, cognitionTags);
	AllTags.put(NERNounTags.Tops, TopsTags);
	AllTags.put(NERNounTags.attribute, attributeTags);
	AllTags.put(NERNounTags.motive, motiveTags);
	AllTags.put(NERNounTags.phenomenon, phenomenonTags);
	AllTags.put(NERNounTags.possession, possessionTags);
	AllTags.put(NERNounTags.plant, plantTags);
	AllTags.put(NERNounTags.relation, relationTags);
	AllTags.put(NERNounTags.shape, shapeTags);
	AllTags.put(NERNounTags.state, stateTags);
	AllTags.put(NERNounTags.feeling, feelingTags);
	AllTags.put(NERNounTags.quantity, quantityTags);
	AllTags.put(NERNounTags.time, timeTags);
    }

    /**
     * This function is to consider some special case. 1. Right now: Phrase like
     * Republic of CHina, :(ROOT (NP (NP (NNP Republic)) (PP (IN of) (NP (NNP
     * China))))) Here China is in another NP. This cause extraction issues and
     * Republic and China are extracted Separately.
     * 
     * @param newIndex
     * @param sentenceTagInfo
     * @return
     */
    private int checkSpecialCasesForHeadword(int newIndex,
	    SentenceNode sentenceTagInfo) {
	boolean continuesearch = true;
	List<SuperSenseWord> nerTags = sentenceTagInfo
		.getStanfordSupersenseTaggedSentence();
	Concept newConcept = new Concept();
	String newConceptString = "";
	String newSSTag = "";

	SuperSenseWord currentHeadWord = nerTags.get(newIndex);
	String tag = currentHeadWord.getSstag();
	// println(tag);

	int newHeadWordIndex = -1;

	if (tag.length() > 2) {
	    newConceptString = currentHeadWord.getToken();
	    String currentHeadWordTag = tag.substring(2, tag.length());
	    for (int i = newIndex + 1; i < nerTags.size() && continuesearch; i++) {
		SuperSenseWord word = nerTags.get(i);
		String w = word.getToken();
		String t = word.getSstag();
		String wordTag = "";
		if (t.length() > 3
			&& (t.contains("noun.") || t.contains("PERSON"))) {

		    wordTag = t.substring(2, t.length());
		    if (!(currentHeadWordTag.equals(wordTag))) {
			continuesearch = false;
		    } else {
			newHeadWordIndex = i;
			newConceptString += " " + w;
			newSSTag = t;
		    }

		}

		else {
		    continuesearch = false;
		}

	    }

	    if (!newConceptString.equals(currentHeadWord.getToken())) {
		// println(newConceptString + "   type: " + newSSTag);
		NERNounTags nt = EntityExtractorUtilities.getInstance()
			.mapNERTagType(newSSTag);
		newConcept.setConcept(newConceptString);
		newConcept.setTag(nt);

		addTags(newConcept, sentenceTagInfo);

	    }

	}
	return newHeadWordIndex;
    }

    private void addTags(Concept concept, SentenceNode sentenceTagInfo) {

	String cString = concept.getConcept();
	Tree ctree = AnalysisUtilities.getInstance().parseSentence(cString).parse;

	// Tree sentenceTree = sentenceTagInfo.sentenceParse;

	String tregex1;
	TregexPattern matchPattern1;
	TregexMatcher matcher1;
	tregex1 = "ROOT < NP=parent";
	matchPattern1 = TregexPatternFactory.getPattern(tregex1);
	matcher1 = matchPattern1.matcher(ctree);

	if (matcher1.find()) {

	    Tree parent = matcher1.getNode("parent");
	    Tree parentCopy = parent.deepCopy();
	    ctree = parentCopy;
	    concept.setConcepTree(ctree);
	}
	if (concept.getConcepTree() != null) {
	    addModifierDetailsToConcept(concept,
		    sentenceTagInfo.getStanfordSupersenseTaggedSentence());

	    Map<Concept, List<Integer>> tags = AllTags.get(concept.getTag());
	    if (tags != null && (tags.size() > 0 && tags.containsKey(concept))) {

		tags.get(concept)
			.add(sentenceTagInfo.getSourceSentenceNumber());

		sentenceTagInfo.getConceptsList().add(concept);

	    }

	    else {

		// println(sentenceTagInfo.getString());
		List<Integer> sentenceNumber = new ArrayList<Integer>();
		sentenceNumber.add(sentenceTagInfo.getSourceSentenceNumber());
		tags.put(concept, sentenceNumber);
		sentenceTagInfo.getConceptsList().add(concept);
	    }

	} else {
	    log.error("The concept tree was null for concept"
		    + concept.getConcept() + "at sentence :"
		    + sentenceTagInfo.getSentenceNumber() + " :"
		    + sentenceTagInfo.getString());
	}

    }

    private void addModifierDetailsToConcept(Concept concept,
	    List<SuperSenseWord> stanfordSupersenseTaggedSentence) {

	String cString = concept.getConcept();
	NounGroup ng = new NounGroup(cString);
	String headword = ng.head();
	Tree cTree = concept.getConcepTree();

	String tregex1;
	TregexPattern matchPattern1;
	TregexMatcher matcher1;
	tregex1 = "NP <  (  RB|JJ=modifier $++  (NNP|NN|NNPS|NNS < " + headword
		+ " )) "
		+ "| < (  ADJP < (RB|JJ=modifier) $++ (NNP|NN|NNPS|NNS < "
		+ headword + "))  ";
	matchPattern1 = TregexPatternFactory.getPattern(tregex1);
	matcher1 = matchPattern1.matcher(cTree);

	String modifierString = "";

	while (matcher1.find()) {

	    Tree modifier = matcher1.getNode("modifier");
	    Tree modifierCopy = modifier.deepCopy();
	    AnalysisUtilities.getInstance();
	    modifierString += AnalysisUtilities.getInstance().treeToString(
		    modifierCopy)
		    + " ";

	}

	if (!modifierString.equals("")) {
	    concept.setModifier(modifierString);
	    concept.setConcept(headword);
	}

	// concept.ad

    }

    private static void println(Object obj) {

	System.out.println(obj);
    }

    @Override
    public void extractEntitiesFromText(String document) {
	// TODO Auto-generated method stub

    }
}
