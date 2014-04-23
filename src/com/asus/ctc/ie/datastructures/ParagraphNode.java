package com.asus.ctc.ie.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asus.ctc.ie.datastructures.interfaces.Paragraph;

import arkref.data.Document;
import edu.stanford.nlp.trees.Tree;

public class ParagraphNode implements Paragraph ,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2222239966908275814L;
	int paragraphNumber;
	String content;
	Document document;
	Map<Integer, SentenceNode> sourceSentenceMap;
	Map<Integer, SentenceNode> extractedSentenceMap;

	public ParagraphNode() {
		paragraphNumber = -1;
		content = "";

		sourceSentenceMap = new HashMap<Integer, SentenceNode>();
		extractedSentenceMap = new HashMap<Integer, SentenceNode>();
	}

	/**
	 * This functions returns the tree List of sentences present in the
	 * paragraph.
	 */

	public List<Tree> getSentences() {

		List<Tree> sentenceList = new ArrayList<Tree>();
		for (SentenceNode sent : sourceSentenceMap.values()) {
			sentenceList.add(sent.getTree());
		}

		return sentenceList;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Map<Integer, SentenceNode> getSourceSentenceMap() {
		return sourceSentenceMap;
	}

	public void setSourceSentenceMap(
			Map<Integer, SentenceNode> sourceSentenceMap) {
		this.sourceSentenceMap = sourceSentenceMap;
	}

	public Map<Integer, SentenceNode> getExtractedSentenceMap() {
		return extractedSentenceMap;
	}

	public void setExtractedSentenceMap(
			Map<Integer, SentenceNode> extractedSentenceMap) {
		this.extractedSentenceMap = extractedSentenceMap;
	}

	public int getParagraphNumber() {
		return paragraphNumber;
	}

	public void setParagraphNumber(int paragraphNumber) {
		this.paragraphNumber = paragraphNumber;
	}

	@Override
	public String toString() {
		return "ParagraphNode [paragraphNumber=" + paragraphNumber
				+ ", content=" + content + ", sourceSentenceMap="
				+ sourceSentenceMap + "]";
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}
