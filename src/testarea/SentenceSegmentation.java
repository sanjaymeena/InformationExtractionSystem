package testarea;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;

import com.asus.ctc.ie.InformationExtraction;
import com.asus.ctc.ie.utilities.AnalysisUtilities;

public class SentenceSegmentation {

	private static final BreakIterator iterator = BreakIterator
			.getSentenceInstance(Locale.US);

	public static ArrayList<String> getSentences(String text) {
		ArrayList<String> sentences = new ArrayList<String>();
		iterator.setText(text);
		int start = iterator.first();
		for (int end = iterator.next(); end != BreakIterator.DONE; 
				start = end, end = iterator.next()) {
			sentences.add(text.substring(start, end));
		}
		return sentences;
	}
	
	
	public static void main(String[] args) {
		
	    
	    String filePath = "resources/testdata/Articles/test##test.txt";
		
		String doc = AnalysisUtilities.getInstance().readDocument(filePath);
		
		
		
		
		String test="Mr. Peterson went PUBLIC."; 
		ArrayList<String> list = getSentences(test);
		int i=0;
		 System.out.println("Printing lines");
		for (String string : list) {
		    i++;
		    System.out.println(i + " " +string);
		}
		

	}
	
	
}
