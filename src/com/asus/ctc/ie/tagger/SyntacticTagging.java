package com.asus.ctc.ie.tagger;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asus.ctc.ie.PrintTextNodeData;
import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.tagger.interfaces.CoReferenceResolution;
import com.asus.ctc.ie.tagger.interfaces.SenseInformation;
import com.asus.ctc.ie.tagger.interfaces.SyntacticParsing;
import com.asus.ctc.ie.tagger.interfaces.TaggingTemplate;
import com.asus.ctc.ie.tagger.interfaces.Tagger;
import com.asus.ctc.ie.tagger.interfaces.TextStructureCreator;
import com.asus.ctc.ie.utilities.AnalysisUtilities;

public class SyntacticTagging extends TaggingTemplate implements Tagger {
    private final static Logger log = LoggerFactory
	    .getLogger(SyntacticTagging.class);

    SyntacticParsing stanfordParser;
    CoReferenceResolution coReference;
    TextStructureCreator textNodeCreator;
    SenseInformation sensePosTagging;
    TextNode textNode;

    public SyntacticTagging() {
	stanfordParser = new SyntacticParsingUsingStanfordParser();
	coReference = new CoreferenceResolutionUsingArkRef();
	textNodeCreator = new TextStructureCreation();
	sensePosTagging=new SuperSenseInformation();
    }

    @Override
    public void performCoreferenceResolution(TextNode textNode) {
	// TODO Auto-generated method stub
	// TODO Auto-generated method stub
	log.info("Perform Coreference Resolution;");

	coReference.resolveCoreferences(textNode);
    }
    @Override
    public void senseTagging(TextNode textNode) {
	// TODO Auto-generated method stub
	log.info("Adding SuperSenseTagged Information");
	sensePosTagging.addSuperSenseInformation(textNode);
    }
    @Override
    public void syntactingParsing(TextNode textNode) {
	// TODO Auto-generated method stub
	// TODO Auto-generated method stub
	log.info("Get syntactic Trees");
	stanfordParser.generateSyntacticParseTrees(textNode);
    }

    @Override
    public TextNode createTextStructure(String document) {
	log.info("Reading Document and creating datastructure...");
	textNode = textNodeCreator.createTextStructure(document);
	return textNode;
    }
	@Override
	public TextNode performSyntacticTagging(TextNode doc) {
		StopWatch sw = new StopWatch();
	   	sw.start();

	   
	   	
	   	textNode=tagging(doc);
	   	sw.stop();

	   
	   	log.info("Total Time taken for Syntactic TaggingTemplate Process:  "
	   		+ sw.toString());

	   	return textNode;
	}
    
    public TextNode performSyntacticTagging(String document) {

   	StopWatch sw = new StopWatch();
   	sw.start();

   	textNode = new TextNode();
   	
   	textNode=tagging(document);
   	sw.stop();

   
   	log.info("Total Time taken for Syntactic TaggingTemplate Process:  "
   		+ sw.toString());

   	return textNode;
       }
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	StopWatch sw = new StopWatch();
   	sw.start();
	
	String filePath = "resources/testdata/Articles/test##test.txt";
	SyntacticTagging taggingModule= new SyntacticTagging();
	String doc = AnalysisUtilities.getInstance().readDocument(filePath);

	
	TextNode tn = taggingModule.performSyntacticTagging(doc);
	/*PrintTextNodeData ptd = new PrintTextNodeData();
   	String output = ptd.printTextNode(tn);
   	System.out.println(output);
   	
   	sw.stop();

   	   
   	log.info("Total Time taken for Syntactic TaggingTemplate Process:  "
   		+ sw.toString());*/

    }



    
}
