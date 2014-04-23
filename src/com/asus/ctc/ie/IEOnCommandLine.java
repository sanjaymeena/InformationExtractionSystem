package com.asus.ctc.ie;

import java.util.Scanner;

import com.asus.ctc.ie.datastructures.TextNode;
import com.asus.ctc.ie.tagger.SuperSenseInformation;
import com.asus.ctc.ie.tagger.SyntacticTagging;
import com.asus.ctc.ie.tagger.interfaces.SenseInformation;

public class IEOnCommandLine {
    InformationExtraction ie = new InformationExtraction();
    SyntacticTagging taggingModule= new SyntacticTagging();
    SenseInformation sensePosTagging= new SuperSenseInformation();
    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub

	IEOnCommandLine tieoc = new IEOnCommandLine();

	tieoc.printMenu();

    }

    private String readLine() {
	Scanner scanner = null;
	String doc = "";

	scanner = new Scanner(System.in);
	System.err.println("\nInput Text:");
	doc = scanner.nextLine();

	return doc;
    }

    private void println(Object obj) {
	// TODO Auto-generated method stub
	System.out.println(obj);
    }

    private void printMenu() {

	int selection = 0;

	StringBuilder sb = new StringBuilder();
	sb.append("======================================================================================================== \n");
	sb.append("|                                  Information Extraction System                                         |\n");
	sb.append("======================================================================================================== \n");

	sb.append("                                   1. Run IE System                                                        \n");
	sb.append("                                   2. Tagging                                                 \n");
	sb.append("                                   3. Coreference Resolution                                                \n");
	sb.append("                                   4. Sense and POS TaggingTemplate                                                  \n");
	sb.append("                                   5. Sentence Simplification                                                \n");
	sb.append("                                   6. Entity Extraction                                                      \n");
	sb.append("                                   7. Relation Extraction                                                     \n");
	sb.append("                                   8. quit                                                                \n");
	println(sb);
	println("Select your choice..");

	while (true) {

	    Scanner scanner = null;
	    String input = "";
	    try {
		scanner = new Scanner(System.in);
		while (!scanner.hasNextInt()) {
		    println("Please select from the given choices");
		    scanner.nextLine();
		}
		selection = scanner.nextInt();
		switch (selection) {

		case 1:
		    input = readLine();

		    ie.processText(input);

		    break;

		case 2:

		    String str="This process of water evaporating , condensing and falling to Earth is the water cycle.";

		    input = str;
		    TextNode tn = taggingModule.performSyntacticTagging(input);
		    sensePosTagging.addSuperSenseInformation(tn);
			PrintTextNodeData ptd = new PrintTextNodeData();
		   	String output = ptd.printTextNode(tn);
		   	System.out.println(output);
		    break;

		case 3:

		    break;

		case 4:

		    break;
		case 5:

		    break;

		case 6:

		    break;
		case 7:

		    break;
		case 8:
		    println("Bye");
		    System.exit(0);
		    break;
		case 9:

		    break;
		default:
		    println("Please select from the given choices");

		}
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}
    }
}
