package com.asus.ctc.ie.tagger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.datastructures.interfaces.Paragraph;
import com.asus.ctc.ie.datastructures.senserelation.SuperSenseWord;
import com.asus.ctc.ie.tagger.interfaces.SenseInformation;
import com.asus.ctc.ie.utilities.AnalysisUtilities;
import com.asus.ctc.ie.utilities.servers.SuperSenseWrapper;

import edu.stanford.nlp.trees.Tree;

public class SuperSenseInformation implements SenseInformation {
    private final static Logger log = LoggerFactory
	    .getLogger(SuperSenseInformation.class);

    /**
     * 
     * 
     * Add SuperSense Tagged Information to all the sentences. This information
     * will be used later on for Concept Extraction and Relation Extraction
     * 
     * 
     * */
    public void addSuperSenseInformation(TextNode textNode) {
	StopWatch sw = new StopWatch();
	sw.start();

	Map<Integer, Paragraph> paragraphMap = textNode.getParagraphMap();

	Iterator<Entry<Integer, Paragraph>> iterator = paragraphMap.entrySet()
		.iterator();

	while (iterator.hasNext()) {
	    Entry<Integer, Paragraph> entry = iterator.next();

	    Paragraph para = entry.getValue();

	    // Map<Integer, SentenceNode> extractedSentenceMap = para
	    // .getExtractedSentenceMap();
	    Map<Integer, SentenceNode> sourceSentenceMap = para
		    .getSourceSentenceMap();

	    /**
	     * Iterate over Source Sentence Nodes
	     */
	    Iterator<Entry<Integer, SentenceNode>> sourceSentenceIter = sourceSentenceMap
		    .entrySet().iterator();
	    while (sourceSentenceIter.hasNext()) {

		Entry<Integer, SentenceNode> entry1 = sourceSentenceIter.next();
		SentenceNode sn = entry1.getValue();

		// Document doc = InformationEngineUtilities.getInstance()
		// .generateArkRefDocument(sn.getTree());

		// sn.setSentenceDoc(doc);

		if (sn.isPerformedSimplification()) {

		    List<SentenceNode> simplfiedSentences = sn
			    .getSimplifiedSentences();
		    for (SentenceNode sentenceNode : simplfiedSentences) {
			addSenseTagstoSentence(sentenceNode);
		    }
		}

		else {

		    if (sn.getStanfordSupersenseTaggedSentence() == null
			    || sn.getStanfordSupersenseTaggedSentence().size() == 0)

		    {

			addSenseTagstoSentence(sn);
		    }

		}
	    }

	    // Iterate over Simplified Sentence Nodes

	    /*
	     * Iterator<Entry<Integer, SentenceNode>> extractedSentenceIter =
	     * extractedSentenceMap .entrySet().iterator(); while
	     * (extractedSentenceIter.hasNext()) {
	     * 
	     * Entry<Integer, SentenceNode> entry1 = extractedSentenceIter
	     * .next(); SentenceNode sn = entry1.getValue();
	     * List<SuperSenseWord> stanfordSupersenseTaggedSentence =
	     * StanfordparsewithSuperSenseTagger(sn .getTree());
	     * 
	     * sn.setStanfordSupersenseTaggedSentence(
	     * stanfordSupersenseTaggedSentence);
	     * 
	     * }
	     */

	}
	sw.stop();

	log.info("Total Time taken for Sense Information SyntacticParsing:  "
		+ sw.toString());
    }

    private void addSenseTagstoSentence(SentenceNode sn) {
	// TODO Auto-generated method stub
	List<SuperSenseWord> stanfordSupersenseTaggedSentence = StanfordparsewithSuperSenseTagger(sn
		.getTree());

	sn.setStanfordSupersenseTaggedSentence(stanfordSupersenseTaggedSentence);
    }

    /**
     * Create a structure of supersense Tags and Stanford pos tags for a input
     * tree.
     * 
     * @param tree
     * @return List<SuperSenseWord>
     */

    public List<SuperSenseWord> StanfordparsewithSuperSenseTagger(Tree t) {
	// TODO Auto-generated method stub

	// TODO Auto-generated method stub
	List<SuperSenseWord> sw = new ArrayList<SuperSenseWord>();

	List<String> converted = new ArrayList<String>();
	List<String> supersenses = SuperSenseWrapper.getInstance()
		.annotateSentenceWithSupersenses(t);
	List<Tree> leaves = t.getLeaves();

	for (int i = 0; i < supersenses.size(); i++) {

	    converted.add(supersenses.get(i));

	}

	while (leaves.size() > converted.size())
	    converted.add("0");
	for (int i = 0; i < leaves.size(); i++) {

	    String str = AnalysisUtilities.getInstance().treeToString(
		    leaves.get(i));
	    Tree preterm = leaves.get(i).parent(t);
	    String pos = preterm.label().value();
	    String stem = AnalysisUtilities.getInstance().getLemma(str, pos);
	    

	    
	    SuperSenseWord word = new SuperSenseWord(str, converted.get(i), pos,stem);
	    sw.add(word);

	}

	return sw;

    }

    /**
     * Create a structure of supersense Tags and Stanford pos tags for a list of
     * input trees
     * 
     * @param inputTrees
     * @return
     */
    public List<List<SuperSenseWord>> StanfordparsewithSuperSenseTagger(
	    List<Tree> inputTrees) {
	// TODO Auto-generated method stub
	List<List<SuperSenseWord>> listOfOutput = new ArrayList<List<SuperSenseWord>>();
	for (Tree t : inputTrees) {

	    List<SuperSenseWord> sw = new ArrayList<SuperSenseWord>();
	    List<String> converted = new ArrayList<String>();
	    List<String> supersenses = SuperSenseWrapper.getInstance()
		    .annotateSentenceWithSupersenses(t);
	    List<Tree> leaves = t.getLeaves();

	    for (int i = 0; i < supersenses.size(); i++) {

		converted.add(supersenses.get(i));

	    }

	    while (leaves.size() > converted.size())
		converted.add("0");
	    for (int i = 0; i < leaves.size(); i++) {

		String str = AnalysisUtilities.getInstance().treeToString(
			leaves.get(i));
		Tree preterm = leaves.get(i).parent(t);
		String pos = preterm.label().value();

		SuperSenseWord word = new SuperSenseWord(str, converted.get(i),
			pos);
		sw.add(word);

	    }
	    listOfOutput.add(sw);

	}
	return listOfOutput;
    }

    /**
     * Create a string from the SuperSenseWord List.
     * 
     * @param list
     * @return str
     */
    public String createStringFromStanfordSuperSenseWordList(
	    List<SuperSenseWord> list) {
	String str = "";

	for (int i = 0; i < list.size(); i++) {
	    SuperSenseWord ssw = list.get(i);
	    String token = ssw.getToken().toLowerCase();

	    str += token + " ";
	}

	return str;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub

    }

}
