package com.asus.ctc.ie.interfaces;

import com.asus.ctc.ie.datastructures.TextNode;

public abstract class InformationExtractionTemplate {

    String document;
    TextNode textNode;

    public final TextNode Process(String document) {

	textNode=tagging(document);
	simplifySentences(textNode);
	addSensePOSInformation(textNode);
	extractEntities(textNode);
	extractRelations(textNode);
	return textNode;
    }
    
    public final TextNode Process(TextNode document) {

	textNode=document;
	tagging(textNode);
	simplifySentences(textNode);
	addSensePOSInformation(textNode);
	extractEntities(textNode);
	extractRelations(textNode);
	return textNode;
    }
    

    public abstract TextNode tagging(String document);
    public abstract TextNode tagging(TextNode document);

    public abstract void addSensePOSInformation(TextNode textNode);

    public abstract void extractEntities(TextNode textNode);

    public abstract void simplifySentences(TextNode textNode);

    public abstract void extractRelations(TextNode textNode);
}
