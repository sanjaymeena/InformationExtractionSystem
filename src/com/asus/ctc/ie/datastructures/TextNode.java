package com.asus.ctc.ie.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asus.ctc.ie.datastructures.entities.Concept;
import com.asus.ctc.ie.datastructures.interfaces.Paragraph;
import com.asus.ctc.ie.datastructures.interfaces.Text;
import com.asus.ctc.ie.utilities.EssentialEnums.NERNounTags;



public class TextNode implements Text,Serializable  {

/**
	 * 
	 */
	private static final long serialVersionUID = 9055662286569753458L;
String name;
String content;
String aboutness;
String summary;
String genre;
Map<Integer, Paragraph> paragraphMap;
Map<NERNounTags, Map<Concept, List<Integer>>> AllTags;	

public TextNode(){
	name="";
	content="";
	aboutness="";
	summary="";
	genre="";
	paragraphMap=new HashMap<Integer, Paragraph>();
	

	
}



public List<SentenceNode> getAllSentences(){
	
	List<SentenceNode>sentencesList=new ArrayList<SentenceNode>();
	
	
	for (Paragraph paragraph : paragraphMap.values()) {
		sentencesList.addAll(paragraph.getSourceSentenceMap().values());
	}
	
	return sentencesList;
	
}

public String getName() {
	return name;
}


public void setName(String name) {
	this.name = name;
}


public String getContent() {
	return content;
}


public void setContent(String content) {
	this.content = content;
}


public String getAboutness() {
	return aboutness;
}


public void setAboutness(String aboutness) {
	this.aboutness = aboutness;
}


public String getSummary() {
	return summary;
}


public void setSummary(String summary) {
	this.summary = summary;
}


public String getGenre() {
	return genre;
}


public void setGenre(String genre) {
	this.genre = genre;
}


public Map<Integer, Paragraph> getParagraphMap() {
	return paragraphMap;
}


public void setParagraphMap(Map<Integer, Paragraph> paragraphMap) {
	this.paragraphMap = paragraphMap;
}



@Override
public String toString() {
	
	String tmp= "TextNode [name=" + name + ", content=" + content + ", aboutness="
			+ aboutness + ", summary=" + summary + ", genre=" + genre
			+ ", paragraphMap=" + paragraphMap + "]";
	

	
	return tmp;
}



public Map<NERNounTags, Map<Concept, List<Integer>>> getAllTags() {
    return AllTags;
}



public void setAllTags(Map<NERNounTags, Map<Concept, List<Integer>>> allTags) {
    AllTags = allTags;
}
}
