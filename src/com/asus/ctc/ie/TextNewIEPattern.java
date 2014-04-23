package com.asus.ctc.ie;

import java.io.IOException;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.entityextractor.EntityExtractionUsingSuperSense;
import com.asus.ctc.ie.entityextractor.interfaces.EntityExtractionFromText;
import com.asus.ctc.ie.nlptransformations.SentenceSimplificationUsingSyntacticRules;
import com.asus.ctc.ie.nlptransformations.interfaces.SentenceSimplification;
import com.asus.ctc.ie.relationextraction.RelationExtractionUsingSuperSense;
import com.asus.ctc.ie.relationextraction.interfaces.RelationExtractionFromText;
import com.asus.ctc.ie.tagger.CoreferenceResolutionUsingArkRef;
import com.asus.ctc.ie.tagger.SuperSenseInformation;
import com.asus.ctc.ie.tagger.SyntacticParsingUsingStanfordParser;
import com.asus.ctc.ie.tagger.TextStructureCreation;
import com.asus.ctc.ie.tagger.interfaces.CoReferenceResolution;
import com.asus.ctc.ie.tagger.interfaces.SenseInformation;
import com.asus.ctc.ie.tagger.interfaces.SyntacticParsing;
import com.asus.ctc.ie.tagger.interfaces.TextStructureCreator;
import com.asus.ctc.ie.utilities.AnalysisUtilities;

public class TextNewIEPattern {
    private final static Logger log = LoggerFactory
	    .getLogger(TextNewIEPattern.class);

   // SyntacticParsingUsingStanfordParser stanfordParser;
    //SuperSenseInformation sensePosTagging;
   // TextStructureCreation textNodeCreator;
    //RelationExtractionUsingSuperSense relationExtraction;
   // SentenceSimplificationUsingSyntacticRules sentenceSimplifier;
   // EntityExtractionUsingSuperSense entityExtractor;
    TextStructureCreator textNodeCreator=new TextStructureCreation();
    SentenceSimplification sentenceSimplifier=new SentenceSimplificationUsingSyntacticRules();
    SenseInformation sensePosTagging=new SuperSenseInformation();
    SyntacticParsing stanfordParser=new SyntacticParsingUsingStanfordParser();
    CoReferenceResolution coReference=new CoreferenceResolutionUsingArkRef();
    RelationExtractionFromText relationExtraction=new RelationExtractionUsingSuperSense();
    EntityExtractionFromText entityExtractor=new EntityExtractionUsingSuperSense();
    /**
     * THIS IS A TEST FUNCTION FOR UKR STRUCTURE
     * 
     * @param filePath
     * @return SentenceKnowledge Map
     */
    public void testUKR(String filePath) {

	StopWatch sw = new StopWatch();
	sw.start();

	stanfordParser = new SyntacticParsingUsingStanfordParser();
	sensePosTagging = new SuperSenseInformation();
	textNodeCreator = new TextStructureCreation();
	relationExtraction = new RelationExtractionUsingSuperSense();
	sentenceSimplifier = new SentenceSimplificationUsingSyntacticRules();
	entityExtractor = new EntityExtractionUsingSuperSense();

	TextNode textNode = new TextNode();

	String doc = AnalysisUtilities.getInstance().readDocument(filePath);

	log.info("Reading Document and creating datastructure...");
	textNode = textNodeCreator.createTextStructure(doc);

	log.info("Get syntactic Trees");
	stanfordParser.generateSyntacticParseTrees(textNode);

	
	log.info("Perform Coreference Resolution;");
	

	coReference.resolveCoreferences(textNode);

	log.info("Perform Sentence Simplification");
	// set properties

	
	
	sentenceSimplifier.simplifySentences(textNode);

	log.info("Add SuperSenseTagged Information");
	sensePosTagging.addSuperSenseInformation(textNode);

	log.info("Extract Entities");
	addEntityInformation(textNode);

	log.info("Relation Extraction ");
	
	relationExtraction.extractRelations(textNode);

	sw.stop();
	
	PrintTextNodeData ptd=new PrintTextNodeData();
	String output=ptd.printTextNode(textNode);
	System.out.println(output);
	log.info("Total Time taken for IE Process:  " + sw.toString());
    }

    private void addEntityInformation(TextNode textNode) {
	entityExtractor.extractEntitiesFromText(textNode);
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	// TODO Auto-generated method stub
	// long startTime = System.currentTimeMillis();
	String filePath = "resources/testdata/Articles/test##test.txt";
	// String filePath7 = "resources/testdata/Articles/test##story.txt";
	// String filePath1 = "resources/testdata/Articles/stories/3littlepigs##story.txt";
	// String filePath2 =
	// "resources/testdata/Articles/stories/The Hare and Tortoise##Story.txt";
	// String filePath3 =
	// "Articles/stories/The Ant and the Grasshopper##story.txt";
	// String filePath4 = "resources/testdata/Articles/articles/SolarSystem##article.txt";
	// String filePath5 = "resources/testdata/Articles/articles/Taipei_Zoo##article.txt";
	// String filePath6 = "resources/testdata/Articles/GeneralArticle##article.txt";
	// String filePath8 = "resources/testdata/Articles/articles/Taipei Zoo##article.txt";
	// String filePath9 =
	// "resources/testdata/Articles/articles/bruno-marss-new-album-unorthodox-jukebox##article.txt";
	// String filePath10 = "resources/testdata/Articles/Taipei##article.txt";
	TextNewIEPattern qg = new TextNewIEPattern();

	qg.testUKR(filePath);

	// QuestionGenerationFromFile qg1 = new QuestionGenerationFromFile();
	// qg1.generateQuestionsFromFile(filePath);

	// println("Time taken for SentenceKnowledgeGraph Generation :" +
	// (System.currentTimeMillis() - startTime) / 1000 + " sec ");
    }

    /**
     * 
     */
    public TextNewIEPattern() {

    }

}
