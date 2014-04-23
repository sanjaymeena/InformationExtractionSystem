package testarea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class TaggerDemo {

    private TaggerDemo() {
    }

    public static void main(String[] args) throws Exception {

	StopWatch sw = new StopWatch();
	sw.start();
	
	String model = "resources" + File.separator + "core_ie_resources"
		+ File.separator + "ie_data" + File.separator + "models"
		+ File.separator + "english-left3words-distsim.tagger";

	String filePath = "resources/testdata/Articles/test##test.txt";
	MaxentTagger tagger = new MaxentTagger(model);
	List<List<HasWord>> sentences = MaxentTagger
		.tokenizeText(new BufferedReader(new FileReader(filePath)));
	for (List<HasWord> sentence : sentences) {
	    ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
	    System.out.println(Sentence.listToString(tSentence, false));
	}

	sw.stop();
	System.out.println(sw.toString());
    }
    
    
    
    

}
