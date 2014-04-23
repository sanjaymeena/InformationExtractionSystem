package com.asus.ctc.ie;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.asus.ctc.eebot.ie.externalresources.conceptnet.ConceptNetDataStructure;
import com.asus.ctc.eebot.ie.externalresources.conceptnet.model.ConceptNetEdge;
import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.datastructures.entities.Concept;
import com.asus.ctc.ie.datastructures.interfaces.Paragraph;
import com.asus.ctc.ie.utilities.EssentialEnums.NERNounTags;

public class PrintTextNodeData {
    StringBuffer display;
    String delimiter_dash = "------------------------------------------------------------------------------------------------------------------\n";
    String delimiter_asterisk = "***************************************************************************************************************\n";

    public String printTextNode(TextNode textNode) {
	display = new StringBuffer();
	display.append(delimiter_asterisk);
	display.append("Document Content: \n" + textNode.getContent());
	display.append(delimiter_dash);
	
	if(textNode.getAllTags()!=null){
	display.append("Entities in Document: \n");

	entityMapPrint(textNode);
	display.append(delimiter_dash);
	}
	
	// print paragraph information;
	printParagraphInformation(textNode);
	// display.append(delimiter_dash);

	return display.toString();
    }

    private void printParagraphInformation(TextNode textNode) {

	display.append("#Paragraph Information......\n");

	Map<Integer, Paragraph> paragraphMap = textNode.getParagraphMap();
	Collection<Paragraph> paragraphs = paragraphMap.values();

	for (Paragraph paragraphNode : paragraphs) {
	    int paraNo = paragraphNode.getParagraphNumber();
	    String para = paragraphNode.getContent();
	    display.append(delimiter_dash);
	    display.append("#Paragraph No: " + paraNo + "\n");
	    display.append("#Content:\n" + para + "\n");
	    display.append(delimiter_dash);
	    display.append("#Sentence Information :\n");
	    // display.append(delimiter_asterisk);
	    Collection<SentenceNode> sourceSentences = paragraphNode
		    .getSourceSentenceMap().values();
	    for (SentenceNode sentenceNode : sourceSentences) {
		

		display.append(sentenceNode.toString() + "\n");
		
	    }

	}

    }

    private void entityMapPrint(TextNode textNode) {

	Map<NERNounTags, Map<Concept, List<Integer>>> entityMap = textNode
		.getAllTags();

	// display.append(entityMap.toString());

	for (Entry<NERNounTags, Map<Concept, List<Integer>>> tag : entityMap
		.entrySet()) {
	    Map<Concept, List<Integer>> conceptList = tag.getValue();

	    if (conceptList.size() > 0) {

		display.append("Type=" + tag.getKey().name() + " : ");
		display.append("Instances with Sentence numbers="
			+ conceptList.toString() + "\n\n");

	    }

	}

	return;
    }

    @SuppressWarnings("unused")
    private String displayConceptInformation(Concept concept) {

	String conceptString = "";

	conceptString += "Name: " + concept.getConcept() + "  ";
	conceptString += "Type: " + concept.getTag().name() + "\n";

	ConceptNetDataStructure cds = concept.getCommonSense();

	if (cds != null && cds.getEdges() != null) {
	    List<ConceptNetEdge> edgeList = cds.getEdges();
	    conceptString += "****************************************************\n";

	    conceptString += "Common Sense Knowledge:\n";

	    String edgeString = "\n";
	    for (int i = 0; i < edgeList.size(); i++) {
		edgeString += edgeList.get(i) + "\n";
	    }

	    conceptString += edgeString;
	}

	return conceptString;

    }

}
