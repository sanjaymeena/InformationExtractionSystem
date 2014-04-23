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
import com.asus.ctc.ie.tagger.interfaces.SyntacticParsing;
import com.asus.ctc.ie.utilities.AnalysisUtilities;

import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class SyntacticParsingUsingStanfordParser implements SyntacticParsing{
	private final static Logger log = LoggerFactory
			.getLogger(SyntacticParsingUsingStanfordParser.class);

	/**
	 * This function generates the syntactic parse trees for sentences in the
	 * text
	 * 
	 * @param textNode
	 */
	public void generateSyntacticParseTrees(TextNode textNode) {
		// TODO Auto-generated method stub

		StopWatch sw = new StopWatch();
		sw.start();

		Tree parse = null;

		Map<Integer, Paragraph> paragraphMap = textNode.getParagraphMap();

		Iterator<Entry<Integer, Paragraph>> iterator = paragraphMap
				.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<Integer, Paragraph> entry = iterator.next();

			Paragraph para = entry.getValue();

			Iterator<Entry<Integer, SentenceNode>> siter = para
					.getSourceSentenceMap().entrySet().iterator();
			while (siter.hasNext()) {

				Entry<Integer, SentenceNode> entry1 = siter.next();
				SentenceNode sn = entry1.getValue();
				String sentence = sn.getString();
				parse = AnalysisUtilities.getInstance().parseSentence(sentence).parse;

				sn.setTree(parse);
				sn.setSourceTree(parse);
			}

		}
		sw.stop();

		log.info("Total Time taken for Syntactic Parsing:  " + sw.toString());
	}
	
	
	

	public List<Tree> generateSyntacticParseTrees(String input) {

		Tree parsed;
		List<String> sentences = AnalysisUtilities.getInstance().getSentences(
				input);

		List<Tree> inputTrees = new ArrayList<Tree>();

		for (String sentence : sentences) {
			parsed = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
			inputTrees.add(parsed);
		}

		return inputTrees;
	}

	public List<List<TypedDependency>> generateStanfordDependencies(String input) {

		Tree parsed;
		List<String> sentences = AnalysisUtilities.getInstance().getSentences(
				input);

		List<Tree> inputTrees = new ArrayList<Tree>();
		List<List<TypedDependency>> dependencyTrees = new ArrayList<List<TypedDependency>>();

		for (String sentence : sentences) {
			parsed = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
			inputTrees.add(parsed);
		}
		for (Tree tree : inputTrees) {
			Tree parse = tree;
			TreebankLanguagePack tlp = new PennTreebankLanguagePack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
			List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
			dependencyTrees.add(tdl);
			System.out.println(tdl);
			System.out.println();

			TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
			tp.printTree(parse);
		}
		return dependencyTrees;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
