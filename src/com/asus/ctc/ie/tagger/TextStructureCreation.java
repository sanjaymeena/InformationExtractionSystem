package com.asus.ctc.ie.tagger;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.asus.ctc.ie.datastructures.ParagraphNode;
import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.datastructures.interfaces.Paragraph;
import com.asus.ctc.ie.tagger.interfaces.TextStructureCreator;
import com.asus.ctc.ie.utilities.AnalysisUtilities;



public class TextStructureCreation implements TextStructureCreator {
	private final static Logger log = LoggerFactory
			.getLogger(TextStructureCreation.class);
	/**
	 * This function creates the Text Node skeletion with paragraph, Sentence
	 * nodes.
	 * 
	 * @param textNode
	 * @param doc
	 */
	public TextNode createTextStructure(String doc) {

		StopWatch sw=new StopWatch();
		sw.start();
		
		TextNode textNode = new TextNode();

		int sentenceNumber = 0;

		// Call singleton
		// AnalysisUtilities.getInstance();

		String patternStr = "(^.*\\S+.*$)+";
		Pattern pattern = Pattern.compile(patternStr, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(doc);

		HashMap<Integer, Paragraph> paragraphMap = new HashMap<Integer, Paragraph>();

		// Read the paragraphs
		int paragraphNumber = 1;
		while (matcher.find()) {

		    Paragraph para = new ParagraphNode();

			// Get the paragraph
			String paragraph = matcher.group();

			// paragraph=AnalysisUtilities.preprocess(paragraph);
			List<String> sentences = AnalysisUtilities.getInstance()
					.getSentences(paragraph);

			HashMap<Integer, SentenceNode> sourceSentenceMap = new HashMap<Integer, SentenceNode>();

			for (String sentence : sentences) {
				sentenceNumber++;
				SentenceNode sn = new SentenceNode();

				// sn.setStr/ntence);
				sn.setOriginal_sentence(sentence);
				sn.setString(sentence);
				sn.setSourceSentenceNumber(sentenceNumber);
				sn.setParagraphNumber(paragraphNumber);

				sourceSentenceMap.put(sentenceNumber, sn);

			}
			// step 1 transformations

			para.setContent(paragraph);
			para.setParagraphNumber(paragraphNumber);
			para.setSourceSentenceMap(sourceSentenceMap);

			paragraphMap.put(paragraphNumber, para);

			paragraphNumber++;
		}

		textNode.setParagraphMap(paragraphMap);
		textNode.setContent(doc);
		sw.stop();
		
		log.info("Total Time take for TextStructureCreation:  "+ sw.toString());
		return textNode;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	    TextStructureCreation tsc=new TextStructureCreation();
	    TextNode tn = tsc.createTextStructure("Jack is a dull boy.");
	    System.err.println(tn.toString());
	}

}
