package com.asus.ctc.ie.datastructures.interfaces;

import java.util.List;
import java.util.Map;

import arkref.data.Document;

import com.asus.ctc.ie.datastructures.SentenceNode;

import edu.stanford.nlp.trees.Tree;

public interface Paragraph {

    public Map<Integer, SentenceNode> getSourceSentenceMap();

    public void setSourceSentenceMap(
	    Map<Integer, SentenceNode> sourceSentenceMap);

    public int getParagraphNumber();

    public void setParagraphNumber(int paragraphNumber);

    public String getContent();

    public List<Tree> getSentences();

    public void setContent(String content);

    public Document getDocument();

    public void setDocument(Document document);
}
