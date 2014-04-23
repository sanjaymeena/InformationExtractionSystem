package com.asus.ctc.ie;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.entityextractor.EntityExtractionUsingSuperSense;
import com.asus.ctc.ie.entityextractor.interfaces.EntityExtractionFromText;
import com.asus.ctc.ie.interfaces.InformationExtractionTemplate;
import com.asus.ctc.ie.nlptransformations.SentenceSimplificationUsingSyntacticRules;
import com.asus.ctc.ie.nlptransformations.interfaces.SentenceSimplification;
import com.asus.ctc.ie.relationextraction.RelationExtractionUsingSuperSense;
import com.asus.ctc.ie.relationextraction.interfaces.RelationExtractionFromText;
import com.asus.ctc.ie.tagger.SuperSenseInformation;
import com.asus.ctc.ie.tagger.SyntacticTagging;
import com.asus.ctc.ie.tagger.interfaces.SenseInformation;
import com.asus.ctc.ie.tagger.interfaces.Tagger;
import com.asus.ctc.ie.utilities.AnalysisUtilities;

public class InformationExtraction extends InformationExtractionTemplate {
    private final static Logger log = LoggerFactory
	    .getLogger(InformationExtraction.class);

    Tagger syntacticTagging;
    SentenceSimplification sentenceSimplifier;

    RelationExtractionFromText relationExtraction;
    EntityExtractionFromText entityExtractor;
    SenseInformation sensePosTagging;

    TextNode textNode;

    public InformationExtraction() {

	syntacticTagging = new SyntacticTagging();
	sentenceSimplifier = new SentenceSimplificationUsingSyntacticRules();
	sensePosTagging = new SuperSenseInformation();

	relationExtraction = new RelationExtractionUsingSuperSense();
	entityExtractor = new EntityExtractionUsingSuperSense();

    }

    @Override
    public TextNode tagging(String document) {
	StopWatch sw = new StopWatch();
	sw.start();

	TextNode tn = syntacticTagging.performSyntacticTagging(document);
	return tn;
    }
    
    

	@Override
	public TextNode tagging(TextNode document) {
		StopWatch sw = new StopWatch();
		sw.start();

		TextNode tn = syntacticTagging.performSyntacticTagging(document);
		return tn;
	}

    @Override
    public void extractEntities(TextNode textNode) {
	// TODO Auto-generated method stub
	log.info("Extract Entities");
	entityExtractor.extractEntitiesFromText(textNode);
    }

    @Override
    public void simplifySentences(TextNode textNode) {
	// TODO Auto-generated method stub
	log.info("Perform Sentence Simplification");
	// set properties

	sentenceSimplifier.simplifySentences(textNode);
    }

    @Override
    public void extractRelations(TextNode textNode) {
	log.info("Relation Extraction ");

	relationExtraction.extractRelations(textNode);
    }

    @Override
    public void addSensePOSInformation(TextNode textNode) {
	// TODO Auto-generated method stub
	log.info("Add SuperSenseTagged Information to Simplified Sentences");
	sensePosTagging.addSuperSenseInformation(textNode);
    }

    public TextNode processText(String document) {

	StopWatch sw = new StopWatch();
	sw.start();

	textNode = new TextNode();
	textNode = Process(document);
	sw.stop();

	PrintTextNodeData ptd = new PrintTextNodeData();
	String output = ptd.printTextNode(textNode);
	System.out.println(output);
	log.info("Total Time taken for Information Extraction Process:  "
		+ sw.toString());

	return textNode;
    }
    
    
    public TextNode processText(TextNode document) {

    	StopWatch sw = new StopWatch();
    	sw.start();

    	textNode = document;
    	textNode = Process(textNode);
    	sw.stop();

    	PrintTextNodeData ptd = new PrintTextNodeData();
    	String output = ptd.printTextNode(textNode);
    	System.out.println(output);
    	log.info("Total Time taken for Information Extraction Process:  "
    		+ sw.toString());

    	return textNode;
        }
    
    
    

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	String filePath = "resources/testdata/Articles/test##test.txt";
	InformationExtraction ie = new InformationExtraction();
	String doc = AnalysisUtilities.getInstance().readDocument(filePath);

	ie.processText(doc);
    }


}
