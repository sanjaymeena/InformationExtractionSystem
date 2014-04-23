package com.asus.ctc.ie.tagger.interfaces;

import com.asus.ctc.ie.datastructures.TextNode;

public abstract class TaggingTemplate {

    String document;
    TextNode textNode;

    public final TextNode tagging(String document) {
	
	textNode = createTextStructure(document);
	syntactingParsing(textNode);
	senseTagging(textNode);
	performCoreferenceResolution(textNode);
	
	
	return textNode;
    }
    
    public final TextNode tagging(TextNode document) {
    	
    	textNode = document;
    	syntactingParsing(textNode);
    	senseTagging(textNode);
    	performCoreferenceResolution(textNode);
    	
    	
    	return textNode;
        }

    public abstract void performCoreferenceResolution(TextNode textNode);
    public abstract void senseTagging(TextNode textNode);
    public abstract void syntactingParsing(TextNode textNode);

    public abstract TextNode createTextStructure(String document);
}
